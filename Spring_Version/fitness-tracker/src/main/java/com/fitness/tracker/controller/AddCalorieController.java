package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Controller
public class AddCalorieController {

    @PostMapping("/addCalorie")
    public String addCalorie(@RequestParam String food,
                             @RequestParam int calories,  HttpSession session) {
        Integer userId = (Integer) session.getAttribute("user_id"); // ✅ user_id from session

        if (userId == null) {
            return "redirect:/landingPage.html";
        }

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO calories(user_id, food, calories, time) \n" +
                            "VALUES (?, ?, ?, CURRENT_TIMESTAMP)"
            );

            ps.setInt(1, userId); // 🔥 TEMP user_id
            ps.setString(2, food);
            ps.setInt(3, calories);

            ps.executeUpdate();
            con.close();

            return "redirect:/calorie.html";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/error.html";
    }
}