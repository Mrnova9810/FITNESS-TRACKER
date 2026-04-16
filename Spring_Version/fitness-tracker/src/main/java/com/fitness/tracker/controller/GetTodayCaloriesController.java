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
public class GetTodayCaloriesController {

    @GetMapping("/getTodayCalories")
    public List<Map<String, Object>> getTodayCalories(HttpSession session) {

        List<Map<String, Object>> list = new ArrayList<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return list; // return empty array []
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT id, food, calories, time::time AS time\n" +
                            "FROM calories\n" +
                            "WHERE user_id = ?\n" +
                            "AND time::date = CURRENT_DATE\n" +
                            "ORDER BY time DESC"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("id", rs.getInt("id"));
                row.put("food", rs.getString("food"));
                row.put("calories", rs.getInt("calories"));
                row.put("time", rs.getString("time"));

                list.add(row);
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}