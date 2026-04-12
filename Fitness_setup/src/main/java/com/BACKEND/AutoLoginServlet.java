package com.BACKEND;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/autoLogin")
public class AutoLoginServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("user_id".equals(c.getName())) {

                    // 🔥 recreate session
                    HttpSession session = request.getSession();
                    session.setAttribute("user_id", Integer.parseInt(c.getValue()));

                    response.sendRedirect("dashboard.html");
                    return;
                }
            }
        }

        // if no cookie
        response.sendRedirect("landingPage.html");
    }
}