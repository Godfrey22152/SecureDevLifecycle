package com.screenblock.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Data
@Document(collection = "user_stats")
public class UserStats {
    @Id
    private String id;
    private String userId;
    private LocalDate date;
    private Map<String, Long> appUsageMinutes; // Map of appBundleId to minutes
}
