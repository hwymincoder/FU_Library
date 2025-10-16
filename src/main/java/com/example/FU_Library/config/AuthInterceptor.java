package com.example.demo.config;

import com.example.demo.model.LoginUser;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");

        String requestURI = request.getRequestURI();

        // Cho phép truy cập trang login mà không cần đăng nhập
        if (requestURI.equals("/login") || requestURI.startsWith("/css/") || requestURI.startsWith("/js/")
                || requestURI.startsWith("/images/")) {
            return true;
        }

        // Nếu chưa đăng nhập, redirect về trang login
        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
