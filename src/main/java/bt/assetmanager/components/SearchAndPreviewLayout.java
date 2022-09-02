package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.entity.Tag;
import bt.assetmanager.data.service.AssetService;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.util.UIUtils;
import bt.log.Log;
import com.vaadin.componentfactory.Autocomplete;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public class SearchAndPreviewLayout<T extends Asset> extends Div
{
    private AssetService<T> assetService;
    private TagService tagService;
    private T currentlySelectedElement;
    private Class<T> clazz;
    private AudioPlayer audioPlayer;
    private Image image;
    private Autocomplete searchTextField;
    private Autocomplete addTagTextField;
    private Checkbox fileNameFilterCheckbox;
    private Button searchButton;
    private Button addTagButton;
    private Label foundFilesLabel;
    private VirtualList<String> tagList;
    private Consumer<List<T>> onSearchConsumer;
    private Consumer<String> onAddTagConsumer;
    private Consumer<Boolean> onViewChange;
    private Button switchLayoutButton;
    private boolean displayLines;
    private Button openFolderButton;
    private Button deleteButton;

    public SearchAndPreviewLayout(Class<T> clazz, AssetService<T> assetService, TagService tagService)
    {
        this.assetService = assetService;
        this.tagService = tagService;
        this.clazz = clazz;
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

    public void onSearch(Consumer<List<T>> consumer)
    {
        this.onSearchConsumer = consumer;
    }

    public void onAddTag(Consumer<String> consumer)
    {
        this.onAddTagConsumer = consumer;
    }

    public void onViewChange(Consumer<Boolean> consumer)
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
            removeButton.addClickListener(e -> {
                onRemoveTagButton(tagName);
            });

            Label label = new Label(tagName);

            HorizontalLayout tagLayout = new HorizontalLayout();
            tagLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, removeButton, label);
            tagLayout.add(removeButton, label);

            return tagLayout;
        }));

        this.switchLayoutButton = new Button(new Icon(VaadinIcon.LINES_LIST));

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
            }
        });

        this.deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Component[] fields = new Component[] { this.clazz.equals(ImageAsset.class) ? this.switchLayoutButton : UIUtils.heightFiller("40px"),
                                               UIUtils.heightFiller("10px"),
                                               this.clazz.equals(ImageAsset.class) ? this.openFolderButton : UIUtils.heightFiller("40px"),
                                               this.foundFilesLabel,
                                               UIUtils.heightFiller("30px"),
                                               this.searchTextField,
                                               this.fileNameFilterCheckbox,
                                               this.searchButton,
                                               UIUtils.heightFiller("50px"),
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
            this.assetService.save(this.currentlySelectedElement);
            this.tagList.setItems(this.currentlySelectedElement.getTags().stream().map(Tag::getName));
        }
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
        this.assetService.save(this.currentlySelectedElement);
        this.tagList.setItems(this.currentlySelectedElement.getTags().stream().map(Tag::getName));
    }

    private void onSearchButton()
    {
        List<T> resultSet = List.of();

        if (this.searchTextField.getValue().trim().isEmpty())
        {
            resultSet = this.assetService.findAll();
        }
        else
        {
            if (this.fileNameFilterCheckbox.getValue())
            {
                resultSet = this.assetService.findByFileName(this.searchTextField.getValue().trim());
            }
            else
            {
                String[] singleTags = this.searchTextField.getValue().split(",");
                List<String> singleTagList = Arrays.asList(singleTags).stream().map(String::trim).map(String::toUpperCase).collect(Collectors.toList());

                resultSet = this.assetService.findByTags(singleTagList);
            }
        }

        this.foundFilesLabel.setText(resultSet.size() + " files found");

        if (this.onSearchConsumer != null)
        {
            this.onSearchConsumer.accept(resultSet);
        }
    }
}