package com.jpomykala.cachepopulatejava;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExportService {

    private final ObjectMapper objectMapper;

    public void export(List<VisitedPage> visitedPages, @NonNull String exportFormat) {
        switch (exportFormat) {
            case "json":
                exportToJson(visitedPages);
                break;
            case "csv":
                exportToCsv(visitedPages);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + exportFormat);
        }
    }

    private void exportToJson(List<VisitedPage> visitedPages) {
        String outputPath = "output.json";

        try {
            Files.deleteIfExists(Paths.get(outputPath));
            objectMapper.writeValue(new File(outputPath), visitedPages);
        } catch (IOException e) {
            throw new RuntimeException("Error while exporting to json", e);
        }
    }

    private void exportToCsv(List<VisitedPage> visitedPages) {
        String outputPath = "output.csv";

        try {
            Files.deleteIfExists(Paths.get(outputPath));
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8))) {
                writer.write("url,title\n");
                for (VisitedPage visitedPage : visitedPages) {
                    writer.write(visitedPage.url() + "," + visitedPage.title() + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while exporting to csv", e);
        }
    }
}
