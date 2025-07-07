package com.fitness.model;

import com.fitness.entity.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ActivityResponse {
    private String id;
    private String userId;
    private ActivityType activityType;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> additionalMetrics;
}
