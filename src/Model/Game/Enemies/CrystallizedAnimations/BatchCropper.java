import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class BatchCropper {
    public static void main(String[] args) throws IOException {
    Path inputDir = Paths.get("");
    Path outputDir = inputDir.resolve("cropped_images");
    Files.createDirectories(outputDir);

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.png")) {
        for (Path entry : stream) {
            BufferedImage img = ImageIO.read(entry.toFile());
            if (img != null) {
                BufferedImage cropped = autoCrop(img);
                
                // Remove spaces (or replace with underscores) from the filename
                String originalName = entry.getFileName().toString();
                String cleanName = originalName.replace(" ", ""); // To remove spaces
                // String cleanName = originalName.replace(" ", "_"); // Alternative: use underscores
                
                ImageIO.write(cropped, "png", outputDir.resolve(cleanName).toFile());
                System.out.println("Processed and renamed: " + cleanName);
            }
        }
    }
}

    private static BufferedImage autoCrop(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        int minX = width, minY = height;
        int maxX = -1, maxY = -1;

        // Iterate to find the bounds
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Check the alpha channel (bits 24-31)
                int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
                if (alpha > 0) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // If no opaque pixels were found, return original image
        if (maxX == -1) return img;

        // Calculate width and height of the crop area
        int w = (maxX - minX) + 1;
        int h = (maxY - minY) + 1;

        return img.getSubimage(minX, minY, w, h);
    }
}