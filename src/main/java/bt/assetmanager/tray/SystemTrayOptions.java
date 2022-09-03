package bt.assetmanager.tray;

import bt.gui.swing.tray.DefaultSwingSystemTrayFrame;
import bt.log.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * @author Lukas Hartwig
 * @since 02.09.2022
 */
public class SystemTrayOptions extends DefaultSwingSystemTrayFrame
{
    private String url;

    public SystemTrayOptions(int port) throws IOException
    {
        super(ImageIO.read(SystemTrayOptions.class.getResourceAsStream("/trayicon.png")));

        this.url = "http://localhost:" + port + "/images";

        var settings = getSystemTraySettings();

        settings.addLabel("Asset manager");
        settings.addSeparator();

        settings.addOption("Open in browser", e -> {

        });

        settings.addOption("Shutdown", e ->
        {
            System.exit(0);
        });

        sendToSystemTray();
    }

    public void openBrowser()
    {
        try
        {
            Desktop.getDesktop().browse(new URI(this.url));
        }
        catch (Exception ex)
        {
            Log.error("Could not open browser", ex);
        }
    }
}