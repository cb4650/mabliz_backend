package com.dztech.rayder.dto;

import java.util.List;

public record VehicleListResponse(boolean success, List<VehicleResponse> vehicles) {
}
