package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.BorrowRecord;
import com.example.demo.service.BookService;
import com.example.demo.service.BorrowService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/manager/borrows")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public String listBorrows(Model model) {
        model.addAttribute("borrows", borrowService.getAllBorrowRecords());
        return "borrows/list";
    }

    @GetMapping("/active")
    public String activeBorrows(Model model) {
        model.addAttribute("borrows", borrowService.getAllBorrowRecords().stream()
                .filter(br -> br.getStatus() == BorrowRecord.Status.BORROWED)
                .toList());
        return "borrows/active";
    }

    @GetMapping("/overdue")
    public String overdueBorrows(Model model) {
        model.addAttribute("borrows", borrowService.getOverdueBorrows());
        return "borrows/overdue";
    }

    @GetMapping("/create")
    public String borrowForm(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("availableBooks", bookService.getAvailableBooks());
        return "borrows/create";
    }

    @PostMapping("/create")
    public String borrowBook(@RequestParam Long userId,
            @RequestParam Long bookId,
            @RequestParam String dueDateStr,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime dueDate = LocalDateTime.parse(dueDateStr);
            boolean success = borrowService.borrowBook(userId, bookId, dueDate);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Mượn sách thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Không thể mượn sách! Kiểm tra điều kiện membership và số dư tài khoản.");
            }
            return "redirect:/manager/borrows";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi mượn sách: " + e.getMessage());
            return "redirect:/manager/borrows/create";
        }
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = borrowService.returnBook(id);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Trả sách thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể trả sách!");
            }
            return "redirect:/manager/borrows";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi trả sách: " + e.getMessage());
            return "redirect:/manager/borrows";
        }
    }

    @PostMapping("/{id}/pay-fine")
    public String payFine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = borrowService.payFine(id);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Thanh toán phạt thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không đủ số dư để thanh toán phạt!");
            }
            return "redirect:/manager/borrows";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thanh toán phạt: " + e.getMessage());
            return "redirect:/manager/borrows";
        }
    }

    @GetMapping("/user/{userId}")
    public String userBorrows(@PathVariable Long userId, Model model) {
        model.addAttribute("borrows", borrowService.getUserBorrowRecords(userId));
        model.addAttribute("user", userService.getUserById(userId).orElse(null));
        return "borrows/user-borrows";
    }
}
