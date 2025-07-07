package com.fitness.controller;

import com.fitness.model.ActivityRequest;
import com.fitness.model.ActivityResponse;
import com.fitness.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest activity) {

        return ResponseEntity.ok(activityService.trackActivity(activity));
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<ActivityResponse>> getAllActivitites(@PathVariable String userId){
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("activity/{activityId}")
    public ResponseEntity<ActivityResponse> getActivityByActivityID(@PathVariable String activityId){
        return ResponseEntity.ok(activityService.getUserActivityById(activityId));
    }



}
