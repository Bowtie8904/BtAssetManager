package bt.assetmanager.tray;

import bt.gui.swing.tray.DefaultSwingSystemTrayFrame;
import bt.log.Log;

import java.awt.*;
import java.net.URI;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public class SystemTrayOptions extends DefaultSwingSystemTrayFrame
{
    public SystemTrayOptions(Image icon)
    {
        super(icon);
        var settings = getSystemTraySettings();

        settings.addLabel("Asset manager");
        settings.addSeparator();

        settings.addOption("Open in browser", e -> {
            try
            {
                Desktop.getDesktop().browse(new URI("http://localhost:4567/images"));
            }
            catch (Exception ex)
            {
                Log.error("Could not open browser", ex);
            }
        });

        settings.addOption("Shutdown", e ->
        {
            System.exit(0);
        });

        sendToSystemTray();
    }
}