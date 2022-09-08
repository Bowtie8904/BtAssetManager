package bt.assetmanager.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
@Entity
public class UserOption
{
    public static final String SAVE_TAGS_IN_FILE_FORMAT_METADATA = "SAVE_TAGS_IN_FILE_FORMAT_METADATA";
    public static final String SAVE_TAGS_IN_METADATA_FILE = "SAVE_TAGS_IN_METADATA_FILE";
    public static final String READ_TAGS_FROM_METADATA_FILE = "READ_TAGS_FROM_METADATA_FILE";
    public static final String DISPLAY_ALL_ASSETS_ON_PAGE_OPEN = "DISPLAY_ALL_ASSETS_ON_PAGE_OPEN";
    public static final String REMOVE_ASSETS_FROM_TOOL_IF_THEY_CANT_BE_FOUND = "REMOVE_ASSETS_FROM_TOOL_IF_THEY_CANT_BE_FOUND";
    public static final String IMAGE_GRID_IMAGES_PER_ROW = "IMAGE_GRID_IMAGES_PER_ROW";
    public static final String IMAGE_GRID_KEEP_ASPECT_RATIO = "IMAGE_GRID_KEEP_ASPECT_RATIO";
    public static final String LAST_SEARCHED_FOLDER = "LAST_SEARCHED_FOLDER";
    public static final String IMAGE_GRID_OR_LIST_VIEW = "IMAGE_GRID_OR_LIST_VIEW";

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 9999)
    private String name;

    @Column(length = 9999)
    private String optionnValue;

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

    public String getOptionValue()
    {
        return optionnValue;
    }

    public void setOptionValue(String optionnValue)
    {
        this.optionnValue = optionnValue;
    }
}