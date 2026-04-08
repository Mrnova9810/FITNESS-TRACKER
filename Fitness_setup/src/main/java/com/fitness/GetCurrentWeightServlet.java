package com.fitness;


import java.io.IOException;
import java.sql.Connection;          // ✅ ADD
import java.sql.DriverManager;      // ✅ ADD
import java.sql.PreparedStatement;  // ✅ ADD
import java.sql.ResultSet;          // ✅ ADD

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // ✅ ADD

@WebServlet("/getCurrentWeight")
public class GetCurrentWeightServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id"); // ✅ use user_id


        if (userId == null) {
            response.getWriter().print("--");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fitness_db",
                "root",
                "1234"
            );

            PreparedStatement ps = con.prepareStatement(
                    "SELECT weight FROM weight WHERE user_id=? ORDER BY date DESC LIMIT 1" // ✅ user_id filter
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                response.getWriter().print(rs.getDouble("weight"));
            } else {
                response.getWriter().print("--");
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}