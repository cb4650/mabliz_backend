package com.dztech.auth.repository;

import com.dztech.auth.model.HelpCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelpCategoryRepository extends JpaRepository<HelpCategory, Long> {

    List<HelpCategory> findByAppId(String appId);

    Optional<HelpCategory> findByAppIdAndCategoryKey(String appId, String categoryKey);

    boolean existsByCategoryKey(String categoryKey);

    @Query("SELECT hc FROM HelpCategory hc WHERE hc.appId = :appId AND hc.categoryKey = :categoryKey")
    Optional<HelpCategory> findByAppIdAndCategoryKeyQuery(@Param("appId") String appId, @Param("categoryKey") String categoryKey);
}