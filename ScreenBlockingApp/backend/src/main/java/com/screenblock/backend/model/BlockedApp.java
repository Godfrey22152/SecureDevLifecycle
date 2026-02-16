package com.screenblock.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "blocked_apps")
public class BlockedApp {
    @Id
    private String id;
    private String userId;
    private String appBundleId; // e.g., com.facebook.katana
    private String appName;
    private boolean isBlocked;
    private List<Schedule> schedules;
}
