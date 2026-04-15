package com.fitness.tracker.controller;


import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@RestController
public class GetWeightDataController {

    @GetMapping("/getWeightData")
    public List<Map<String, Object>> getWeightData(HttpSession session) {

        List<Map<String, Object>> list = new ArrayList<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return list; // returns []
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT date, weight FROM weight WHERE user_id=? ORDER BY date"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("date", rs.getString("date"));
                row.put("weight", rs.getDouble("weight"));

                list.add(row);
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}