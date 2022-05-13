/*
 * SeamCarverBackward
 * Alex Eidt
 */

import java.util.List;

/*
 * Implements the Seam Carving algorithm using backward energy.
 */
public class SeamCarverBackward extends SeamCarverBase implements SeamCarver {

    public SeamCarverBackward(int[][] image) {
        super(image);
        this.energy = Utils.sobel(image);
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
     * Learn more: https://www.youtube.com/watch?v=rpB6zQNsbQU
     */
    private void energyMap() {
        // Create Energy Map to find least paths through the image.
        // Copy last row of image into energy map.
        for (int w = 0; w < this.width; w++) {
            this.map[this.height - 1][w] = this.energy.get(this.height - 1).get(w);
        }
        // Create energy map.
        for (int h = this.height - 2; h >= 0; h--) {
            List<Integer> row = this.energy.get(h);
            this.map[h][0] = row.get(0) + Utils.min(this.map[h + 1][0], this.map[h + 1][1]);
            int w;
            for (w = 1; w < this.width - 1; w++) {
                this.map[h][w] = row.get(w) +
                        Utils.min(this.map[h + 1][w - 1], this.map[h + 1][w], this.map[h + 1][w + 1]);
            }
            this.map[h][w] = row.get(w) + Utils.min(this.map[h + 1][w - 1], this.map[h + 1][w]);
        }
    }
}
