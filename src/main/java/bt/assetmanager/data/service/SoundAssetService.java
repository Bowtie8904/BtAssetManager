package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.SoundAsset;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 31.08.2022
 */
@Service
public class SoundAssetService implements AssetService<SoundAsset>
{
    @Override
    public List<SoundAsset> findByTags(List<String> tags)
    {
        return null;
    }

    @Override
    public List<SoundAsset> findByFileName(String fileName)
    {
        return null;
    }

    @Override
    public void save(SoundAsset entity)
    {

    }

    @Override
    public List<SoundAsset> findAll()
    {
        return null;
    }
}