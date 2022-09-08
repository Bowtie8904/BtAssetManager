package bt.assetmanager.data.repository;

import bt.assetmanager.data.entity.SoundAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 28.08.2022
 */
@Repository
public interface SoundAssetRepository extends JpaRepository<SoundAsset, Long>
{
    @Query("select distinct sa " +
            "from SoundAsset sa " +
            "join sa.tags as t  " +
            "where t.name in :tagNames " +
            "group by sa " +
            "having count(t.id) = :size " +
            "order by sa.path")
    public List<SoundAsset> getAllForTags(@Param("tagNames") List<String> tagNames, @Param("size") Long size);

    public List<SoundAsset> findByFileNameContainingIgnoreCase(String fileName);

    public boolean existsByPathIgnoreCase(String path);

    public List<SoundAsset> findAllByOrderByPathAsc();
}