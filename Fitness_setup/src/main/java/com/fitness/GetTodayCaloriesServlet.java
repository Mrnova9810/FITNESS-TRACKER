package com.fitness;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/getTodayCalories")
public class GetTodayCaloriesServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id");
        if (userId == null) {
            response.getWriter().print("[]"); // no user logged in
            return;
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/fitness_db",
                "root",
                "1234"
            );

            // 🔥 FIXED QUERY
            PreparedStatement ps = con.prepareStatement(
                "SELECT id, food, calories, TIME(time) as time " +
                "FROM calories WHERE user_id=? AND DATE(time)=CURDATE() " +
                "ORDER BY time DESC"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (!first) json.append(",");

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"food\":\"").append(rs.getString("food")).append("\",")
                    .append("\"calories\":").append(rs.getInt("calories")).append(",")
                    .append("\"time\":\"").append(rs.getString("time")).append("\"")
                    .append("}");

                first = false;
            }

            json.append("]");
            out.print(json.toString());

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}