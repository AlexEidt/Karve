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

    public SeamCarver create(File file, EnergyType type) {
        return this.create(Utils.readImage(file), type);
    }

    public SeamCarver create(String filename, EnergyType type) {
        return this.create(Utils.readImage(filename), type);
    }

    public SeamCarver create(int[][] image, EnergyType type) {
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
