package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GetProfileController {

    @GetMapping("/getProfile")
    public Map<String, Object> getProfile(HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Integer userId = (Integer) session.getAttribute("user_id");

            if (userId == null) {
                response.put("status", "error");
                response.put("message", "session expired");
                return response;
            }

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT u.username, u.email, " +
                            "up.age, up.height, up.goal, up.profile_pic " +
                            "FROM users u " +
                            "LEFT JOIN user_profile up ON u.id = up.user_id " +
                            "WHERE u.id = ?"
            );

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                response.put("status", "success");
                response.put("username", safe(rs.getString("username")));
                response.put("email", safe(rs.getString("email")));
                response.put("goal", safe(rs.getString("goal")));
                response.put("age", rs.getInt("age"));
                response.put("height", rs.getInt("height"));
                response.put("profilePic", safe(rs.getString("profile_pic")));

            } else {
                response.put("status", "error");
                response.put("message", "user not found");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "server error");
        }

        return response;
    }

    private String safe(String value) {
        if (value == null) return "";
        return value.replace("\"", "").replace("\n", "").replace("\r", "");
    }
}