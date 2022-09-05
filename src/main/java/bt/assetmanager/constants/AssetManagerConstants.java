package bt.assetmanager.constants;

import java.nio.file.Path;

/**
 * @author Lukas Hartwig
 * @since 05.09.2022
 */
public final class AssetManagerConstants
{
    public static final Path TAMP_FILE_DIRECTORY = Path.of(".\\tempFiles\\");

    static
    {
        TAMP_FILE_DIRECTORY.toFile().mkdirs();
    }
}