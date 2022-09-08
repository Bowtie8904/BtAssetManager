package bt.assetmanager.views.sounds;

import bt.assetmanager.components.assetview.AssetView;
import bt.assetmanager.data.entity.SoundAsset;
import bt.assetmanager.data.service.SoundAssetService;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.data.service.UserOptionService;
import bt.assetmanager.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Sounds - Asset manager")
@Route(value = "sounds", layout = MainLayout.class)
@Uses(Icon.class)
public class SoundsView extends Div
{
    @Autowired
    public SoundsView(SoundAssetService assetService, TagService tagService, UserOptionService optionsService)
    {
        addClassNames("images-view");
        SplitLayout splitLayout = new AssetView<SoundAsset>(SoundAsset.class, assetService, tagService, optionsService);
        add(splitLayout);
    }
}
