package bt.assetmanager.views.import_;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
public class ImageAssetImportRow
{
    private boolean shouldImport;
    private String fileName;
    private String path;
    private long size;

    public boolean isShouldImport()
    {
        return shouldImport;
    }

    public void setShouldImport(boolean shouldImport)
    {
        this.shouldImport = shouldImport;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }
}