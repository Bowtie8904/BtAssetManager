package bt.assetmanager.components.assetview;

import bt.assetmanager.data.entity.Asset;
import com.vaadin.flow.component.html.Div;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public abstract class AssetDisplay<T extends Asset> extends Div
{
    protected Consumer<T> onElementSelection;
    protected Class<T> clazz;

    public AssetDisplay(Class<T> clazz)
    {
        this.clazz = clazz;
    }

    protected void setup()
    {
    }

    public void onElementSelection(Consumer<T> consumer)
    {
        this.onElementSelection = consumer;
    }

    public abstract void setItems(List<T> items);
}
