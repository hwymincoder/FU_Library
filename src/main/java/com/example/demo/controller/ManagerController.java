package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.model.LoginUser;
import com.example.demo.service.AuthorService;
import com.example.demo.service.BookService;
import com.example.demo.service.BorrowService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private AuthorService authorService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        
        if (loginUser == null || !loginUser.isManager()) {
            return "redirect:/login";
        }

        // Thống kê tổng quan
        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        model.addAttribute("availableBooks", bookService.getAvailableBooks().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalAuthors", authorService.getAllAuthors().size());
        
        long activeBorrows = borrowService.getAllBorrowRecords().stream()
                .filter(br -> br.getStatus() == com.example.demo.entity.BorrowRecord.Status.BORROWED)
                .count();
        model.addAttribute("activeBorrows", activeBorrows);
        
        long overdueBorrows = borrowService.getOverdueBorrows().size();
        model.addAttribute("overdueBorrows", overdueBorrows);

        // Các borrow records gần đây
        model.addAttribute("recentBorrows", borrowService.getAllBorrowRecords().stream()
                .sorted((a, b) -> b.getBorrowDate().compareTo(a.getBorrowDate()))
                .limit(10)
                .toList());

        return "manager/dashboard";
    }
}


