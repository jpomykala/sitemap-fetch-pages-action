package com.jpomykala.cachepopulatejava;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PopulateCacheCommandTest {

    @Autowired
    private PopulateCacheCommand populateCacheCommand;

    @Test
    void walkOverDomain() throws JsonProcessingException {
        populateCacheCommand.fetch("https://renderform.io/sitemap.xml", false, "csv", -1);
    }
}
