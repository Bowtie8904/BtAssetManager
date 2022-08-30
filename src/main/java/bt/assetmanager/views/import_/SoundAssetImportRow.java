package bt.assetmanager.views.import_;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
public class SoundAssetImportRow
{
    private boolean shouldImport;
    private String fileName;
    private String absolutePath;
    private String relativePath;
    private String parentFolderName;

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

    public String getAbsolutePath()
    {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath)
    {
        this.absolutePath = absolutePath;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath(String relativePath)
    {
        this.relativePath = relativePath;
    }

    public String getParentFolderName()
    {
        return parentFolderName;
    }

    public void setParentFolderName(String parentFolderName)
    {
        this.parentFolderName = parentFolderName;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof SoundAssetImportRow)
        {
            return ((SoundAssetImportRow)o).getAbsolutePath().equalsIgnoreCase(this.absolutePath);
        }

        return false;
    }
}