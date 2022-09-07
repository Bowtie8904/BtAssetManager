package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.ImageAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
@Service
public class ImageAssetService extends BaseAssetService<ImageAsset>
{
    @Autowired
    private ImageAssetRepository imageAssetRepo;

    @Override
    public List<ImageAsset> findByTags(List<String> tags)
    {
        return this.imageAssetRepo.getAllForTags(tags, (long)tags.size());
    }

    @Override
    public List<ImageAsset> findByFileName(String fileName)
    {
        return this.imageAssetRepo.findByFileNameContainingIgnoreCase(fileName);
    }

    @Override
    public void save(ImageAsset entity, boolean saveTagsInMetadataFile)
    {
        this.imageAssetRepo.save(entity);
        super.save(entity, saveTagsInMetadataFile);
    }

    @Override
    public void delete(ImageAsset entity)
    {
        this.imageAssetRepo.delete(entity);
    }

    @Override
    public List<ImageAsset> findAll()
    {
        return this.imageAssetRepo.findAllByOrderByPathAsc();
    }
}