package com.fitness.service;

import com.fitness.entity.Activity;
import com.fitness.model.ActivityRequest;
import com.fitness.model.ActivityResponse;
import com.fitness.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityResponse trackActivity(ActivityRequest request) {
        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .activityType(request.getActivityType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
        Activity savedActivity = activityRepository.save(activity);
        return mapToActivityResponse(savedActivity);

    }

    private ActivityResponse mapToActivityResponse(Activity savedActivity) {
        ActivityResponse activityResponse = new ActivityResponse();
        activityResponse.setId(savedActivity.getId());
        activityResponse.setUserId(savedActivity.getUserId());
        activityResponse.setActivityType(savedActivity.getActivityType());
        activityResponse.setDuration(savedActivity.getDuration());
        activityResponse.setStartTime(savedActivity.getStartTime());
        activityResponse.setAdditionalMetrics(savedActivity.getAdditionalMetrics());
        activityResponse.setCaloriesBurned(savedActivity.getCaloriesBurned());
        activityResponse.setUpdatedAt(savedActivity.getUpdatedAt());
        activityResponse.setCreatedAt(activityResponse.getUpdatedAt());

        return activityResponse;
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);

        List<ActivityResponse> activityResponses = activities.stream().map(this::mapToActivityResponse).collect(Collectors.toList());
        return activityResponses;
    }

    public ActivityResponse getUserActivityById(String activityId) {
        return activityRepository
                .findById(activityId)
                .map(this::mapToActivityResponse)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
    }
}
