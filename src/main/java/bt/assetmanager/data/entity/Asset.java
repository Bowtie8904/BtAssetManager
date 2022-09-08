package bt.assetmanager.data.entity;

import bt.assetmanager.obj.HasFilePath;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public interface Asset extends HasFilePath
{
    public List<Tag> getTags();

    public void setTags(List<Tag> tags);

    public String getFileName();
}