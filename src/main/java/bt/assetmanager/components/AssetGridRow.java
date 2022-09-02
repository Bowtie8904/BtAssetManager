package bt.assetmanager.components;

import bt.assetmanager.data.entity.Asset;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public class AssetGridRow
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