package com.dztech.auth.repository;

import com.dztech.auth.model.HelpItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpItemRepository extends JpaRepository<HelpItem, Long> {

    List<HelpItem> findByCategoryId(Long categoryId);

    @Query("SELECT hi FROM HelpItem hi WHERE hi.category.id = :categoryId ORDER BY hi.id")
    List<HelpItem> findByCategoryIdOrderByCreatedAt(@Param("categoryId") Long categoryId);

    @Query("SELECT hi FROM HelpItem hi JOIN FETCH hi.category WHERE hi.id = :id")
    HelpItem findByIdWithCategory(@Param("id") Long id);
}