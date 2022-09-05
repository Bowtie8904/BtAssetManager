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
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas Hartwig
 * @since 05.09.2022
 */
public final class ImageFileMetadataUtils
{
    public static void saveWindowsImageExifMetadataTags(File imageFile, List<Tag> tags)
    {
        String fileFormat = "invalid";

        try
        {
            // figure out what file format we have since only jpeg is supported
            fileFormat = Imaging.guessFormat(imageFile).getName();
        }
        catch (IOException e)
        {
            Log.error("Failed to guess file format", e);
        }

        // library only supports jpeg formats for now
        if (fileFormat.equalsIgnoreCase("JPEG"))
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

                Path tempDestFile = Path.of(AssetManagerConstants.TAMP_FILE_DIRECTORY.toString(), imageFile.getName());

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
                Log.error("Failed to save tags to Windows file metadata", e);
            }
        }
    }
}