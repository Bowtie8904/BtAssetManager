package bt.assetmanager.components.assetview;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.util.UIUtils;
import bt.log.Log;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.StreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public class AssetGridDisplay<T extends Asset> extends AssetDisplay<T>
{
    private Grid<AssetGridDisplay.AssetGridRow> grid;
    private int elementsPerRow;
    private Image selectedImage;

    public AssetGridDisplay(Class<T> clazz, int elementsPerRow)
    {
        super(clazz);
        setClassName("grid-wrapper");
        this.elementsPerRow = elementsPerRow;
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
        List<AssetGridDisplay.AssetGridRow> rows = new LinkedList<>();
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
            this.selectedImage.getStyle().set("border", "6px solid transparent");
        }

        this.selectedImage = image;
        this.selectedImage.getStyle().set("border", "6px solid DarkOrange");
        this.selectedImage.getStyle().set("border-radius", "25px");
    }

    protected Grid<AssetGridDisplay.AssetGridRow> createGrid()
    {
        Grid<AssetGridDisplay.AssetGridRow> newGrid = new Grid<AssetGridDisplay.AssetGridRow>(AssetGridRow.class, false);

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
                                              image.setHeight("80px");
                                              image.setWidth("80px");
                                              image.getStyle().set("border", "6px solid transparent");

                                              image.addClickListener(e -> {
                                                  if (this.onElementSelection != null)
                                                  {
                                                      this.onElementSelection.accept(asset);
                                                  }

                                                  selectImage(image);
                                              });

                                              return image;
                                          }
                                          else
                                          {
                                              return UIUtils.heightFiller("80px");
                                          }
                                      }
                              )
            ).setHeader("").setKey("image" + i);
        }

        newGrid.setSelectionMode(Grid.SelectionMode.NONE);

        return newGrid;
    }

    class AssetGridRow
    {
        private Map<Integer, Asset> assets;
        private int currentIndex;

        public AssetGridRow()
        {
            this.assets = new HashMap<>();
            this.currentIndex = 0;
        }

        public void add(Asset asset)
        {
            this.assets.put(this.currentIndex, asset);
            this.currentIndex++;
        }

        public Asset get(int index)
        {
            return this.assets.get(index);
        }
    }
}