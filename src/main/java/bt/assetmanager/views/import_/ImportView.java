package bt.assetmanager.views.import_;

import bt.assetmanager.data.entity.SamplePerson;
import bt.assetmanager.data.service.ImageFileEndingRepository;
import bt.assetmanager.data.service.SamplePersonService;
import bt.assetmanager.data.service.SoundFileEndingRepository;
import bt.assetmanager.views.MainLayout;
import bt.log.Log;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PageTitle("Import")
@Route(value = "import/:samplePersonID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
public class ImportView extends Div implements BeforeEnterObserver
{
    private final String SAMPLEPERSON_ID = "samplePersonID";
    private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "import/%s/edit";
    private final SamplePersonService samplePersonService;
    private Grid<SamplePerson> grid = new Grid<>(SamplePerson.class, false);
    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private DatePicker dateOfBirth;
    private TextField occupation;
    private Checkbox important;
    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");
    private BeanValidationBinder<SamplePerson> binder;
    private SamplePerson samplePerson;

    private Grid<ImageAssetImportRow> imageGrid = new Grid<>(ImageAssetImportRow.class, false);
    private ImageFileEndingRepository imageFileEndingRepo;
    private SoundFileEndingRepository soundFileEndingRepo;
    private TextField directoryTextField;
    private TextField imageFileEndingsTextField;
    private TextField soundFileEndingsTextField;
    private Button searchButton = new Button("Search");
    private TextField applyTagsTextField;

    @Autowired
    public ImportView(SamplePersonService samplePersonService,
                      ImageFileEndingRepository imageFileEndingRepo,
                      SoundFileEndingRepository soundFileEndingRepo)
    {
        this.samplePersonService = samplePersonService;
        this.imageFileEndingRepo = imageFileEndingRepo;
        this.soundFileEndingRepo = soundFileEndingRepo;
        addClassNames("import-view");

        // Create UI
        SplitLayout innerSplitLayout = new SplitLayout();
        SplitLayout outerSplitLayout = new SplitLayout();

        createImageGrid(innerSplitLayout);
        createSoundGrid(innerSplitLayout);

        outerSplitLayout.addToPrimary(innerSplitLayout);
        createEditorLayout(outerSplitLayout);

        add(outerSplitLayout);

        this.imageGrid.addColumn(new ComponentRenderer<>(
                                         row -> {
                                             Checkbox checkbox = new Checkbox();
                                             checkbox.setValue(row.isShouldImport());

                                             checkbox.addValueChangeListener(event -> {
                                                 row.setShouldImport(event.getValue());
                                                 Log.info(row.getFileName() + " " + row.isShouldImport());
                                             });

                                             return checkbox;
                                         }
                                 )
        ).setHeader("Import").setKey("shouldImport");

        this.imageGrid.addColumn(new ComponentRenderer<>(
                                         row -> {
                                             StreamResource imageResource = new StreamResource(row.getFileName() + "", () -> {
                                                 try
                                                 {
                                                     return new FileInputStream(row.getPath());
                                                 }
                                                 catch (final FileNotFoundException e)
                                                 {
                                                     return null;
                                                 }
                                             });

                                             Image image = new Image(imageResource, "Couldn't load image");
                                             image.setHeight("30px");

                                             return image;
                                         }
                                 )
        ).setHeader("").setKey("image");

        this.imageGrid.addColumn("fileName").setAutoWidth(true);
        this.imageGrid.addColumn("path").setAutoWidth(true);
        this.imageGrid.addColumn("size").setAutoWidth(true);
        this.imageGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        File dir = new File("F:\\Workspace\\HealthRPG\\images\\equipment\\crimson_rogue");

        List<ImageAssetImportRow> rows = Stream.of(dir.listFiles()).map(file -> {
            ImageAssetImportRow row = new ImageAssetImportRow();
            row.setPath(file.getAbsolutePath());
            row.setFileName(file.getName());
            return row;
        }).collect(Collectors.toList());

        this.imageGrid.setItems(rows);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event)
    {
        Optional<UUID> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(UUID::fromString);
        if (samplePersonId.isPresent())
        {
            Optional<SamplePerson> samplePersonFromBackend = samplePersonService.get(samplePersonId.get());
            if (samplePersonFromBackend.isPresent())
            {
                populateForm(samplePersonFromBackend.get());
            }
            else
            {
                Notification.show(
                        String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ImportView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout)
    {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new TextField("Email");
        phone = new TextField("Phone");
        dateOfBirth = new DatePicker("Date Of Birth");
        occupation = new TextField("Occupation");
        important = new Checkbox("Important");
        Component[] fields = new Component[] { firstName, lastName, email, phone, dateOfBirth, occupation, important };

        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv)
    {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createImageGrid(SplitLayout splitLayout)
    {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(this.imageGrid);
    }

    private void createSoundGrid(SplitLayout splitLayout)
    {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToSecondary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid()
    {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm()
    {
        populateForm(null);
    }

    private void populateForm(SamplePerson value)
    {
        this.samplePerson = value;
        binder.readBean(this.samplePerson);

    }
}
