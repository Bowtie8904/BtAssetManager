package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.assetmanager.data.service.AssetService;
import bt.assetmanager.data.service.TagService;
import bt.log.Log;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Lukas Hartwig
 * @since 01.09.2022
 */
public class AssetView<T extends Asset> extends SplitLayout
{
    private Grid<T> grid;
    private SearchAndPreviewLayout<T> searchAndPreview;
    private Class<T> clazz;

    public AssetView(Class<T> clazz, AssetService<T> assetService, TagService tagService)
    {
        this.grid = new Grid<>(clazz, false);
        this.clazz = clazz;

        this.searchAndPreview = new SearchAndPreviewLayout(clazz, assetService, tagService);
        this.searchAndPreview.onSearch(this.grid::setItems);
        addToSecondary(this.searchAndPreview);

        createGrid();
    }

    private void createGrid()
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

        if (this.clazz.equals(ImageAsset.class))
        {
            this.grid.addColumn(new ComponentRenderer<>(
                                        row -> {
                                            StreamResource imageResource = new StreamResource(row.getFileName() + "", () -> {
                                                try
                                                {
                                                    return new FileInputStream(row.getPath());
                                                }
                                                catch (final FileNotFoundException e)
                                                {
                                                    Log.error("", e);
                                                    return null;
                                                }
                                            });

                                            Image image = new Image(imageResource, "Couldn't load image");
                                            image.setHeight("50px");

                                            return image;
                                        }
                                )
            ).setHeader("").setKey("image");
        }
        else
        {
            this.grid.addColumn(new ComponentRenderer<>(
                                        element -> {
                                            Button button = new Button("Play");
                                            button.addClickListener(e -> {
                                                this.searchAndPreview.setSelectedElement(element);
                                                this.searchAndPreview.playSound();
                                            });

                                            return button;
                                        }
                                )
            ).setHeader("").setKey("playSound").setAutoWidth(true);
        }

        this.grid.addColumn("fileName").setAutoWidth(true);
        this.grid.addColumn("path").setAutoWidth(true);
        this.grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        this.grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        addToPrimary(wrapper);
        wrapper.add(this.grid);
    }
}