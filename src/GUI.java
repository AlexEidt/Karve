/*
 * GUI
 * Alex Eidt
 * Runs the Graphical User Interface (GUI) window that allows the user
 * to interface with the Karver.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class GUI {
    public static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    public static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    // Determines the range (0 - SLIDER) of values for the slider.
    public static volatile int SLIDER = 1000;
    // Color of the seams (if highlighting).
    public static final int SEAM_COLOR = new Color(88, 150, 236).getRGB();
    // File path to the Icons folder.
    public static String ICONS_FOLDER = "Icons";
    // Size of the button icons.
    public static final int ICON_SIZE = 30;
    // Energy Type to use for Seam Carving. Either Forward or Backward.
    public static final EnergyType ENERGY_TYPE = EnergyType.BACKWARD;

    // Determines the width of the "brush" used to mark the priority mask by clicking on the image.
    private int brushWidth;
    // Flag storing whether the carving animation is happening.
    private boolean carving;
    // Flag storing whether the carving is being recorded.
    private boolean recording;
    // Flag storing whether the display image should be updated.
    private boolean update;
    // The direction the carving animation plays. Removing -> False, Adding -> True.
    private boolean direction;
    // Flag that determines whether removed/added seams should be colored.
    private boolean highlight;
    // Flag that determines whether horizontal or vertical seam carving happens.
    private boolean horizontal;
    // Track frame numbers when recording or taking snapshots.
    private int count;
    // Scaling factor for display image.
    private int scale;
    // The display image.
    private final JLabel displayImage;
    // Stores the vertical and horizontal Seam Carvers.
    private final SeamCarver[] carver;
    // The index of the current Seam Carver in "this.carver".
    private int idx;
    // Create Seam Carvers.
    private final SeamCarverFactory factory;

    public GUI() {
        this.carver = new SeamCarver[]{null, null};
        this.factory = new SeamCarverFactory();
        this.update = true;

        JFrame frame = new JFrame("Karve");
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Add the display image showing the image being carved.
        this.displayImage = new JLabel(icon("dragdrop.png", ICON_SIZE / 4), JLabel.CENTER);
        panel.add(this.displayImage);

        // The menuPanel stores all the buttons, checkboxes and the slider.
        JPanel menuPanel = new JPanel();

        // Add File Drag and Drop to the Image Label.
        this.addDropTarget(frame, menuPanel);
        // Add Priority Masking with Mouse Clicking to Image Label.
        this.addMouseListener();

        // Add the "Karve" logo.
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel(icon("logo.png", ICON_SIZE * 3 / 4), JLabel.CENTER);
        // Add "title" to a JPanel to center it.
        titlePanel.add(title);
        menuPanel.add(titlePanel);

        // Add the slider.
        JPanel sliderPanel = new JPanel();
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, SLIDER, SLIDER / 2);
        slider.setFocusable(false);
        ImageIcon[] speeds = new ImageIcon[]{
                icon("speed1.png"),
                icon("speed2.png"),
                icon("speed3.png")
        };
        JLabel speedometer = new JLabel(speeds[1], JLabel.CENTER);
        slider.addChangeListener(e -> speedometer.setIcon(speeds[slider.getValue() / (SLIDER / speeds.length + 1)]));
        sliderPanel.add(speedometer);
        sliderPanel.add(slider);
        menuPanel.add(sliderPanel);

        // Add the checkboxes for "Show Seams", "Horizontal", and "Record".
        // "Show Seams" checkbox.
        Font font = new Font("Arial", Font.PLAIN, 15);
        JPanel checkBoxPanel = new JPanel(new GridLayout(2, 2));
        JCheckBox highlightCheckBox = new JCheckBox("Show Seams");
        highlightCheckBox.setFont(font);
        highlightCheckBox.addItemListener(e -> this.highlight = !this.highlight);
        checkBoxPanel.add(highlightCheckBox);
        // "Horizontal" checkbox.
        JCheckBox horizontalCheckBox = new JCheckBox("Horizontal");
        horizontalCheckBox.setFont(font);
        horizontalCheckBox.addItemListener(e -> {
            this.horizontal = !this.horizontal;
            this.idx = this.horizontal ? 1 : 0;
            if (this.update) this.setDisplayImage();
            frame.setTitle("Karve - " + this.carver[this.idx].getWidth() + " x " + this.carver[this.idx].getHeight());
        });
        checkBoxPanel.add(horizontalCheckBox);
        // "Recording" checkbox.
        JCheckBox recordingCheckBox = new JCheckBox("Recording");
        recordingCheckBox.setFont(font);
        recordingCheckBox.addItemListener(e -> this.recording = !this.recording);
        checkBoxPanel.add(recordingCheckBox);
        // "Update" checkbox.
        JCheckBox updateCheckBox = new JCheckBox("Update");
        updateCheckBox.setFont(font);
        updateCheckBox.setSelected(this.update);
        updateCheckBox.addItemListener(e -> {
            this.update = !this.update;
            this.carver[this.idx].updateImage(this.highlight, SEAM_COLOR);
            if (this.update) this.setDisplayImage();
            this.carver[this.idx].setUpdate(this.update);
        });
        checkBoxPanel.add(updateCheckBox);

        menuPanel.add(checkBoxPanel);

        // Add all "Pause/Play", "Add", "Remove" and "Snapshot" buttons.
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
        ImageIcon play = icon("play.png");
        ImageIcon pause = icon("pause.png");
        JButton playButton = new JButton("Animate Seams");
        playButton.setIcon(play);
        JButton addButton = new JButton("Add Seam");
        addButton.setIcon(icon("add.png"));
        JButton removeButton = new JButton("Remove Seam");
        removeButton.setIcon(icon("remove.png"));
        JButton snapshotButton = new JButton("Snapshot");
        snapshotButton.setIcon(icon("snapshot.png"));

        // Function to run in separate thread when the "Play" button is pressed.
        Runnable animate = () -> {
            // Carve the image all the way until nothing is left.
            // Then begin reconstructing the image by the seams that were removed
            // and repeat until the user stops carving.
            while (this.carving) {
                if (this.direction) {
                    this.carveAdd(frame, slider);
                    this.carveRemove(frame, slider);
                } else {
                    this.carveRemove(frame, slider);
                    this.carveAdd(frame, slider);
                }
            }
        };

        Thread[] thread = {new Thread(animate)};
        // Manage animation thread when "Play/Pause" button is clicked.
        playButton.addActionListener(e -> {
            addButton.setEnabled(this.carving);
            removeButton.setEnabled(this.carving);
            updateCheckBox.setEnabled(this.carving);
            this.carving = !this.carving;
            if (this.carving) {
                playButton.setIcon(pause);
                thread[0].start();
            } else {
                playButton.setIcon(play);
                thread[0] = new Thread(animate);
            }
        });
        // Add seam back when "Add" button is clicked.
        addButton.addActionListener(e -> {
            this.direction = true;
            boolean valid = this.carver[this.idx].add(this.highlight, SEAM_COLOR);
            if (valid) {
                if (this.recording) captureSnapshot(this.carver[this.idx]);
                if (this.update) this.setDisplayImage();
                frame.setTitle("Karve - " + this.carver[this.idx].getWidth() + " x " + this.carver[this.idx].getHeight());
            }
        });
        // Remove seam when "Remove" button is clicked.
        removeButton.addActionListener(e -> {
            this.direction = false;
            boolean valid = this.carver[this.idx].remove(this.highlight, SEAM_COLOR);
            if (valid) {
                if (this.recording) captureSnapshot(this.carver[this.idx]);
                if (this.update) this.setDisplayImage();
                frame.setTitle("Karve - " + this.carver[this.idx].getWidth() + " x " + this.carver[this.idx].getHeight());
            }
        });
        // Create a snapshot of the current image when the "Snapshot" button is clicked.
        snapshotButton.addActionListener(e -> {
            if (!this.recording) captureSnapshot(this.carver[this.idx]);
        });
        buttonPanel.add(playButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(snapshotButton);

        menuPanel.add(buttonPanel);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        this.setEnabled(menuPanel, false);

        panel.add(menuPanel);

        frame.add(panel);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /*
     * Add Seams back to the image when using the animate feature.
     *
     * @param frame     The UI Window. The title is updated to reflect the current size of the image.
     * @param slider    The slider used to determine animation speed.
     */
    private void carveAdd(JFrame frame, JSlider slider) {
        while (this.carving && carver[this.idx].add(this.highlight, SEAM_COLOR)) {
            if (this.recording) captureSnapshot(this.carver[this.idx]);
            if (this.update) this.setDisplayImage();
            frame.setTitle("Karve - " + this.carver[this.idx].getWidth() + " x " + this.carver[this.idx].getHeight());
            Utils.delay(SLIDER - slider.getValue());
        }
    }

    /*
     * Remove Seams from the image when using the animate feature.
     *
     * @param frame     The UI Window. The title is updated to reflect the current size of the image.
     * @param slider    The slider used to determine animation speed.
     */
    private void carveRemove(JFrame frame, JSlider slider) {
        while (this.carving && carver[this.idx].remove(this.highlight, SEAM_COLOR)) {
            if (this.recording) captureSnapshot(this.carver[this.idx]);
            if (this.update) this.setDisplayImage();
            frame.setTitle("Karve - " + this.carver[this.idx].getWidth() + " x " + this.carver[this.idx].getHeight());
            Utils.delay(SLIDER - slider.getValue());
        }
    }

    /*
     * Sets/Updates the display image to the current state of the selected
     * Seam Carver.
     */
    private void setDisplayImage() {
        SeamCarver carver = this.carver[this.idx];
        ImageIcon icon = getScaledImage(carver);
        this.displayImage.setIcon(icon);
        this.displayImage.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    }

    /*
     * Enables/Disables all components in a JPanel.
     *
     * @param panel     The panel to enable/disable.
     * @param enable    If true, enable all elements, otherwise disable.
     */
    private void setEnabled(JPanel panel, boolean enable) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                setEnabled((JPanel) comp, enable);
            } else {
                comp.setEnabled(enable);
            }
        }
    }

    /*
     * Adds a drop target to the display image. This allows the user to drag and drop
     * files into the panel and have them be processed.
     * Code from:
     * https://stackoverflow.com/questions/811248/how-can-i-use-drag-and-drop-in-swing-to-get-file-path
     *
     * @param frame     The current window frame. Used to update the title.
     * @param menuPanel The menuPanel to enable once the user drops in an image.
     */
    private void addDropTarget(JFrame frame, JPanel menuPanel) {
        this.displayImage.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                if (carving) return;
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List) evt
                                    .getTransferable()
                                    .getTransferData(DataFlavor.javaFileListFlavor);
                    File image = droppedFiles.get(0);

                    // Vertical Seam Carver
                    carver[0] = factory.create(image, ENERGY_TYPE);
                    // Horizontal Seam Carver
                    carver[1] = factory.create(
                            Utils.transpose(Utils.mirror(Utils.readImage(image))),
                            ENERGY_TYPE
                    );

                    brushWidth = Utils.min(carver[0].getWidth(), carver[0].getHeight()) / 120;
                    if (brushWidth == 0) {
                        brushWidth = 5;
                    }

                    idx = horizontal ? 1 : 0;
                    scale = Utils.getDimensions(carver[idx].getWidth(), carver[idx].getHeight());

                    frame.setTitle("Karve - " + carver[idx].getWidth() + " x " + carver[idx].getHeight());

                    ImageIcon imageIcon = getScaledImage(carver[idx]);
                    if (horizontal) {
                        displayImage.setVerticalAlignment(JLabel.CENTER);
                    } else {
                        displayImage.setHorizontalAlignment(JLabel.RIGHT);
                    }
                    displayImage.setPreferredSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
                    displayImage.setIcon(imageIcon);

                    setEnabled(menuPanel, true);
                    frame.pack();

                    evt.dropComplete(true);
                } catch (Exception e) {
                    evt.dropComplete(false);
                }
            }
        });
    }

    /*
     * Adds a mouse listener to the display image which allows the user to
     * mark high and low priority areas on the image for carving.
     */
    private void addMouseListener() {
        // Change pixels values of sobel image to change where seams appear.
        // Change pixels by clicking on the image.
        this.displayImage.addMouseMotionListener(new MouseAdapter() {
            /*
             * Activates whenever the user clicks and drags their mouse over any part
             * of the display image. The coordinates the user click on are converted into
             * pixel coordinates corresponding to the actual image. Depending on
             * a left or right-click the corresponding pixels are colored red or green
             * to show areas of low or high priority.
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (carving || !update || carver[0] == null || ENERGY_TYPE == EnergyType.FORWARD) return;
                float x = e.getX(), y = e.getY();
                int imageWidth = carver[idx].getWidth(), imageHeight = carver[idx].getHeight();
                // If horizontal, swap image width and height for calculations below.
                if (horizontal) { int temp = imageWidth; imageWidth = imageHeight; imageHeight = temp; }
                float labelStepW = (float) imageWidth / displayImage.getWidth();
                float labelStepH = (float) imageHeight / displayImage.getHeight();
                int cX = (int) (x * labelStepW + 0.5f); // X coordinate on actual image.
                int cY = (int) (y * labelStepH + 0.5f); // Y coordinate on actual image.
                if (horizontal) { int temp = cX; cX = cY; cY = temp; cY = imageWidth - cY; }
                if (horizontal) { int temp = imageWidth; imageWidth = imageHeight; imageHeight = temp; }
                int[] image = carver[idx].getImage();
                boolean isLeftClick = SwingUtilities.isLeftMouseButton(e);
                for (int row = cY - brushWidth; row <= cY + brushWidth; row++) {
                    if (row < 0 || row >= imageHeight) continue;
                    for (int col = cX - brushWidth; col <= cX + brushWidth; col++) {
                        if (col < 0 || col >= imageWidth) continue;
                        // If left click, remove edge at given coordinate.
                        // If right click, add edge.
                        carver[idx].setEnergy(col, row, isLeftClick ? 0 : 255);
                        image[row * imageWidth + col] = isLeftClick ? Color.RED.getRGB() : Color.GREEN.getRGB();
                    }
                }
                setDisplayImage();
            }
        });
    }

    /*
     * Scales the image being carved.
     *
     * @param carver    The SeamCarver being used to carve the image.
     * @return          An ImageIcon representing the scaled image.
     */
    private ImageIcon getScaledImage(SeamCarver carver) {
        int width = carver.getWidth();
        int height = carver.getHeight();
        BufferedImage display = Utils.bufferImage(carver.getImage(), width, height);
        if (this.horizontal) {
            // Rotate display image and swap width and height.
            display = Utils.rotate90(display);
            int temp = width;
            width = height;
            height = temp;
        }
        Image displayIcon = new ImageIcon(display).getImage();
        displayIcon = displayIcon.getScaledInstance(
                Utils.max(width / this.scale, 1),
                Utils.max(height / this.scale, 1),
                Image.SCALE_FAST
        );
        return new ImageIcon(displayIcon);
    }

    /*
     * Captures the current image and saves to a PNG file in the "Snapshots" directory.
     *
     * @param carver    The SeamCarver being used to carve the image.
     * @return          See "Snapshots" directory.
     */
    private void captureSnapshot(SeamCarver carver) {
        try {
            Utils.writeImage(
                    carver.getImage(),
                    carver.getWidth(),
                    carver.getHeight(),
                    this.horizontal,
                    Utils.joinPath(Main.SNAPSHOTS_DIR, "Snapshot" + this.count++ + ".png")
            );
        } catch (IOException ioException) {
            System.out.println("Failed to create Snapshot Image.");
            ioException.printStackTrace();
        }
    }

    /*
     * Creates Image Icons for components used in the User Interface.
     *
     * @param filename  The filename/path of the Icon to use.
     * @param dims      The scaling factors to use to resize the Icon.
     * @return          An ImageIcon scaled to the given dimensions.
     */
    private ImageIcon icon(String filename, int... dims) {
        URL url = getClass().getResource(ICONS_FOLDER + "/" + filename);
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(url));
        int width, height;
        if (dims.length == 0) {
            width = height = ICON_SIZE;
        } else {
            width = icon.getIconWidth() / dims[0];
            height = icon.getIconHeight() / dims[0];
        }
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_FAST));
    }
}
