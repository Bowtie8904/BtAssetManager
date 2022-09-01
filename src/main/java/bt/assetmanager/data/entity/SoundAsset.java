package bt.assetmanager.data.entity;

import javax.persistence.*;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 28.08.2022
 */
@Entity
public class SoundAsset implements Asset
{
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 9999, unique = true)
    private String path;

    @Column(length = 9999)
    private String fileName;

    @ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @JoinColumn(name = "tagId", referencedColumnName = "id")
    private List<Tag> tags;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

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

    public List<Tag> getTags()
    {
        return tags;
    }

    public void setTags(List<Tag> tags)
    {
        this.tags = tags;
    }
}