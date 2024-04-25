package com.jpomykala.cachepopulatejava;

import lombok.Builder;

@Builder
public record VisitedPage(
        String url,
        String title
) {
}
