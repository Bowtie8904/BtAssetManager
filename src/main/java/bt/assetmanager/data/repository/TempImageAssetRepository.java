package bt.assetmanager.data.repository;

import bt.assetmanager.data.entity.TempImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 28.08.2022
 */
@Repository
public interface TempImageAssetRepository extends JpaRepository<TempImageAsset, String>
{
    @Query("select tia " +
            "from TempImageAsset tia " +
            "where not exists " +
            "(" +
            "select ia " +
            "from ImageAsset ia " +
            "where ia.path = tia.path" +
            ")")
    public List<TempImageAsset> getAllNonExisting();
}