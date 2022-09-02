package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.log.Log;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Lukas Hartwig
 * @since 01.09.2022
 */
public class AssetList<T extends Asset> extends Div
{
    private Grid<T> grid;
    private Consumer<T> onElementSelection;
    private Consumer<T> onElementPlay;
    private Class<T> clazz;

    public AssetList(Class<T> clazz)
    {
        setClassName("grid-wrapper");
        this.grid = new Grid<>(clazz, false);
        this.clazz = clazz;
        createGrid();
    }

    public void setItems(List<T> items)
    {
        this.grid.setItems(items);
        this.grid.scrollToStart();
    }

    public void onElementSelection(Consumer<T> consumer)
    {
        this.onElementSelection = consumer;
    }

    public void onElementPlay(Consumer<T> consumer)
    {
        this.onElementPlay = consumer;
    }

    private void createGrid()
    {
        this.grid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent())
            {
                if (this.onElementSelection != null)
                {
                    this.onElementSelection.accept(e.getFirstSelectedItem().get());
                }
            }
        });

        this.grid.addColumn(new ComponentRenderer<>(
                                    element -> {
                                        Button button = new Button("Open folder");
                                        button.addClickListener(e -> {
                                            try
                                            {
                                                Desktop.getDesktop().open(new File(element.getPath()).getParentFile());
                                            }
                                            catch (IOException ex)
                                            {
                                                Log.error("Failed to open file location", ex);
                                            }
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
                                                if (this.onElementPlay != null)
                                                {
                                                    this.onElementPlay.accept(element);
                                                }
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

        add(this.grid);
    }
}