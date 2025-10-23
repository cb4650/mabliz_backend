package com.dztech.auth.repository;

import com.dztech.auth.model.PreferredLanguage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferredLanguageRepository extends JpaRepository<PreferredLanguage, Long> {
}
