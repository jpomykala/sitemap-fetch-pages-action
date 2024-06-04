package com.jpomykala.cachepopulatejava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class PopulateCacheCommand {

    private final ExportService exportService;
    private final SitemapFetcher sitemapFetcher;
    private final ScreenshotService screenshotService;
    private final BrowserContextRunner browserContextRunner;

    @SneakyThrows
    @ShellMethod(key = "fetch", value = "Fetch all urls from sitemap to populate cache")
    public void fetch(
            @ShellOption final String sitemap,
            @ShellOption(defaultValue = "false") final boolean screenshots,
            @ShellOption(defaultValue = "") final String exportFormat,
            @ShellOption(defaultValue = "-1") final int maxPages
    ) {

        log.info("Starting cache population for sitemap: {}", sitemap);

        String githubStepSummary = System.getenv("GITHUB_STEP_SUMMARY");
        if (StringUtils.hasText(githubStepSummary)) {
            log.info("Github step summary: {}", githubStepSummary);
            String sample = """
                    ## Cache population
                    My awesome cache population step
                    """;
            Files.write(Paths.get(githubStepSummary), sample.getBytes());
        } else {
            log.info("No Github step summary found");
        }

        final String domain = DomainUtils.getDomain(sitemap);
        final List<String> sitemapUrls = sitemapFetcher.fetchAllSiteMapUrls(sitemap);
        log.info("Found {} urls in sitemap", sitemapUrls.size());

        browserContextRunner.run(domain, context -> {
            int visitedPagesCounter = 0;
            Duration totalTime = Duration.ZERO;
            List<VisitedPage> visitedPages = new ArrayList<>();

            for (String url : sitemapUrls) {
                if (maxPages > 0 && visitedPagesCounter > maxPages) {
                    log.info("Max pages limit reached");
                    break;
                }

                var start = System.currentTimeMillis();
                Page page;
                try {
                    page = context.newPage();
                    page.navigate(url);
                    page.waitForLoadState(LoadState.LOAD);
                } catch (Exception e) {
                    log.error("Error loading page: {}", url, e);
                    continue;
                }

                var end = System.currentTimeMillis();
                if (screenshots) {
                    screenshotService.takeScreenshot(page);
                }

                visitedPages.add(VisitedPage.builder()
                        .url(url)
                        .title(page.title())
                        .build()
                );

                page.close();
                visitedPagesCounter++;
                var percentage = (float) visitedPagesCounter / sitemapUrls.size() * 100;
                var percentageFormatted = String.format("%.2f", percentage);
                var timeElapsed = end - start;
                totalTime = totalTime.plusMillis(timeElapsed);
                log.info("({}%) Loaded ({}ms) {}", percentageFormatted, timeElapsed, url);
            }

            long totalTimeMillis = totalTime.toMillis();
            String totalTimeString = DurationFormatUtils.formatDurationWords(totalTimeMillis, true, true);
            Duration avgDuration = totalTime.dividedBy(visitedPagesCounter);
            log.info("Visited {} urls in {} (avg {}ms)",
                    visitedPagesCounter,
                    totalTimeString,
                    avgDuration.toMillis()
            );

            if (StringUtils.hasText(exportFormat)) {
                exportService.export(visitedPages, exportFormat);
            }
        });
    }
}
