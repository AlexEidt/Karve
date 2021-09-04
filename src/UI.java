import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UI {
    public static volatile boolean CARVING = false;
    public static volatile boolean HIGHLIGHT = false;
    public static volatile boolean HORIZONTAL = false;
    public static String FILENAME = "ibex.png";
    public static final int ICON_SIZE = 30;
    public static final Color SEAM_COLOR = new Color(88, 150, 236);

    public static void main(String[] args) {
        File snapshotsDirectory = new File("Snapshots/");
        snapshotsDirectory.mkdir();
        int[][] image = Utils.readImage(FILENAME);
        SeamCarver verticalCarver = new SeamCarver(image);
//        SeamCarver horizontalCarver = new SeamCarver(Utils.transpose(image));
        SeamCarver[] carver = {verticalCarver};

        int color = SEAM_COLOR.getRGB();
        int highlightColor = ((color << 16) & 0xFF) | ((color << 8) & 0xFF) | (color & 0xFF);

        JFrame frame = new JFrame("Karver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel imageLabel = new JLabel(getScaledImage(carver[0]), JLabel.CENTER);
        JPanel imagePanel = new JPanel();
        imagePanel.add(imageLabel);
        panel.add(imageLabel);

        JPanel menuPanel = new JPanel();
        Font font = new Font("Arial", Font.PLAIN, 15);

        JPanel sliderPanel = new JPanel(new GridLayout(2, 1));
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
        slider.setFocusable(false);
        JLabel speed = new JLabel("Speed", JLabel.CENTER);
        speed.setBorder(new EmptyBorder(10, 10, 10, 10));
        speed.setFont(font);
        sliderPanel.add(speed);
        sliderPanel.add(slider);
        menuPanel.add(sliderPanel);

        JPanel checkBoxPanel = new JPanel(new GridLayout(1, 1));
        JCheckBox highlight = new JCheckBox("Show Seams");
        highlight.setFont(font);
        highlight.addItemListener(e -> HIGHLIGHT = !HIGHLIGHT);
        checkBoxPanel.add(highlight);
//        JCheckBox horizontal = new JCheckBox("Horizontal");
//        horizontal.setFont(font);
//        horizontal.addItemListener(e -> {
//            HORIZONTAL = !HORIZONTAL;
//            carver[0] = HORIZONTAL ? horizontalCarver : verticalCarver;
//        });
//        checkBoxPanel.add(horizontal);
        menuPanel.add(checkBoxPanel);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
        ImageIcon play = buttonIcon("Icons/play.png");
        ImageIcon pause = buttonIcon("Icons/pause.png");
        JButton playButton = new JButton(play);
        JButton addButton = new JButton(buttonIcon("Icons/add.png"));
        JButton removeButton = new JButton(buttonIcon("Icons/remove.png"));
        JButton snapshotButton = new JButton(buttonIcon("Icons/snapshot.png"));

        Runnable animate = () -> {
            while (CARVING) {
                while (carver[0].remove(HIGHLIGHT, highlightColor)) {
                    imageLabel.setIcon(getScaledImage(carver[0]));
                    delay(slider.getValue());
                }
                while (carver[0].add(HIGHLIGHT, highlightColor)) {
                    imageLabel.setIcon(getScaledImage(carver[0]));
                    delay(slider.getValue());
                }
            }
        };

        Thread[] thread = {new Thread(animate)};

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
        });

        addButton.addActionListener(e -> {
            CARVING = false;
            carver[0].add(HIGHLIGHT, highlightColor);
            imageLabel.setIcon(getScaledImage(carver[0]));
        });
        removeButton.addActionListener(e -> {
            CARVING = false;
            carver[0].remove(HIGHLIGHT, highlightColor);
            imageLabel.setIcon(getScaledImage(carver[0]));
        });
        snapshotButton.addActionListener(e -> {
            try {
                Utils.writeImage(
                        carver[0].getImage(),
                        carver[0].getWidth(),
                        carver[0].getHeight(),
                        "Snapshots/Snapshot",
                        "png"
                );
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        buttonPanel.add(playButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(snapshotButton);

        menuPanel.add(buttonPanel);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        panel.add(menuPanel);

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void delay(long delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    public static ImageIcon getScaledImage(SeamCarver carver) {
        int width = carver.getWidth();
        int height = carver.getHeight();
        BufferedImage display = Utils.bufferImage(carver.getImage(), width, height);
        ImageIcon displayIcon = new ImageIcon(display);
        Image scaledImage = displayIcon.getImage()
                .getScaledInstance(width / 2, height / 2, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    public static ImageIcon buttonIcon(String filename) {
        Image icon = new ImageIcon(filename).getImage();
        return new ImageIcon(icon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT));
    }
}
