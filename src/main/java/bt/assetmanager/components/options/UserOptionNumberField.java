package bt.assetmanager.components.options;

import bt.assetmanager.data.service.UserOptionService;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
public class UserOptionNumberField extends HorizontalLayout
{
    public UserOptionNumberField(UserOptionService optionsService, String optionName, String labelText, String tooltip, int min, int max)
    {
        IntegerField field = new IntegerField();
        field.setValue(optionsService.getIntValue(optionName));
        field.setMin(min);
        field.setMax(max);
        field.setHasControls(true);
        field.setHelperText("Between " + min + " and " + max);
        field.getElement().setProperty("title", tooltip);

        field.addValueChangeListener(e -> {
            if (e.getValue() >= min && e.getValue() <= max)
            {
                optionsService.setValue(optionName, e.getValue());
            }
        });

        Label label = new Label(labelText);
        label.getElement().setProperty("title", tooltip);

        setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, field, label);
        add(field, label);
    }
}