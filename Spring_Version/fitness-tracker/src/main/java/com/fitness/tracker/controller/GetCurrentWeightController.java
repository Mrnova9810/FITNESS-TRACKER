package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class GetCurrentWeightController {

    @GetMapping("/getCurrentWeight")
    public String getCurrentWeight(HttpSession session) {

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return "--";
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT weight FROM weight WHERE user_id=? ORDER BY date DESC LIMIT 1"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double weight = rs.getDouble("weight");
                con.close();
                return String.valueOf(weight); // keep frontend same
            }

            con.close();
            return "--";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "--";
    }
}