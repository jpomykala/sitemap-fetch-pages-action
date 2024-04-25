package com.jpomykala.cachepopulatejava;

import java.net.URI;

public class DomainUtils {

    private DomainUtils() {
    }

    public static String getDomain(String url) {
        URI uri = URI.create(url);
        return uri.getScheme() + "://" + uri.getHost();
    }

    public static String getPath(String url) {
        URI uri = URI.create(url);
        return uri.getPath();
    }
}
