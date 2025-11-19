package com.dztech.auth.dto;

import java.util.List;

public record AdminDriverListResponse(boolean success, List<AdminDriverListItem> drivers) {}
