package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.util.metadata.FileMetadataUtils;
import bt.assetmanager.util.metadata.image.ImageFileMetadataUtils;

import java.io.File;

/**
 * @author Lukas Hartwig
 * @since 04.09.2022
 */
public abstract class BaseAssetService<T extends Asset> implements AssetService<T>
{
    @Override
    public void save(T entity)
    {
        saveMetadata(entity);
    }

    public void saveMetadata(T entity)
    {
        saveTagsInOSFileMetadata(entity);
        FileMetadataUtils.addTagsToMetadataFile(entity);
    }

    protected void saveTagsInOSFileMetadata(T entity)
    {
        File file = new File(entity.getPath());

        if (ImageFileMetadataUtils.isValidImageFormat(file))
        {
            ImageFileMetadataUtils.saveWindowsExifMetadataTags(file, entity.getTags());
        }
    }
}