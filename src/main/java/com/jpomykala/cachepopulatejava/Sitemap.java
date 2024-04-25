package com.jpomykala.cachepopulatejava;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record Sitemap(@JacksonXmlProperty(localName = "url") List<Url> urls) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Url(
            @JacksonXmlProperty(localName = "loc") String loc,
            @JacksonXmlProperty(localName = "lastmod") String lastmod,
            @JacksonXmlProperty(localName = "changefreq") String changefreq,
            @JacksonXmlProperty(localName = "priority") String priority
    ) {
    }

}
