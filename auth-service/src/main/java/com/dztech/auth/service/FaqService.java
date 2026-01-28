package com.dztech.auth.service;

import com.dztech.auth.dto.FaqRequest;
import com.dztech.auth.dto.FaqResponse;
import com.dztech.auth.model.Faq;
import com.dztech.auth.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    @Transactional(readOnly = true)
    public List<FaqResponse> getAllFaqs() {
        List<Faq> faqs = faqRepository.findAll();
        return faqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FaqResponse> getFaqsByAppId(String appId) {
        validateAppId(appId);
        List<Faq> faqs = faqRepository.findByAppId(appId);
        return faqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FaqResponse> getFaqsByAppIdAndCategory(String appId, String category) {
        validateAppId(appId);
        List<Faq> faqs = faqRepository.findByAppIdAndCategory(appId, category);
        return faqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FaqResponse> getFaqsByAppIdAndCategoryAndSubCategory(String appId, String category, String subCategory) {
        validateAppId(appId);
        List<Faq> faqs = faqRepository.findByAppIdAndCategoryAndSubCategory(appId, category, subCategory);
        return faqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FaqResponse getFaqById(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ not found with id: " + id));
        return mapToResponse(faq);
    }

    @Transactional
    public FaqResponse createFaq(FaqRequest request) {
        validateAppId(request.getAppId());

        Faq faq = Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .appId(request.getAppId())
                .build();

        Faq saved = faqRepository.save(faq);
        return mapToResponse(saved);
    }

    @Transactional
    public FaqResponse updateFaq(Long id, FaqRequest request) {
        validateAppId(request.getAppId());

        Faq existing = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ not found with id: " + id));

        Faq updated = Faq.builder()
                .id(existing.getId())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .appId(request.getAppId())
                .createdAt(existing.getCreatedAt())
                .build();

        Faq saved = faqRepository.save(updated);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new IllegalArgumentException("FAQ not found with id: " + id);
        }
        faqRepository.deleteById(id);
    }

    private void validateAppId(String appId) {
        if (!"rydd".equals(appId) && !"rydc".equals(appId)) {
            throw new IllegalArgumentException("Invalid app ID. Must be 'rydd' or 'rydc'");
        }
    }

    private FaqResponse mapToResponse(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .subCategory(faq.getSubCategory())
                .appId(faq.getAppId())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
