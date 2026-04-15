package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class GetCalorieTargetController {

    @GetMapping("/getCalorieTarget")
    public int getCalorieTarget(HttpSession session) {

        try {
            if (session == null || session.getAttribute("user_id") == null) {
                return 2200; // fallback
            }

            Integer userId = (Integer) session.getAttribute("user_id");

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT calorie_target FROM user_stats WHERE user_id = ?"
            );
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            int calories = 2200; // default fallback

            if (rs.next()) {
                calories = rs.getInt("calorie_target");
            }

            con.close();

            return calories;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 2200; // fallback on error
    }
}