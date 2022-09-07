package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.SoundAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
@Service
public class SoundAssetService extends BaseAssetService<SoundAsset>
{
    @Autowired
    private SoundAssetRepository soundAssetRepo;

    @Override
    public List<SoundAsset> findByTags(List<String> tags)
    {
        return this.soundAssetRepo.getAllForTags(tags, (long)tags.size());
    }

    @Override
    public List<SoundAsset> findByFileName(String fileName)
    {
        return this.soundAssetRepo.findByFileNameContainingIgnoreCase(fileName);
    }

    @Override
    public void save(SoundAsset entity, boolean saveTagsInMetadataFile)
    {
        this.soundAssetRepo.save(entity);
        super.save(entity, saveTagsInMetadataFile);
    }

    @Override
    public void delete(SoundAsset entity)
    {
        this.soundAssetRepo.delete(entity);
    }

    @Override
    public List<SoundAsset> findAll()
    {
        return this.soundAssetRepo.findAllByOrderByPathAsc();
    }
}