import java.io.*;
import java.util.*;

public class Steptwo {

    /**
     * Builds the Bad Character Table (BCT) for the given pattern
     * using the Boyer–Moore Bad Character Heuristic formula:
     * shift = max(1, length - index - 1)
     * Also adds '*' entry representing all other characters.
     */
    public static Map<Character, Integer> buildBadCharTable(String pattern) {
        Map<Character, Integer> table = new LinkedHashMap<>();
        int m = pattern.length();

        // For each character in the pattern, compute its shift value
        for (int i = 0; i < m; i++) {
            char c = pattern.charAt(i);
            int shift = Math.max(1, m - i - 1);
            table.put(c, shift); // latest occurrence overwrites earlier ones
        }

        // Add '*' entry for all other characters (not in pattern)
        table.put('*', m);

        return table;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // --- Step 1: Get input/output filenames from the user (no hardcoding) ---
        System.out.print("Enter input filename (patterns file): ");
        String inputFile = sc.nextLine();

        System.out.print("Enter output filename (BCT output file): ");
        String outputFile = sc.nextLine();

        List<String> patterns = new ArrayList<>();

        // --- Step 2: Read patterns from input file ---
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    patterns.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading input file: " + e.getMessage());
            return;
        }

        // --- Step 3: Generate and write formatted BCT output ---
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // Header (Table format)
            writer.write(String.format("%-10s %-20s %-50s%n",
                    "Index, j", "jth Pattern", "BCT for jth pattern"));
            writer.write("=".repeat(85));
            writer.newLine();

            // Process each pattern
            for (int j = 0; j < patterns.size(); j++) {
                String pattern = patterns.get(j);
                Map<Character, Integer> bct = buildBadCharTable(pattern);

                // Build formatted BCT string in order of pattern appearance
                StringBuilder tableData = new StringBuilder();
                Set<Character> seen = new HashSet<>();

                for (char c : pattern.toCharArray()) {
                    if (!seen.contains(c)) {
                        tableData.append(c).append(":").append(bct.get(c)).append(" ");
                        seen.add(c);
                    }
                }

                // Add '*' entry at the end (for all other characters)
                tableData.append("*:").append(bct.get('*'));

                // Write formatted row
                writer.write(String.format("%-10d %-20s %-50s%n",
                        j + 1, pattern, tableData.toString().trim()));
            }

            System.out.println("\n File '" + outputFile + "' created successfully.");
            System.out.println("   BCT entries generated for " + patterns.size() + " patterns.\n");

        } catch (IOException e) {
            System.out.println(" Error writing output file: " + e.getMessage());
        }
    }
}

