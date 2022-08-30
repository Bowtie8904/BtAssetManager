package bt.assetmanager.components;

import bt.log.Log;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataCommunicator;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchyMapper;

import java.lang.reflect.Method;

/**
 * @author Lukas Hartwig
 * @since 30.08.2022
 */
public class ScrollTreeGrid<T> extends TreeGrid<T>
{
    public ScrollTreeGrid(Class<T> beanType)
    {
        super(beanType);
    }

    public ScrollTreeGrid()
    {
        super();
    }

    public ScrollTreeGrid(HierarchicalDataProvider<T, ?> dataProvider)
    {
        super(dataProvider);
    }

    /**
     * The method for scrolling to an item. Takes into account lazy loading nature
     * of grid and does the scroll operation only until the grid has finished
     * loading data
     *
     * @param item the item where to scroll to
     */
    public void scrollToItem(T item)
    {
        int index = getIndexForItem(item);
        Log.info(index + "");
        if (index >= 0)
        {
            scrollToIndex(index);
        }
    }

    /**
     * This is a method for getting the row index of an item in a treegrid. This
     * works but is prone to break in the future versions due to its usage of
     * reflection to access private methods to get access to the index.
     *
     * @param <T>
     */
    private int getIndexForItem(T item)
    {
        HierarchicalDataCommunicator<T> dataCommunicator = super.getDataCommunicator();
        Method getHierarchyMapper = null;

        try
        {
            getHierarchyMapper = HierarchicalDataCommunicator.class.getDeclaredMethod("getHierarchyMapper");
            getHierarchyMapper.setAccessible(true);
            HierarchyMapper<T, ?> mapper = (HierarchyMapper)getHierarchyMapper.invoke(dataCommunicator);
            return mapper.getIndex(item);
        }
        catch (Exception e)
        {
            Log.error("Failed to find index for item", e);
        }

        return -1;
    }
}