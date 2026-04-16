package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Controller
public class SetupController {

    @PostMapping("/setup")
    public String setup(
            @RequestParam int age,
            @RequestParam int height,
            @RequestParam double weight,
            @RequestParam String gender,
            @RequestParam String activity,
            @RequestParam String goal,
            HttpSession session
    ) {

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return "redirect:/landingPage.html";
            }

            // 🔹 BMR
            double bmr;
            if ("male".equals(gender)) {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            }

            // 🔹 Activity multiplier
            double multiplier = 1.2;
            if ("light".equals(activity)) multiplier = 1.375;
            else if ("moderate".equals(activity)) multiplier = 1.55;
            else if ("heavy".equals(activity)) multiplier = 1.725;

            // 🔹 TDEE
            double tdee = bmr * multiplier;

            // 🔹 Goal calories
            int calories;
            if ("fat_loss".equals(goal)) {
                calories = (int) (tdee - 400);
            } else if ("muscle_gain".equals(goal)) {
                calories = (int) (tdee + 300);
            } else {
                calories = (int) tdee;
            }

            Connection conn = DBConnection.getConnection();

            PreparedStatement check = conn.prepareStatement(
                    "SELECT * FROM user_profile WHERE user_id = ?"
            );
            check.setInt(1, userId);
            ResultSet rsCheck = check.executeQuery();

            if (rsCheck.next()) {
                // 🔁 Already exists → just redirect (or update if you want)
                conn.close();
                return "redirect:/dashboard.html";
            }

            // 🔹 Insert profile
            PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO user_profile (user_id, age, height, gender, activity_level, goal) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps1.setInt(1, userId);
            ps1.setInt(2, age);
            ps1.setInt(3, height);
            ps1.setString(4, gender);
            ps1.setString(5, activity);
            ps1.setString(6, goal);
            ps1.executeUpdate();

            // 🔹 Insert stats
            PreparedStatement ps2 = conn.prepareStatement(
                    "INSERT INTO user_stats (user_id, current_weight, calorie_target, tdee, last_updated)\n" +
                            "VALUES (?, ?, ?, ?, CURRENT_DATE)"
            );
            ps2.setInt(1, userId);
            ps2.setDouble(2, weight);
            ps2.setInt(3, calories);
            ps2.setDouble(4, tdee);
            ps2.executeUpdate();

            // 🔹 Insert weight
            PreparedStatement ps3 = conn.prepareStatement(
                    "INSERT INTO weight (user_id, weight, date)\n" +
                            "VALUES (?, ?, CURRENT_DATE)"
            );
            ps3.setInt(1, userId);
            ps3.setDouble(2, weight);
            ps3.executeUpdate();

            conn.close();

            return "redirect:/dashboard.html";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/landingPage.html";
    }
}