package bt.assetmanager.views.import_;

import bt.assetmanager.components.AudioPlayer;
import bt.assetmanager.components.ScrollTreeGrid;
import bt.assetmanager.components.TagSearchTextField;
import bt.assetmanager.data.entity.*;
import bt.assetmanager.data.service.*;
import bt.assetmanager.util.UIUtils;
import bt.assetmanager.views.MainLayout;
import bt.log.Log;
import com.vaadin.componentfactory.Autocomplete;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.filesystemdataprovider.FilesystemData;
import org.vaadin.filesystemdataprovider.FilesystemDataProvider;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Import - Asset manager")
@Route(value = "import", layout = MainLayout.class)
@Uses(Icon.class)
public class ImportView extends Div
{
    private static File selectedOriginDirectory;
    private TempImageAssetRepository tempImageRepo;
    private TempSoundAssetRepository tempSoundRepo;
    private Grid<AssetImportRow> imageGrid = new Grid<>(AssetImportRow.class, false);
    private Grid<AssetImportRow> soundGrid = new Grid<>(AssetImportRow.class, false);
    private ImageFileEndingRepository imageFileEndingRepo;
    private SoundFileEndingRepository soundFileEndingRepo;
    private ImageAssetRepository imageRepo;
    private SoundAssetRepository soundRepo;
    private TagService tagService;
    private List<String> soundFileEndings;
    private List<String> imageFileEndings;
    private List<AssetImportRow> soundFiles;
    private List<AssetImportRow> imageFiles;
    private TextField directoryTextField;
    private TextField imageFileEndingsTextField;
    private TextField soundFileEndingsTextField;
    private Button browseOriginButton;
    private Button searchButton;
    private Autocomplete applyTagsTextField;
    private Button importButton;
    private AudioPlayer audioPlayer;
    private Label imageCountLabel;
    private Label soundCountLabel;
    private Button selectAllImportCheckboxButton;
    private Button deselectAllImportCheckboxButton;
    private AssetImportRow lastCheckedImageRow;
    private AssetImportRow lastCheckedSoundRow;

    @Autowired
    public ImportView(ImageFileEndingRepository imageFileEndingRepo,
                      SoundFileEndingRepository soundFileEndingRepo,
                      TagService tagService,
                      ImageAssetRepository imageRepo,
                      SoundAssetRepository soundRepo,
                      TempImageAssetRepository tempImageRepo,
                      TempSoundAssetRepository tempSoundRepo)
    {
        this.imageFileEndingRepo = imageFileEndingRepo;
        this.soundFileEndingRepo = soundFileEndingRepo;
        this.tagService = tagService;
        this.imageRepo = imageRepo;
        this.soundRepo = soundRepo;
        this.tempImageRepo = tempImageRepo;
        this.tempSoundRepo = tempSoundRepo;

        this.soundFileEndings = this.soundFileEndingRepo.findAll().stream().map(end -> end.getEnding()).collect(Collectors.toList());
        this.imageFileEndings = this.imageFileEndingRepo.findAll().stream().map(end -> end.getEnding()).collect(Collectors.toList());

        this.imageFiles = new ArrayList<>();
        this.soundFiles = new ArrayList<>();

        addClassNames("import-view");

        // Create UI
        SplitLayout innerSplitLayout = new SplitLayout();
        SplitLayout outerSplitLayout = new SplitLayout();

        createImageGrid(innerSplitLayout);
        createSoundGrid(innerSplitLayout);

        outerSplitLayout.addToPrimary(innerSplitLayout);
        createImportLayout(outerSplitLayout);

        add(outerSplitLayout);

        this.imageGrid.setItems(this.imageFiles);
        this.soundGrid.setItems(this.soundFiles);
    }

    private void selectOriginDirectory()
    {
        List<File> rootFiles = new ArrayList<>();

        File[] drives = File.listRoots();
        if (drives != null && drives.length > 0)
        {
            for (File aDrive : drives)
            {
                rootFiles.add(aDrive);
            }
        }

        File rootBase = rootFiles.get(0);

        FilesystemData root = new FilesystemData(rootBase, false);
        rootFiles.remove(0);
        FilesystemDataProvider fileSystem = new FilesystemDataProvider(root);

        for (File aRoot : rootFiles)
        {
            fileSystem.getTreeData().addRootItems(aRoot);
        }

        ScrollTreeGrid<File> tree = new ScrollTreeGrid<>();
        tree.setDataProvider(fileSystem);
        tree.addHierarchyColumn(file -> {
            if (file.getName() != null && !file.getName().isEmpty())
            {
                return file.getName();
            }
            else
            {
                return FileSystemView.getFileSystemView().getSystemDisplayName(file);
            }
        }).setHeader("Name");

        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        Button selectButton = new Button("Select");
        selectButton.addClickListener(e -> {
            Optional<File> selectedDirectory = tree.getSelectionModel().getFirstSelectedItem();

            if (selectedDirectory.isPresent())
            {
                File file = selectedDirectory.get();

                if (!file.isDirectory())
                {
                    file = file.getParentFile();
                }

                this.directoryTextField.setValue(file.getAbsolutePath());
                selectedOriginDirectory = file;
                dialog.close();
            }
        });

        Button closeButton = new Button("Close");
        closeButton.addClickListener(e -> {
            dialog.close();
        });

        tree.setWidth("750px");
        tree.setHeight("500px");

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, selectButton, closeButton);
        buttonLayout.add(selectButton, UIUtils.widthFiller("10px"), closeButton);

        dialog.add(tree);
        dialog.add(buttonLayout);

        if (selectedOriginDirectory != null)
        {
            File parentDir = selectedOriginDirectory.getParentFile();
            List<File> parentDirs = new ArrayList<>();

            while (parentDir != null)
            {
                parentDirs.add(0, parentDir);
                parentDir = parentDir.getParentFile();
            }

            tree.expand(parentDirs);
            tree.scrollToItem(selectedOriginDirectory);
        }

        dialog.open();
    }

    private void createImportLayout(SplitLayout splitLayout)
    {
        Div layoutDiv = new Div();
        layoutDiv.setClassName("input-layout");

        Div innerDiv = new Div();
        innerDiv.setClassName("input");
        layoutDiv.add(innerDiv);

        FormLayout formLayout = new FormLayout();
        this.directoryTextField = new TextField("Directory");

        if (selectedOriginDirectory != null && selectedOriginDirectory.exists())
        {
            this.directoryTextField.setValue(selectedOriginDirectory.getAbsolutePath());
        }

        this.browseOriginButton = new Button("Browse");
        this.browseOriginButton.addClickListener(e -> selectOriginDirectory());
        this.imageFileEndingsTextField = new TextField("Image file endings (comma separated)");
        this.imageFileEndingsTextField.setValue(String.join(", ", this.imageFileEndings));

        this.soundFileEndingsTextField = new TextField("Sound file endings (comma separated)");
        this.soundFileEndingsTextField.setValue(String.join(", ", this.soundFileEndings));

        this.importButton = new Button("Import");
        this.importButton.setEnabled(false);
        this.importButton.addClickListener(e -> importFiles());
        this.importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.imageCountLabel = new Label("0 images found");
        this.soundCountLabel = new Label("0 sounds found");

        this.applyTagsTextField = new TagSearchTextField(4, this.tagService);
        this.applyTagsTextField.setLabel("Apply these tags on import (comma separated)");

        this.searchButton = new Button("Search");
        this.searchButton.addClickListener(e -> {
            this.importButton.setEnabled(false);

            this.lastCheckedImageRow = null;
            this.lastCheckedSoundRow = null;

            replaceImageEndings();
            replaceSoundEndings();

            this.imageFiles.clear();
            this.soundFiles.clear();

            List<TempImageAsset> tempImageFiles = new LinkedList<>();
            List<TempSoundAsset> tempSoundFiles = new LinkedList<>();

            if (this.directoryTextField.getValue().trim().isEmpty())
            {
                this.directoryTextField.setErrorMessage("Select a folder to search in");
                this.directoryTextField.setInvalid(true);
                return;
            }

            selectedOriginDirectory = new File(this.directoryTextField.getValue());

            if (selectedOriginDirectory.exists() && !selectedOriginDirectory.isDirectory())
            {
                selectedOriginDirectory = selectedOriginDirectory.getParentFile();
            }

            if (!selectedOriginDirectory.exists())
            {
                Notification.show("Folder " + selectedOriginDirectory.getAbsolutePath() + " does not exist");
                return;
            }

            fillFileGrids(selectedOriginDirectory, tempImageFiles, tempSoundFiles);

            this.tempImageRepo.saveAll(tempImageFiles);
            this.tempSoundRepo.saveAll(tempSoundFiles);

            tempImageFiles = this.tempImageRepo.getAllNonExisting();
            tempSoundFiles = this.tempSoundRepo.getAllNonExisting();

            this.imageFiles = tempImageFiles.parallelStream().map(temp -> {
                var row = new AssetImportRow();
                row.setFileName(temp.getFileName());
                row.setAbsolutePath(temp.getPath());
                row.setRelativePath(temp.getPath().substring(selectedOriginDirectory.getAbsolutePath().length()));
                return row;
            }).collect(Collectors.toList());

            this.soundFiles = tempSoundFiles.parallelStream().map(temp -> {
                var row = new AssetImportRow();
                row.setFileName(temp.getFileName());
                row.setAbsolutePath(temp.getPath());
                row.setRelativePath(temp.getPath().substring(selectedOriginDirectory.getAbsolutePath().length()));
                return row;
            }).collect(Collectors.toList());

            this.tempImageRepo.deleteAll();
            this.tempSoundRepo.deleteAll();

            for (int i = 0; i < this.imageFiles.size(); i++)
            {
                var row = this.imageFiles.get(i);
                row.setIndex(i);
                createCheckBoxForRow(row);

                row.getImportCheckbox().addClickListener(event -> {
                    if (event.isShiftKey() && this.lastCheckedImageRow != null)
                    {
                        selectAllInRange(this.imageFiles, row.getIndex(), this.lastCheckedImageRow.getIndex());
                    }

                    this.lastCheckedImageRow = row;
                });
            }

            for (int i = 0; i < this.soundFiles.size(); i++)
            {
                var row = this.soundFiles.get(i);
                row.setIndex(i);
                createCheckBoxForRow(row);

                row.getImportCheckbox().addClickListener(event -> {
                    if (event.isShiftKey() && this.lastCheckedSoundRow != null)
                    {
                        selectAllInRange(this.soundFiles, row.getIndex(), this.lastCheckedSoundRow.getIndex());
                    }

                    this.lastCheckedSoundRow = row;
                });
            }

            this.imageCountLabel.setText(this.imageFiles.size() + " images found");
            this.soundCountLabel.setText(this.soundFiles.size() + " sounds found");

            this.imageGrid.setItems(this.imageFiles);
            this.soundGrid.setItems(this.soundFiles);

            this.importButton.setEnabled(!this.imageFiles.isEmpty() || !this.soundFiles.isEmpty());

            this.imageGrid.scrollToStart();
            this.soundGrid.scrollToStart();
        });

        this.selectAllImportCheckboxButton = new Button("Import all");
        this.selectAllImportCheckboxButton.setWidth("160px");
        this.selectAllImportCheckboxButton.addClickListener(e -> {
            for (var row : this.imageFiles)
            {
                row.checkImportBox(true);
            }

            for (var row : this.soundFiles)
            {
                row.checkImportBox(true);
            }
        });

        this.deselectAllImportCheckboxButton = new Button("Import none");
        this.deselectAllImportCheckboxButton.setWidth("160px");
        this.deselectAllImportCheckboxButton.addClickListener(e -> {
            for (var row : this.imageFiles)
            {
                row.checkImportBox(false);
            }

            for (var row : this.soundFiles)
            {
                row.checkImportBox(false);
            }
        });

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, this.selectAllImportCheckboxButton, this.deselectAllImportCheckboxButton);
        buttonLayout.add(this.selectAllImportCheckboxButton, this.deselectAllImportCheckboxButton);

        this.audioPlayer = new AudioPlayer();
        this.audioPlayer.setVisible(false);

        Component[] fields = new Component[] { this.directoryTextField,
                                               this.browseOriginButton,
                                               this.imageFileEndingsTextField,
                                               this.soundFileEndingsTextField,
                                               UIUtils.heightFiller("20px"),
                                               this.imageCountLabel,
                                               this.soundCountLabel,
                                               UIUtils.heightFiller("10px"),
                                               this.searchButton,
                                               UIUtils.heightFiller("15px"),
                                               new Hr(),
                                               this.applyTagsTextField,
                                               UIUtils.heightFiller("15px"),
                                               new Hr(),
                                               UIUtils.heightFiller("15px"),
                                               buttonLayout,
                                               UIUtils.heightFiller("30px"),
                                               new Hr(),
                                               UIUtils.heightFiller("30px"),
                                               this.importButton,
                                               UIUtils.heightFiller("15px"),
                                               new Hr(),
                                               UIUtils.heightFiller("15px"),
                                               this.audioPlayer
        };

        formLayout.add(fields);
        innerDiv.add(formLayout);

        splitLayout.addToSecondary(layoutDiv);
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

        checkbox.addValueChangeListener(event -> {
            row.setShouldImport(event.getValue());
        });

        row.setImportCheckbox(checkbox);
    }

    private void importFiles()
    {
        this.lastCheckedImageRow = null;
        this.lastCheckedSoundRow = null;

        List<Tag> tags = new ArrayList<>();

        for (String tag : this.applyTagsTextField.getValue().trim().split(","))
        {
            tag = tag.trim();

            if (!tag.isEmpty())
            {
                tags.add(this.tagService.obtainTag(tag));
            }
        }

        if (tags.isEmpty())
        {
            tags.add(this.tagService.obtainTag("UNTAGGED"));
        }

        List<AssetImportRow> selectedImages = this.imageFiles.stream()
                                                             .filter(AssetImportRow::isShouldImport)
                                                             .collect(Collectors.toList());

        List<AssetImportRow> selectedSounds = this.soundFiles.stream()
                                                             .filter(AssetImportRow::isShouldImport)
                                                             .collect(Collectors.toList());

        Notification.show("Importing " + selectedImages.size() + " images and " + selectedSounds.size() + " sounds");

        Log.info("Starting to import " + selectedImages.size() + " images");

        for (AssetImportRow row : selectedImages)
        {
            ImageAsset asset = new ImageAsset();
            asset.setTags(tags);

            asset.setPath(row.getAbsolutePath());
            asset.setFileName(row.getFileName());

            this.imageRepo.save(asset);
            Log.debug("Saved image asset " + asset.getPath());

            this.imageFiles.remove(row);
        }

        this.imageCountLabel.setText(this.imageFiles.size() + " images found");
        Log.info("Done importing " + selectedImages.size() + " images");

        this.imageGrid.setItems(this.imageFiles);

        for (int i = 0; i < this.imageFiles.size(); i++)
        {
            var row = this.imageFiles.get(i);
            row.setIndex(i);
        }

        Log.info("Starting to import " + selectedSounds.size() + " sounds");

        for (AssetImportRow row : selectedSounds)
        {
            SoundAsset asset = new SoundAsset();
            asset.setTags(tags);

            asset.setPath(row.getAbsolutePath());
            asset.setFileName(row.getFileName());

            this.soundRepo.save(asset);
            Log.debug("Saved sound asset " + asset.getPath());

            this.soundFiles.remove(row);
        }

        this.soundCountLabel.setText(this.soundFiles.size() + " sounds found");
        Log.info("Done importing " + selectedSounds.size() + " sounds");

        this.soundGrid.setItems(this.soundFiles);

        for (int i = 0; i < this.soundFiles.size(); i++)
        {
            var row = this.soundFiles.get(i);
            row.setIndex(i);
        }
    }

    private void fillFileGrids(File root, List<TempImageAsset> tempImageFiles, List<TempSoundAsset> tempSoundFiles)
    {
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

                    if (this.imageFileEndings.contains(fileEnding.toLowerCase().trim()))
                    {
                        var asset = new TempImageAsset();
                        asset.setPath(file.getAbsolutePath());
                        asset.setFileName(file.getName());
                        tempImageFiles.add(asset);
                    }
                    else if (this.soundFileEndings.contains(fileEnding.toLowerCase().trim()))
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
        this.imageFileEndingRepo.deleteAll();
        String[] endings = this.imageFileEndingsTextField.getValue().split(",");

        for (String ending : endings)
        {
            var imageEnding = new ImageFileEnding();
            imageEnding.setEnding(ending.trim().toLowerCase());
            this.imageFileEndingRepo.save(imageEnding);
        }

        this.imageFileEndings = this.imageFileEndingRepo.findAll().stream().map(end -> end.getEnding()).collect(Collectors.toList());
    }

    private void replaceSoundEndings()
    {
        this.soundFileEndingRepo.deleteAll();
        String[] endings = this.soundFileEndingsTextField.getValue().split(",");

        for (String ending : endings)
        {
            var soundEnding = new SoundFileEnding();
            soundEnding.setEnding(ending.trim().toLowerCase());
            this.soundFileEndingRepo.save(soundEnding);
        }

        this.soundFileEndings = this.soundFileEndingRepo.findAll().stream().map(end -> end.getEnding()).collect(Collectors.toList());
    }

    private void createImageGrid(SplitLayout splitLayout)
    {
        this.imageGrid.addColumn(new ComponentRenderer<>(
                                         row -> {
                                             return row.getImportCheckbox();
                                         }
                                 )
        ).setHeader("Import").setKey("shouldImport");

        this.imageGrid.addColumn(new ComponentRenderer<>(
                                         row -> {
                                             StreamResource imageResource = new StreamResource(row.getFileName() + "", () -> {
                                                 try
                                                 {
                                                     return new FileInputStream(row.getAbsolutePath());
                                                 }
                                                 catch (final FileNotFoundException e)
                                                 {
                                                     return null;
                                                 }
                                             });

                                             Image image = new Image(imageResource, "Couldn't load image");
                                             image.setHeight("50px");

                                             return image;
                                         }
                                 )
        ).setHeader("").setKey("image");

        this.imageGrid.addColumn("fileName").setAutoWidth(true);
        this.imageGrid.addColumn("relativePath").setAutoWidth(true);
        this.imageGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(this.imageGrid);
    }

    private void createSoundGrid(SplitLayout splitLayout)
    {
        this.soundGrid.addColumn(new ComponentRenderer<>(
                                         row -> {
                                             return row.getImportCheckbox();
                                         }
                                 )
        ).setHeader("Import").setKey("shouldImport");

        this.soundGrid.addColumn(new ComponentRenderer<>(
                                         row -> {
                                             Button playButton = new Button("Play");
                                             playButton.addClickListener(e -> {
                                                 StreamResource soundResource = new StreamResource(row.getFileName() + "", () -> {
                                                     try
                                                     {
                                                         return new FileInputStream(row.getAbsolutePath());
                                                     }
                                                     catch (final FileNotFoundException ex)
                                                     {
                                                         return null;
                                                     }
                                                 });

                                                 this.audioPlayer.setSource(soundResource);
                                                 this.audioPlayer.setVisible(true);
                                                 this.audioPlayer.play();
                                             });

                                             return playButton;
                                         }
                                 )
        ).setHeader("").setKey("audio").setAutoWidth(true);

        this.soundGrid.addColumn("fileName").setAutoWidth(true);
        this.soundGrid.addColumn("relativePath").setAutoWidth(true);
        this.soundGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToSecondary(wrapper);
        wrapper.add(this.soundGrid);
    }
}