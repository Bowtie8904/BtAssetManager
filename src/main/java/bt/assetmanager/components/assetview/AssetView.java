package bt.assetmanager.components.assetview;

import bt.assetmanager.components.AssetSearchPanel;
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
    private AssetSearchPanel<T> assetSearchPanel;
    private Class<T> clazz;
    private List<T> items = new ArrayList<>();

    public AssetView(Class<T> clazz, AssetService<T> assetService, TagService tagService)
    {
        this.clazz = clazz;

        this.assetSearchPanel = new AssetSearchPanel(clazz, assetService, tagService);
        addToSecondary(this.assetSearchPanel);

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
        this.assetSearchPanel.onSearch(list -> {
            this.items = list;
            this.grid.setItems(list);
        });
        this.grid.onElementSelection(element -> this.assetSearchPanel.setSelectedElement(element));
        addToPrimary(this.grid);

        this.assetSearchPanel.onViewChange(displayList -> {
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