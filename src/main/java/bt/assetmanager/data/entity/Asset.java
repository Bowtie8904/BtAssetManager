package bt.assetmanager.data.entity;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public interface Asset
{
    public List<Tag> getTags();

    public void setTags(List<Tag> tags);

    public String getPath();

    public String getFileName();
}