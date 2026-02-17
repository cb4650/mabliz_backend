package com.dztech.rayder.dto;

import org.springframework.web.multipart.MultipartFile;

public record DriverReachedRequest(
        MultipartFile selfieImage) {
}
