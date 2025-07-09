package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.client.Activity;
import com.fitness.aiservice.entity.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("AI Response: {}", aiResponse);

        return processAiReponse(activity, aiResponse);
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
            Analyze this fitness activity and provide detailed recommendations in the following JSON format:
            {
                "analysis": {
                    "overall": "Overall analysis here",
                    "pace": "Pace analysis here",
                    "heartRate": "Heart rate analysis here",
                    "caloriesBurned": "Calories analysis here"
                },
                "improvements": [
                    {
                        "area": "Area name",
                        "recommendation": "Detailed recommendation"
                    }
                ],
                "workouts": [
                    {
                        "workout": "Workout name",
                        "description": "Detailed workout description"
                    }
                ],
                "safety": [
                    "Safety point 1",
                    "Safety point 2"
                ]
            }

            Analyze this activity:
            Activity Type: %s
            Duration: %d minutes
            Calories Burned: %d
            Additional Metrics: %s

            Provide detailed analysis focusing on performance, improvements, next workout steps, and safety guidelines.
            Ensure the response follows the EXACT JSON format shown above.
            """,
                activity.getActivityType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }

    private Recommendation processAiReponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories Burned:");

            List<String> improvements = extractImprovement(analysisJson.path("improvements"));
            List<String> workouts = extractWorkouts(analysisJson.path("workouts"));
            List<String> safety = extractSafety(analysisJson.path("safety"));

            return Recommendation.builder()
                    .userId(activity.getUserId())
                    .activityId(activity.getId())
                    .activityType(activity.getActivityType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .safety(safety)
                    .suggesstions(workouts)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(" ")
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty() ? Collections.singletonList("No safety guidance provided") : safety;
    }

    private List<String> extractWorkouts(JsonNode workoutsNode) {
        List<String> workouts = new ArrayList<>();
        if (workoutsNode.isArray()) {
            workoutsNode.forEach(workoutNode -> {
                String workout = workoutNode.path("workout").asText();
                String description = workoutNode.path("description").asText();
                workouts.add(String.format("%s: %s", workout, description));
            });
        }
        return workouts.isEmpty() ? Collections.singletonList("No specific workout provided") : workouts;
    }

    private List<String> extractImprovement(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvementNode -> {
                String area = improvementNode.path("area").asText();
                String detail = improvementNode.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, detail));
            });
        }
        return improvements.isEmpty() ? Collections.singletonList("No specific improvement provided") : improvements;
    }
}
