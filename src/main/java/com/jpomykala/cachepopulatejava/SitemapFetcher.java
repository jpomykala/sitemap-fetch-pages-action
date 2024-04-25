package com.jpomykala.cachepopulatejava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SitemapFetcher {

    private final RestClient restClient;
    private final XmlMapper xmlMapper;

    public List<String> fetchAllSiteMapUrls(String sitemapUrl) throws JsonProcessingException {
        String sitemapBody = restClient.get().uri(sitemapUrl).retrieve().body(String.class);
        Sitemap sitemap = xmlMapper.readValue(sitemapBody, Sitemap.class);
        return sitemap.urls()
                .stream()
                .map(Sitemap.Url::loc)
                .toList();
    }

}
