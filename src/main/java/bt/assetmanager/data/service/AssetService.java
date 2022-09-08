package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.Asset;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public interface AssetService<T extends Asset & Serializable> extends Serializable
{
    public List<T> findByTags(List<String> tags);

    public List<T> findByFileName(String fileName);

    public void save(T entity, boolean saveTagsInMetadataFile);

    public void delete(T entity);

    public List<T> findAll();
}