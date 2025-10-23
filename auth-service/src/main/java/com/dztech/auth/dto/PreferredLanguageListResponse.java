package com.dztech.auth.dto;

import java.util.List;

public record PreferredLanguageListResponse(boolean success, List<PreferredLanguageView> data) {
}
