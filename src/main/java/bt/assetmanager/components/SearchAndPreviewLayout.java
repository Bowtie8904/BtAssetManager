package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.entity.Tag;
import bt.assetmanager.data.service.AssetService;
import bt.assetmanager.data.service.TagService;
import com.vaadin.componentfactory.Autocomplete;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

        Span span = new Span();
        span.setHeight("30px");

        Span span2 = new Span();
        span2.setHeight("50px");

        Component[] fields = new Component[] { this.foundFilesLabel,
                                               span,
                                               this.searchTextField,
                                               this.fileNameFilterCheckbox,
                                               this.searchButton,
                                               span2,
                                               new Hr(),
                                               this.clazz.equals(ImageAsset.class) ? this.image : this.audioPlayer,
                                               new Hr(),
                                               this.addTagTextField,
                                               this.addTagButton,
                                               this.tagList
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