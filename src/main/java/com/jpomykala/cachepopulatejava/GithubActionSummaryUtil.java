package com.jpomykala.cachepopulatejava;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class GithubActionSummaryUtil {

    private GithubActionSummaryUtil() {
    }

    public static void writeSummary(String summary) {
        String githubStepSummary = System.getenv("GITHUB_STEP_SUMMARY");
        if (StringUtils.hasText(githubStepSummary)) {
            log.info("Writing action summary to file: {}", githubStepSummary);
            tryWrite(summary, githubStepSummary);
        } else {
            log.info("No Github step summary found");
        }
    }

    private static void tryWrite(String summary, String githubStepSummary) {
        try {
            Files.write(Paths.get(githubStepSummary), summary.getBytes());
        } catch (IOException e) {
            log.error("Error writing summary to file", e);
        }
    }
}
