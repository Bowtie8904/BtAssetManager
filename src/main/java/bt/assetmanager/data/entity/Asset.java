package bt.assetmanager.data.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public interface Asset extends Serializable
{
    public List<Tag> getTags();

    public void setTags(List<Tag> tags);

    public String getPath();

    public String getFileName();
}