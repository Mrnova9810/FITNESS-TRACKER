package com.BACKEND;

import java.io.IOException;
import java.sql.Connection;              // ✅ ADD
import java.sql.DriverManager;          // ✅ ADD
import java.sql.PreparedStatement;      // ✅ ADD

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/deleteWeight")
public class DeleteWeightServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("user_id"); // ✅ user_id from session

        if (userId == null) {
            response.sendRedirect("index.html");
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
                    "DELETE FROM weight WHERE id=? AND user_id=?"
            );

            ps.setInt(1, id);
            ps.setInt(2, userId);

            ps.executeUpdate();

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}