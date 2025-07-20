package com.retry.vuga.utils.subtitle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubtitleParser {

    public static List<Subtitle> parseSRT(String filePath) throws IOException {
        List<Subtitle> subtitles = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("\\d+")) {
                // Skip index line
                String timeLine = reader.readLine();
                String[] times = timeLine.split(" --> ");
                int startTime = parseTime(times[0]);
                int endTime = parseTime(times[1]);

                StringBuilder textBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                    textBuilder.append(line).append("\n");
                }
                subtitles.add(new Subtitle(startTime, endTime, textBuilder.toString().trim()));
            }
        }
        reader.close();
        return subtitles;
    }

    private static int parseTime(String time) {
        String[] parts = time.split("[:,]");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int milliseconds = Integer.parseInt(parts[3]);
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + milliseconds;
    }

    public static class Subtitle {
        private int startTime;
        private int endTime;
        private String text;

        public Subtitle(int startTime, int endTime, String text) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.text = text;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public String getText() {
            return text;
        }
    }
}
