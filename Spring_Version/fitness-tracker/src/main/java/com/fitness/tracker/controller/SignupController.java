package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;

@Controller
public class SignupController {

    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         HttpSession session,
                         HttpServletResponse response) {

        try {
            username = username.trim();
            email = email.trim();
            password = password.trim();

            Connection con = DBConnection.getConnection();

            // 🔐 Hash password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            // 🔥 Insert + get generated ID
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(username, email, password) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            int result = ps.executeUpdate();

            if (result > 0) {

                ResultSet rs = ps.getGeneratedKeys();
                int userId = -1;

                if (rs.next()) {
                    userId = rs.getInt(1);
                }

                // 🔥 Create session
                session.setAttribute("user_id", userId);
                session.setAttribute("username", username);

                // 🔥 ADD COOKIE (FIXED)
                Cookie cookie = new Cookie("user_id", String.valueOf(userId));
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                cookie.setPath("/");
                response.addCookie(cookie);

                con.close();

                return "redirect:/setup.html";
            }

            con.close();
            return "redirect:/signup.html?error=true";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/signup.html?error=true";
    }
}