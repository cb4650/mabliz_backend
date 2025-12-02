package com.dztech.rayder.repository;

import com.dztech.rayder.model.ForceUpdate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForceUpdateRepository extends JpaRepository<ForceUpdate, Long> {

    Optional<ForceUpdate> findByAppIdAndPlatform(String appId, ForceUpdate.Platform platform);
}
