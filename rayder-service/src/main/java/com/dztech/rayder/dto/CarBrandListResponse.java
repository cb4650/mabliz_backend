package com.dztech.rayder.dto;

import java.util.List;

public record CarBrandListResponse(boolean success, List<CarBrandCategoryResponse> data) {}
