/*
 * SeamCarverBase
 * Alex Eidt
 * Implements the SeamCarverBase class.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/*
 * The SeamCarverBase class implements the basic Seam Carving operations
 * that are inherited by all children, which implement different carving
 * approaches.
 */
public class SeamCarverBase {

    // Height of the image.
    protected int height;
    // Width of the image.
    protected int width;
    // Prevents a new image to be written to the "data" field every time a seam is removed.
    protected boolean update;
    // Stores the indices of the seams that were removed from the image.
    protected Stack<int[]> seams;
    // Stores the values of the seams that were removed from the image.
    // The indices of the values in these lists corresponds to those in "seams".
    protected Stack<int[]> values;
    // Stores the values of the seams that were removed from the internal energy image.
    protected Stack<int[]> energyValues;
    // The "energy" image to use.
    protected List<List<Integer>> energy;
    // The actual image.
    protected List<List<Integer>> image;
    // The current image as a flattened array.
    protected int[] data;
    // The energy map used to quickly compute new seams.
    protected int[][] map;

    // Constructor which takes in an 2D image array where each int represents the RGB pixel.
    public SeamCarverBase(int[][] image) {
        this.height = image.length;
        this.width = image[0].length;
        this.update = true;
        this.seams = new Stack<>();
        this.values = new Stack<>();
        this.energyValues = new Stack<>();
        this.image = new ArrayList<>(this.height);
        this.data = new int[this.height * this.width];
        this.map = new int[this.height][this.width];

        for (int h = 0; h < this.height; h++) {
            this.image.add(new ArrayList<>(this.width));
        }

        // Copy the "image" into "this.image" and "this.data".
        Utils.parallel((cpu, cpus) -> {
            for (int h = cpu; h < this.height; h += cpus) {
                for (int w = 0; w < this.width; w++) {
                    this.image.get(h).add(image[h][w]);
                    this.data[h * this.width + w] = image[h][w];
                }
            }
        });
    }

    // Returns the width of the image.
    public int getWidth() {
        return this.width;
    }

    // Returns the height of the image.
    public int getHeight() {
        return this.height;
    }

    // Returns the current state of the image as a flattened array.
    public int[] getImage() {
        return this.data;
    }

    /*
     * Updates the "update" property, which determines if the display
     * image should be updated when seams are added/removed.
     *
     * @param update    Value to set.
     */
    public void setUpdate(boolean update) {
        this.update = update;
    }

    /*
     * Sets the energy to the given value at the given coordinates.
     *
     * @param x     X Coordinate in columns of image.
     * @param y     Y Coordinate in rows of image.
     * @param val   Value to set at the given coordinate.
     */
    public void setEnergy(int x, int y, int val) {
        this.energy.get(y).set(x, val);
    }

    /*
     * Adds "count" seams to the image;
     *
     * @param count     Number of seams to add.
     * @param highlight If true, highlight the added seam.
     * @param color     The color of the highlighted seam.
     * @return          The number of seams that were actually added.
     */
    public int add(int count, boolean highlight, int color) {
        if (this.seams.isEmpty() || count <= 0) return 0;
        this.update = false;
        int index = 1;
        while (index++ < count && this.seams.size() > 1) {
            this.add(highlight, color);
        }
        this.update = true;
        this.add(highlight, color);
        return index - 1;
    }

    /*
     * Adds the most recently removed seam back onto the image.
     *
     * @param highlight If true, highlight the added seam.
     * @param color     The color of the highlighted seam.
     * @return          true if seam could be added, false otherwise.
     */
    public boolean add(boolean highlight, int color) {
        if (this.seams.isEmpty()) return false;

        int[] path = this.seams.pop();
        int[] values = this.values.pop();
        int[] energy = this.energyValues.pop();

        // Go through all indices of the most recently removed
        // seam and add the corresponding values back into the
        // images.
        Utils.parallel((cpu, cpus) -> {
            for (int i = cpu; i < path.length; i += cpus) {
                this.image.get(i).add(path[i], values[i]);
                this.energy.get(i).add(path[i], energy[i]);
            }
        });

        this.width += 1;
        if (this.update) {
            if (highlight) {
                this.updateImage(path, color);
            } else {
                this.updateImage();
            }
        }
        return true;
    }

    /*
     * Remove "count" seams from the image;
     *
     * @param count     Number of seams to remove.
     * @param highlight If true, highlight the added seam.
     * @param color     The color of the highlighted seam.
     * @return          The number of seams that were actually removed.
     */
    public int remove(int count, boolean highlight, int color) {
        if (this.width == 2 || count <= 0) return 0;
        this.update = false;
        int index = 1;
        while (index++ < count && this.width > 3) {
            this.remove(highlight, color);
        }
        this.update = true;
        this.remove(highlight, color);
        return index - 1;
    }

    /*
     * Removes the next seam from the image.
     *
     * @param highlight If true, highlight the added seam.
     * @param color     The color of the highlighted seam.
     * @return          true if seam could be removed, false otherwise.
     */
    public boolean remove(boolean highlight, int color) {
        if (this.width == 2) return false;

        int[] path = new int[this.height];
        int[] values = new int[this.height];
        int[] energyValues = new int[this.height];

        // Find the minimum value in the first row of the energy map.
        int minIndex = Utils.argmin(this.map[0], this.width);
        path[0] = minIndex;
        values[0] = this.image.get(0).remove(minIndex);
        energyValues[0] = this.energy.get(0).remove(minIndex);
        // After finding the minimum value in the first row of the energy
        // map, move through all rows of the image to find a seam. Seams must
        // be connected, therefore only the three pixels directly below the current
        // index will be considered as the next part of the seam.
        for (int h = 1; h < this.height; h++) {
            int[] row = this.map[h];
            if (minIndex == 0) {
                minIndex = Utils.min(row[0], row[1]) == row[0] ? 0 : 1;
            } else if (minIndex == this.width - 1) {
                int minValue = Utils.min(row[this.width - 2], row[this.width - 1]);
                minIndex = row[this.width - 2] == minValue ? this.width - 2 : this.width - 1;
            } else {
                int minValue = Utils.min(row[minIndex - 1], row[minIndex], row[minIndex + 1]);
                if (row[minIndex - 1] == minValue) minIndex = minIndex - 1;
                else if (row[minIndex + 1] == minValue) minIndex = minIndex + 1;
            }
            path[h] = minIndex;
        }

        Utils.parallel((cpu, cpus) -> {
            for (int h = 1 + cpu; h < this.height; h += cpus) {
                values[h] = this.image.get(h).remove(path[h]);
                energyValues[h] = this.energy.get(h).remove(path[h]);
            }
        });

        this.width -= 1;
        if (this.update) {
            if (highlight) {
                this.updateImage(path, color);
            } else {
                this.updateImage();
            }
        }
        this.seams.push(path);
        this.values.push(values);
        this.energyValues.push(energyValues);
        return true;
    }

    /*
     * Updates the display image.
     *
     * @param highlight If true, highlight the added seam.
     * @param color     The color of the highlighted seam.
     */
    public void updateImage(boolean highlight, int color) {
        if (highlight && !this.seams.isEmpty()) {
            this.updateImage(this.seams.peek(), color);
        } else {
            this.updateImage();
        }
    }

    // Updates the current flattened image to match the current state of the image.
    protected void updateImage() {
        Utils.parallel((cpu, cpus) -> {
            for (int h = cpu; h < this.height; h += cpus) {
                for (int w = 0; w < this.width; w++) {
                    this.data[h * this.width + w] = this.image.get(h).get(w);
                }
            }
        });
    }

    /*
     * Updates the current flattened image to match the current state of the image.
     * Adds the highlighted seam to the flattened image as well.
     *
     * @param path      Array of indices of each seam value.
     * @param color     The seam color to use.
     */
    protected void updateImage(int[] path, int color) {
        Utils.parallel((cpu, cpus) -> {
            for (int h = cpu; h < this.height; h += cpus) {
                for (int w = 0; w < this.width; w++) {
                    this.data[h * this.width + w] = this.image.get(h).get(w);
                }
                int pathIndex = path[h];
                for (int i = pathIndex - 1; i <= pathIndex + 1; i++) {
                    if (i < 0 || i >= this.width) continue;
                    this.data[h * this.width + i] = color;
                }
            }
        });
    }
}
