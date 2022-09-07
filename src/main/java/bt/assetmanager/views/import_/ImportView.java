package bt.assetmanager.views.import_;

import bt.assetmanager.components.AudioPlayer;
import bt.assetmanager.components.ScrollTreeGrid;
import bt.assetmanager.components.TagSearchTextField;
import bt.assetmanager.data.repository.ImageFileEndingRepository;
import bt.assetmanager.data.repository.SoundFileEndingRepository;
import bt.assetmanager.data.repository.TempImageAssetRepository;
import bt.assetmanager.data.repository.TempSoundAssetRepository;
import bt.assetmanager.data.service.ImageAssetService;
import bt.assetmanager.data.service.SoundAssetService;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.util.UIUtils;
import bt.assetmanager.views.MainLayout;
import bt.assetmanager.workers.FileImportWorker;
import bt.assetmanager.workers.FileSearchWorker;
import com.vaadin.componentfactory.Autocomplete;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@PageTitle("Import - Asset manager")
@Route(value = "import", layout = MainLayout.class)
@Uses(Icon.class)
public class ImportView extends Div
{
    private static File selectedOriginDirectory;
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private TempImageAssetRepository tempImageRepo;
    private TempSoundAssetRepository tempSoundRepo;
    private Grid<AssetImportRow> imageGrid = new Grid<>(AssetImportRow.class, false);
    private Grid<AssetImportRow> soundGrid = new Grid<>(AssetImportRow.class, false);
    private ImageFileEndingRepository imageFileEndingRepo;
    private SoundFileEndingRepository soundFileEndingRepo;
    private ImageAssetService imageService;
    private SoundAssetService soundService;
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
    private ProgressBar progressBar;
    private Label progressLabel;
    private Label progressFolderLabel;

    @Autowired
    public ImportView(ImageFileEndingRepository imageFileEndingRepo,
                      SoundFileEndingRepository soundFileEndingRepo,
                      TagService tagService,
                      ImageAssetService imageService,
                      SoundAssetService soundService,
                      TempImageAssetRepository tempImageRepo,
                      TempSoundAssetRepository tempSoundRepo)
    {
        this.imageFileEndingRepo = imageFileEndingRepo;
        this.soundFileEndingRepo = soundFileEndingRepo;
        this.tagService = tagService;
        this.imageService = imageService;
        this.soundService = soundService;
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

    public static File getSelectedOriginDirectory()
    {
        return selectedOriginDirectory;
    }

    public static void setSelectedOriginDirectory(File selectedOriginDirectory)
    {
        ImportView.selectedOriginDirectory = selectedOriginDirectory;
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
            searchFiles();
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

        this.progressBar = new ProgressBar();
        this.progressBar.setVisible(false);

        this.progressLabel = new Label("");
        this.progressFolderLabel = new Label("");

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
                                               UIUtils.heightFiller("10px"),
                                               this.progressLabel,
                                               this.progressBar,
                                               this.progressFolderLabel,
                                               UIUtils.heightFiller("15px"),
                                               new Hr(),
                                               UIUtils.heightFiller("15px"),
                                               this.audioPlayer
        };

        formLayout.add(fields);
        innerDiv.add(formLayout);

        splitLayout.addToSecondary(layoutDiv);
    }

    private void searchFiles()
    {
        this.importButton.setEnabled(false);
        this.searchButton.setEnabled(false);
        this.selectAllImportCheckboxButton.setEnabled(false);
        this.deselectAllImportCheckboxButton.setEnabled(false);
        this.browseOriginButton.setEnabled(false);

        UI.getCurrent().setPollInterval(500);
        UI ui = UI.getCurrent();

        executorService.submit(() -> {
            var worker = new FileSearchWorker(this);

            worker.onFinish(() -> {
                this.progressBar.setVisible(false);
                this.progressFolderLabel.setText("");
                this.progressLabel.setText("");

                this.imageCountLabel.setText(this.imageFiles.size() + " images found");
                this.soundCountLabel.setText(this.soundFiles.size() + " sounds found");

                this.imageGrid.setItems(this.imageFiles);
                this.soundGrid.setItems(this.soundFiles);

                this.importButton.setEnabled(!this.imageFiles.isEmpty() || !this.soundFiles.isEmpty());
                this.searchButton.setEnabled(true);
                this.selectAllImportCheckboxButton.setEnabled(true);
                this.deselectAllImportCheckboxButton.setEnabled(true);
                this.browseOriginButton.setEnabled(true);

                this.imageGrid.scrollToStart();
                this.soundGrid.scrollToStart();
            });

            worker.work(ui);
        });
    }

    private void importFiles()
    {
        this.lastCheckedImageRow = null;
        this.lastCheckedSoundRow = null;
        this.importButton.setEnabled(false);
        this.searchButton.setEnabled(false);
        this.selectAllImportCheckboxButton.setEnabled(false);
        this.deselectAllImportCheckboxButton.setEnabled(false);
        this.browseOriginButton.setEnabled(false);

        UI.getCurrent().setPollInterval(500);
        UI ui = UI.getCurrent();

        executorService.submit(() -> {
            var worker = new FileImportWorker(this);

            worker.onFinish(() -> {
                this.progressBar.setVisible(false);
                this.importButton.setEnabled(true);
                this.searchButton.setEnabled(true);
                this.selectAllImportCheckboxButton.setEnabled(true);
                this.deselectAllImportCheckboxButton.setEnabled(true);
                this.browseOriginButton.setEnabled(true);
                this.soundGrid.setItems(this.soundFiles);
                this.imageGrid.setItems(this.imageFiles);
                this.soundCountLabel.setText(this.soundFiles.size() + " sounds found");
                this.imageCountLabel.setText(this.imageFiles.size() + " images found");
                this.progressFolderLabel.setText("");
                this.progressLabel.setText("");
            });

            worker.work(ui);
        });
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

    public Grid<AssetImportRow> getImageGrid()
    {
        return imageGrid;
    }

    public Grid<AssetImportRow> getSoundGrid()
    {
        return soundGrid;
    }

    public ImageAssetService getImageService()
    {
        return imageService;
    }

    public SoundAssetService getSoundService()
    {
        return soundService;
    }

    public TagService getTagService()
    {
        return tagService;
    }

    public List<AssetImportRow> getSoundFiles()
    {
        return soundFiles;
    }

    public void setSoundFiles(List<AssetImportRow> soundFiles)
    {
        this.soundFiles = soundFiles;
    }

    public List<AssetImportRow> getImageFiles()
    {
        return imageFiles;
    }

    public void setImageFiles(List<AssetImportRow> imageFiles)
    {
        this.imageFiles = imageFiles;
    }

    public Autocomplete getApplyTagsTextField()
    {
        return applyTagsTextField;
    }

    public Label getImageCountLabel()
    {
        return imageCountLabel;
    }

    public Label getSoundCountLabel()
    {
        return soundCountLabel;
    }

    public ProgressBar getProgressBar()
    {
        return progressBar;
    }

    public Label getProgressLabel()
    {
        return progressLabel;
    }

    public Label getProgressFolderLabel()
    {
        return progressFolderLabel;
    }

    public AssetImportRow getLastCheckedImageRow()
    {
        return lastCheckedImageRow;
    }

    public void setLastCheckedImageRow(AssetImportRow lastCheckedImageRow)
    {
        this.lastCheckedImageRow = lastCheckedImageRow;
    }

    public AssetImportRow getLastCheckedSoundRow()
    {
        return lastCheckedSoundRow;
    }

    public void setLastCheckedSoundRow(AssetImportRow lastCheckedSoundRow)
    {
        this.lastCheckedSoundRow = lastCheckedSoundRow;
    }

    public TempImageAssetRepository getTempImageRepo()
    {
        return tempImageRepo;
    }

    public TempSoundAssetRepository getTempSoundRepo()
    {
        return tempSoundRepo;
    }

    public ImageFileEndingRepository getImageFileEndingRepo()
    {
        return imageFileEndingRepo;
    }

    public SoundFileEndingRepository getSoundFileEndingRepo()
    {
        return soundFileEndingRepo;
    }

    public List<String> getSoundFileEndings()
    {
        return soundFileEndings;
    }

    public void setSoundFileEndings(List<String> soundFileEndings)
    {
        this.soundFileEndings = soundFileEndings;
    }

    public List<String> getImageFileEndings()
    {
        return imageFileEndings;
    }

    public void setImageFileEndings(List<String> imageFileEndings)
    {
        this.imageFileEndings = imageFileEndings;
    }

    public TextField getDirectoryTextField()
    {
        return directoryTextField;
    }

    public TextField getImageFileEndingsTextField()
    {
        return imageFileEndingsTextField;
    }

    public TextField getSoundFileEndingsTextField()
    {
        return soundFileEndingsTextField;
    }
}