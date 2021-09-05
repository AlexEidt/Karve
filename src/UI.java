/*
 * UI
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
import java.util.concurrent.TimeUnit;

public class UI {
    // CARVING is a flag storing whether or not the carving animation is happening.
    public static volatile boolean CARVING = false;
    // RECORDING is a flag storing whether the carving is being recorded.
    public static volatile boolean RECORDING = false;
    // The direction the carving animation plays. Removing -> False, Adding -> True.
    public static volatile boolean DIRECTION = false;
    // Track frame numbers when recording.
    public static volatile int COUNT = 0;
    // Scaling factor for display image.
    public static volatile int SCALE = 1;
    // Size of the button icons.
    public static final int ICON_SIZE = 30;

    // -------------------------------------------------------------------
    // USER SPECIFIED
    // -------------------------------------------------------------------
    // HIGHLIGHT determines whether removed/added seams should be colored.
    public static volatile boolean HIGHLIGHT = false;
    // HORIZONTAL determines whether horizontal or vertical seam carving happens.
    public static volatile boolean HORIZONTAL = false;
    // The image file name to seam carve.
    public static String FILENAME = "Documentation/starwars.png";
    // Color of the seams (if HIGHLIGHT is true).
    public static final Color SEAM_COLOR = new Color(88, 150, 236);

    public static void main(String[] args) {
        File snapshotsDirectory = new File("Snapshots/");
        snapshotsDirectory.mkdir();

        // Create Seam Carver for vertical Seam Carving.
        SeamCarver verticalCarver = new SeamCarver(Utils.readImage(FILENAME));
        // Create Seam Carver for horizontal Seam Carving by mirror and then
        // transposing the image.
        SeamCarver horizontalCarver = new SeamCarver(
                Utils.transpose(Utils.mirror(Utils.readImage(FILENAME))));
        SeamCarver[] carver = {verticalCarver};

        SCALE = getDimensions(carver[0].getWidth(), carver[0].getHeight());

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
        ImageIcon play = buttonIcon("Icons/play.png");
        ImageIcon pause = buttonIcon("Icons/pause.png");
        JButton playButton = new JButton(play);
        JButton addButton = new JButton(buttonIcon("Icons/add.png"));
        JButton removeButton = new JButton(buttonIcon("Icons/remove.png"));
        JButton snapshotButton = new JButton(buttonIcon("Icons/snapshot.png"));

        // Function to run in separate thread when the "Play" button is pressed.
        Runnable animate = () -> {
            // Carve the image all the way until nothing is left.
            // Then begin reconstructing the image by the seams that were removed
            // and repeat until the user stops carving.
            while (CARVING) {
                if (DIRECTION) {
                    while (carver[0].add(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        delay(slider.getValue());
                    }
                    while (carver[0].remove(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        delay(slider.getValue());
                    }
                } else {
                    while (carver[0].remove(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        delay(slider.getValue());
                    }
                    while (carver[0].add(HIGHLIGHT, highlightColor)) {
                        if (RECORDING) captureSnapshot(carver[0]);
                        imageLabel.setIcon(getScaledImage(carver[0]));
                        delay(slider.getValue());
                    }
                }

            }
        };

        Thread[] thread = {new Thread(animate)};
        // Manage animation thread when "Play/Pause" button is clicked.
        playButton.addActionListener(e -> {
            if (CARVING) {
                playButton.setIcon(play);
                if (thread[0].isAlive()) thread[0].stop();
                thread[0] = new Thread(animate);
            } else {
                playButton.setIcon(pause);
                thread[0].start();
            }
            CARVING = !CARVING;
            addButton.setEnabled(!CARVING);
            removeButton.setEnabled(!CARVING);
        });
        // Add seam back when "Add" button is clicked.
        addButton.addActionListener(e -> {
            DIRECTION = true;
            CARVING = false;
            carver[0].add(HIGHLIGHT, highlightColor);
            imageLabel.setIcon(getScaledImage(carver[0]));
        });
        // Remove seam when "Remove" button is clicked.
        removeButton.addActionListener(e -> {
            DIRECTION = false;
            CARVING = false;
            carver[0].remove(HIGHLIGHT, highlightColor);
            imageLabel.setIcon(getScaledImage(carver[0]));
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
            private final int imageLabelWidth = imageLabel.getWidth();
            private final int imageLabelHeight = imageLabel.getHeight();
            private final int imageWidth = carver[0].getWidth();
            private final int imageHeight = carver[0].getHeight();
            private float labelStepW = (float) imageWidth / imageLabelWidth;
            private float labelStepH = (float) imageHeight / imageLabelHeight;
            @Override
            public void mouseDragged(MouseEvent e) {
                float x = e.getX();
                float y = e.getY();
                int cX = (int) (x * labelStepW + 0.5f);
                int cY = (int) (y * labelStepH + 0.5f);
                if (HORIZONTAL) { int temp = cX; cX = cY; cY = temp; }
                for (int i = cY - 1; i <= cY + 1; i++) {
                    if (i < 0 || i >= imageHeight) continue;
                    for (int j = cX - 1; j <= cX + 1; j++) {
                        if (j < 0 || j >= imageWidth) continue;
                        carver[0].setEdge(j, i);
                    }
                }
            }
        });

        frame.setVisible(true);
    }

    /*
     * Finds the optimal scaling factor such that the display image will be
     * approximately half the screen width and height.
     *
     * @param w         Width of Image.
     * @param h         Height of Image.
     * @return          Scaling factor.
     */
    public static int getDimensions(int w, int h) {
        float width = (float) Toolkit.getDefaultToolkit().getScreenSize().width / 2f;
        float height = (float) Toolkit.getDefaultToolkit().getScreenSize().height / 2f;
        int scale = 1;
        float max = 1000000f;
        for (int i = 2; i < 21; i++) {
            if (w % i == 0 && h % i == 0) {
                float tempH = Math.abs(height - ((float) h / i));
                float tempW = Math.abs(width - ((float) w / i));
                if (tempH + tempW < max) {
                    max = tempH + tempW;
                    scale = i;
                }
            }
        }
        return scale;
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
     * Delays the main thread for "delay" milliseconds.
     *
     * @param delay:    Delay in milliseconds.
     */
    public static void delay(int delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
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
        BufferedImage display = Utils.bufferImage(carver.getImage(), carver.getWidth(), carver.getHeight());
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
