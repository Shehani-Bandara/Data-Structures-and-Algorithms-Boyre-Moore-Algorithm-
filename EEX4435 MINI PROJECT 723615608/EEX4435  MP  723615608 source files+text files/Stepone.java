import java.io.*;
import java.util.*;

public class Stepone {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // --- Step 1: Get input and output filenames from user (no hardcoding) ---
        System.out.print("Enter input paragraph filename: ");
        String inputFile = sc.nextLine();

        System.out.print("Enter output filename to save results: ");
        String outputFile = sc.nextLine();

        // List to hold all extracted words
        List<String> words = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            int ch;
            StringBuilder currentWord = new StringBuilder();

            // --- Step 2: Read character by character ---
            while ((ch = br.read()) != -1) {
                char c = (char) ch;

                // If it's whitespace, treat as word boundary
                if (Character.isWhitespace(c)) {
                    if (currentWord.length() > 0) {
                        words.add(currentWord.toString());
                        currentWord.setLength(0);
                    }
                } else {
                    // Keep all characters (letters, digits, punctuation, symbols)
                    currentWord.append(c);
                }
            }

            // Add last word if file doesn't end with space
            if (currentWord.length() > 0) {
                words.add(currentWord.toString());
            }

            // --- Step 3: Write results to output file ---
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                writer.write(String.format("%-10s %-30s %-20s%n", "Index (i)", "ith Word", "Length of ith Word"));
                writer.write("-------------------------------------------------------------------\n");

                for (int i = 0; i < words.size(); i++) {
                    String word = words.get(i);
                    writer.write(String.format("%-10d %-30s %-20d%n", i + 1, word, word.length()));
                }
            }

            System.out.println("\n File '" + outputFile + "' created successfully with " + words.size() + " words.\n");

        } catch (IOException e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
}
