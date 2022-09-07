package bt.assetmanager.data.repository;

import bt.assetmanager.data.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long>
{
    public Tag findByNameIgnoreCase(String name);

    public boolean existsByNameIgnoreCase(String name);

    public List<Tag> findFirst5ByNameContainingIgnoreCase(String name);
}