package bt.assetmanager.components.options;

import bt.assetmanager.data.service.UserOptionService;
import com.vaadin.flow.component.checkbox.Checkbox;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
public class UserOptionCheckbox extends Checkbox
{

    public UserOptionCheckbox(UserOptionService optionsService, String optionName, String label, String tooltip)
    {
        setValue(optionsService.getBooleanValue(optionName));
        setLabel(label);
        getElement().setProperty("title", tooltip);

        addValueChangeListener(e -> optionsService.setValue(optionName, e.getValue()));
    }
}