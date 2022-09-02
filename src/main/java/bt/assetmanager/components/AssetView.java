package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.service.AssetService;
import bt.assetmanager.data.service.TagService;
import com.vaadin.flow.component.splitlayout.SplitLayout;

/**
 * @author Lukas Hartwig
 * @since 01.09.2022
 */
public class AssetView<T extends Asset> extends SplitLayout
{
    private AssetList<T> grid;
    private SearchAndPreviewLayout<T> searchAndPreview;

    public AssetView(Class<T> clazz, AssetService<T> assetService, TagService tagService)
    {
        this.grid = new AssetList<T>(clazz);

        this.searchAndPreview = new SearchAndPreviewLayout(clazz, assetService, tagService);
        this.searchAndPreview.onSearch(this.grid::setItems);
        addToSecondary(this.searchAndPreview);

        this.grid.onElementSelection(element -> this.searchAndPreview.setSelectedElement(element));
        this.grid.onElementPlay(element -> {
            this.searchAndPreview.setSelectedElement(element);
            this.searchAndPreview.playSound();
        });

        addToPrimary(this.grid);
    }
}