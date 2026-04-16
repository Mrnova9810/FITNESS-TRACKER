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
public class GetWeightHistoryController {

    @GetMapping("/getWeightHistory")
    public List<Map<String, Object>> getWeightHistory(HttpSession session) {

        List<Map<String, Object>> list = new ArrayList<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                return list; // []
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT id, date, weight\n" +
                            "FROM weight\n" +
                            "WHERE user_id = ?\n" +
                            "ORDER BY date DESC"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("id", rs.getInt("id"));
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