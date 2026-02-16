package com.screenblock.backend.repository;

import com.screenblock.backend.model.BlockedApp;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface BlockedAppRepository extends MongoRepository<BlockedApp, String> {
    List<BlockedApp> findByUserId(String userId);
}
