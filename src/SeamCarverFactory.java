/*
 * SeamCarverFactory
 * Alex Eidt
 */

import java.io.File;

enum EnergyType {
    BACKWARD,
    FORWARD
}

public class SeamCarverFactory {

    public SeamCarver create(File file, boolean horizontal, EnergyType type) {
        return this.create(Utils.readImage(file), horizontal, type);
    }

    public SeamCarver create(String filename, boolean horizontal, EnergyType type) {
        return this.create(Utils.readImage(filename), horizontal, type);
    }

    private SeamCarver create(int[][] image, boolean horizontal, EnergyType type) {
        if (horizontal) {
            image = Utils.transpose(Utils.mirror(image));
        }
        switch (type) {
            case BACKWARD:
                return new SeamCarverBackward(image);
            case FORWARD:
                return new SeamCarverForward(image);
            default:
                throw new IllegalArgumentException("Invalid EnergyType");
        }
    }
}
