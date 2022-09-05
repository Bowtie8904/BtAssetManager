package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.util.metadata.image.ImageFileMetadataUtils;
import bt.log.Log;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.lang3.SystemUtils;

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
        saveTagsInOSFileMetadata(entity);
    }

    protected void saveTagsInOSFileMetadata(T entity)
    {
        if (Imaging.hasImageFileExtension(entity.getFileName()))
        {
            if (SystemUtils.IS_OS_WINDOWS)
            {
                ImageFileMetadataUtils.saveWindowsImageExifMetadataTags(new File(entity.getPath()), entity.getTags());
            }
            else
            {
                Log.warn("OS is not supported (" + SystemUtils.OS_NAME + "), cant save tags in file metadata");
            }
        }
    }
}