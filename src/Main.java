/*
 * Main
 * Alex Eidt
 * Runs the Graphical User Interface (GUI) window that allows the user
 * to interface with the Karver.
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    // CARVING is a flag storing whether or not the carving animation is happening.
    public static volatile boolean CARVING = false;
    // RECORDING is a flag storing whether the carving is being recorded.
    public static volatile boolean RECORDING = false;
    // The direction the carving animation plays. Removing -> False, Adding -> True.
    public static volatile boolean DIRECTION = false;
    // HIGHLIGHT is a flag that determines whether removed/added seams should be colored.
    public static volatile boolean HIGHLIGHT = false;
    // HORIZONTAL is a flag that determines whether horizontal or vertical seam carving happens.
    public static volatile boolean HORIZONTAL = false;
    // Track frame numbers when recording or taking snapshots.
    public static volatile int COUNT = 0;
    // Scaling factor for display image.
    public static volatile int SCALE = 1;
    // Size of the button icons.
    public static final int ICON_SIZE = 30;
    // File path to the Icons folder.
    public static String ICONS_FOLDER = Utils.joinPath("Icons");

    // -------------------------------------------------------------------
    // USER SPECIFIED
    // -------------------------------------------------------------------
    // Determines the width of the "brush" used to remove edges by clicking on the image.
    public static volatile int BRUSH_WIDTH = 5;
    // The image file name to seam carve.
    public static String FILENAME = Utils.joinPath("Documentation", "starwars.png");
    // Color of the seams (if highlighting).
    public static final Color SEAM_COLOR = new Color(88, 150, 236);

    public static void main(String[] args) {
        File snapshotsDirectory = new File("Snapshots/");
        snapshotsDirectory.mkdir();

        // Create Seam Carver for vertical Seam Carving.
        SeamCarver verticalCarver = new SeamCarver(FILENAME);
        // Create Seam Carver for horizontal Seam Carving by mirroring and then
        // transposing the image.
        SeamCarver horizontalCarver = new SeamCarver(
                Utils.transpose(Utils.mirror(Utils.readImage(FILENAME))));
        SeamCarver[] carver = {verticalCarver};

        SCALE = Utils.getDimensions(carver[0].getWidth(), carver[0].getHeight());

        // The seam color.
        int highlightColor = SEAM_COLOR.getRGB();

        JFrame frame = new JFrame("Karve");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Add the Image being carved.
        JLabel imageLabel = new JLabel(getScaledImage(carver[0]), JLabel.CENTER);
        JPanel imagePanel = new JPanel();
        imagePanel.add(imageLabel);
        panel.add(imageLabel);

        // The menuPanel stores all the buttons, checkboxes and the slider.
        JPanel menuPanel = new JPanel();
        Font font = new Font("Arial", Font.PLAIN, 15);

        // Add the "Karve" title.
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Karve", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        titlePanel.add(title);
        menuPanel.add(titlePanel);

        // Add the slider.
        JPanel sliderPanel = new JPanel(new GridLayout(2, 1));
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
        slider.setFocusable(false);
        JLabel speed = new JLabel("Speed", JLabel.CENTER);
        speed.setBorder(new EmptyBorder(10, 10, 10, 10));
        speed.setFont(font);
        sliderPanel.add(speed);
        sliderPanel.add(slider);
        menuPanel.add(sliderPanel);

        // Add the checkboxes for "Show Seams", "Horizontal", and "Record".
        // "Show Seams" checkbox.
        JPanel checkBoxPanel = new JPanel(new GridLayout());
        JCheckBox highlight = new JCheckBox("Show Seams");
        highlight.setFont(font);
        highlight.addItemListener(e -> HIGHLIGHT = !HIGHLIGHT);
        checkBoxPanel.add(highlight);
        // "Horizontal" checkbox.
        JCheckBox horizontal = new JCheckBox("Horizontal");
        horizontal.setFont(font);
        horizontal.addItemListener(e -> {
            HORIZONTAL = !HORIZONTAL;
            carver[0] = HORIZONTAL ? horizontalCarver : verticalCarver;
            imageLabel.setIcon(getScaledImage(carver[0]));
        });
        checkBoxPanel.add(horizontal);
        // "Recording" checkbox.
        JCheckBox recording = new JCheckBox("Recording");
        recording.setFont(font);
        recording.addItemListener(e -> RECORDING = !RECORDING);
        checkBoxPanel.add(recording);
        menuPanel.add(checkBoxPanel);

        // Add all "Pause/Play", "Add", "Remove" and "Snapshot" buttons.
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
        ImageIcon play = buttonIcon(ICONS_FOLDER + "play.png");
        ImageIcon pause = buttonIcon(ICONS_FOLDER + "pause.png");
        JButton playButton = new JButton(play);
        JButton addButton = new JButton(buttonIcon(ICONS_FOLDER + "add.png"));
        JButton removeButton = new JButton(buttonIcon(ICONS_FOLDER + "remove.png"));
        JButton snapshotButton = new JButton(buttonIcon(ICONS_FOLDER + "snapshot.png"));

        // Function to run in separate thread when the "Play" button is pressed.
        Runnable animate = () -> {
            // Carve the image all the way until nothing is left.
            // Then begin reconstructing the image by the seams that were removed
            // and repeat until the user stops carving.
            while (CARVING) {
                if (DIRECTION) {
                    while (CARVING && carver[0].add(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        Utils.delay(slider.getValue());
                    }
                    while (CARVING && carver[0].remove(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        Utils.delay(slider.getValue());
                    }
                } else {
                    while (CARVING && carver[0].remove(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        Utils.delay(slider.getValue());
                    }
                    while (CARVING && carver[0].add(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        Utils.delay(slider.getValue());
                    }
                }
            }
        };

        Thread[] thread = {new Thread(animate)};
        // Manage animation thread when "Play/Pause" button is clicked.
        playButton.addActionListener(e -> {
            addButton.setEnabled(CARVING);
            removeButton.setEnabled(CARVING);
            CARVING = !CARVING;
            if (CARVING) {
                playButton.setIcon(pause);
                thread[0].start();
            } else {
                playButton.setIcon(play);
                thread[0] = new Thread(animate);
            }
        });
        // Add seam back when "Add" button is clicked.
        addButton.addActionListener(e -> {
            DIRECTION = true;
            boolean valid = carver[0].add(HIGHLIGHT, highlightColor);
            if (valid) imageLabel.setIcon(getScaledImage(carver[0]));
        });
        // Remove seam when "Remove" button is clicked.
        removeButton.addActionListener(e -> {
            DIRECTION = false;
            boolean valid = carver[0].remove(HIGHLIGHT, highlightColor);
            if (valid) imageLabel.setIcon(getScaledImage(carver[0]));
        });
        // Create a snapshot of the current image when the "Snapshot" button is clicked.
        snapshotButton.addActionListener(e -> captureSnapshot(carver[0]));
        buttonPanel.add(playButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(snapshotButton);

        menuPanel.add(buttonPanel);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        panel.add(menuPanel);

        frame.add(panel);
        frame.pack();

        // Change pixels values of sobel image to change where seams appear.
        // Change pixels by clicking on the image.
        imageLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            /*
             * Activates whenever the user clicks and drags their mouse over any part
             * of the display image. The coordinates the user click on are converted into
             * pixel coordinates corresponding to the actual image. Depending on
             * a left or right click the corresponding pixels are colored red or green
             * to show areas of low or high priority.
             */
            public void mouseDragged(MouseEvent e) {
                if (CARVING) return;
                float x = e.getX(), y = e.getY();
                int imageLabelWidth = imageLabel.getWidth(), imageLabelHeight = imageLabel.getHeight();
                int imageWidth = carver[0].getWidth(), imageHeight = carver[0].getHeight();
                // If horizontal, swap image width and height for calculations below.
                if (HORIZONTAL) { int temp = imageWidth; imageWidth = imageHeight; imageHeight = temp; }
                float labelStepW = (float) imageWidth / imageLabelWidth;
                float labelStepH = (float) imageHeight / imageLabelHeight;
                int cX = (int) (x * labelStepW + 0.5f); // X coordinate on actual image.
                int cY = (int) (y * labelStepH + 0.5f); // Y coordinate on actual image.
                if (HORIZONTAL) { int temp = cX; cX = cY; cY = temp; cY = imageWidth - cY; }
                if (HORIZONTAL) { int temp = imageWidth; imageWidth = imageHeight; imageHeight = temp; }
                int[] image = carver[0].getImage();
                for (int row = cY - BRUSH_WIDTH; row <= cY + BRUSH_WIDTH; row++) {
                    if (row < 0 || row >= imageHeight) continue;
                    for (int col = cX - BRUSH_WIDTH; col <= cX + BRUSH_WIDTH; col++) {
                        if (col < 0 || col >= imageWidth) continue;
                        // If left click, remove edge at given coordinate.
                        // If right click, add edge.
                        boolean isLeftClick = SwingUtilities.isLeftMouseButton(e);
                        carver[0].setEdge(col, row, isLeftClick ? -500 : 500);
                        image[row * imageWidth + col] = isLeftClick ? Color.RED.getRGB() : Color.GREEN.getRGB();
                    }
                }
                imageLabel.setIcon(getScaledImage(carver[0]));
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /*
     * Scales the image being carved.
     *
     * @param carver:   The SeamCarver being used to carve the image.
     * @return          An ImageIcon representing the scaled image.
     */
    public static ImageIcon getScaledImage(SeamCarver carver) {
        int width = carver.getWidth();
        int height = carver.getHeight();
        BufferedImage display = Utils.bufferImage(carver.getImage(), width, height);
        if (HORIZONTAL) {
            display = Utils.rotate90(display);
            int temp = width;
            width = height;
            height = temp;
        }
        Image displayIcon = new ImageIcon(display).getImage();
        displayIcon = displayIcon.getScaledInstance(
                Math.max(width / SCALE, 1),
                Math.max(height / SCALE, 1),
                Image.SCALE_SMOOTH);
        return new ImageIcon(displayIcon);
    }

    /*
     * Captures the current image and saves to a PNG file in the "Snapshots" directory.
     *
     * @param carver    The SeamCarver being used to carve the image.
     * @return          See "Snapshots" directory.
     */
    public static void captureSnapshot(SeamCarver carver) {
        try {
            Utils.writeImage(
                    carver.getImage(), carver.getWidth(), carver.getHeight(),
                    HORIZONTAL,
                    "Snapshots/Snapshot" + COUNT++ + ".png");
        } catch (IOException ioException) {
            System.out.println("Failed to create Snapshot Image.");
            ioException.printStackTrace();
        }
    }

    /*
     * Creates Image Icons for the buttons used in the User Interface.
     *
     * @param filename  The filename/path of the Icon to use.
     * @return          An ImageIcon scaled to "ICON_SIZE" for use on the button.
     */
    public static ImageIcon buttonIcon(String filename) {
        Image icon = new ImageIcon(filename).getImage();
        return new ImageIcon(icon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
    }
}
