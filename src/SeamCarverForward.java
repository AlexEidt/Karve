/*
 * SeamCarverForward
 * Alex Eidt
 */

import java.util.*;

/*
 * Implements the Seam Carving algorithm using forward energy.
 */
public class SeamCarverForward extends SeamCarverBase implements SeamCarver {

    // Used as a cache to keep track of the minimum error created by removing a seam.
    private final int[][] minimums;

    public SeamCarverForward(int[][] image) {
        super(image);
        int[][] gray = Utils.grayscale(image);
        this.energy = new ArrayList<>(this.height);
        for (int h = 0; h < this.height; h++) {
            List<Integer> row = new ArrayList<>(this.width);
            for (int w = 0; w < this.width; w++) {
                row.add(gray[h][w]);
            }
            this.energy.add(row);
        }
        this.minimums = new int[this.height][this.width];
        this.energyMap();
    }

    public boolean add(boolean highlight, int color) {
        boolean valid = super.add(highlight, color);
        if (valid) this.energyMap();
        return valid;
    }

    public boolean remove(boolean highlight, int color) {
        boolean valid = super.remove(highlight, color);
        if (valid) this.energyMap();
        return valid;
    }

    /*
     * Creates the energy map from the gradient image.
     * Learn more: https://github.com/axu2/improved-seam-carving
     */
    private void energyMap() {
        for (int w = 0; w < this.width; w++) {
            int left = (w - 1 + this.width) % this.width;
            int right = (w + 1) % this.width;

            int cU = Math.abs(this.energy.get(0).get(right) - this.energy.get(0).get(left));
            this.minimums[0][w] = cU;
            this.map[0][w] = cU;
        }
        for (int h = 1; h < this.height; h++) {
            for (int w = 0; w < this.width; w++) {
                int left = (w - 1 + this.width) % this.width;
                int right = (w + 1) % this.width;

                int cU = Math.abs(this.energy.get(h).get(right) - this.energy.get(h).get(left));
                int cL = Math.abs(this.energy.get(h - 1).get(w) - this.energy.get(h).get(left)) + cU;
                int cR = Math.abs(this.energy.get(h - 1).get(w) - this.energy.get(h).get(right)) + cU;

                int mU = this.minimums[h - 1][w] + cU;
                int mL = this.minimums[h - 1][left] + cL;
                int mR = this.minimums[h - 1][right] + cR;

                int min = Utils.min(mU, mL, mR);
                int cMin;
                if (min == mU) {
                    cMin = cU;
                } else if (min == mL) {
                    cMin = cL;
                } else {
                    cMin = cR;
                }

                this.minimums[h][w] = min;
                this.map[h][w] = cMin;
            }
        }
    }
}
