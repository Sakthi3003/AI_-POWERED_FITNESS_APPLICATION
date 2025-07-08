package com.fitness.service;

import com.fitness.entity.Activity;
import com.fitness.model.ActivityRequest;
import com.fitness.model.ActivityResponse;
import com.fitness.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;

    private final UserValidationService userValidationService;

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;



    public ActivityResponse trackActivity(ActivityRequest request) {
        boolean isValidUSer = userValidationService.validateUser(request.getUserId());

        if(!isValidUSer){
            throw new RuntimeException("Invalid user");
        }
        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .activityType(request.getActivityType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
        Activity savedActivity = activityRepository.save(activity);

        // send all the activity in the rabbit mq
        try{
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
        }catch (Exception e){
            log.error("Failed to public");
        }
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
