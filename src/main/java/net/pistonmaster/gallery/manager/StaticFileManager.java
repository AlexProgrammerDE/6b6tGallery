package net.pistonmaster.gallery.manager;

import com.luciad.imageio.webp.WebPWriteParam;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.gallery.gif.GifSequenceWriter;
import net.pistonmaster.gallery.gif.ImageFrame;
import net.pistonmaster.gallery.storage.ImageStorage;
import net.pistonmaster.gallery.utils.IDGenerator;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.pistonmaster.gallery.gif.GifUtil.readGif;

@RequiredArgsConstructor
public class StaticFileManager {
    private static final List<String> ALLOWED_IMAGE_EXTENSION = List.of("png", "jpg", "jpeg", "webp", "gif", "tiff", "bmp", "wbmp");
    private static final int MAX_IMAGE_SIZE_MB = 5;
    private static final long MEGABYTE = 1024L * 1024L;
    private final Path submitFilesPath;
    private final Path publicFilesPath;

    public StaticFileManager(String submitFilesDir, String publicFilesDir) {
        this.submitFilesPath = Path.of(submitFilesDir);
        this.publicFilesPath = Path.of(publicFilesDir);
    }

    public static long bytesToMB(long bytes) {
        return bytes / MEGABYTE;
    }

    public void init() {
        try {
            Files.createDirectories(submitFilesPath);
            Files.createDirectories(publicFilesPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImageStorage uploadImage(byte[] imageData, ContentDisposition imageMetaData) {
        if (bytesToMB(imageData.length) > MAX_IMAGE_SIZE_MB) {
            throw new WebApplicationException("Image is too big", 413);
        }

        String imageId = IDGenerator.generateID();
        String fileExtension = FilenameUtils.getExtension(imageMetaData.getFileName());
        if (!ALLOWED_IMAGE_EXTENSION.contains(fileExtension)) {
            throw new WebApplicationException("Invalid image extension!", 400);
        }

        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData))) {
            List<ImageReader> readers = new ArrayList<>();
            ImageIO.getImageReaders(in).forEachRemaining(readers::add);
            if (readers.isEmpty()) {
                throw new WebApplicationException("Invalid image format!", 400);
            }

            ImageReader reader = null;
            for (ImageReader reader2 : readers) {
                if (reader2.toString().contains("twelvemonkeys")) {
                    reader = reader2;
                    break;
                }
            }

            if (reader == null) {
                reader = readers.get(0);
            }

            reader.setInput(in, true, false);
            BufferedImage image = reader.read(0);
            IIOMetadata metadata = reader.getImageMetadata(0);
            reader.dispose();

            Path dataPath = submitFilesPath.resolve(imageId);
            Path imagePath = dataPath.resolve(imageId + "." + fileExtension.toLowerCase());

            Files.createDirectories(dataPath);

            try (ImageOutputStream out = ImageIO.createImageOutputStream(Files.newOutputStream(imagePath))) {
                if (reader.getFormatName().equalsIgnoreCase("gif")) {
                    ImageFrame[] frames = readGif(reader);
                    GifSequenceWriter writer =
                            new GifSequenceWriter(out, frames[0].getImage().getType(), frames[0].getDelay(), true, "PistonPost");

                    writer.writeToSequence(frames[0].getImage());
                    for (int i = 1; i < frames.length; i++) {
                        BufferedImage nextImage = frames[i].getImage();
                        writer.writeToSequence(nextImage);
                    }

                    writer.close();
                } else {
                    ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
                    ImageWriter writer = ImageIO.getImageWriters(type, fileExtension).next();

                    ImageWriteParam param = writer.getDefaultWriteParam();
                    if (param.canWriteCompressed()) {
                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                        if (param instanceof JPEGImageWriteParam jpegParam) {
                            jpegParam.setOptimizeHuffmanTables(true);
                        }

                        if (param instanceof WebPWriteParam) {
                            param.setCompressionType(param.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]);
                        } else {
                            param.setCompressionType(param.getCompressionTypes()[0]);
                        }

                        param.setCompressionQuality(1.0f);
                    }

                    writer.setOutput(out);
                    writer.write(null, new IIOImage(image, null, metadata), param);
                    writer.dispose();
                }
            }

            int width = image.getWidth();
            int height = image.getHeight();
            return new ImageStorage(imageId, fileExtension.toLowerCase(), width, height);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
