package com.fitness.tracker.controller;

import com.fitness.tracker.service.CalorieService;
import org.springframework.beans.factory.annotation.Autowired;
import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class LoginController {

    @Autowired
    private CalorieService calorieService;

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        jakarta.servlet.http.HttpServletResponse response) {

        try {
            username = username.trim();
            password = password.trim();

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String storedHash = rs.getString("password");

                if (BCrypt.checkpw(password, storedHash)) {
                    int userId = rs.getInt("id");

                    session.setAttribute("user_id", userId);
                    session.setAttribute("username", rs.getString("username"));

                    Cookie cookie = new Cookie("user_id", String.valueOf(userId));
                    cookie.setMaxAge(7 * 24 * 60 * 60);
                    cookie.setPath("/");
                    response.addCookie(cookie);

                    calorieService.recalculateCalories(userId);

                    PreparedStatement ps2 = con.prepareStatement(
                            "SELECT * FROM user_profile WHERE user_id = ?"
                    );
                    ps2.setInt(1, userId);
                    ResultSet rs2 = ps2.executeQuery();

                    if (rs2.next()) {
                        con.close();
                        return "SUCCESS";
                    } else {
                        con.close();
                        return "SETUP";
                    }
                }
            }
                 con.close();
                return "FAIL";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}