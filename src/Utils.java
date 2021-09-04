import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static int min(int a, int b, int c) {
        return (a < b) ? (a < c ? a : c) : (b < c ? b : c);
    }

    public static int min(int a, int b) {
        return a > b ? a : b;
    }

    public static BufferedImage bufferImage(int[] image, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferedImage.setRGB(0, 0, width, height, image, 0, width);
        return bufferedImage;
    }

    public static double gauss(double x, double y, double sigma) {
        double coefficient = 1 / (2 * Math.PI * sigma * sigma);
        return coefficient * Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
    }

    public static double[][] gaussian(int size, double sigma) {
        double[][] kernel = new double[size][size];
        double total = 0.0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double value = gauss(x - size / 2.0, y - size / 2.0, sigma);
                kernel[y][x] = value;
                total += value;
            }
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                kernel[y][x] /= total;
            }
        }
        return kernel;
    }

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

    public static List<List<Byte>> sobel(int[][] image) {
        int height = image.length + 2, width = image[0].length + 2;
        byte[][] gray = pad(grayscale(image), 1);
        List<List<Byte>> result = new ArrayList<>(height);
        for (int h = 1; h < height - 1; h++) {
            List<Byte> row = new ArrayList<>(width);
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
                row.add((byte) Math.sqrt(sx * sx + sy * sy));
            }
            result.add(row);
        }
        return result;
    }

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

    // Code from this StackOverflow Thread:
    // https://stackoverflow.com/questions/8639567/java-rotating-images
    public static BufferedImage rotate(BufferedImage image, Double angle) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));
        int w = image.getWidth();
        int h = image.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(h, w, image.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.translate((newW - w) / 2, (newH - h) / 2);
        graphic.rotate(Math.toRadians(angle), w / 2, h / 2);
        graphic.drawRenderedImage(image, null);
        graphic.dispose();
        return rotated;
    }

    public static void writeImage(
            int[] image,
            int width,
            int height,
            boolean horizontal,
            String filename,
            String extension
    ) throws IOException {
        File file = new File(filename + "." + extension);
        int i = 0;
        while (file.exists()) {
            file = new File(filename + i++ + "." + extension);
        }
        BufferedImage bufferedImage = bufferImage(image, width, height);
        if (horizontal) bufferedImage = rotate(bufferedImage, 90.0);
        ImageIO.write(bufferedImage, extension, file);
    }

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
