package bt.assetmanager.util.metadata.image;

import bt.assetmanager.constants.AssetManagerConstants;
import bt.assetmanager.data.entity.Tag;
import bt.log.Log;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Lukas Hartwig
 * @since 05.09.2022
 */
public final class ImageFileMetadataUtils
{
    private static Set<String> supportedFileFormats;

    static
    {
        supportedFileFormats = new HashSet<>();
        supportedFileFormats.add("jpeg");
    }

    public static boolean isValidImageFormat(File imageFile)
    {
        String fileFormat = "invalid";

        try
        {
            fileFormat = Imaging.guessFormat(imageFile).getName();
        }
        catch (IOException e)
        {
            Log.error("Failed to guess file format", e);
        }

        return supportedFileFormats.contains(fileFormat.toLowerCase());
    }

    public static Set<String> getTagsFromWindowsFileMetadata(File imageFile)
    {
        Set<String> tagSet = new HashSet<>();

        if (SystemUtils.IS_OS_WINDOWS && ImageFileMetadataUtils.isValidImageFormat(imageFile))
        {
            try
            {
                ImageMetadata metadata = Imaging.getMetadata(imageFile);

                if (metadata instanceof JpegImageMetadata)
                {
                    JpegImageMetadata jpegMetadata = (JpegImageMetadata)metadata;

                    TiffField field = jpegMetadata.findEXIFValueWithExactMatch(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);

                    for (String tag : field.getValue().toString().split(";"))
                    {
                        tagSet.add(tag.trim().toUpperCase());
                    }
                }
            }
            catch (IOException | ImageReadException e)
            {
                Log.error("Failed to read tags from Windows file metadata", e);
            }
        }

        return tagSet;
    }

    public static void saveWindowsExifMetadataTags(File imageFile, List<Tag> tags)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            // create semicolon separated list of the tags
            String tagString = tags.stream()
                                   .map(Tag::getName)
                                   .collect(Collectors.joining(";"));

            try
            {
                TiffOutputSet outputSet = null;

                ImageMetadata metadata = Imaging.getMetadata(imageFile);
                JpegImageMetadata jpegMetadata = (JpegImageMetadata)metadata;

                if (jpegMetadata != null)
                {
                    TiffImageMetadata exif = jpegMetadata.getExif();

                    if (null != exif)
                    {
                        outputSet = exif.getOutputSet();
                    }
                }

                if (outputSet == null)
                {
                    outputSet = new TiffOutputSet();
                }

                // overwrite tags in file metadata
                TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();
                exifDirectory.removeField(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS);
                exifDirectory.add(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS, tagString);

                Path tempDestFile = Path.of(AssetManagerConstants.TEMP_FILE_DIRECTORY.toString(), imageFile.getName());

                try (FileOutputStream fos = new FileOutputStream(tempDestFile.toFile());
                     OutputStream os = new BufferedOutputStream(fos))
                {
                    // write new data to a new temp file
                    new ExifRewriter().updateExifMetadataLossy(imageFile, os, outputSet);

                    // replace the original file with the just created temp file
                    Files.move(tempDestFile, imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            catch (IOException | ImageReadException | ImageWriteException e)
            {
                Log.error("Failed to save tags to Windows file metadata " + imageFile.getAbsolutePath());
                Log.error(e.getMessage());
            }
        }
    }
}