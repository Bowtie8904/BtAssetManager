package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.ImageAsset;
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
public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long>
{
    @Query("select distinct ia " +
            "from ImageAsset ia " +
            "join ia.tags as t  " +
            "where t.name in :tagNames " +
            "group by ia " +
            "having count(t.id) = :size " +
            "order by ia.path")
    public List<ImageAsset> getAllForTags(@Param("tagNames") List<String> tagNames, @Param("size") Long size);

    public List<ImageAsset> findByFileNameContainingIgnoreCase(String fileName);

    public boolean existsByPathIgnoreCase(String path);
}