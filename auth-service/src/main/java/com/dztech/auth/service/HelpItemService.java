package com.dztech.auth.service;

import com.dztech.auth.dto.HelpItemRequest;
import com.dztech.auth.dto.HelpItemResponse;
import com.dztech.auth.model.HelpCategory;
import com.dztech.auth.model.HelpItem;
import com.dztech.auth.repository.HelpCategoryRepository;
import com.dztech.auth.repository.HelpItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HelpItemService {

    private final HelpItemRepository helpItemRepository;
    private final HelpCategoryRepository helpCategoryRepository;

    @Transactional(readOnly = true)
    public HelpItemResponse getHelpItemById(Long id) {
        Optional<HelpItem> itemOpt = helpItemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Help item not found with id: " + id);
        }
        HelpItem item = itemOpt.get();
        return mapToResponse(item);
    }

    @Transactional
    public HelpItemResponse createHelpItem(HelpItemRequest request) {
        Optional<HelpCategory> categoryOpt = helpCategoryRepository.findById(request.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Help category not found with id: " + request.getCategoryId());
        }
        HelpCategory category = categoryOpt.get();

        HelpItem item = new HelpItem();
        item.setCategory(category);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setEmail(request.getEmail());

        HelpItem saved = helpItemRepository.save(item);
        return mapToResponse(saved);
    }

    @Transactional
    public HelpItemResponse updateHelpItem(Long id, HelpItemRequest request) {
        Optional<HelpItem> existingOpt = helpItemRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Help item not found with id: " + id);
        }
        HelpItem existing = existingOpt.get();

        Optional<HelpCategory> categoryOpt = helpCategoryRepository.findById(request.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("Help category not found with id: " + request.getCategoryId());
        }
        HelpCategory category = categoryOpt.get();

        existing.setCategory(category);
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setEmail(request.getEmail());

        HelpItem saved = helpItemRepository.save(existing);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteHelpItem(Long id) {
        if (!helpItemRepository.existsById(id)) {
            throw new IllegalArgumentException("Help item not found with id: " + id);
        }
        helpItemRepository.deleteById(id);
    }

    private HelpItemResponse mapToResponse(HelpItem item) {
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