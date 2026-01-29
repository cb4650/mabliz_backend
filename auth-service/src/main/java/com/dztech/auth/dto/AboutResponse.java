package com.dztech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AboutResponse {

    private boolean success;
    private AboutData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AboutData {
        private String title;
        private String description;
    }
}
