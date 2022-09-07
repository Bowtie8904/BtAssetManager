package bt.assetmanager.views.import_;

import java.io.File;
import java.util.*;

/**
 * @author Lukas Hartwig
 * @since 06.09.2022
 */
public class ImportFileBundler
{
    private Map<String, Bundle> bundlesFiles;

    public ImportFileBundler()
    {
        this.bundlesFiles = new HashMap<>();
    }

    private Bundle obtainBundle(AssetImportRow asset)
    {
        String folder = new File(asset.getAbsolutePath()).getParent().toLowerCase();
        Bundle bundle = this.bundlesFiles.get(folder);

        if (bundle == null)
        {
            bundle = new Bundle();
            this.bundlesFiles.put(folder, bundle);
        }

        return bundle;
    }

    public void addImage(AssetImportRow asset)
    {
        Bundle bundle = obtainBundle(asset);
        bundle.addImage(asset);
    }

    public void addSound(AssetImportRow asset)
    {
        Bundle bundle = obtainBundle(asset);
        bundle.addSound(asset);
    }

    public Set<String> getFolderNames()
    {
        return this.bundlesFiles.keySet();
    }

    public Bundle getBundle(String folder)
    {
        return this.bundlesFiles.get(folder);
    }

    public class Bundle
    {
        private List<AssetImportRow> imageAssets;
        private List<AssetImportRow> soundAssets;

        public Bundle()
        {
            this.imageAssets = new LinkedList<>();
            this.soundAssets = new LinkedList<>();
        }

        public void addImage(AssetImportRow asset)
        {
            this.imageAssets.add(asset);
        }

        public void addSound(AssetImportRow asset)
        {
            this.soundAssets.add(asset);
        }

        public List<AssetImportRow> getImageAssets()
        {
            return imageAssets;
        }

        public List<AssetImportRow> getSoundAssets()
        {
            return soundAssets;
        }
    }
}