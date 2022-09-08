package bt.assetmanager.workers;

import bt.assetmanager.constants.AssetManagerConstants;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.entity.SoundAsset;
import bt.assetmanager.data.entity.Tag;
import bt.assetmanager.data.entity.UserOption;
import bt.assetmanager.data.service.UserOptionService;
import bt.assetmanager.util.metadata.FileMetadataUtils;
import bt.assetmanager.views.import_.AssetImportRow;
import bt.assetmanager.views.import_.ImportFileBundler;
import bt.assetmanager.views.import_.ImportView;
import bt.log.Log;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lukas Hartwig
 * @since 07.09.2022
 */
public class FileImportWorker implements BackgroundWorker
{
    private ImportView importView;
    private Runnable onFinish;
    private UserOptionService optionsService;

    public FileImportWorker(ImportView importView, UserOptionService optionsService)
    {
        this.importView = importView;
        this.optionsService = optionsService;
    }

    public void onFinish(Runnable onFinish)
    {
        this.onFinish = onFinish;
    }

    @Override
    public void work(UI ui)
    {
        List<Tag> tags = new ArrayList<>();

        // get tags from tag field in case there are any
        for (String tag : this.importView.getApplyTagsTextField().getValue().trim().split(","))
        {
            tag = tag.trim();

            if (!tag.isEmpty())
            {
                tags.add(this.importView.getTagService().obtainTag(tag));
            }
        }

        // get selected rows for images and sounds
        List<AssetImportRow> selectedImages = this.importView.getImageFiles().stream()
                                                             .filter(AssetImportRow::isShouldImport)
                                                             .toList();

        List<AssetImportRow> selectedSounds = this.importView.getSoundFiles().stream()
                                                             .filter(AssetImportRow::isShouldImport)
                                                             .toList();

        int totalNumFiles = selectedImages.size() + selectedSounds.size();
        int processedFiles = 0;

        ui.access(() -> {
            this.importView.getProgressBar().setIndeterminate(true);
            this.importView.getProgressBar().setVisible(true);
            this.importView.getProgressLabel().setText("Bundling files");
        });

        ui.access(() -> Notification.show("Importing " + selectedImages.size() + " images and " + selectedSounds.size() + " sounds"));

        Tag untaggedTag = this.importView.getTagService().obtainTag(AssetManagerConstants.UNTAGGED_TAG_NAME);

        ImportFileBundler bundler = new ImportFileBundler();

        Log.info("Starting to bundle " + totalNumFiles + " files");

        for (AssetImportRow row : selectedImages)
        {
            bundler.addImage(row);
        }

        for (AssetImportRow row : selectedSounds)
        {
            bundler.addSound(row);
        }

        Log.info("Done bundling " + totalNumFiles + " files into " + bundler.getFolderNames().size() + " folders");

        ui.access(() -> {
            this.importView.getProgressBar().setIndeterminate(false);
            this.importView.getProgressBar().setValue(0);
            this.importView.getProgressLabel().setText("Importing...    0 / " + totalNumFiles + " (0%)");
        });

        Log.info("Starting to import " + totalNumFiles + " files");

        boolean readFromMetadataFile = this.optionsService.getBooleanValue(UserOption.READ_TAGS_FROM_METADATA_FILE);
        boolean writeToMetadataFile = this.optionsService.getBooleanValue(UserOption.SAVE_TAGS_IN_METADATA_FILE);

        for (String folder : bundler.getFolderNames())
        {
            ui.access(() -> this.importView.getProgressFolderLabel().setText(folder));

            ImportFileBundler.Bundle bundle = bundler.getBundle(folder);
            List<String> fileContent = null;

            if (readFromMetadataFile)
            {
                fileContent = FileMetadataUtils.getMetadataFileContent(folder);
            }

            for (AssetImportRow row : bundle.getImageAssets())
            {
                ImageAsset asset = new ImageAsset();
                asset.setPath(row.getAbsolutePath());
                asset.setFileName(row.getFileName());

                if (readFromMetadataFile)
                {
                    asset.setTags(new ArrayList<>(getTagsForImportFile(tags, row, fileContent, untaggedTag)));
                }
                else
                {
                    asset.setTags(tags);
                }

                this.importView.getImageService().save(asset, false);
                Log.debug("Saved image asset " + asset.getPath());

                this.importView.getImageFiles().remove(row);

                processedFiles++;

                if (processedFiles % 100 == 0)
                {
                    updateProgress(totalNumFiles, processedFiles, ui);
                }
            }

            for (AssetImportRow row : bundle.getSoundAssets())
            {
                SoundAsset asset = new SoundAsset();
                asset.setPath(row.getAbsolutePath());
                asset.setFileName(row.getFileName());

                if (readFromMetadataFile)
                {
                    asset.setTags(new ArrayList<>(getTagsForImportFile(tags, row, fileContent, untaggedTag)));
                }
                else
                {
                    asset.setTags(tags);
                }

                this.importView.getSoundService().save(asset, false);
                Log.debug("Saved sound asset " + asset.getPath());

                this.importView.getSoundFiles().remove(row);

                processedFiles++;

                if (processedFiles % 100 == 0)
                {
                    updateProgress(totalNumFiles, processedFiles, ui);
                }
            }

            if (writeToMetadataFile)
            {
                Log.info("Saving metadata to file in " + folder);
                FileMetadataUtils.overwriteMetadataFile(folder, fileContent);
                Log.info("Done saving metadata to file in " + folder);
            }

            updateProgress(totalNumFiles, processedFiles, ui);
        }

        Log.info("Done importing " + totalNumFiles + " files");

        for (int i = 0; i < this.importView.getImageFiles().size(); i++)
        {
            var row = this.importView.getImageFiles().get(i);
            row.setIndex(i);
        }

        for (int i = 0; i < this.importView.getSoundFiles().size(); i++)
        {
            var row = this.importView.getSoundFiles().get(i);
            row.setIndex(i);
        }

        ui.access(() -> {
            if (this.onFinish != null)
            {
                this.onFinish.run();
            }
        });

        ui.setPollInterval(-1);
    }

    private void updateProgress(int totalNumFiles, int processedFiles, UI ui)
    {
        double completion = processedFiles / (double)totalNumFiles;
        ui.access(() -> {
            this.importView.getProgressBar().setValue(completion);
            this.importView.getProgressLabel().setText("Importing...    " + processedFiles + " / " + totalNumFiles + " (" + (int)(completion * 100) + "%)");
        });
    }

    private Set<Tag> getTagsForImportFile(List<Tag> startTags, AssetImportRow row, List<String> fileContent, Tag untaggedTag)
    {
        Set<Tag> tagSet = new HashSet<>(startTags);

        boolean found = false;
        int index = -1;

        for (int i = 0; i < fileContent.size(); i++)
        {
            String line = fileContent.get(i);

            if (FileMetadataUtils.lineMatch(row.getFileName(), line))
            {
                for (String tag : FileMetadataUtils.getTagsFromLine(line))
                {
                    tagSet.add(this.importView.getTagService().obtainTag(tag.trim()));
                }

                index = i;
                found = true;
                break;
            }
        }

        if (tagSet.isEmpty())
        {
            tagSet.add(untaggedTag);
        }

        String tagString = tagSet.stream()
                                 .map(Tag::getName)
                                 .collect(Collectors.joining(","));

        String lineContent = row.getFileName() + ": " + tagString;

        if (found)
        {
            fileContent.set(index, lineContent);
        }
        else
        {
            fileContent.add(lineContent);
        }

        return tagSet;
    }
}