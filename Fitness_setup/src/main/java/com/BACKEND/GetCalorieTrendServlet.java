package com.BACKEND;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/getCalorieTrend")
public class GetCalorieTrendServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id"); // ✅ use user_id

        if (userId == null) {
            response.sendRedirect("index.html");
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

            PreparedStatement ps = con.prepareStatement(
                    "SELECT DATE(time) as day, SUM(calories) as total " +
                            "FROM calories WHERE user_id=? " +
                            "GROUP BY DATE(time) ORDER BY day ASC LIMIT 7"
            );

            ps.setInt(1, userId); // ✅ bind user_id

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (!first) json.append(",");

                json.append("{")
                        .append("\"date\":\"").append(rs.getString("day")).append("\",")
                        .append("\"calories\":").append(rs.getInt("total"))
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