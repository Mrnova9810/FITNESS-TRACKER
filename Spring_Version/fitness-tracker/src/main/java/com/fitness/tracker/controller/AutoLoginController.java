package com.fitness.tracker.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutoLoginController {

    @GetMapping("/autoLogin")
    public String autoLogin(jakarta.servlet.http.HttpServletRequest request,
                            HttpSession session) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie c : cookies) {

                if ("user_id".equals(c.getName())) {

                    try {
                        int userId = Integer.parseInt(c.getValue());

                        // 🔥 recreate session
                        session.setAttribute("user_id", userId);

                        return "redirect:/dashboard.html";

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // ❌ no valid cookie
        return "redirect:/landingPage.html";
    }
}