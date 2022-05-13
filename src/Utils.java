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
        return a < b ? a : b;
    }

    // Returns the maximum of "a" and "b".
    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    // Modulus a % m.
    public static int mod(int a, int m) {
        return (a % m + m) % m;
    }

    interface ParallelFunc {
        void process(int cpu, int cpus);
    }

    /*
     * Runs the given function in parallel on separate threads on all CPU cores.
     *
     * @param func      The function to run.
     */
    public static void parallel(ParallelFunc func) {
        int cpus = Runtime.getRuntime().availableProcessors();

        Thread[] threads = new Thread[cpus];
        for (int i = 0; i < cpus; i++) {
            int cpu = i;
            threads[cpu] = new Thread(() -> func.process(cpu, cpus));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException ignored) {
        }
    }

    /*
     * Grayscales the image.
     *
     * @param image     The image to grayscale. Each int represents an RGB pixel.
     * @return          The grayscaled image. Each int represents the grayscale value.
     */
    public static int[][] grayscale(int[][] image) {
        int height = image.length, width = image[0].length;
        int[][] gray = new int[height][width];

        parallel((cpu, cpus) -> {
            for (int h = cpu; h < height; h += cpus) {
                for (int w = 0; w < width; w++) {
                    int pixel = image[h][w];
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;
                    gray[h][w] = (3 * r + 4 * g + b) / 8;
                }
            }
        });

        return gray;
    }

    /*
     * Find the index of the minimum value in "data".
     *
     * @param data      The input array to find the minimum index of.
     * @param size      The actual size of "data".
     * @return          An integer representing the index of the minimum value in "data".
     */
    public static int argmin(int[] data, int size) {
        int index = 0;
        int min = data[0];
        for (int i = 1; i < size; i++) {
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
     * @param image     The image to pad.
     * @param pad       The width of the padding along with edges.
     * @return          The padded image.
     */
    public static int[][] pad(int[][] image, int pad) {
        int height = image.length, width = image[0].length;
        int pad2 = pad * 2;
        int[][] result = new int[height + pad2][width + pad2];

        parallel((cpu, cpus) -> {
            int h, w;
            for (h = pad + cpu; h < height + pad; h += cpus) {
                for (w = 0; w < pad; w++) result[h][w] = image[h - pad][0];
                for (w = pad; w < width + pad; w++) result[h][w] = image[h - pad][w - pad];
                for (w = width + pad; w < width + pad2; w++) result[h][w] = image[h - pad][width - 1];
            }
            for (h = cpu; h < pad; h += cpus) {
                for (w = 0; w < pad; w++) result[h][w] = image[0][0];
                for (w = pad; w < width + pad; w++) result[h][w] = image[0][w - pad];
                for (w = width + pad; w < width + pad2; w++) result[h][w] = image[0][width - 1];
            }
            for (h = height + pad + cpu; h < height + pad2; h += cpus) {
                for (w = 0; w < pad; w++) result[h][w] = image[height - 1][0];
                for (w = pad; w < width + pad; w++) result[h][w] = image[height - 1][w - pad];
                for (w = width + pad; w < width + pad2; w++) result[h][w] = image[height - 1][width - 1];
            }
        });

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
        int[][] gray = pad(grayscale(image), 1);
        List<List<Integer>> result = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            result.add(new ArrayList<>(width));
        }

        parallel((cpu, cpus) -> {
            for (int h = 1 + cpu; h < height - 1; h += cpus) {
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
                    result.get(h - 1).add(Math.abs(sx) + Math.abs(sy));
                }
            }
        });

        return result;
    }

    /*
     * Transposes the given image.
     * The transposition is cache-oblivious.
     *
     * @param image     The image to transpose.
     * @return          The transposed image.
     */
    public static int[][] transpose(int[][] image) {
        int height = image.length, width = image[0].length;
        int blockSize = 8;
        int[][] result = new int[width][height];

        parallel((cpu, cpus) -> {
            for (int h = cpu * blockSize; h < height; h += blockSize * cpus) {
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
        });

        return result;
    }

    /*
     * Mirrors the image along the vertical axis.
     *
     * @param image     Image to mirror.
     * @return          The mirrored image.
     */
    public static int[][] mirror(int[][] image) {
        int height = image.length, width = image[0].length;

        parallel((cpu, cpus) -> {
            for (int h = cpu; h < height; h += cpus) {
                for (int w = 0; w < width / 2; w++) {
                    int temp = image[h][w];
                    image[h][w] = image[h][width - 1 - w];
                    image[h][width - 1 - w] = temp;
                }
            }
        });

        return image;
    }

    /*
     * Delays the main thread for "delay" milliseconds.
     *
     * @param delay:    Delay in milliseconds.
     */
    public static void delay(int delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException ignored) {}
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
     * Writes the given integer array to an image.
     *
     * @param image         Flattened image as an integer array. Each int represents an RGB pixel.
     * @param width         The width of the image.
     * @param height        The height of the image.
     * @param horizontal    For use with horizontal seam carving.
     * @param filename      Image file name.
     * @throws              IOException if the given image could not be created.
     */
    public static void writeImage(
            int[] image,
            int width,
            int height,
            boolean horizontal,
            String filename
    ) {
        File file = new File(filename);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        if (horizontal) {
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    bufferedImage.setRGB(height - y - 1, x, image[index++]);
                }
            }
        } else {
            bufferedImage.setRGB(0, 0, width, height, image, 0, width);
        }
        try {
            ImageIO.write(bufferedImage, "PNG", file);
        } catch (IOException ignored) {}
    }

    /*
     * Reads an image into a 2D Integer array, where each int represents an RGB pixel.
     *
     * @param filename      Image file to read.
     * @return              2D Integer array.
     */
    public static int[][] readImage(String filename) {
        return readImage(new File(filename));
    }

    public static int[][] readImage(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            int[][] pixels = new int[height][width];

            parallel((cpu, cpus) -> {
                for (int h = cpu; h < height; h += cpus) {
                    for (int w = 0; w < width; w++) {
                        pixels[h][w] = image.getRGB(w, h);
                    }
                }
            });

            return pixels;
        } catch (IOException e) {
            return null;
        }
    }
}
