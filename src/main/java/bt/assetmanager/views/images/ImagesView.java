package bt.assetmanager.views.images;

import bt.assetmanager.components.SearchAndPreviewLayout;
import bt.assetmanager.constants.Constants;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.service.ImageAssetService;
import bt.assetmanager.data.service.TagService;
import bt.assetmanager.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

@PageTitle("Images")
@Route(value = "images/:samplePersonID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class ImagesView extends Div
{
    private Grid<ImageAsset> grid = new Grid<>(ImageAsset.class, false);
    private SearchAndPreviewLayout<ImageAsset> searchAndPreview;

    @Autowired
    public ImagesView(ImageAssetService assetService, TagService tagService)
    {

        addClassNames("images-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGrid(splitLayout);

        this.searchAndPreview = new SearchAndPreviewLayout(ImageAsset.class, assetService, tagService);
        this.searchAndPreview.onSearch(this.grid::setItems);
        splitLayout.addToSecondary(this.searchAndPreview);

        add(splitLayout);
    }

    private void createGrid(SplitLayout splitLayout)
    {
        this.grid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent())
            {
                this.searchAndPreview.setSelectedElement(e.getFirstSelectedItem().get());
            }
        });

        this.grid.addColumn(new ComponentRenderer<>(
                                    element -> {
                                        Button button = new Button("Open folder");
                                        button.addClickListener(e -> {

                                        });

                                        return button;
                                    }
                            )
        ).setHeader("").setKey("openFolder").setAutoWidth(true);

        this.grid.addColumn(new ComponentRenderer<>(
                                    row -> {
                                        StreamResource imageResource = new StreamResource(row.getPath() + "", () -> {
                                            try
                                            {
                                                return new FileInputStream(Path.of(Constants.IMPORT_DIRECTORY.getAbsolutePath(), row.getPath()).toString());
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

        this.grid.addColumn("fileName").setAutoWidth(true);
        this.grid.addColumn("path").setAutoWidth(true);
        this.grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(this.grid);
    }
}
