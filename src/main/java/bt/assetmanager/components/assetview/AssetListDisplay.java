package bt.assetmanager.components.assetview;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.ImageAsset;
import bt.log.Log;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.server.StreamResource;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 01.09.2022
 */
public class AssetListDisplay<T extends Asset> extends AssetDisplay<T>
{
    private Grid<T> grid;
    private SerializableConsumer<T> onElementPlay;

    public AssetListDisplay(Class<T> clazz)
    {
        super(clazz);
        setClassName("grid-wrapper");
        setup();
    }

    @Override
    protected void setup()
    {
        super.setup();
        this.grid = createGrid();
        add(this.grid);
    }

    public void onElementPlay(SerializableConsumer<T> consumer)
    {
        this.onElementPlay = consumer;
    }

    @Override
    public void setItems(List<T> items)
    {
        this.grid.setItems(items);
        this.grid.scrollToStart();
    }

    protected Grid<T> createGrid()
    {
        Grid<T> newGrid = new Grid<>(this.clazz, false);

        newGrid.addSelectionListener(e -> {
            if (e.getFirstSelectedItem().isPresent() && this.onElementSelection != null)
            {
                this.onElementSelection.accept(e.getFirstSelectedItem().get());
            }
        });

        newGrid.addColumn(new ComponentRenderer<>(
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
            newGrid.addColumn(new ComponentRenderer<>(
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
            newGrid.addColumn(new ComponentRenderer<>(
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

        newGrid.addColumn("fileName").setAutoWidth(true);
        newGrid.addColumn("path").setAutoWidth(true);
        newGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        newGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        return newGrid;
    }
}