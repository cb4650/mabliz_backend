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
public class HelpItemResponse {

    private Long id;
    private String title;
    private String description;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}