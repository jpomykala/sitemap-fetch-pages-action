package com.jpomykala.cachepopulatejava;

import lombok.Builder;

@Builder
public record ErroredPage(
        String url,
        String error
) {
}
