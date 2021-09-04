public class SeamManager {
    private SeamCarver horizontal, vertical;

    public SeamManager(SeamCarver horizontal, SeamCarver vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
}
