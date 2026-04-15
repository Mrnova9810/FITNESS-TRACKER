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
public class GetCalorieTrendController {

    @GetMapping("/getCalorieTrend")
    public List<Map<String, Object>> getCalorieTrend(HttpSession session) {

        List<Map<String, Object>> list = new ArrayList<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return list;
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT DATE(time) as day, SUM(calories) as total " +
                            "FROM calories WHERE user_id=? " +
                            "GROUP BY DATE(time) ORDER BY day ASC LIMIT 7"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("date", rs.getString("day"));
                row.put("calories", rs.getInt("total"));

                list.add(row);
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}