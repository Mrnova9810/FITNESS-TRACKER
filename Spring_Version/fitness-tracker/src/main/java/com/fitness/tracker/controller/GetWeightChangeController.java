package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class GetWeightChangeController {

    @GetMapping("/getWeightChange")
    public String getWeightChange(HttpSession session) {

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return "NA";
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT weight\n" +
                            "FROM weight\n" +
                            "WHERE user_id = ?\n" +
                            "ORDER BY \"date\" DESC\n" +
                            "LIMIT 2"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            double latest = 0, previous = 0;
            int count = 0;

            if (rs.next()) {
                latest = rs.getDouble("weight");
                count++;
            }

            if (rs.next()) {
                previous = rs.getDouble("weight");
                count++;
            }

            con.close();

            // 🔥 same logic as before
            if (count < 2) {
                return "NA";
            }

            double change = latest - previous;

            return String.valueOf(change);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "NA";
    }
}