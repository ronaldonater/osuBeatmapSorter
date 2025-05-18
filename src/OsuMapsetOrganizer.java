import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * OsuMapsetOrganizer - A tool to organize osu beatmap collections
 *
 * This program analyzes a practice mapset folder containing multiple beatmaps
 * and organizes each difficulty with its corresponding background image and audio file
 * into separate folders based on the audio filename.
 */
public class OsuMapsetOrganizer {

    // Regular expressions to extract relevant information from .osu files
    private static final Pattern AUDIO_FILENAME_PATTERN = Pattern.compile("AudioFilename:\\s*(.+)");
    private static final Pattern BACKGROUND_PATTERN = Pattern.compile("\\d+,\\d+,\"(.+)\",");
    private static final Pattern TITLE_PATTERN = Pattern.compile("Title:(.+)");
    private static final Pattern ARTIST_PATTERN = Pattern.compile("Artist:(.+)");
    private static final Pattern VERSION_PATTERN = Pattern.compile("Version:(.+)");

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java OsuMapsetOrganizer <mapset_folder_path>");
            return;
        }

        String mapsetFolderPath = args[0];
        File mapsetFolder = new File(mapsetFolderPath);

        if (!mapsetFolder.exists() || !mapsetFolder.isDirectory()) {
            System.out.println("Error: The specified mapset folder does not exist or is not a directory.");
            return;
        }

        try {
            organizeMapset(mapsetFolder);
        } catch (IOException e) {
            System.err.println("Error organizing mapset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void organizeMapset(File mapsetFolder) throws IOException {
        System.out.println("Analyzing mapset folder: " + mapsetFolder.getAbsolutePath());

        // Find all .osu files in the folder
        File[] osuFiles = mapsetFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".osu"));

        if (osuFiles == null || osuFiles.length == 0) {
            System.out.println("No .osu files found in the specified folder.");
            return;
        }

        System.out.println("Found " + osuFiles.length + " .osu files. Analyzing...");

        // Process each .osu file to create individual folders
        for (File osuFile : osuFiles) {
            DifficultyInfo diffInfo = extractDifficultyInfo(osuFile);
            if (diffInfo == null) {
                System.err.println("Skipping " + osuFile.getName() + " - unable to extract required information");
                continue;
            }

            // Create a folder name for this difficulty
            String folderName;

            // Use version (difficulty name) if available, otherwise use osu filename
            if (diffInfo.version != null && !diffInfo.version.trim().isEmpty()) {
                folderName = sanitizeFileName(diffInfo.version);
            } else {
                folderName = getNameWithoutExtension(osuFile.getName());
            }

            // Add artist and title prefix if available
            if (diffInfo.artist != null && !diffInfo.artist.trim().isEmpty() &&
                    diffInfo.title != null && !diffInfo.title.trim().isEmpty()) {
                folderName = sanitizeFileName(diffInfo.artist + " - " + diffInfo.title + " [" + folderName + "]");
            }

            File newFolder = new File(mapsetFolder, folderName);
            if (!newFolder.exists() && !newFolder.mkdir()) {
                System.err.println("Failed to create folder: " + newFolder.getAbsolutePath());
                continue;
            }

            System.out.println("Created folder: " + newFolder.getName());

            // Copy the .osu file to the new folder
            Files.copy(diffInfo.osuFile.toPath(), new File(newFolder, diffInfo.osuFile.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  - Copied difficulty: " + diffInfo.osuFile.getName());

            // Copy the audio file to the new folder
            if (diffInfo.audioFilename != null) {
                File audioFile = new File(mapsetFolder, diffInfo.audioFilename);
                if (audioFile.exists()) {
                    Files.copy(audioFile.toPath(), new File(newFolder, diffInfo.audioFilename).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("  - Copied audio: " + diffInfo.audioFilename);
                } else {
                    System.err.println("  - Warning: Audio file not found: " + diffInfo.audioFilename);
                }
            }

            // Copy the background image if present
            if (diffInfo.backgroundFilename != null) {
                File bgFile = new File(mapsetFolder, diffInfo.backgroundFilename);
                if (bgFile.exists()) {
                    Files.copy(bgFile.toPath(), new File(newFolder, diffInfo.backgroundFilename).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("  - Copied background: " + diffInfo.backgroundFilename);
                } else {
                    System.err.println("  - Warning: Background file not found: " + diffInfo.backgroundFilename);
                }
            }
        }

        System.out.println("Organization complete!");
    }

    private static DifficultyInfo extractDifficultyInfo(File osuFile) throws IOException {
        DifficultyInfo info = new DifficultyInfo();
        info.osuFile = osuFile;

        try (BufferedReader reader = new BufferedReader(new FileReader(osuFile))) {
            String line;
            boolean inEventsSection = false;

            while ((line = reader.readLine()) != null) {
                // Check for section markers
                if (line.equals("[Events]")) {
                    inEventsSection = true;
                } else if (line.startsWith("[")) {
                    inEventsSection = false;
                }

                // Extract audio filename
                Matcher audioMatcher = AUDIO_FILENAME_PATTERN.matcher(line);
                if (audioMatcher.find()) {
                    info.audioFilename = audioMatcher.group(1).trim();
                }

                // Extract title
                Matcher titleMatcher = TITLE_PATTERN.matcher(line);
                if (titleMatcher.find()) {
                    info.title = titleMatcher.group(1).trim();
                }

                // Extract artist
                Matcher artistMatcher = ARTIST_PATTERN.matcher(line);
                if (artistMatcher.find()) {
                    info.artist = artistMatcher.group(1).trim();
                }

                // Extract version (difficulty name)
                Matcher versionMatcher = VERSION_PATTERN.matcher(line);
                if (versionMatcher.find()) {
                    info.version = versionMatcher.group(1).trim();
                }

                // Extract background filename if in Events section
                if (inEventsSection) {
                    Matcher bgMatcher = BACKGROUND_PATTERN.matcher(line);
                    if (bgMatcher.find()) {
                        info.backgroundFilename = bgMatcher.group(1).trim();
                    }
                }
            }
        }

        // Return null if essential information is missing
        if (info.audioFilename == null) {
            System.err.println("Warning: No audio filename found in " + osuFile.getName());
            return null;
        }

        return info;
    }

    private static String getNameWithoutExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }

    private static String sanitizeFileName(String name) {
        // Replace invalid characters for Windows file names
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static class DifficultyInfo {
        File osuFile;
        String audioFilename;
        String backgroundFilename;
        String title;
        String artist;
        String version;
    }
}