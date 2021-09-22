/*
 * Utils
 * Alex Eidt
 * Contains a collection of useful (and unrelated) functions
 * for the Seam Carver.
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {
    // Returns the minimum of "a", "b", and "c".
    public static int min(int a, int b, int c) {
        return (a < b) ? (a < c ? a : c) : (b < c ? b : c);
    }

    // Returns the minimum of "a" and "b".
    public static int min(int a, int b) {
        return a > b ? a : b;
    }

    /*
     * Converts and integer array representing pixels to a BufferedImage, which is needed
     * to display the image on the User Interface.
     *
     * @param image     The flattened image. Each int represents an RGB pixel.
     * @param width     The width of the image.
     * @param height    The height of the image.
     * @return          The BufferedImage of the input image.
     */
    public static BufferedImage bufferImage(int[] image, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferedImage.setRGB(0, 0, width, height, image, 0, width);
        return bufferedImage;
    }

    /*
     * Grayscales the image.
     *
     * @param image     The image to grayscale. Each int represents an RGB pixel.
     * @return          The grayscaled image. Each byte represents the grayscale value.
     */
    public static byte[][] grayscale(int[][] image) {
        int height = image.length, width = image[0].length;
        byte[][] gray = new byte[height][width];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int pixel = image[h][w];
                int r = (pixel << 16) & 0xFF;
                int g = (pixel << 8) & 0xFF;
                int b = pixel & 0xFF;
                gray[h][w] = (byte) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        return gray;
    }

    /*
     * Find the index of the minimum value in "data".
     *
     * @param data      The input array to find the minimum index of.
     * @return          An integer representing the index of the minimum value in "data".
     */
    public static int minIndex(int[] data) {
        int index = 0;
        int min = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] < min) {
                min = data[i];
                index = i;
            }
        }
        return index;
    }

    /*
     * Edge pads an image.
     *
     * @param image     The image to pad. Grayscaled images only.
     * @param pad       The width of the padding along with edges.
     * @return          The padded image.
     */
    public static byte[][] pad(byte[][] image, int pad) {
        int height = image.length, width = image[0].length;
        int pad2 = pad * 2;
        byte[][] result = new byte[height + pad2][width + pad2];

        int h, w;
        for (h = pad; h < height + pad; h++) {
            for (w = 0; w < pad; w++) result[h][w] = image[h - pad][0];
            for (w = pad; w < width + pad; w++) result[h][w] = image[h - pad][w - pad];
            for (w = width + pad; w < width + pad2; w++) result[h][w] = image[h - pad][width - 1];
        }
        for (h = 0; h < pad; h++) {
            for (w = 0; w < pad; w++) result[h][w] = image[0][0];
            for (w = pad; w < width + pad; w++) result[h][w] = image[0][w - pad];
            for (w = width + pad; w < width + pad2; w++) result[h][w] = image[0][width - 1];
        }
        for (h = height + pad; h < height + pad2; h++) {
            for (w = 0; w < pad; w++) result[h][w] = image[height - 1][0];
            for (w = pad; w < width + pad; w++) result[h][w] = image[height - 1][w - pad];
            for (w = width + pad; w < width + pad2; w++) result[h][w] = image[height - 1][width - 1];
        }
        return result;
    }

    /*
     * Find the gradients of the given image using the sobel filter.
     *
     * @param image     The image to edge.
     * @return          Sobel image.
     */
    public static List<List<Integer>> sobel(int[][] image) {
        int height = image.length + 2, width = image[0].length + 2;
        byte[][] gray = pad(grayscale(image), 1);
        List<List<Integer>> result = new ArrayList<>(height);
        for (int h = 1; h < height - 1; h++) {
            List<Integer> row = new ArrayList<>(width);
            for (int w = 1; w < width - 1; w++) {
                int sx = gray[h - 1][w - 1] -
                        gray[h - 1][w + 1] +
                        2 * gray[h][w - 1] -
                        2 * gray[h][w + 1] +
                        gray[h + 1][w - 1] -
                        gray[h + 1][w - 1];
                int sy = gray[h - 1][w - 1] +
                        2 * gray[h - 1][w] +
                        gray[h - 1][w + 1] -
                        gray[h + 1][w - 1] -
                        2 * gray[h + 1][w] -
                        gray[h + 1][w + 1];
                row.add((int) Math.sqrt(sx * sx + sy * sy));
            }
            result.add(row);
        }
        return result;
    }

    /*
     * Transposes the given image.
     * The transposition is cache-oblivious.
     *
     * @param image     The image to tranpose.
     * @return          The transposed image.
     */
    public static int[][] transpose(int[][] image) {
        int height = image.length, width = image[0].length;
        int blockSize = 8;
        int[][] result = new int[width][height];
        for (int h = 0; h < height; h += blockSize) {
            for (int w = 0; w < width; w += blockSize) {
                for (int i = h; i < i + blockSize; i++) {
                    if (i >= height) break;
                    for (int j = w; j < w + blockSize; j++) {
                        if (j >= width) break;
                        result[j][i] = image[i][j];
                    }
                }
            }
        }
        return result;
    }

    /*
     * Mirros the image along the vertical axis.
     *
     * @param image     Image to mirror.
     * @return          The mirrored image.
     */
    public static int[][] mirror(int[][] image) {
        int height = image.length, width = image[0].length;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width / 2; w++) {
                int temp = image[h][w];
                image[h][w] = image[h][width - 1 - w];
                image[h][width - 1 - w] = temp;
            }
        }
        return image;
    }

    /*
     * Captures the current image and saves to a PNG file in the "Snapshots" directory.
     *
     * @param carver        The SeamCarver being used to carve the image.
     * @param filename      Filename of the output image.
     * @param horizontal    For use with horizontal seam carving.
     * @return              See "Snapshots" directory.
     */
    public static void captureSnapshot(SeamCarver carver, String filename, boolean horizontal) {
        try {
            Utils.writeImage(
                    carver.getImage(), carver.getWidth(), carver.getHeight(),
                    horizontal,
                    filename);
        } catch (IOException ioException) {
            System.out.println("Failed to create Snapshot Image.");
            ioException.printStackTrace();
        }
    }

    /*
     * Delays the main thread for "delay" milliseconds.
     *
     * @param delay:    Delay in milliseconds.
     */
    public static void delay(int delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    /*
     * Finds the optimal scaling factor such that the display image will be
     * approximately half the screen width and height.
     *
     * @param w         Width of Image.
     * @param h         Height of Image.
     * @return          Scaling factor.
     */
    public static int getDimensions(int w, int h) {
        float width = (float) Toolkit.getDefaultToolkit().getScreenSize().width / 2f;
        float height = (float) Toolkit.getDefaultToolkit().getScreenSize().height / 2f;
        int scale = 1;
        float max = 1000000f;
        for (int i = 2; i < 21; i++) {
            float tempH = Math.abs(height - ((float) h / i));
            float tempW = Math.abs(width - ((float) w / i));
            if (tempH + tempW < max) {
                max = tempH + tempW;
                scale = i;
            }
        }
        return scale;
    }

    /*
     * Joins the given list of files together to their full file path.
     *
     * @param files     List of files/directories in the file path.
     * @return          The full file path to the given files.
     */
    public static String joinPath(String... files) {
        String currentDir = Paths.get(System.getProperty("user.dir")).toString();
        String filePath = Paths.get(currentDir, files).toString();
        if (files.length > 0 && !files[files.length - 1].matches("\\.[A-Za-z\\d]+$")) {
            filePath = Paths.get(filePath, "x").toString();
            return filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

    /*
     * Rotates the given BufferedImage by 90 degrees.
     * Original Code from this StackOverflow Thread:
     * https://stackoverflow.com/questions/8639567/java-rotating-images
     *
     * @param image     BufferedImage to rotate.
     * @return          The rotated BufferedImage.
     */
    public static BufferedImage rotate90(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage rotated = new BufferedImage(height, width, image.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.translate((height - width) / 2, (width - height) / 2);
        graphic.rotate(Math.PI / 2, width / 2, height / 2);
        graphic.drawRenderedImage(image, null);
        graphic.dispose();
        return rotated;
    }

    /*
     * Writes the given integer array to an image.
     *
     * @param image         Flattened image as an integer array. Each int represents an RGB pixel.
     * @param width         The width of the image.
     * @param height        The height of the image.
     * @param horizontal    For use with horizontal seam carving.
     * @param filename      Image file name.
     */
    public static void writeImage(
            int[] image,
            int width,
            int height,
            boolean horizontal,
            String filename
    ) throws IOException {
        File file = new File(filename);
        BufferedImage bufferedImage = bufferImage(image, width, height);
        if (horizontal) bufferedImage = rotate90(bufferedImage);
        ImageIO.write(bufferedImage, "PNG", file);
    }

    /*
     * Reads an image into a 2D Integer array, where each int represents an RGB pixel.
     *
     * @param filename      Image file name to read.
     * @return              2D Integer array.
     */
    public static int[][] readImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(new File(filename));
            int width = image.getWidth();
            int height = image.getHeight();
            int[][] pixels = new int[height][width];
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    pixels[h][w] = image.getRGB(w, h);
                }
            }
            return pixels;
        } catch (IOException e) {
            System.out.println("Error opening " + filename);
            System.out.println(e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
