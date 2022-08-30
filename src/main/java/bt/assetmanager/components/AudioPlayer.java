package bt.assetmanager.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.AbstractStreamResource;

/**
 * @author Lukas Hartwig
 * @since 30.08.2022
 */
@Tag("audio")
public class AudioPlayer extends Component
{
    public AudioPlayer()
    {
        getElement().setAttribute("controls", true);
        getElement().setAttribute("loop", "");
    }

    public void setSource(String path)
    {
        getElement().setProperty("src", path);
    }

    public void setSource(AbstractStreamResource resource)
    {
        getElement().setAttribute("src", resource);
    }

    public void play()
    {
        getElement().callJsFunction("play");
    }
}