package com.jpomykala.cachepopulatejava;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ScreenshotType;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class ScreenshotService {

    public void takeScreenshot(Page page) {
        String url = page.url();
        var screenshotOptions = getScreenshotOptions(url);
        page.screenshot(screenshotOptions);
    }

    private Page.ScreenshotOptions getScreenshotOptions(String url) {
        var fileSaveUrl = getSafeFilePath(url);
        return new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(ScreenshotType.JPEG)
                .setQuality(75)
                .setPath(Paths.get("screenshots/" + fileSaveUrl + ".jpg"));
    }

    private String getSafeFilePath(String url) {
        String path = DomainUtils.getPath(url);
        var fileSaveUrl = path.replace("/", "-");
        if (fileSaveUrl.startsWith("-")) {
            fileSaveUrl = fileSaveUrl.substring(1);
        }

        if (fileSaveUrl.endsWith("-")) {
            fileSaveUrl = fileSaveUrl.substring(0, fileSaveUrl.length() - 1);
        }
        return fileSaveUrl;
    }
}
