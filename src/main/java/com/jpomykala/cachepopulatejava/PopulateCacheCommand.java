package com.jpomykala.cachepopulatejava;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class PopulateCacheCommand
{

  private final ExportService exportService;
  private final SitemapFetcher sitemapFetcher;
  private final ScreenshotService screenshotService;
  private final BrowserContextRunner browserContextRunner;

  @SneakyThrows
  @ShellMethod(key = "fetch", value = "Fetch all urls from sitemap to populate cache")
  public void fetch(@ShellOption final String sitemap, @ShellOption(defaultValue = "false") final boolean screenshots, @ShellOption(defaultValue = "") final String exportFormat, @ShellOption(defaultValue = "-1") final int maxPages)
  {

    try
    {
      log.info("Starting cache population for sitemap: {}", sitemap);

      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("## Cache population\n");

      final String domain = DomainUtils.getDomain(sitemap);
      final List<String> sitemapUrls = sitemapFetcher.fetchAllSiteMapUrls(sitemap);
      log.info("Found {} urls in sitemap", sitemapUrls.size());

      stringBuilder.append("Sitemap: ").append(sitemap).append("\n");
      stringBuilder.append("Found ").append(sitemapUrls.size()).append(" urls\n");
      if (maxPages > 0)
      {
        stringBuilder.append("Max pages: ").append(maxPages).append("\n");
      }


      List<ErroredPage> erroredPages = new ArrayList<>();
      browserContextRunner.run(domain, context -> {
        int visitedPagesCounter = 0;
        Duration totalTime = Duration.ZERO;
        List<VisitedPage> visitedPages = new ArrayList<>();

        for (String url : sitemapUrls)
        {
          if (maxPages > 0 && visitedPagesCounter > maxPages)
          {
            log.info("Max pages limit reached");
            break;
          }

          var start = System.currentTimeMillis();
          try
          {
            Page page = context.newPage();
            page.navigate(url);
            page.waitForLoadState(LoadState.LOAD);

            var end = System.currentTimeMillis();
            if (screenshots)
            {
              screenshotService.takeScreenshot(page);
            }

            visitedPages.add(VisitedPage.builder().url(url).title(page.title()).build());

            page.close();
            visitedPagesCounter++;
            var percentage = (float) visitedPagesCounter / sitemapUrls.size() * 100;
            var percentageFormatted = String.format("%.2f", percentage);
            var timeElapsed = end - start;
            totalTime = totalTime.plusMillis(timeElapsed);
            log.info("({}%) Loaded ({}ms) {}", percentageFormatted, timeElapsed, url);
          } catch (Exception e)
          {
            log.error("Error loading page: {}", url, e);
            erroredPages.add(ErroredPage.builder().url(url).error(e.getMessage()).build());
          }
        }

        long totalTimeMillis = totalTime.toMillis();
        String totalTimeString = DurationFormatUtils.formatDurationWords(totalTimeMillis, true, true);
        Duration avgDuration = totalTime.dividedBy(visitedPagesCounter);
        long avgDurationMillis = avgDuration.toMillis();
        log.info("Visited {} urls in {} (avg {}ms)", visitedPagesCounter, totalTimeString, avgDurationMillis);

        stringBuilder.append("Visited ").append(visitedPagesCounter).append(" urls in ").append(totalTimeString).append(" (avg ").append(avgDurationMillis).append("ms)\n");
        if (!erroredPages.isEmpty())
        {
          stringBuilder.append("## Errors\n");
          erroredPages.forEach(erroredPage -> {
            log.warn("Error loading page: {} - {}", erroredPage.url(), erroredPage.error());
            stringBuilder.append("- ").append(erroredPage.url()).append(" - ").append(erroredPage.error()).append("\n");
          });
        }

        GithubActionSummaryUtil.writeSummary(stringBuilder.toString());
        if (StringUtils.hasText(exportFormat))
        {
          exportService.export(visitedPages, exportFormat);
        }
      });
    } catch (Exception e)
    {
      log.warn("Error populating cache", e);
    }
  }
}
