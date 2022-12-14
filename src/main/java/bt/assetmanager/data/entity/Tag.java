package bt.assetmanager.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

/**
 * @author Lukas Hartwig
 * @since 28.08.2022
 */
@Entity
public class Tag
{
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Tag)
        {
            return ((Tag)o).getName().equalsIgnoreCase(this.name);
        }
        else if (o instanceof String)
        {
            return ((String)o).equalsIgnoreCase(this.name);
        }

        return false;
    }
}