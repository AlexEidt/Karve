import java.io.File;

public class Main {
    public static final String SNAPSHOTS_DIR = "Snapshots/";

    public static void main(String... args) {
        File snapshotsDirectory = new File(SNAPSHOTS_DIR);
        snapshotsDirectory.mkdir();

        new GUI();
    }
}
