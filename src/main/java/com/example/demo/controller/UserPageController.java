package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Book;
import com.example.demo.entity.BorrowRecord;
import com.example.demo.entity.Cart;
import com.example.demo.entity.User;
import com.example.demo.model.LoginUser;
import com.example.demo.service.BookService;
import com.example.demo.service.BorrowService;
import com.example.demo.service.CartService;
import com.example.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserPageController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BorrowService borrowService;

    @GetMapping("/home")
    public String home(Model model, HttpSession session, @RequestParam(required = false) String search) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        
        if (loginUser == null || !loginUser.isUser()) {
            return "redirect:/login";
        }

        // Lấy thông tin user
        Optional<User> userOpt = userService.getUserById(loginUser.getId());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);

        // Lấy danh sách sách
        List<Book> books;
        if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooksByTitle(search);
            model.addAttribute("search", search);
        } else {
            books = bookService.getAvailableBooks();
        }
        model.addAttribute("books", books);

        // Số lượng sách trong giỏ
        model.addAttribute("cartCount", cartService.getCartCount(loginUser.getId()));

        return "user/home";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        
        if (loginUser == null || !loginUser.isUser()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.getUserById(loginUser.getId());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);

        // Lấy lịch sử mượn sách
        List<BorrowRecord> borrowRecords = borrowService.getUserBorrowRecords(loginUser.getId());
        model.addAttribute("borrowRecords", borrowRecords);

        // Lấy các sách đang mượn
        List<BorrowRecord> activeBorrows = borrowService.getActiveBorrowsByUser(loginUser.getId());
        model.addAttribute("activeBorrows", activeBorrows);

        return "user/profile";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long bookId, HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        
        if (loginUser == null || !loginUser.isUser()) {
            return "redirect:/login";
        }

        boolean success = cartService.addToCart(loginUser.getId(), bookId);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Đã thêm sách vào giỏ!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Sách đã có trong giỏ hoặc không tồn tại!");
        }

        return "redirect:/user/home";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        
        if (loginUser == null || !loginUser.isUser()) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.getUserById(loginUser.getId());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);

        List<Cart> cartItems = cartService.getUserCart(loginUser.getId());
        model.addAttribute("cartItems", cartItems);

        return "user/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long cartId, RedirectAttributes redirectAttributes) {
        boolean success = cartService.removeFromCart(cartId);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Đã xóa sách khỏi giỏ!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa sách!");
        }

        return "redirect:/user/cart";
    }

    @PostMapping("/cart/confirm")
    public String confirmBorrow(HttpSession session, RedirectAttributes redirectAttributes) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        
        if (loginUser == null || !loginUser.isUser()) {
            return "redirect:/login";
        }

        List<Cart> cartItems = cartService.getUserCart(loginUser.getId());
        
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ của bạn đang trống!");
            return "redirect:/user/cart";
        }

        Optional<User> userOpt = userService.getUserById(loginUser.getId());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();

        // Kiểm tra membership
        if (user.getMembership() == null || user.getMembershipExpiresAt() == null ||
                user.getMembershipExpiresAt().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần có membership để mượn sách!");
            return "redirect:/user/cart";
        }

        // Kiểm tra số sách đã mượn
        List<BorrowRecord> activeBorrows = borrowService.getActiveBorrowsByUser(loginUser.getId());
        int remainingSlots = user.getMembership().getMaxBooksPerMonth() - activeBorrows.size();

        if (cartItems.size() > remainingSlots) {
            redirectAttributes.addFlashAttribute("error", 
                "Bạn chỉ còn có thể mượn " + remainingSlots + " cuốn sách nữa!");
            return "redirect:/user/cart";
        }

        // Tính due date (14 ngày)
        LocalDateTime dueDate = LocalDateTime.now().plusDays(14);

        int successCount = 0;
        for (Cart cartItem : cartItems) {
            boolean success = borrowService.borrowBook(loginUser.getId(), cartItem.getBook().getId(), dueDate);
            if (success) {
                successCount++;
            }
        }

        // Xóa giỏ hàng
        cartService.clearUserCart(loginUser.getId());

        if (successCount > 0) {
            redirectAttributes.addFlashAttribute("success", 
                "Đã mượn thành công " + successCount + " cuốn sách!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể mượn sách. Vui lòng thử lại!");
        }

        return "redirect:/user/profile";
    }
}

