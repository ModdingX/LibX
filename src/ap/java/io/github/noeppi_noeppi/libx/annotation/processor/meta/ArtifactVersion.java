package io.github.noeppi_noeppi.libx.annotation.processor.meta;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public record ArtifactVersion(List<List<String>> parts) implements Comparable<ArtifactVersion> {

    public static ArtifactVersion INVALID = new ArtifactVersion(List.of());

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ArtifactVersion v)) return false;
        if (this.parts.isEmpty() && v.parts.isEmpty()) return true;
        if (this.parts.isEmpty() || v.parts.isEmpty()) return false;
        if (this.parts.size() != v.parts.size()) return false;
        for (int i = 0; i < this.parts.size(); i++) {
            if (!equalParts(this.parts.get(i), v.parts.get(i))) return false;
        }
        return true;
    }

    @Override
    public int compareTo(@NotNull ArtifactVersion v) {
        if (this.parts.isEmpty() && v.parts.isEmpty()) return 0;
        if (this.parts.isEmpty()) return -1;
        if (v.parts.isEmpty()) return 1;
        if (this.parts.size() < v.parts.size()) return -1;
        if (this.parts.size() > v.parts.size()) return 1;
        for (int i = 0; i < this.parts.size(); i++) {
            int result = compareParts(this.parts.get(i), v.parts.get(i));
            if (result != 0) return result;
        }
        return 0;
    }

    private static boolean equalParts(List<String> s1, List<String> s2) {
        List<String> normalized1 = normalize(s1);
        List<String> normalized2 = normalize(s2);
        if (normalized1.size() != normalized2.size()) return false;
        for (int i = 0; i < normalized1.size(); i++) {
            if (!normalized1.get(i).equals(normalized2.get(i))) return false;
        }
        return true;
    }
    
    private static int compareParts(List<String> s1, List<String> s2) {
        List<String> normalized1 = normalize(s1);
        List<String> normalized2 = normalize(s2);
        int minIdx = Math.min(normalized1.size(), normalized2.size());
        for (int i = 0; i < minIdx; i++) {
            int result = compareStrings(normalized1.get(i), normalized2.get(i));
            if (result != 0) return result;
        }
        return Integer.compare(normalized1.size(), normalized2.size());
    }
    
    private static int compareStrings(String s1, String s2) {
        try {
            long l1 = Long.parseLong(s1);
            long l2 = Long.parseLong(s2);
            return Long.compare(l1, l2);
        } catch (NumberFormatException e) {
            return s1.compareTo(s2);
        }
    }

    private static List<String> normalize(List<String> list) {
        List<String> normalized = new ArrayList<>(list);
        Collections.reverse(normalized);
        normalized = new ArrayList<>(normalized.stream().map(ArtifactVersion::displayString).dropWhile(s -> {
            try {
                return Long.parseLong(s) == 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }).toList());
        Collections.reverse(normalized);
        return normalized;
    }
    
    @Override
    public String toString() {
        if (this.parts.isEmpty()) return "INVALID";
        return this.parts.stream()
                .map(l -> l.stream()
                        .map(ArtifactVersion::displayString)
                        .collect(Collectors.joining(".")))
                .collect(Collectors.joining("-"));
    }
    
    public static ArtifactVersion parse(String version) {
        if ("INVALID".equals(version)) return INVALID;
        String[] strings = version.split("-");
        if (strings.length == 0) return INVALID;
        List<List<String>> parts = Arrays.stream(strings)
                .map(part -> Arrays.stream(part.split("\\."))
                        .map(String::strip)
                        .filter(s -> !s.isEmpty())
                        .toList())
                .filter(l -> !l.isEmpty())
                .toList();
        return new ArtifactVersion(parts);
    }
    
    private static String displayString(String str) {
        try {
            return Long.toString(Long.parseLong(str)).toLowerCase(Locale.ROOT);
        } catch (NumberFormatException e) {
            return str.toLowerCase(Locale.ROOT);
        }
    }
}
