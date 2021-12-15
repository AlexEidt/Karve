/*
 * Main
 * Alex Eidt
 * Runs the Karve Application.
 */

import java.io.File;

public class Main {
    public static final String SNAPSHOTS_DIR = "Snapshots/";

    public static void main(String... args) {
        File snapshotsDirectory = new File(SNAPSHOTS_DIR);
        snapshotsDirectory.mkdir();
        // Run the "Karve" GUI.
        new GUI();
    }
}
