package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.TempSoundAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 28.08.2022
 */
@Repository
public interface TempSoundAssetRepository extends JpaRepository<TempSoundAsset, String>
{
    @Query("select tsa " +
            "from TempSoundAsset tsa " +
            "where not exists " +
            "(" +
            "select sa " +
            "from SoundAsset sa " +
            "where sa.path = tsa.path" +
            ")")
    public List<TempSoundAsset> getAllNonExisting();
}