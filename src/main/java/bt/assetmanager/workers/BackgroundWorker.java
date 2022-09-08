package bt.assetmanager.workers;

import com.vaadin.flow.component.UI;

/**
 * @author Lukas Hartwig
 * @since 07.09.2022
 */
public interface BackgroundWorker
{
    public void work(UI ui);
}