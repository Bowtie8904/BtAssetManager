package bt.assetmanager.components.options;

import bt.assetmanager.data.entity.UserOption;
import bt.assetmanager.data.service.UserOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
@Component
public class DefaultUserOptionsSetter implements ApplicationListener<ApplicationReadyEvent>
{
    @Autowired
    private UserOptionService optionsService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        this.optionsService.setValueIfNotExist(UserOption.DISPLAY_ALL_ASSETS_ON_PAGE_OPEN, false);
        this.optionsService.setValueIfNotExist(UserOption.IMAGE_GRID_IMAGES_PER_ROW, 10);
        this.optionsService.setValueIfNotExist(UserOption.IMAGE_GRID_KEEP_ASPECT_RATIO, false);
        this.optionsService.setValueIfNotExist(UserOption.IMAGE_GRID_OR_LIST_VIEW, "grid");
        this.optionsService.setValueIfNotExist(UserOption.LAST_SEARCHED_FOLDER, "");
        this.optionsService.setValueIfNotExist(UserOption.READ_TAGS_FROM_METADATA_FILE, true);
        this.optionsService.setValueIfNotExist(UserOption.REMOVE_ASSETS_FROM_TOOL_IF_THEY_CANT_BE_FOUND, false);
        this.optionsService.setValueIfNotExist(UserOption.SAVE_TAGS_IN_FILE_FORMAT_METADATA, false);
        this.optionsService.setValueIfNotExist(UserOption.SAVE_TAGS_IN_METADATA_FILE, true);
    }
}