package com.screenblock.backend.model;

import lombok.Data;

@Data
public class Schedule {
    private String dayOfWeek; // e.g., MONDAY
    private String startTime; // e.g., 09:00
    private String endTime;   // e.g., 17:00
}
