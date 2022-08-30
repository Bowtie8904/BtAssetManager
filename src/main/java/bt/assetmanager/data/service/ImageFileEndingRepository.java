package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.ImageFileEnding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
@Repository
public interface ImageFileEndingRepository extends JpaRepository<ImageFileEnding, Long>
{
}