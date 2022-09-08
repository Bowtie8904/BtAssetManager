package bt.assetmanager.views.options;

import bt.assetmanager.components.options.UserOptionCheckbox;
import bt.assetmanager.components.options.UserOptionNumberField;
import bt.assetmanager.data.entity.UserOption;
import bt.assetmanager.data.service.UserOptionService;
import bt.assetmanager.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
@PageTitle("Options - Asset manager")
@Route(value = "options", layout = MainLayout.class)
@Uses(Icon.class)
public class OptionsView extends Div
{
    private UserOptionService optionsService;

    @Autowired
    public OptionsView(UserOptionService optionsService)
    {
        this.optionsService = optionsService;
        addClassNames("options-view");

        SplitLayout layout = new SplitLayout();

        createLeftSide(layout);
        createRightSide(layout);

        add(layout);
    }

    private void createLeftSide(SplitLayout layout)
    {
        VerticalLayout wrapper = new VerticalLayout();
        layout.addToPrimary(wrapper);

        wrapper.add(new UserOptionCheckbox(optionsService,
                                           UserOption.DISPLAY_ALL_ASSETS_ON_PAGE_OPEN,
                                           "Show all assets when opening an asset page",
                                           "If enabled all assets will be loaded and shown when entering an asset page. If disabled you need to click the search button once."));

        wrapper.add(new UserOptionNumberField(optionsService,
                                              UserOption.IMAGE_GRID_IMAGES_PER_ROW,
                                              "Number of images per row in image grid view",
                                              "Defines the number of images per row when using the grid view on the Images page",
                                              1,
                                              20));

        wrapper.add(new UserOptionCheckbox(optionsService,
                                           UserOption.IMAGE_GRID_KEEP_ASPECT_RATIO,
                                           "Keep aspect ratio of images in grid view",
                                           "If enabled images in grid view will be displayed with their correct aspect ratio. If disabled images will be stretched/squished into a square."));
    }

    private void createRightSide(SplitLayout layout)
    {
        VerticalLayout wrapper = new VerticalLayout();
        layout.addToSecondary(wrapper);

        wrapper.add(new UserOptionCheckbox(optionsService,
                                           UserOption.SAVE_TAGS_IN_FILE_FORMAT_METADATA,
                                           "Save tags in the file formats metadata (currently only JPEG on Windows supported)",
                                           "If enabled tags will be saved in the metadata of the file itself utilizing the \"Tags\" property."));

        wrapper.add(new UserOptionCheckbox(optionsService,
                                           UserOption.SAVE_TAGS_IN_METADATA_FILE,
                                           "Save tags in a separate metadata file in the same folder as the assets",
                                           "If enabled tags are also saved in a separate metadata file in the same folder as the asset."));

        wrapper.add(new UserOptionCheckbox(optionsService,
                                           UserOption.READ_TAGS_FROM_METADATA_FILE,
                                           "Read tags from separate metadata file on import",
                                           "If enabled tags will be read from the separate metadata file when an asset is imported. This allows you to safely move file structures without losing your tags."));

        wrapper.add(new UserOptionCheckbox(optionsService,
                                           UserOption.REMOVE_ASSETS_FROM_TOOL_IF_THEY_CANT_BE_FOUND,
                                           "Remove assets from this tool if their files can't be found anymore",
                                           "If enabled this will remove assets from the tool if their files don't exist anymore, for example if you moved them."));
    }
}