package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.entity.Tag;
import bt.assetmanager.data.service.AssetService;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.util.UIUtils;
import bt.assetmanager.workers.AssetSearchWorker;
import bt.log.Log;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.StreamResource;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public class AssetSearchPanel<T extends Asset & Serializable> extends Div
{
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private AssetService<T> assetService;
    private TagService tagService;
    private T currentlySelectedElement;
    private Class<T> clazz;
    private AudioPlayer audioPlayer;
    private Image image;
    private TagSearchTextField searchTextField;
    private TagSearchTextField addTagTextField;
    private Checkbox fileNameFilterCheckbox;
    private Button searchButton;
    private Button addTagButton;
    private Label foundFilesLabel;
    private VirtualList<String> tagList;
    private SerializableConsumer<List<T>> onSearchConsumer;
    private SerializableConsumer<Boolean> onViewChange;
    private Button switchLayoutButton;
    private boolean displayLines;
    private Button openFolderButton;
    private Button deleteButton;
    private ProgressBar progressBar;
    private Label progressLabel;
    private boolean saveTagsInMetadata;

    public AssetSearchPanel(Class<T> clazz, AssetService<T> assetService, TagService tagService, boolean gridView, boolean saveTagsInMetadata)
    {
        this.assetService = assetService;
        this.tagService = tagService;
        this.clazz = clazz;
        this.displayLines = !gridView;
        this.saveTagsInMetadata = saveTagsInMetadata;
        createUI();
    }

    public void setSelectedElement(T element)
    {
        this.currentlySelectedElement = element;

        StreamResource resource = new StreamResource(this.currentlySelectedElement.getFileName() + "", () -> {
            try
            {
                return new FileInputStream(this.currentlySelectedElement.getPath());
            }
            catch (final FileNotFoundException e)
            {
                return null;
            }
        });

        if (this.clazz.equals(ImageAsset.class))
        {
            this.image.setSrc(resource);
        }
        else
        {
            this.audioPlayer.setSource(resource);
        }

        this.tagList.setItems(this.currentlySelectedElement.getTags().stream().map(Tag::getName));
    }

    public void playSound()
    {
        if (this.audioPlayer != null)
        {
            this.audioPlayer.play();
        }
    }

    public void onSearch(SerializableConsumer<List<T>> consumer)
    {
        this.onSearchConsumer = consumer;
    }

    public void onViewChange(SerializableConsumer<Boolean> consumer)
    {
        this.onViewChange = consumer;
    }

    private void createUI()
    {
        setClassName("input-layout");

        Div innerDiv = new Div();
        innerDiv.setClassName("input");
        add(innerDiv);

        FormLayout formLayout = new FormLayout();

        this.image = new Image();
        this.image.setWidth("300px");
        this.audioPlayer = new AudioPlayer();

        this.searchTextField = new TagSearchTextField(4, this.tagService);
        this.searchTextField.setLabel("Search (comma separated tags)");
        this.addTagTextField = new TagSearchTextField(4, this.tagService);
        this.addTagTextField.setLabel("Tag to apply");

        this.foundFilesLabel = new Label("0 files found");

        this.fileNameFilterCheckbox = new Checkbox("Search for file name instead of tags");

        this.searchButton = new Button("Search");
        this.searchButton.addClickListener(e -> onSearchButton());
        this.searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.addTagButton = new Button("Add tag");
        this.addTagButton.addClickListener(e -> onAddTagButton());

        this.tagList = new VirtualList<>();
        this.tagList.setRenderer(new ComponentRenderer<>(tagName -> {
            Button removeButton = new Button("x");
            removeButton.addClickListener(e -> onRemoveTagButton(tagName));

            Label label = new Label(tagName);

            HorizontalLayout tagLayout = new HorizontalLayout();
            tagLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, removeButton, label);
            tagLayout.add(removeButton, label);

            return tagLayout;
        }));

        this.switchLayoutButton = new Button();

        if (this.displayLines)
        {
            this.switchLayoutButton.setIcon(new Icon(VaadinIcon.GRID_SMALL));
        }
        else
        {
            this.switchLayoutButton.setIcon(new Icon(VaadinIcon.LINES_LIST));
        }

        this.switchLayoutButton.addClickListener(e -> {
            this.displayLines = !this.displayLines;

            if (this.displayLines)
            {
                this.switchLayoutButton.setIcon(new Icon(VaadinIcon.GRID_SMALL));
            }
            else
            {
                this.switchLayoutButton.setIcon(new Icon(VaadinIcon.LINES_LIST));
            }

            if (this.onViewChange != null)
            {
                this.onViewChange.accept(this.displayLines);
            }
        });

        this.openFolderButton = new Button("Open folder");

        this.openFolderButton.addClickListener(e -> {
            if (this.currentlySelectedElement != null)
            {
                try
                {
                    Desktop.getDesktop().open(new File(this.currentlySelectedElement.getPath()).getParentFile());
                }
                catch (IOException ex)
                {
                    Log.error("Failed to open file location", ex);
                }
            }
        });

        this.deleteButton = new Button("Delete");

        this.deleteButton.addClickListener(e -> {
            if (this.currentlySelectedElement != null)
            {
                this.assetService.delete(this.currentlySelectedElement);

                Notification.show("Removed file " + this.currentlySelectedElement.getFileName());
            }
        });

        this.deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        this.progressBar = new ProgressBar();
        this.progressBar.setVisible(false);

        this.progressLabel = new Label("");

        Component[] fields = new Component[] { this.clazz.equals(ImageAsset.class) ? this.switchLayoutButton : UIUtils.heightFiller("40px"),
                                               UIUtils.heightFiller("10px"),
                                               this.clazz.equals(ImageAsset.class) ? this.openFolderButton : UIUtils.heightFiller("40px"),
                                               this.foundFilesLabel,
                                               UIUtils.heightFiller("30px"),
                                               this.searchTextField,
                                               this.fileNameFilterCheckbox,
                                               this.searchButton,
                                               UIUtils.heightFiller("10px"),
                                               this.progressLabel,
                                               this.progressBar,
                                               new Hr(),
                                               this.clazz.equals(ImageAsset.class) ? this.image : this.audioPlayer,
                                               new Hr(),
                                               this.addTagTextField,
                                               this.addTagButton,
                                               this.tagList,
                                               UIUtils.heightFiller("40px"),
                                               this.deleteButton
        };

        formLayout.add(fields);
        innerDiv.add(formLayout);
    }

    private void onAddTagButton()
    {
        List<Tag> tags = this.currentlySelectedElement.getTags();
        String newTagName = this.addTagTextField.getValue().trim();

        if (!tags.contains(newTagName) && !newTagName.isEmpty())
        {
            tags.add(this.tagService.obtainTag(newTagName));

            if (tags.size() > 1)
            {
                // if we have more than one tag we can remove the UNTAGGED tag if it exists
                Tag removeTag = new Tag();
                removeTag.setName("UNTAGGED");

                tags.remove(removeTag);
            }

            this.currentlySelectedElement.setTags(tags);
            this.assetService.save(this.currentlySelectedElement, this.saveTagsInMetadata);
            this.tagList.setItems(this.currentlySelectedElement.getTags().stream().map(Tag::getName));
        }

        this.addTagTextField.focus();
    }

    private void onRemoveTagButton(String tagName)
    {
        List<Tag> tags = this.currentlySelectedElement.getTags();

        Tag removeTag = new Tag();
        removeTag.setName(tagName);

        tags.remove(removeTag);

        if (tags.isEmpty())
        {
            tags.add(this.tagService.obtainTag("UNTAGGED"));
        }

        this.currentlySelectedElement.setTags(tags);
        this.assetService.save(this.currentlySelectedElement, this.saveTagsInMetadata);
        this.tagList.setItems(this.currentlySelectedElement.getTags().stream().map(Tag::getName));
    }

    public void onSearchButton()
    {
        this.switchLayoutButton.setEnabled(false);
        this.openFolderButton.setEnabled(false);
        this.searchButton.setEnabled(false);
        this.addTagButton.setEnabled(false);
        this.deleteButton.setEnabled(false);

        UI.getCurrent().setPollInterval(500);
        UI ui = UI.getCurrent();

        executorService.submit(() -> {
            var worker = new AssetSearchWorker<T>(this);

            worker.onFinish(() -> {
                this.progressBar.setVisible(false);
                this.progressLabel.setText("");

                this.switchLayoutButton.setEnabled(true);
                this.openFolderButton.setEnabled(true);
                this.searchButton.setEnabled(true);
                this.addTagButton.setEnabled(true);
                this.deleteButton.setEnabled(true);
            });

            worker.work(ui);
        });
    }

    public AssetService<T> getAssetService()
    {
        return assetService;
    }

    public TagSearchTextField getSearchTextField()
    {
        return searchTextField;
    }

    public Checkbox getFileNameFilterCheckbox()
    {
        return fileNameFilterCheckbox;
    }

    public Label getFoundFilesLabel()
    {
        return foundFilesLabel;
    }

    public Consumer<List<T>> getOnSearchConsumer()
    {
        return onSearchConsumer;
    }

    public ProgressBar getProgressBar()
    {
        return progressBar;
    }

    public Label getProgressLabel()
    {
        return progressLabel;
    }
}