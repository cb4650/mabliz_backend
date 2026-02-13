package com.dztech.auth.service;

import com.dztech.auth.dto.HelpCategoryRequest;
import com.dztech.auth.dto.HelpCategoryResponse;
import com.dztech.auth.dto.HelpItemResponse;
import com.dztech.auth.model.HelpCategory;
import com.dztech.auth.model.HelpItem;
import com.dztech.auth.repository.HelpCategoryRepository;
import com.dztech.auth.repository.HelpItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HelpCategoryService {

    private final HelpCategoryRepository helpCategoryRepository;
    private final HelpItemRepository helpItemRepository;

    @Transactional(readOnly = true)
    public List<HelpCategoryResponse> getAllHelpCategories(String appId) {
        validateAppId(appId);
        List<HelpCategory> categories = helpCategoryRepository.findByAppId(appId);
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HelpCategoryResponse getHelpCategoryByCategoryKey(String appId, String categoryKey) {
        validateAppId(appId);
        HelpCategory category = helpCategoryRepository.findByAppIdAndCategoryKey(appId, categoryKey)
                .orElseThrow(() -> new IllegalArgumentException("Help category not found with key: " + categoryKey));
        return mapToResponse(category);
    }

    @Transactional(readOnly = true)
    public HelpCategoryResponse getHelpCategoryById(Long id) {
        HelpCategory category = helpCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Help category not found with id: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public HelpCategoryResponse createHelpCategory(HelpCategoryRequest request) {
        validateAppId(request.getAppId());

        if (helpCategoryRepository.existsByCategoryKey(request.getCategoryKey())) {
            throw new IllegalArgumentException("Help category with key '" + request.getCategoryKey() + "' already exists");
        }

        HelpCategory category = new HelpCategory();
        category.setTitle(request.getTitle());
        category.setCategoryKey(request.getCategoryKey());
        category.setAppId(request.getAppId());

        HelpCategory saved = helpCategoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public HelpCategoryResponse updateHelpCategory(Long id, HelpCategoryRequest request) {
        validateAppId(request.getAppId());

        HelpCategory existing = helpCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Help category not found with id: " + id));

        // Check if category key is being changed and if it already exists
        if (!existing.getCategoryKey().equals(request.getCategoryKey()) &&
            helpCategoryRepository.existsByCategoryKey(request.getCategoryKey())) {
            throw new IllegalArgumentException("Help category with key '" + request.getCategoryKey() + "' already exists");
        }

        existing.setTitle(request.getTitle());
        existing.setCategoryKey(request.getCategoryKey());
        existing.setAppId(request.getAppId());

        HelpCategory saved = helpCategoryRepository.save(existing);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteHelpCategory(Long id) {
        if (!helpCategoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Help category not found with id: " + id);
        }
        helpCategoryRepository.deleteById(id);
    }

    private void validateAppId(String appId) {
        if (!"rydd".equals(appId) && !"rydc".equals(appId)) {
            throw new IllegalArgumentException("Invalid app ID. Must be 'rydd' or 'rydc'");
        }
    }

    private HelpCategoryResponse mapToResponse(HelpCategory category) {
        List<HelpItem> items = helpItemRepository.findByCategoryIdOrderByCreatedAt(category.getId());
        List<HelpItemResponse> itemResponses = items.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        HelpCategoryResponse response = new HelpCategoryResponse();
        response.setId(category.getId());
        response.setTitle(category.getTitle());
        response.setCategoryKey(category.getCategoryKey());
        response.setAppId(category.getAppId());
        response.setItems(itemResponses);
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }

    private HelpItemResponse mapItemToResponse(HelpItem item) {
        HelpItemResponse response = new HelpItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());
        response.setEmail(item.getEmail());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}