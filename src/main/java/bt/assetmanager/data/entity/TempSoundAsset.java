package bt.assetmanager.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Lukas Hartwig
 * @since 28.08.2022
 */
@Entity
public class TempSoundAsset
{
    @Id
    @Column(length = 9999)
    private String path;

    @Column(length = 9999)
    private String fileName;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
}