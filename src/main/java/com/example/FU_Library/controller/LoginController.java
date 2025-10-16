package com.example.demo.controller;

import com.example.demo.model.LoginUser;
import com.example.demo.service.UserService;
import com.example.demo.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private ManagerService managerService;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        // Nếu đã đăng nhập thì redirect về dashboard
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/dashboard";
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
            return "redirect:/dashboard";
        }

        // Kiểm tra User
        Optional<com.example.demo.entity.User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            LoginUser loginUser = new LoginUser(userOpt.get());
            session.setAttribute("loginUser", loginUser);
            return "redirect:/dashboard";
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
