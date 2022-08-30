package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.SoundAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lukas Hartwig
 * @since 28.08.2022
 */
@Repository
public interface SoundAssetRepository extends JpaRepository<SoundAsset, Long>
{
}