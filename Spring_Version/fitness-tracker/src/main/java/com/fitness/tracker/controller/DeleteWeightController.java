package com.fitness.tracker.controller;

import com.fitness.tracker.util.DBConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Controller
public class DeleteWeightController {

    @PostMapping("/deleteWeight")
    public String deleteWeight(@RequestParam int id,
                               HttpSession session) {

        Integer userId = (Integer) session.getAttribute("user_id");

        // 🔒 not logged in
        if (userId == null) {
            return "redirect:/landingPage.html";
        }

        try {
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM weight WHERE id=? AND user_id=?"
            );

            ps.setInt(1, id);
            ps.setInt(2, userId);

            ps.executeUpdate();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔁 redirect back
        return "redirect:/weight.html";
    }
}