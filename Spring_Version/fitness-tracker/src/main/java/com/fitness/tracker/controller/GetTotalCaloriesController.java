package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class GetTotalCaloriesController {

    @GetMapping("/getTotalCalories")
    public String getTotalCalories(HttpSession session) {

        int totalCalories = 0;

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return "0";
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT SUM(calories) FROM calories WHERE user_id=? AND DATE(time)=CURDATE()"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                totalCalories = rs.getInt(1);
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.valueOf(totalCalories);
    }
}