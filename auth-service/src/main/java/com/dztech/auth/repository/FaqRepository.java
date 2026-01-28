package com.dztech.auth.repository;

import com.dztech.auth.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByAppId(String appId);

    List<Faq> findByAppIdAndCategory(String appId, String category);

    List<Faq> findByAppIdAndCategoryAndSubCategory(String appId, String category, String subCategory);

    boolean existsByAppId(String appId);
}
