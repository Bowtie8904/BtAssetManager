package bt.assetmanager.views.options;

import bt.assetmanager.components.options.UserOptionCheckbox;
import bt.assetmanager.components.options.UserOptionNumberField;
import bt.assetmanager.data.entity.UserOption;
import bt.assetmanager.data.service.ImageAssetService;
import bt.assetmanager.data.service.SoundAssetService;
import bt.assetmanager.data.service.UserOptionService;
import bt.assetmanager.util.UIUtils;
import bt.assetmanager.views.MainLayout;
import bt.assetmanager.workers.ExportTagsToMetadataWorker;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
@PageTitle("Options - Asset manager")
@Route(value = "options", layout = MainLayout.class)
@Uses(Icon.class)
public class OptionsView extends Div
{
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private UserOptionService optionsService;
    private ImageAssetService imageService;
    private SoundAssetService soundService;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Button exportTagsButton;

    @Autowired
    public OptionsView(UserOptionService optionsService, ImageAssetService imageService, SoundAssetService soundService)
    {
        this.optionsService = optionsService;
        this.imageService = imageService;
        this.soundService = soundService;
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

        this.exportTagsButton = new Button("Export tags to metadata");
        this.exportTagsButton.getElement().setProperty("title", "Exports all existing tags to the metadata. Make sure to enable the appropriate metadata options above.");
        this.exportTagsButton.addClickListener(e -> writeTagsToMetadata());

        wrapper.add(UIUtils.heightFiller("150px"), this.exportTagsButton);

        this.progressBar = new ProgressBar();
        this.progressBar.setVisible(false);
        this.progressBar.setWidth("300px");

        this.progressLabel = new Label("");

        wrapper.add(this.progressLabel, this.progressBar);
    }

    private void writeTagsToMetadata()
    {
        this.exportTagsButton.setEnabled(false);
        UI.getCurrent().setPollInterval(500);
        UI ui = UI.getCurrent();

        executorService.submit(() -> {
            var worker = new ExportTagsToMetadataWorker(this, this.imageService, this.soundService, this.optionsService);

            worker.onFinish(() -> {
                this.exportTagsButton.setEnabled(true);
                this.progressBar.setVisible(false);
                this.progressLabel.setText("");
                Notification.show("Finished exporting tags");
            });

            worker.work(ui);
        });
    }

    public ProgressBar getProgressBar()
    {
        return progressBar;
    }

    public Label getProgressLabel()
    {
        return progressLabel;
    }
}