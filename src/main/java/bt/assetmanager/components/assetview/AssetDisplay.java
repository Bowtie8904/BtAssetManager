package bt.assetmanager.components.assetview;

import bt.assetmanager.data.entity.Asset;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializableConsumer;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public abstract class AssetDisplay<T extends Asset> extends Div
{
    protected SerializableConsumer<T> onElementSelection;
    protected Class<T> clazz;

    protected AssetDisplay(Class<T> clazz)
    {
        this.clazz = clazz;
    }

    protected void setup()
    {
    }

    public void onElementSelection(SerializableConsumer<T> consumer)
    {
        this.onElementSelection = consumer;
    }

    public abstract void setItems(List<T> items);

    public abstract void removeItem(T item);
}
