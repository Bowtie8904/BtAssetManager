package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.Asset;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
public interface AssetService<T extends Asset>
{
    public List<T> findByTags(List<String> tags);

    public List<T> findByFileName(String fileName);

    public void save(T entity);

    public void delete(T entity);

    public List<T> findAll();
}