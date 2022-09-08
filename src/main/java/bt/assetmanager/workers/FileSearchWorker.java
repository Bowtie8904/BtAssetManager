package bt.assetmanager.workers;

import bt.assetmanager.data.entity.ImageFileEnding;
import bt.assetmanager.data.entity.SoundFileEnding;
import bt.assetmanager.data.entity.TempImageAsset;
import bt.assetmanager.data.entity.TempSoundAsset;
import bt.assetmanager.views.import_.AssetImportRow;
import bt.assetmanager.views.import_.ImportView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 07.09.2022
 */
public class FileSearchWorker implements BackgroundWorker
{
    private ImportView importView;
    private Runnable onFinish;
    private UI ui;

    public FileSearchWorker(ImportView importView)
    {
        this.importView = importView;
    }

    public void onFinish(Runnable onFinish)
    {
        this.onFinish = onFinish;
    }

    @Override
    public void work(UI ui)
    {
        this.ui = ui;

        ui.access(() -> {
            this.importView.getProgressBar().setIndeterminate(true);
            this.importView.getProgressBar().setVisible(true);
            this.importView.getProgressLabel().setText("Searching files...");
        });

        this.importView.setLastCheckedImageRow(null);
        this.importView.setLastCheckedSoundRow(null);

        replaceImageEndings();
        replaceSoundEndings();

        ui.access(() -> {
            this.importView.setImageFiles(new ArrayList<>());
            this.importView.setSoundFiles(new ArrayList<>());
        });
        ui.access(() -> this.importView.getImageGrid().setItems(this.importView.getImageFiles()));
        ui.access(() -> this.importView.getSoundGrid().setItems(this.importView.getSoundFiles()));

        List<TempImageAsset> tempImageFiles = new LinkedList<>();
        List<TempSoundAsset> tempSoundFiles = new LinkedList<>();

        this.importView.setSelectedOriginDirectory(new File(this.importView.getDirectoryTextField().getValue()));

        if (this.importView.getSelectedOriginDirectory().exists() && !this.importView.getSelectedOriginDirectory().isDirectory())
        {
            this.importView.setSelectedOriginDirectory(this.importView.getSelectedOriginDirectory().getParentFile());
        }

        if (!this.importView.getSelectedOriginDirectory().exists())
        {
            Notification.show("Folder " + this.importView.getSelectedOriginDirectory().getAbsolutePath() + " does not exist");
            return;
        }

        fillFileGrids(this.importView.getSelectedOriginDirectory(), tempImageFiles, tempSoundFiles);

        this.ui.access(() -> this.importView.getProgressFolderLabel().setText(""));
        this.ui.access(() -> this.importView.getProgressLabel().setText("Filtering out already imported files..."));

        this.importView.getTempImageRepo().saveAll(tempImageFiles);
        this.importView.getTempSoundRepo().saveAll(tempSoundFiles);

        tempImageFiles = this.importView.getTempImageRepo().getAllNonExisting();
        tempSoundFiles = this.importView.getTempSoundRepo().getAllNonExisting();

        this.importView.setImageFiles(tempImageFiles.parallelStream().map(temp -> {
            var row = new AssetImportRow();
            row.setFileName(temp.getFileName());
            row.setAbsolutePath(temp.getPath());
            row.setRelativePath(temp.getPath().substring(this.importView.getSelectedOriginDirectory().getAbsolutePath().length()));
            return row;
        }).toList());

        this.importView.setSoundFiles(tempSoundFiles.parallelStream().map(temp -> {
            var row = new AssetImportRow();
            row.setFileName(temp.getFileName());
            row.setAbsolutePath(temp.getPath());
            row.setRelativePath(temp.getPath().substring(this.importView.getSelectedOriginDirectory().getAbsolutePath().length()));
            return row;
        }).toList());

        this.importView.getTempImageRepo().deleteAll();
        this.importView.getTempSoundRepo().deleteAll();

        this.ui.access(() -> this.importView.getProgressLabel().setText("Creating rows..."));

        for (int i = 0; i < this.importView.getImageFiles().size(); i++)
        {
            var row = this.importView.getImageFiles().get(i);
            row.setIndex(i);
            createCheckBoxForRow(row);

            row.getImportCheckbox().addClickListener(event -> {
                if (event.isShiftKey() && this.importView.getLastCheckedImageRow() != null)
                {
                    selectAllInRange(this.importView.getImageFiles(), row.getIndex(), this.importView.getLastCheckedImageRow().getIndex());
                }

                this.importView.setLastCheckedImageRow(row);
            });
        }

        for (int i = 0; i < this.importView.getSoundFiles().size(); i++)
        {
            var row = this.importView.getSoundFiles().get(i);
            row.setIndex(i);
            createCheckBoxForRow(row);

            row.getImportCheckbox().addClickListener(event -> {
                if (event.isShiftKey() && this.importView.getLastCheckedSoundRow() != null)
                {
                    selectAllInRange(this.importView.getSoundFiles(), row.getIndex(), this.importView.getLastCheckedSoundRow().getIndex());
                }

                this.importView.setLastCheckedSoundRow(row);
            });
        }

        ui.access(() -> {
            if (this.onFinish != null)
            {
                this.onFinish.run();
            }
        });

        ui.setPollInterval(-1);
    }

    public void selectAllInRange(List<AssetImportRow> rows, int index1, int index2)
    {
        int min = Math.min(index1, index2);
        int max = Math.max(index1, index2);

        for (int i = min; i <= max; i++)
        {
            rows.get(i).checkImportBox(true);
        }
    }

    private void createCheckBoxForRow(AssetImportRow row)
    {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(row.isShouldImport());

        checkbox.addValueChangeListener(event -> row.setShouldImport(event.getValue()));

        row.setImportCheckbox(checkbox);
    }

    private void fillFileGrids(File root, List<TempImageAsset> tempImageFiles, List<TempSoundAsset> tempSoundFiles)
    {
        this.ui.access(() -> this.importView.getProgressFolderLabel().setText(root.getAbsolutePath()));

        for (File file : root.listFiles())
        {
            if (file.isDirectory())
            {
                fillFileGrids(file, tempImageFiles, tempSoundFiles);
            }
            else
            {
                if (file.getName().lastIndexOf(".") > -1)
                {
                    String fileEnding = file.getName().substring(file.getName().lastIndexOf(".") + 1);

                    if (this.importView.getImageFileEndings().contains(fileEnding.toLowerCase().trim()))
                    {
                        var asset = new TempImageAsset();
                        asset.setPath(file.getAbsolutePath());
                        asset.setFileName(file.getName());
                        tempImageFiles.add(asset);
                    }
                    else if (this.importView.getSoundFileEndings().contains(fileEnding.toLowerCase().trim()))
                    {
                        var asset = new TempSoundAsset();
                        asset.setPath(file.getAbsolutePath());
                        asset.setFileName(file.getName());
                        tempSoundFiles.add(asset);

                    }
                }
            }
        }
    }

    private void replaceImageEndings()
    {
        this.importView.getImageFileEndingRepo().deleteAll();
        String[] endings = this.importView.getImageFileEndingsTextField().getValue().split(",");

        for (String ending : endings)
        {
            var imageEnding = new ImageFileEnding();
            imageEnding.setEnding(ending.trim().toLowerCase());
            this.importView.getImageFileEndingRepo().save(imageEnding);
        }

        this.importView.setImageFileEndings(this.importView.getImageFileEndingRepo().findAll().stream().map(ImageFileEnding::getEnding).toList());
    }

    private void replaceSoundEndings()
    {
        this.importView.getSoundFileEndingRepo().deleteAll();
        String[] endings = this.importView.getSoundFileEndingsTextField().getValue().split(",");

        for (String ending : endings)
        {
            var soundEnding = new SoundFileEnding();
            soundEnding.setEnding(ending.trim().toLowerCase());
            this.importView.getSoundFileEndingRepo().save(soundEnding);
        }

        this.importView.setSoundFileEndings(this.importView.getSoundFileEndingRepo().findAll().stream().map(SoundFileEnding::getEnding).toList());
    }
}