package com.fitness.tracker.controller;

import com.fitness.tracker.service.CalorieService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RecalculateCaloriesController {

    @Autowired
    private CalorieService calorieService;

    @PostMapping("/recalculateCalories")
    public String recalculateCalories(HttpSession session) {

        Integer userId = (Integer) session.getAttribute("user_id");

        if (userId == null) {
            return "ERROR: NOT_LOGGED_IN";
        }


        return calorieService.recalculateCalories(userId);
    }
}