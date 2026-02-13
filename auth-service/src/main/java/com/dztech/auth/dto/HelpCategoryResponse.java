package com.dztech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpCategoryResponse {

    private Long id;
    private String title;
    private String categoryKey;
    private String appId;
    private List<HelpItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;
}