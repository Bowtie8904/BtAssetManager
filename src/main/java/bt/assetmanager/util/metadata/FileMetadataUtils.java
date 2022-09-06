package bt.assetmanager.util.metadata;

import bt.assetmanager.constants.AssetManagerConstants;
import bt.assetmanager.data.entity.Asset;
import bt.assetmanager.data.entity.Tag;
import bt.log.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas Hartwig
 * @since 06.09.2022
 */
public final class FileMetadataUtils
{
    private static final String METADATA_FILE_NAME = ".asset_tags.meta";

    public static void addTagsToMetadataFile(Asset asset)
    {
        try
        {
            File assetFile = new File(asset.getPath());
            File metadataFile = Path.of(assetFile.getParent(), METADATA_FILE_NAME).toFile();

            List<String> fileContent = null;

            if (!metadataFile.exists())
            {
                fileContent = new LinkedList<>();
                metadataFile.createNewFile();
            }
            else
            {
                fileContent = new ArrayList<>(Files.readAllLines(metadataFile.toPath(), StandardCharsets.UTF_8));
            }

            String tagString = asset.getTags().stream()
                                    .map(Tag::getName)
                                    .collect(Collectors.joining(","));

            String lineContent = asset.getFileName() + ": " + tagString;

            boolean found = false;

            for (int i = 0; i < fileContent.size(); i++)
            {
                if (fileContent.get(i).toLowerCase().startsWith(asset.getFileName().toLowerCase()))
                {
                    fileContent.set(i, lineContent);
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                fileContent.add(lineContent);
            }

            Files.write(metadataFile.toPath(), fileContent, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            Log.error("Failed to write tags to external metadata file", e);
        }
    }

    public static Set<String> getTagsFromMetadataFile(Asset asset)
    {
        Set<String> tagSet = null;

        try
        {
            File assetFile = new File(asset.getPath());
            File metadataFile = Path.of(assetFile.getParent(), METADATA_FILE_NAME).toFile();

            if (metadataFile.exists())
            {
                List<String> fileContent = new ArrayList<>(Files.readAllLines(metadataFile.toPath(), StandardCharsets.UTF_8));

                for (int i = 0; i < fileContent.size(); i++)
                {
                    String line = fileContent.get(i);

                    if (line.toLowerCase().startsWith(asset.getFileName().toLowerCase() + ":"))
                    {
                        tagSet = Stream.of(line.substring(line.lastIndexOf(":") + 1).split(","))
                                       .map(String::trim)
                                       .map(String::toUpperCase)
                                       .filter(tag -> !tag.equals(AssetManagerConstants.UNTAGGED_TAG_NAME))
                                       .distinct()
                                       .collect(Collectors.toSet());
                        break;
                    }
                }
            }

            if (tagSet == null)
            {
                tagSet = Set.of();
            }
        }
        catch (IOException e)
        {
            Log.error("Failed to write tags to external metadata file", e);
        }

        return tagSet;
    }
}