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

@WebServlet("/getWeightChange")
public class GetWeightChangeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id");

        if (userId == null) {
            response.getWriter().print("0"); // user not logged in
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
                "SELECT weight FROM weight WHERE user_id=? ORDER BY date DESC LIMIT 2"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            double latest = 0, previous = 0;

            if (rs.next()) latest = rs.getDouble("weight");
            if (rs.next()) previous = rs.getDouble("weight");

            double change = latest - previous;

            response.getWriter().print(change);

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}