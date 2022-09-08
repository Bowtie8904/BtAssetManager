package bt.assetmanager.obj;

import java.io.File;
import java.util.*;

/**
 * @author Lukas Hartwig
 * @since 06.09.2022
 */
public class FileBundler<T extends HasFilePath>
{
    private Map<String, Bundle<T>> bundlesFiles;
    private int size;

    public FileBundler()
    {
        this.bundlesFiles = new HashMap<>();
        this.size = 0;
    }

    private Bundle<T> obtainBundle(T asset)
    {
        String folder = new File(asset.getPath()).getParent().toLowerCase();
        return this.bundlesFiles.computeIfAbsent(folder, f -> new Bundle<T>());
    }

    public void addImage(T asset)
    {
        Bundle<T> bundle = obtainBundle(asset);
        bundle.addImage(asset);
        this.size++;
    }

    public void addSound(T asset)
    {
        Bundle<T> bundle = obtainBundle(asset);
        bundle.addSound(asset);
        this.size++;
    }

    public Set<String> getFolderNames()
    {
        return this.bundlesFiles.keySet();
    }

    public Bundle<T> getBundle(String folder)
    {
        return this.bundlesFiles.get(folder);
    }

    public int getSize()
    {
        return size;
    }

    public class Bundle<K>
    {
        private List<K> imageAssets;
        private List<K> soundAssets;

        public Bundle()
        {
            this.imageAssets = new LinkedList<>();
            this.soundAssets = new LinkedList<>();
        }

        public void addImage(K asset)
        {
            this.imageAssets.add(asset);
        }

        public void addSound(K asset)
        {
            this.soundAssets.add(asset);
        }

        public List<K> getImageAssets()
        {
            return imageAssets;
        }

        public List<K> getSoundAssets()
        {
            return soundAssets;
        }
    }
}