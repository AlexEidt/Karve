import java.util.*;
import java.util.List;

public class SeamCarver {

    private Stack<List<Integer>> seams, values;
    private Stack<List<Byte>> edgeValues;
    private int[] data;
    private List<List<Integer>> image;
    private int[][] map;
    private List<List<Byte>> edges;
    private int height;
    private int width;

    public SeamCarver(int[][] image) {
        this.height = image.length;
        this.width = image[0].length;
        this.seams = new Stack<>();
        this.values = new Stack<>();
        this.edgeValues = new Stack<>();
        this.image = new ArrayList<>(this.height);
        this.edges = Utils.sobel(image);
        this.data = new int[this.height * this.width];

        createMap();
        int index = 0;
        for (int h = 0; h < this.height; h++) {
            List<Integer> imageRow = new ArrayList<>(this.width);
            for (int w = 0; w < this.width; w++) {
                imageRow.add(image[h][w]);
                this.data[index++] = image[h][w];
            }
            this.image.add(imageRow);
        }
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int[] getImage() {
        return this.data;
    }

    public boolean add(boolean highlight, int color) {
        if (this.seams.isEmpty()) {
            return false;
        }
        List<Integer> path = this.seams.pop();
        List<Integer> values = this.values.pop();
        List<Byte> edges = this.edgeValues.pop();

        for (int i = 0; i < path.size(); i++) {
            this.image.get(i).add(path.get(i), values.get(i));
            this.edges.get(i).add(path.get(i), edges.get(i));
        }
        this.width += 1;
        if (highlight) {
            updateImage(path, color);
        } else {
            updateImage();
        }
        createMap();
        return true;
    }

    public boolean remove(boolean highlight, int color) {
        if (this.width == 2) {
            return false;
        }
        List<Integer> path = new ArrayList<>(this.height);
        List<Integer> values = new ArrayList<>(this.height);
        List<Byte> edgeValues = new ArrayList<>(this.height);
        int minIndex = Utils.minIndex(this.map[0]);
        path.add(minIndex);
        values.add(this.image.get(0).remove(minIndex));
        edgeValues.add(this.edges.get(0).remove(minIndex));
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
            path.add(minIndex);
            values.add(this.image.get(h).remove(minIndex));
            edgeValues.add(this.edges.get(h).remove(minIndex));
        }
        this.width -= 1;
        if (highlight) {
            updateImage(path, color);
        } else {
            updateImage();
        }
        createMap();
        this.seams.push(path);
        this.values.push(values);
        this.edgeValues.push(edgeValues);
        return true;
    }

    private void createMap() {
        // Create Energy Map to find least paths through the image.
        int[][] map = new int[this.height][this.width];
        // Copy last row of image into energy map.
        for (int w = 0; w < this.width; w++) {
            map[this.height - 1][w] = this.edges.get(this.height - 1).get(w);
        }
        // Create energy map.
        for (int h = this.height - 2; h >= 0; h--) {
            List<Byte> row = this.edges.get(h);
            map[h][0] = row.get(0) + Utils.min(map[h + 1][0], map[h + 1][1]);
            int w;
            for (w = 1; w < this.width - 1; w++) {
                map[h][w] = row.get(w) + Utils.min(map[h + 1][w - 1], map[h + 1][w], map[h + 1][w + 1]);
            }
            map[h][w] = row.get(w) + Utils.min(map[h + 1][w - 1], map[h + 1][w]);
        }
        this.map = map;
    }

    private void updateImage() {
        int[] data = new int[this.height * this.width];
        int index = 0;
        for (int h = 0; h < this.height; h++) {
            List<Integer> row = this.image.get(h);
            for (int w = 0; w < this.width; w++) {
                data[index++] = row.get(w);
            }
        }
        this.data = data;
    }

    private void updateImage(List<Integer> path, int color) {
        int[] data = new int[this.height * this.width];
        int index = 0;
        for (int h = 0; h < this.height; h++) {
            List<Integer> row = this.image.get(h);
            for (int w = 0; w < this.width; w++) {
                data[index++] = row.get(w);
            }
            int pathIndex = path.get(h);
            for (int i = pathIndex - 1; i <= pathIndex + 1; i++) {
                if (i < 0 || i >= this.width) continue;
                data[h * this.width + i] = color;
            }
        }
        this.data = data;
    }
}
