package bt.assetmanager.views.images;

import bt.assetmanager.components.AssetView;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.service.ImageAssetService;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Asset manager - Images")
@Route(value = "images", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class ImagesView extends Div
{
    @Autowired
    public ImagesView(ImageAssetService assetService, TagService tagService)
    {
        addClassNames("images-view");
        SplitLayout splitLayout = new AssetView<ImageAsset>(ImageAsset.class, assetService, tagService);
        add(splitLayout);
    }
}
