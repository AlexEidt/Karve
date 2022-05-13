/*
 * Console
 * Alex Eidt
 * Runs the Seam Carver from the console. For those interested in carving x number
 * of seams from an image without the user interface.
 */

import java.io.File;
import java.util.Locale;
import java.util.Scanner;

public class Console {
    public static void main(String[] args) {
        System.out.println("Welcome to Karve!\n");
        Scanner console = new Scanner(System.in);
        System.out.println("Enter Image File Name: ");
        String filename = console.next();
        File file = new File(filename);
        while (!file.exists() || !file.isFile()) {
            System.out.println(filename + " file was not found. Enter File Name again: ");
            filename = console.next();
            file = new File(filename);
        }
        boolean horizontal = getUserData(console, "Horizontal (h) or Vertical (v) Seams?: ", "vh") == 'h';
        boolean showSeams = getUserData(console, "Show last highlighted seam? (y/n): ", "ny") == 'y';
        boolean energyType = getUserData(console, "Use Forward (f) or Backward (b) Energy?: ", "fb") == 'f';
        EnergyType type = energyType ? EnergyType.FORWARD : EnergyType.BACKWARD;

        int highlightColor = 0;
        if (showSeams) {
            System.out.println("Enter highlight color as R,G,B: ");
            String rgb = console.next();
            String[] colors = rgb.split(",");
            int r = Integer.parseInt(colors[0]) & 0xFF;
            int g = Integer.parseInt(colors[1]) & 0xFF;
            int b = Integer.parseInt(colors[2]) & 0xFF;
            highlightColor = (r << 16) | (g << 8) | b;
        }
        System.out.println("Number of Seams to remove?: ");
        int seams = console.nextInt();

        SeamCarverFactory factory = new SeamCarverFactory();
        SeamCarver carver = factory.create(filename, horizontal, type);

        System.out.println("Carving...");
        int numCarved = carver.remove(seams, showSeams, highlightColor);
        System.out.println(numCarved + " seams carved from " + filename + ".");
        System.out.println("Output file name: ");
        String output = console.next();
        Utils.writeImage(carver.getImage(), carver.getWidth(), carver.getHeight(), horizontal, output);
        System.out.println("Carved image saved as " + output + ".");

        console.close();
    }

    /*
     * Prompt the user for a response and continue prompting until the user
     * enters a valid choice.
     *
     * @param console   The console scanner.
     * @param prompt    The prompt to print to the user.
     * @param valid     Valid responses to the prompt.
     * @return          The character the user responded.
     */
    public static char getUserData(Scanner console, String prompt, String valid) {
        System.out.println(prompt);
        String data = console.next().toLowerCase(Locale.ROOT);
        while (!valid.contains(data)) {
            System.out.println("Invalid entry. " + prompt);
            data = console.next().toLowerCase(Locale.ROOT);
        }
        return data.charAt(0);
    }
}
