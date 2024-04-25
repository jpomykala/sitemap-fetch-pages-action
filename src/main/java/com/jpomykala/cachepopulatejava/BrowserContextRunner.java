package com.jpomykala.cachepopulatejava;

import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
public class BrowserContextRunner {


    public void run(String domain, Consumer<BrowserContext> contextSupplier) {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch()) {
                try (BrowserContext context = browser.newContext()) {
                    context.setDefaultTimeout(10_000);
                    context.setDefaultNavigationTimeout(10_000);
                    context.onResponse(onResponseConsumer(domain));
                    context.onRequestFailed(onRequestFailedConsumer(domain));
                    contextSupplier.accept(context);
                }
            }
        }
    }

    private static Consumer<Response> onResponseConsumer(String domain) {
        return response -> {
            String url = response.url();
            if (!url.contains(domain)) {
                return;
            }
            Map<String, String> headers = response.headers();
            String cacheControl = headers.get("cache-control");
            if (cacheControl != null && cacheControl.contains("private")) {
                log.warn("Private cache control: {}", url);
            }
            if (cacheControl != null && cacheControl.contains("no-cache")) {
                log.warn("No cache control: {}", url);
            }

            if (cacheControl != null && cacheControl.contains("no-store")) {
                log.warn("No store cache control: {}", url);
            }

            if (cacheControl != null && cacheControl.contains("max-age=0")) {
                log.warn("Max age 0 cache control: {}", url);
            }

            if (cacheControl != null && cacheControl.contains("must-revalidate")) {
                log.warn("Must revalidate cache control: {}", url);
            }

            if (cacheControl != null && cacheControl.contains("proxy-revalidate")) {
                log.warn("Proxy revalidate cache control: {}", url);
            }

            if (cacheControl == null) {
                log.warn("No cache control: {}", url);
            }
        };
    }


    private Consumer<Request> onRequestFailedConsumer(String domain) {
        return request -> {
            String url = request.url();

            if (!url.contains(domain)) {
                return;
            }

            Optional<Integer> statusCode = Optional.of(request)
                    .map(Request::response)
                    .map(Response::status);
            if (statusCode.isPresent() && statusCode.get() >= 200 && statusCode.get() <= 299) {
                return;
            }

            if (statusCode.isPresent()) {
                log.error("- Request failed ({}): {}", request.response().status(), url);
                return;
            }

            log.error("- Request failed: {} -> {}", request.failure(), url);
        };
    }

}
