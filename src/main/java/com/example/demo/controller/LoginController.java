package com.example.demo.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.LoginUser;
import com.example.demo.service.ManagerService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private ManagerService managerService;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        // Nếu đã đăng nhập thì redirect về trang tương ứng
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser != null) {
            if (loginUser.isManager()) {
                return "redirect:/manager/dashboard";
            } else {
                return "redirect:/user/home";
            }
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra Manager trước
        Optional<com.example.demo.entity.Manager> managerOpt = managerService.findByEmail(email);
        if (managerOpt.isPresent() && managerOpt.get().getPassword().equals(password)) {
            LoginUser loginUser = new LoginUser(managerOpt.get());
            session.setAttribute("loginUser", loginUser);
            return "redirect:/manager/dashboard";
        }

        // Kiểm tra User
        Optional<com.example.demo.entity.User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            // Kiểm tra user có bị ban không
            if (userOpt.get().getIsBanned()) {
                redirectAttributes.addFlashAttribute("error", "Tài khoản của bạn đã bị khóa!");
                return "redirect:/login";
            }
            LoginUser loginUser = new LoginUser(userOpt.get());
            session.setAttribute("loginUser", loginUser);
            return "redirect:/user/home";
        }

        // Đăng nhập thất bại
        redirectAttributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng!");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loginUser");
        return "redirect:/login";
    }
}
