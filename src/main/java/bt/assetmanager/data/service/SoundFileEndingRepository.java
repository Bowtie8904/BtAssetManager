package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.SoundFileEnding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
@Repository
public interface SoundFileEndingRepository extends JpaRepository<SoundFileEnding, Long>
{
}