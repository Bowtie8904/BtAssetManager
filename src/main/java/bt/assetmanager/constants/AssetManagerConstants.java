package bt.assetmanager.constants;

import java.nio.file.Path;

/**
 * @author Lukas Hartwig
 * @since 05.09.2022
 */
public final class AssetManagerConstants
{
    public static final Path TEMP_FILE_DIRECTORY = Path.of(".\\tempFiles\\");
    public static final String UNTAGGED_TAG_NAME = "UNTAGGED";

    static
    {
        TEMP_FILE_DIRECTORY.toFile().mkdirs();
    }
}