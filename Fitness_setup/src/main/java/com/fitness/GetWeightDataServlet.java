package com.fitness;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/getWeightData")
public class GetWeightDataServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id");

        if (userId == null) {
            response.getWriter().print("[]"); // no user logged in, return empty JSON
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
                "SELECT date, weight FROM weight WHERE user_id=? ORDER BY date"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (!first) json.append(",");

                json.append("{")
                    .append("\"date\":\"").append(rs.getString("date")).append("\",")
                    .append("\"weight\":").append(rs.getDouble("weight"))
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