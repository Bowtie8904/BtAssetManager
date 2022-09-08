package bt.assetmanager.data.repository;

import bt.assetmanager.data.entity.UserOption;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
public interface UserOptionsRepository extends JpaRepository<UserOption, Long>
{
    public UserOption findByName(String name);
}