package bt.assetmanager.views.import_;

import com.vaadin.flow.component.checkbox.Checkbox;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
public class AssetImportRow
{
    private boolean shouldImport;
    private String fileName;
    private String absolutePath;
    private String relativePath;
    private Checkbox importCheckbox;
    private int index;

    public boolean isShouldImport()
    {
        return shouldImport;
    }

    public void setShouldImport(boolean shouldImport)
    {
        this.shouldImport = shouldImport;
    }

    public void checkImportBox(boolean checked)
    {
        if (this.importCheckbox != null)
        {
            this.importCheckbox.setValue(checked);
        }
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

    public Checkbox getImportCheckbox()
    {
        return importCheckbox;
    }

    public void setImportCheckbox(Checkbox importCheckbox)
    {
        this.importCheckbox = importCheckbox;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof AssetImportRow)
        {
            return ((AssetImportRow)o).getAbsolutePath().equalsIgnoreCase(this.absolutePath);
        }

        return false;
    }
}