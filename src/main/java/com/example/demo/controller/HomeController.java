package com.example.demo.controller;

import com.example.demo.model.LoginUser;
import com.example.demo.service.BookService;
import com.example.demo.service.UserService;
import com.example.demo.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowService borrowService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        model.addAttribute("availableBooks", bookService.getAvailableBooks().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("activeBorrows", borrowService.getAllBorrowRecords().stream()
                .filter(br -> br.getStatus() == com.example.demo.entity.BorrowRecord.Status.BORROWED)
                .count());
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        return home(model, session);
    }
}
