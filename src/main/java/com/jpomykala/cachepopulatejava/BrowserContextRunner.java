package com.jpomykala.cachepopulatejava;

import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
public class BrowserContextRunner
{


  public void run(String domain, Consumer<BrowserContext> contextSupplier)
  {
    try (Playwright playwright = Playwright.create())
    {
      List<BrowserType> browserTypes = List.of(playwright.chromium());
      for (BrowserType browserType : browserTypes)
      {
        try (Browser browser = browserType.launch())
        {
          try (BrowserContext context = browser.newContext())
          {
            context.setDefaultTimeout(60_000);
            context.setDefaultNavigationTimeout(60_000);
            context.setExtraHTTPHeaders(Map.of(
                    "X-Cache-Populate", "true"
            ));
            context.onRequestFailed(onRequestFailedConsumer(domain));
            contextSupplier.accept(context);
          }
        }
      }
    }
  }

  private Consumer<Request> onRequestFailedConsumer(String domain)
  {
    return request -> {
      String url = request.url();

      if (!url.contains(domain))
      {
        return;
      }

      Optional<Integer> statusCode = Optional.of(request)
              .map(Request::response)
              .map(Response::status);
      if (statusCode.isPresent() && statusCode.get() >= 200 && statusCode.get() <= 299)
      {
        return;
      }

      if (statusCode.isPresent())
      {
        log.error("- Request failed ({}): {}", request.response().status(), url);
        return;
      }

      log.error("- Request failed: {} -> {}", request.failure(), url);
    };
  }

}
