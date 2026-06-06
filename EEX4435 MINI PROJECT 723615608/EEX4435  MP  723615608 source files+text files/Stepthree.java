import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Stepthree - No Hardcoding Version
 *
 * Reads all file names from user input, performs Boyer–Moore pattern matching,
 * and writes a neatly formatted output table.
 */
public class Stepthree {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // --- No hardcoding: user provides filenames ---
        System.out.print("Enter the paragraph output filename (e.g., oParag7.txt): ");
        String oParag = sc.nextLine();

        System.out.print("Enter the pattern table filename (e.g., oBCT7.txt): ");
        String oBCT = sc.nextLine();

        System.out.print("Enter output filename to save pattern match results (e.g., PattMatch7.txt): ");
        String out = sc.nextLine();

        try {
            List<ParagEntry> words = readOParag(oParag);
            List<PatternEntry> patterns = readOBCT(oBCT);

            List<PatternMatchResult> results = new ArrayList<>();
            for (PatternEntry pe : patterns) {
                PatternMatchResult pmr = new PatternMatchResult(pe.index, pe.pattern);
                for (ParagEntry we : words) {
                    List<Integer> matches = boyerMooreSearch(we.word, pe.pattern);
                    if (!matches.isEmpty()) {
                        List<String> ranges = new ArrayList<>();
                        for (int s : matches) {
                            int start1 = s + 1; // 1-based index
                            int end1 = start1 + pe.pattern.length() - 1;
                            ranges.add(start1 + " - " + end1);
                        }
                        pmr.addMatch(we.word, ranges);
                    }
                }
                results.add(pmr);
            }

            writeOutput(out, results);
            System.out.println("\n Finished successfully. Output written to: " + out);

        } catch (IOException e) {
            System.err.println(" I/O Error: " + e.getMessage());
        }
    }

    // -------------------- File Readers --------------------

    private static List<ParagEntry> readOParag(String filename) throws IOException {
        List<ParagEntry> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            int idx;
            try { idx = Integer.parseInt(parts[0]); } catch (NumberFormatException ex) { continue; }

            String word;
            if (parts.length >= 3 && parts[parts.length - 1].matches("\\d+")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < parts.length - 1; i++) {
                    if (i > 1) sb.append(' ');
                    sb.append(parts[i]);
                }
                word = sb.toString();
            } else {
                word = parts[1];
            }

            if (!word.isEmpty()) out.add(new ParagEntry(idx, word));
        }
        return out;
    }

    private static List<PatternEntry> readOBCT(String filename) throws IOException {
        List<PatternEntry> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            int idx;
            try { idx = Integer.parseInt(parts[0]); } catch (NumberFormatException ex) { continue; }
            if (parts.length < 2) continue;
            String pattern = parts[1];
            if (!pattern.isEmpty()) out.add(new PatternEntry(idx, pattern));
        }
        return out;
    }

    // -------------------- Output Writer --------------------

    private static void writeOutput(String filename, List<PatternMatchResult> results) throws IOException {
        // Determine column widths dynamically
        int maxPatternLen = results.stream().mapToInt(p -> p.pattern.length()).max().orElse(10);
        int maxWordLen = results.stream()
                .flatMap(p -> p.matches.stream())
                .mapToInt(m -> m.word.length())
                .max().orElse(10);

        String headerFmt = "%-8s %-" + (maxPatternLen + 2) + "s %s%n";
        String matchFmt  = "%-" + (8 + maxPatternLen + 2) + "s %-" + (maxWordLen + 2) + "s %s%n";

        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filename))) {
            // Header
            bw.write(String.format(headerFmt, "Index", "Pattern", "Matching words and its location range(s)"));
            int separatorLen = 8 + maxPatternLen + 2 + maxWordLen + 30;
            bw.write("-".repeat(separatorLen));
            bw.newLine();

            // Pattern and matches
            for (PatternMatchResult pmr : results) {
                bw.write(String.format("%-8d %-" + (maxPatternLen + 2) + "s%n", pmr.index, pmr.pattern));

                if (pmr.matches.isEmpty()) {
                    bw.write(String.format(matchFmt, "", "-- No matching found --", ""));
                } else {
                    for (PatternMatchResult.MatchInfo mi : pmr.matches) {
                        bw.write(String.format(matchFmt, "", mi.word, String.join(" and ", mi.ranges)));
                    }
                }
                bw.newLine();
            }
        }
    }

    // -------------------- Data Classes --------------------

    private static class ParagEntry {
        int index;
        String word;
        ParagEntry(int i, String w) { index = i; word = w; }
    }

    private static class PatternEntry {
        int index;
        String pattern;
        PatternEntry(int i, String p) { index = i; pattern = p; }
    }

    private static class PatternMatchResult {
        int index;
        String pattern;
        List<MatchInfo> matches = new ArrayList<>();
        PatternMatchResult(int i, String p) { index = i; pattern = p; }
        void addMatch(String word, List<String> ranges) {
            matches.add(new MatchInfo(word, ranges));
        }
        static class MatchInfo {
            String word;
            List<String> ranges;
            MatchInfo(String w, List<String> r) { word = w; ranges = r; }
        }
    }

    // -------------------- Boyer–Moore Algorithm --------------------

    private static List<Integer> boyerMooreSearch(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text.isEmpty() || pattern.isEmpty() || pattern.length() > text.length()) return matches;

        int n = text.length();
        int m = pattern.length();
        int[] bad = preprocessBadCharacter(pattern);
        int[] good = preprocessGoodSuffix(pattern);

        int s = 0;
        while (s <= n - m) {
            int j = m - 1;
            while (j >= 0 && pattern.charAt(j) == text.charAt(s + j)) j--;
            if (j < 0) {
                matches.add(s);
                s += (m > 1) ? good[0] : 1;
            } else {
                int bcShift = j - bad[text.charAt(s + j)];
                int gsShift = good[j];
                s += Math.max(1, Math.max(bcShift, gsShift));
            }
        }
        return matches;
    }

    private static int[] preprocessBadCharacter(String pattern) {
        int[] bad = new int[256];
        Arrays.fill(bad, -1);
        for (int i = 0; i < pattern.length(); i++) {
            bad[pattern.charAt(i)] = i;
        }
        return bad;
    }

    private static int[] preprocessGoodSuffix(String pattern) {
        int m = pattern.length();
        int[] suff = new int[m];
        int[] good = new int[m];
        Arrays.fill(good, m);
        suff[m - 1] = m;

        int g = m - 1, f = 0;
        for (int i = m - 2; i >= 0; --i) {
            if (i > g && suff[i + m - 1 - f] < i - g)
                suff[i] = suff[i + m - 1 - f];
            else {
                if (i < g) g = i;
                f = i;
                while (g >= 0 && pattern.charAt(g) == pattern.charAt(g + m - 1 - f))
                    g--;
                suff[i] = f - g;
            }
        }

        for (int i = 0; i < m - 1; i++)
            good[m - 1 - suff[i]] = m - 1 - i;

        int j = 0;
        for (int i = m - 1; i >= -1; i--) {
            if (i == -1 || suff[i] == i + 1) {
                for (; j < m - 1 - i; j++) {
                    if (good[j] == m) good[j] = m - 1 - i;
                }
            }
        }
        return good;
    }
}
