package bt.assetmanager.data.service;

import bt.assetmanager.data.entity.UserOption;
import bt.assetmanager.data.repository.UserOptionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Lukas Hartwig
 * @since 08.09.2022
 */
@Service
public class UserOptionService
{
    @Autowired
    private UserOptionsRepository optionsRepo;

    public String getValue(String optionName)
    {
        UserOption option = this.optionsRepo.findByName(optionName);
        String value = null;

        if (option != null)
        {
            value = option.getOptionValue();
        }

        return value;
    }

    public boolean getBooleanValue(String optionName)
    {
        UserOption option = this.optionsRepo.findByName(optionName);
        boolean value = false;

        if (option != null)
        {
            value = Boolean.parseBoolean(option.getOptionValue());
        }

        return value;
    }

    public int getIntValue(String optionName)
    {
        UserOption option = this.optionsRepo.findByName(optionName);
        int value = 0;

        if (option != null)
        {
            value = Integer.parseInt(option.getOptionValue());
        }

        return value;
    }

    public void setValue(String optionName, Object value)
    {
        UserOption option = this.optionsRepo.findByName(optionName);

        if (option == null)
        {
            option = new UserOption();
            option.setName(optionName);
        }

        option.setOptionValue(value != null ? value.toString() : "");

        this.optionsRepo.save(option);
    }

    public void setValueIfNotExist(String optionName, Object value)
    {
        UserOption option = this.optionsRepo.findByName(optionName);

        if (option == null)
        {
            option = new UserOption();
            option.setName(optionName);
            option.setOptionValue(value != null ? value.toString() : "");

            this.optionsRepo.save(option);
        }
    }
}
