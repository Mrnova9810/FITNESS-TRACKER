package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UpdateWeightController {

    @PostMapping("/updateWeight")
    public Map<String, String> updateWeight(
            @RequestParam int id,
            @RequestParam double weight,
            HttpSession session
    ) {

        Map<String, String> response = new HashMap<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                response.put("status", "unauthorized");
                return response;
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE weight SET weight=? WHERE id=? AND user_id=?"
            );

            ps.setDouble(1, weight);
            ps.setInt(2, id);
            ps.setInt(3, userId);

            int rows = ps.executeUpdate();

            con.close();

            if (rows == 0) {
                response.put("status", "forbidden");
                return response;
            }

            response.put("status", "success");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.put("status", "error");
        return response;
    }
}