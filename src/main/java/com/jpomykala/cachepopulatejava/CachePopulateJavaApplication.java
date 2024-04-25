package com.jpomykala.cachepopulatejava;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class CachePopulateJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CachePopulateJavaApplication.class, args);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .build();
    }

    @Bean
    public XmlMapper xmlMapper() {
        return XmlMapper.builder()
                .defaultUseWrapper(false)
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
