package bt.assetmanager.components.assetview;

import bt.assetmanager.components.AssetSearchPanel;
import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.entity.UserOption;
import bt.assetmanager.data.service.AssetService;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.data.service.UserOptionService;
import com.vaadin.flow.component.splitlayout.SplitLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 01.09.2022
 */
public class AssetView<T extends Asset> extends SplitLayout
{
    private AssetDisplay<T> grid;
    private AssetSearchPanel<T> assetSearchPanel;
    private Class<T> clazz;
    private transient List<T> items = new ArrayList<>();
    private UserOptionService optionsService;

    public AssetView(Class<T> clazz, AssetService<T> assetService, TagService tagService, UserOptionService optionsService)
    {
        this.clazz = clazz;
        this.optionsService = optionsService;

        boolean showGridView = this.clazz.equals(ImageAsset.class) && this.optionsService.getValue(UserOption.IMAGE_GRID_OR_LIST_VIEW).equals("grid");
        boolean saveTagsInMetadata = this.optionsService.getBooleanValue(UserOption.SAVE_TAGS_IN_FILE_FORMAT_METADATA);

        this.assetSearchPanel = new AssetSearchPanel<>(clazz, assetService, tagService, showGridView, saveTagsInMetadata);
        addToSecondary(this.assetSearchPanel);

        if (showGridView)
        {
            createGridView();
        }
        else
        {
            createListView();
        }

        this.assetSearchPanel.onViewChange(displayList -> {
            if (Boolean.TRUE.equals(displayList))
            {
                createListView();
                this.optionsService.setValue(UserOption.IMAGE_GRID_OR_LIST_VIEW, "list");
            }
            else
            {
                createGridView();
                this.optionsService.setValue(UserOption.IMAGE_GRID_OR_LIST_VIEW, "grid");
            }

            this.grid.setItems(this.items);
        });

        if (this.optionsService.getBooleanValue(UserOption.DISPLAY_ALL_ASSETS_ON_PAGE_OPEN))
        {
            this.assetSearchPanel.onSearchButton();
        }
    }

    protected void createGridView()
    {
        this.grid = new AssetGridDisplay<>(this.clazz,
                                           this.optionsService.getIntValue(UserOption.IMAGE_GRID_IMAGES_PER_ROW),
                                           this.optionsService.getBooleanValue(UserOption.IMAGE_GRID_KEEP_ASPECT_RATIO));
        this.assetSearchPanel.onSearch(list -> {
            this.items = list;
            this.grid.setItems(list);
        });
        this.grid.onElementSelection(element -> this.assetSearchPanel.setSelectedElement(element));
        addToPrimary(this.grid);
    }

    protected void createListView()
    {
        this.grid = new AssetListDisplay<>(this.clazz);
        this.assetSearchPanel.onSearch(list -> {
            this.items = list;
            this.grid.setItems(list);
        });
        this.grid.onElementSelection(element -> this.assetSearchPanel.setSelectedElement(element));

        ((AssetListDisplay<T>)this.grid).onElementPlay(element -> {
            this.assetSearchPanel.setSelectedElement(element);
            this.assetSearchPanel.playSound();
        });
        addToPrimary(this.grid);
    }
}