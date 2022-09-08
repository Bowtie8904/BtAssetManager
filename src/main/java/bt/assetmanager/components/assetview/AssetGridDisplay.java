package bt.assetmanager.components.assetview;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.util.UIUtils;
import bt.log.Log;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public class AssetGridDisplay<T extends Asset> extends AssetDisplay<T>
{
    private Grid<AssetGridRow> grid;
    private int elementsPerRow;
    private Image selectedImage;
    private boolean keepImageAspectRatio;

    public AssetGridDisplay(Class<T> clazz, int elementsPerRow, boolean keepImageAspectRatio)
    {
        super(clazz);
        setClassName("grid-wrapper");
        this.elementsPerRow = elementsPerRow;
        this.keepImageAspectRatio = keepImageAspectRatio;
        setup();
    }

    @Override
    protected void setup()
    {
        super.setup();
        this.grid = createGrid();
        add(this.grid);
    }

    @Override
    public void setItems(List<T> items)
    {
        List<AssetGridRow> rows = new LinkedList<>();
        int currentIndex = 0;
        AssetGridRow currentRow = new AssetGridRow();

        if (!items.isEmpty())
        {
            rows.add(currentRow);
        }

        for (T item : items)
        {
            if (currentIndex >= this.elementsPerRow)
            {
                currentRow = new AssetGridRow();
                rows.add(currentRow);
                currentIndex = 0;
            }

            currentRow.add(item);
            currentIndex++;
        }

        this.grid.setItems(rows);
        this.grid.scrollToStart();
    }

    public void selectImage(Image image)
    {
        if (this.selectedImage != null)
        {
            setImageBorder(this.selectedImage, "6px solid transparent");
        }

        this.selectedImage = image;
        setImageBorder(this.selectedImage, "6px solid DarkOrange");
        this.selectedImage.getStyle().set("border-radius", "15px");
    }

    protected Grid<AssetGridRow> createGrid()
    {
        Grid<AssetGridRow> newGrid = new Grid<>(AssetGridRow.class, false);

        String imageSize = (100.0 / this.elementsPerRow) + "%";

        for (int i = 0; i < this.elementsPerRow; i++)
        {
            final int finalCounter = i;
            newGrid.addColumn(new ComponentRenderer<>(
                                      row -> {
                                          T asset = (T)row.get(finalCounter);

                                          if (asset != null)
                                          {
                                              StreamResource imageResource = new StreamResource(asset.getFileName() + "", () -> {
                                                  try
                                                  {
                                                      return new FileInputStream(asset.getPath());
                                                  }
                                                  catch (final FileNotFoundException e)
                                                  {
                                                      Log.error("", e);
                                                      return null;
                                                  }
                                              });

                                              Image image = new Image(imageResource, "Couldn't load image");

                                              setImageBorder(image, "6px solid transparent");
                                              image.getStyle().set("border-radius", "15px");

                                              image.addClickListener(e -> {
                                                  if (this.onElementSelection != null)
                                                  {
                                                      this.onElementSelection.accept(asset);
                                                  }

                                                  selectImage(image);
                                              });

                                              if (this.keepImageAspectRatio)
                                              {
                                                  image.setWidth("100%");
                                                  image.setHeight("100%");

                                                  return image;
                                              }
                                              else
                                              {
                                                  Div wrapper = new Div();
                                                  wrapper.setWidth("100%");

                                                  wrapper.addClassName("image-container");
                                                  image.addClassName("grid-image-ignore-ratio");
                                                  wrapper.add(image);

                                                  return wrapper;
                                              }
                                          }
                                          else
                                          {
                                              return UIUtils.heightFiller("80px");
                                          }
                                      }
                              )
            ).setHeader("").setKey("image" + i).setWidth(imageSize);
        }

        newGrid.setSelectionMode(Grid.SelectionMode.NONE);
        newGrid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);

        return newGrid;
    }

    private void setImageBorder(Image image, String borderValue)
    {
        image.getStyle().set("border", borderValue);
    }
}