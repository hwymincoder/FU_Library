package com.example.demo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.model.LoginUser;

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

        // Cho phép truy cập trang login và static resources mà không cần đăng nhập
        if (requestURI.equals("/login") || requestURI.startsWith("/css/") || requestURI.startsWith("/js/")
                || requestURI.startsWith("/images/")) {
            return true;
        }

        // Nếu chưa đăng nhập, redirect về trang login
        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Kiểm tra phân quyền theo role
        if (requestURI.startsWith("/manager/")) {
            // Chỉ Manager mới được truy cập /manager/*
            if (!loginUser.isManager()) {
                response.sendRedirect("/user/home");
                return false;
            }
        } else if (requestURI.startsWith("/user/")) {
            // Chỉ User mới được truy cập /user/*
            if (!loginUser.isUser()) {
                response.sendRedirect("/manager/dashboard");
                return false;
            }
        }
        
        // Redirect cho các path cũ (để tương thích ngược)
        if (requestURI.equals("/") || requestURI.equals("/dashboard")) {
            if (loginUser.isManager()) {
                response.sendRedirect("/manager/dashboard");
            } else {
                response.sendRedirect("/user/home");
            }
            return false;
        }

        return true;
    }
}
