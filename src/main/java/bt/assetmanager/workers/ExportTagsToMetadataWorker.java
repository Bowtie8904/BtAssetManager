package bt.assetmanager.workers;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.UserOption;
import bt.assetmanager.data.service.ImageAssetService;
import bt.assetmanager.data.service.SoundAssetService;
import bt.assetmanager.data.service.UserOptionService;
import bt.assetmanager.obj.FileBundler;
import bt.assetmanager.util.metadata.FileMetadataUtils;
import bt.assetmanager.util.metadata.image.ImageFileMetadataUtils;
import bt.assetmanager.views.options.OptionsView;
import bt.log.Log;
import com.vaadin.flow.component.UI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
public class ExportTagsToMetadataWorker implements BackgroundWorker
{
    private ImageAssetService imageService;
    private SoundAssetService soundService;
    private UserOptionService optionsService;
    private OptionsView optionsView;
    private Runnable onFinish;

    public ExportTagsToMetadataWorker(OptionsView optionsView,
                                      ImageAssetService imageService,
                                      SoundAssetService soundService,
                                      UserOptionService optionsService)
    {
        this.imageService = imageService;
        this.soundService = soundService;
        this.optionsService = optionsService;
        this.optionsView = optionsView;
    }

    public void onFinish(Runnable onFinish)
    {
        this.onFinish = onFinish;
    }

    @Override
    public void work(UI ui)
    {
        ui.access(() -> {
            this.optionsView.getProgressBar().setIndeterminate(true);
            this.optionsView.getProgressBar().setVisible(true);
            this.optionsView.getProgressLabel().setText("Bundling assets...");
        });

        boolean writeToFoleFormatMetadata = this.optionsService.getBooleanValue(UserOption.SAVE_TAGS_IN_FILE_FORMAT_METADATA);
        boolean writeToMetadataFile = this.optionsService.getBooleanValue(UserOption.SAVE_TAGS_IN_METADATA_FILE);

        FileBundler<Asset> bundler = new FileBundler<>();

        for (var asset : this.imageService.findAll())
        {
            bundler.addImage(asset);
        }

        for (var asset : this.soundService.findAll())
        {
            bundler.addSound(asset);
        }

        int totalAssets = bundler.getSize();
        int processedAssets = 0;

        ui.access(() -> {
            this.optionsView.getProgressBar().setIndeterminate(false);
            this.optionsView.getProgressBar().setValue(0);
            this.optionsView.getProgressLabel().setText("Writing tags to metadata...    0 / " + totalAssets + " (0%)");
        });

        for (String folder : bundler.getFolderNames())
        {
            var bundle = bundler.getBundle(folder);
            List<String> fileContent = new ArrayList<>(bundle.getImageAssets().size() + bundle.getSoundAssets().size());

            for (var asset : bundle.getImageAssets())
            {
                if (writeToMetadataFile)
                {
                    String tagString = FileMetadataUtils.tagsToString(asset.getTags());
                    fileContent.add(asset.getFileName() + ": " + tagString);
                }

                if (writeToFoleFormatMetadata)
                {
                    ImageFileMetadataUtils.saveWindowsExifMetadataTags(asset);
                }

                processedAssets++;

                if (processedAssets % 100 == 0)
                {
                    updateProgress(totalAssets, processedAssets, ui);
                }
            }

            for (var asset : bundle.getSoundAssets())
            {
                if (writeToMetadataFile)
                {
                    String tagString = FileMetadataUtils.tagsToString(asset.getTags());
                    fileContent.add(asset.getFileName() + ": " + tagString);
                }

                if (writeToFoleFormatMetadata)
                {
                    ImageFileMetadataUtils.saveWindowsExifMetadataTags(asset);
                }

                processedAssets++;

                if (processedAssets % 100 == 0)
                {
                    updateProgress(totalAssets, processedAssets, ui);
                }
            }

            if (writeToMetadataFile)
            {
                Log.info("Saving metadata to file in " + folder);
                FileMetadataUtils.overwriteMetadataFile(folder, fileContent);
                Log.info("Done saving metadata to file in " + folder);
            }

            updateProgress(totalAssets, processedAssets, ui);
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
            this.optionsView.getProgressBar().setValue(completion);
            this.optionsView.getProgressLabel().setText("Writing tags to metadata...    " + processedFiles + " / " + totalNumFiles + " (" + (int)(completion * 100) + "%)");
        });
    }
}