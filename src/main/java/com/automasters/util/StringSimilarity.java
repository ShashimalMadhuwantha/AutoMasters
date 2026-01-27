package com.automasters.util;

public class StringSimilarity {

    /**
     * Calculate Levenshtein distance between two strings
     * Returns the minimum number of single-character edits needed
     */
    public static int levenshteinDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Calculate similarity percentage (0-100)
     */
    public static double similarityPercentage(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0)
            return 100.0;
        return (1.0 - (double) distance / maxLength) * 100.0;
    }

    /**
     * Normalize string for comparison
     * Removes spaces, special characters, converts to lowercase
     */
    public static String normalize(String str) {
        if (str == null)
            return "";
        // Remove spaces and special characters, keep only alphanumeric
        return str.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    /**
     * Check if two strings are similar (>= 80% similarity)
     */
    public static boolean areSimilar(String s1, String s2) {
        return areSimilar(s1, s2, 80.0);
    }

    /**
     * Check if two strings are similar with custom threshold
     */
    public static boolean areSimilar(String s1, String s2, double threshold) {
        String norm1 = normalize(s1);
        String norm2 = normalize(s2);

        // Exact match after normalization
        if (norm1.equals(norm2)) {
            return true;
        }

        // Check similarity percentage
        return similarityPercentage(norm1, norm2) >= threshold;
    }
}
