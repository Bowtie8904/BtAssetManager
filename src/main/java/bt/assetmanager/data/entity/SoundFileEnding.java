package bt.assetmanager.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Lukas Hartwig
 * @since 29.08.2022
 */
@Entity
public class SoundFileEnding
{
    @Id
    @GeneratedValue
    private Long id;

    private String ending;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getEnding()
    {
        return ending;
    }

    public void setEnding(String ending)
    {
        this.ending = ending;
    }
}