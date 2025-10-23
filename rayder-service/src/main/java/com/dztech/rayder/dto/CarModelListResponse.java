package com.dztech.rayder.dto;

import java.util.List;

public record CarModelListResponse(boolean success, List<CarModelResponse> models) {
}
