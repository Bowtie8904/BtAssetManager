package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.service.AssetService;
import bt.assetmanager.data.service.TagService;
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
    private SearchAndPreviewLayout<T> searchAndPreview;
    private Class<T> clazz;
    private List<T> items = new ArrayList<>();

    public AssetView(Class<T> clazz, AssetService<T> assetService, TagService tagService)
    {
        this.clazz = clazz;

        this.searchAndPreview = new SearchAndPreviewLayout(clazz, assetService, tagService);
        addToSecondary(this.searchAndPreview);

        if (this.clazz.equals(ImageAsset.class))
        {
            createGridView();
        }
        else
        {
            createListView();
        }
    }

    protected void createGridView()
    {
        this.grid = new AssetGridDisplay<T>(this.clazz, 10);
        this.searchAndPreview.onSearch(list -> {
            this.items = list;
            this.grid.setItems(list);
        });
        this.grid.onElementSelection(element -> this.searchAndPreview.setSelectedElement(element));
        addToPrimary(this.grid);

        this.searchAndPreview.onViewChange(displayList -> {
            if (displayList)
            {
                createListView();
            }
            else
            {
                createGridView();
            }

            this.grid.setItems(this.items);
        });
    }

    protected void createListView()
    {
        this.grid = new AssetListDisplay<T>(this.clazz);
        this.searchAndPreview.onSearch(list -> {
            this.items = list;
            this.grid.setItems(list);
        });
        this.grid.onElementSelection(element -> this.searchAndPreview.setSelectedElement(element));

        ((AssetListDisplay<T>)this.grid).onElementPlay(element -> {
            this.searchAndPreview.setSelectedElement(element);
            this.searchAndPreview.playSound();
        });
        addToPrimary(this.grid);
    }
}