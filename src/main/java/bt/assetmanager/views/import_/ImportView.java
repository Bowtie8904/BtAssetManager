package bt.assetmanager.views.import_;

import bt.assetmanager.components.AudioPlayer;
import bt.assetmanager.components.ScrollTreeGrid;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.entity.ImageFileEnding;
import bt.assetmanager.data.entity.SoundFileEnding;
import bt.assetmanager.data.entity.Tag;
import bt.assetmanager.data.service.ImageFileEndingRepository;
import bt.assetmanager.data.service.SoundFileEndingRepository;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.views.MainLayout;
import bt.log.Log;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
import java.util.stream.Collectors;

@PageTitle("Import")
@Route(value = "import/:samplePersonID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
public class ImportView extends Div
{
    private static File lastSelectedDirectory;
    private Grid<ImageAssetImportRow> imageGrid = new Grid<>(ImageAssetImportRow.class, false);
    private Grid<SoundAssetImportRow> soundGrid = new Grid<>(SoundAssetImportRow.class, false);
    private ImageFileEndingRepository imageFileEndingRepo;
    private SoundFileEndingRepository soundFileEndingRepo;
    private TagService tagService;
    private List<String> soundFileEndings;
    private List<String> imageFileEndings;
    private List<SoundAssetImportRow> soundFiles;
    private List<ImageAssetImportRow> imageFiles;
    private TextField directoryTextField;
    private TextField imageFileEndingsTextField;
    private TextField soundFileEndingsTextField;
    private Button browseOriginButton;
    private Button searchButton;
    private TextField applyTagsTextField;
    private TextField destinationTextField;
    private Button browseDestinationButton;
    private Button importButton;
    private File selectedOriginDirectory;
    private File selectedDestinationDirectory;

    @Autowired
    public ImportView(ImageFileEndingRepository imageFileEndingRepo,
                      SoundFileEndingRepository soundFileEndingRepo,
                      TagService tagService)
    {
        this.imageFileEndingRepo = imageFileEndingRepo;
        this.soundFileEndingRepo = soundFileEndingRepo;
        this.tagService = tagService;

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
        createEditorLayout(outerSplitLayout);

        add(outerSplitLayout);

        this.imageGrid.setItems(this.imageFiles);
        this.soundGrid.setItems(this.soundFiles);
    }

    private void selectOriginDirectory(File additionalRoot)
    {
        List<File> rootFiles = new ArrayList<>();

        if (additionalRoot != null)
        {
            //rootFiles.add(additionalRoot);
        }

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
                lastSelectedDirectory = file.getParentFile();
                this.selectedOriginDirectory = file;
                dialog.close();
            }
        });

        Button closeButton = new Button("Close");
        closeButton.addClickListener(e -> {
            dialog.close();
        });

        tree.setWidth("750px");
        tree.setHeight("500px");

        dialog.add(tree);
        dialog.add(selectButton);
        dialog.add(closeButton);

        if (this.selectedOriginDirectory != null)
        {
            File parentDir = this.selectedOriginDirectory.getParentFile();
            List<File> parentDirs = new ArrayList<>();

            while (parentDir != null)
            {
                parentDirs.add(0, parentDir);
                parentDir = parentDir.getParentFile();
            }

            tree.expand(parentDirs);
            tree.scrollToItem(this.selectedOriginDirectory);
        }

        dialog.open();
    }

    private void selectDestinationDirectory(File additionalRoot)
    {
        List<File> rootFiles = new ArrayList<>();

        if (additionalRoot != null)
        {
            rootFiles.add(additionalRoot);
        }

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

                this.destinationTextField.setValue(file.getAbsolutePath());
                lastSelectedDirectory = file.getParentFile();
                this.selectedDestinationDirectory = file;
                dialog.close();
            }

            this.importButton.setEnabled(this.selectedOriginDirectory != null);
        });

        Button closeButton = new Button("Close");
        closeButton.addClickListener(e -> {
            dialog.close();
        });

        tree.setWidth("750px");
        tree.setHeight("500px");

        dialog.add(tree);
        dialog.add(selectButton);
        dialog.add(closeButton);

        if (this.selectedDestinationDirectory != null)
        {
            File parentDir = this.selectedDestinationDirectory.getParentFile();
            List<File> parentDirs = new ArrayList<>();

            while (parentDir != null)
            {
                parentDirs.add(0, parentDir);
                parentDir = parentDir.getParentFile();
            }

            tree.expand(parentDirs);
            tree.scrollToItem(this.selectedDestinationDirectory);
        }

        dialog.open();
    }

    private void createEditorLayout(SplitLayout splitLayout)
    {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        this.directoryTextField = new TextField("Directory");
        this.directoryTextField.setEnabled(false);
        this.browseOriginButton = new Button("Browse");
        this.browseOriginButton.addClickListener(e -> selectOriginDirectory(lastSelectedDirectory));
        this.imageFileEndingsTextField = new TextField("Image file endings (comma separated)");
        this.imageFileEndingsTextField.setValue(String.join(", ", this.imageFileEndings));

        this.soundFileEndingsTextField = new TextField("Sound file endings (comma separated)");
        this.soundFileEndingsTextField.setValue(String.join(", ", this.soundFileEndings));

        Span span = new Span();
        span.setHeight("50px");

        this.importButton = new Button("Import");
        this.importButton.setEnabled(false);
        this.importButton.addClickListener(e -> importFiles());

        this.searchButton = new Button("Search");
        this.searchButton.addClickListener(e -> {
            this.importButton.setEnabled(false);

            replaceImageEndings();
            replaceSoundEndings();

            this.imageFiles.clear();
            this.soundFiles.clear();

            fillFileGrids(this.selectedOriginDirectory);

            this.imageGrid.setItems(this.imageFiles);
            this.soundGrid.setItems(this.soundFiles);

            this.importButton.setEnabled(!this.imageFiles.isEmpty() || !this.soundFiles.isEmpty());
        });

        this.applyTagsTextField = new TextField("Apply these tags on import (comma separated)");
        this.destinationTextField = new TextField("Destination folder");
        this.browseDestinationButton = new Button("Browse");
        this.browseDestinationButton.addClickListener(e -> selectDestinationDirectory(lastSelectedDirectory));

        Span span2 = new Span();
        span2.setHeight("50px");

        Component[] fields = new Component[] { this.directoryTextField,
                                               this.browseOriginButton,
                                               this.imageFileEndingsTextField,
                                               this.soundFileEndingsTextField,
                                               span,
                                               this.searchButton,
                                               new Hr(),
                                               this.applyTagsTextField,
                                               this.destinationTextField,
                                               this.browseDestinationButton,
                                               span2
        };

        formLayout.add(fields);
        editorDiv.add(formLayout);

        this.importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        span = new Span();
        span.setHeight("50px");
        editorDiv.add(new Hr(), span, this.importButton);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void importFiles()
    {
        List<Tag> tags = new ArrayList<>();

        for (String tag : this.applyTagsTextField.getValue().trim().split(","))
        {
            tags.add(this.tagService.obtainTag(tag));
        }

        this.imageFiles.stream()
                       .filter(ImageAssetImportRow::isShouldImport)
                       .forEach(row -> {
                           ImageAsset asset = new ImageAsset();
                           asset.setTags(tags);

                           String newFilePath = this.selectedDestinationDirectory.getAbsolutePath() + row.getRelativePath();
                           Log.info(newFilePath);
                       });
    }

    private void fillFileGrids(File root)
    {
        for (File file : root.listFiles())
        {
            if (file.isDirectory())
            {
                fillFileGrids(file);
            }
            else
            {
                if (file.getName().lastIndexOf(".") > -1)
                {
                    String fileEnding = file.getName().substring(file.getName().lastIndexOf(".") + 1);

                    if (this.imageFileEndings.contains(fileEnding.toLowerCase().trim()))
                    {
                        var row = new ImageAssetImportRow();
                        row.setFileName(file.getName());
                        row.setAbsolutePath(file.getAbsolutePath());
                        row.setRelativePath(file.getAbsolutePath().substring(this.selectedOriginDirectory.getAbsolutePath().length()));
                        this.imageFiles.add(row);
                    }
                    else if (this.soundFileEndings.contains(fileEnding.toLowerCase().trim()))
                    {
                        var row = new SoundAssetImportRow();
                        row.setFileName(file.getName());
                        row.setAbsolutePath(file.getAbsolutePath());
                        row.setRelativePath(file.getAbsolutePath().substring(this.selectedOriginDirectory.getAbsolutePath().length()));
                        this.soundFiles.add(row);
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
                                             Checkbox checkbox = new Checkbox();
                                             checkbox.setValue(row.isShouldImport());

                                             checkbox.addValueChangeListener(event -> {
                                                 row.setShouldImport(event.getValue());
                                                 Log.info(row.getFileName() + " " + row.isShouldImport());
                                             });

                                             return checkbox;
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
                                             Checkbox checkbox = new Checkbox();
                                             checkbox.setValue(row.isShouldImport());

                                             checkbox.addValueChangeListener(event -> {
                                                 row.setShouldImport(event.getValue());
                                                 Log.info(row.getFileName() + " " + row.isShouldImport());
                                             });

                                             return checkbox;
                                         }
                                 )
        ).setHeader("Import").setKey("shouldImport");

        this.soundGrid.addColumn(new ComponentRenderer<>(
                                         row -> {
                                             StreamResource soundResource = new StreamResource(row.getFileName() + "", () -> {
                                                 try
                                                 {
                                                     return new FileInputStream(row.getAbsolutePath());
                                                 }
                                                 catch (final FileNotFoundException e)
                                                 {
                                                     return null;
                                                 }
                                             });

                                             AudioPlayer player = new AudioPlayer();
                                             player.setSource(soundResource);

                                             return player;
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