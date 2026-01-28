package com.dztech.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqResponse {

    private Long id;
    private String question;
    private String answer;
    private String category;
    private String subCategory;
    private String appId;
    private Instant createdAt;
    private Instant updatedAt;
}
