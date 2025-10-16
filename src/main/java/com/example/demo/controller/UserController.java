package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.Membership;
import com.example.demo.model.LoginUser;
import com.example.demo.service.UserService;
import com.example.demo.service.MembershipService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MembershipService membershipService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users/list";
    }

    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (userService.isEmailExists(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
                return "redirect:/users/create";
            }

            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được thêm thành công!");
            return "redirect:/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm người dùng: " + e.getMessage());
            return "redirect:/users/create";
        }
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("memberships", membershipService.getAllMemberships());
                    return "users/detail";
                })
                .orElse("redirect:/users");
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "users/edit";
                })
                .orElse("redirect:/users");
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             RedirectAttributes redirectAttributes) {
        try {
            user.setId(id);
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được cập nhật thành công!");
            return "redirect:/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật người dùng: " + e.getMessage());
            return "redirect:/users/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/ban")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.banUser(id);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/{id}/deposit")
    public String depositForm(@PathVariable Long id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "users/deposit";
                })
                .orElse("redirect:/users");
    }

    @PostMapping("/{id}/deposit")
    public String depositMoney(@PathVariable Long id,
                               @RequestParam Double amount,
                               RedirectAttributes redirectAttributes) {
        try {
            if (amount <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số tiền phải lớn hơn 0!");
                return "redirect:/users/" + id + "/deposit";
            }

            boolean success = userService.depositMoney(id, amount);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Nạp tiền thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng!");
            }
            return "redirect:/users/" + id + "/deposit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi nạp tiền: " + e.getMessage());
            return "redirect:/users/" + id + "/deposit";
        }
    }

    @PostMapping("/{id}/membership")
    public String purchaseMembership(@PathVariable Long id,
                                     @RequestParam Long membershipId,
                                     RedirectAttributes redirectAttributes) {
        try {
            boolean success = userService.purchaseMembership(id, membershipId);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Mua gói membership thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không đủ số dư hoặc gói membership không hợp lệ!");
            }
            return "redirect:/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi mua membership: " + e.getMessage());
            return "redirect:/users/" + id;
        }
    }


    /*
     * Profile
     * */
    @GetMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {
        LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        if (!"USER".equals(loginUser.getRole())) {
            return "redirect:/dashboard";
        }

        User user = userService.getUserById(loginUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        model.addAttribute("user", user);

        return "users/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phone,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        com.example.demo.model.LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        try {
            User updatedUser = userService.updateUserProfile(loginUser.getId(), name, phone);
            loginUser.setName(updatedUser.getName());
            session.setAttribute("loginUser", loginUser);
            redirectAttributes.addFlashAttribute("success", "Thông tin cá nhân đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật thông tin: " + e.getMessage());
        }

        return "redirect:/users/profile";
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        com.example.demo.model.LoginUser loginUser = (LoginUser) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/users/profile";
        }

        try {
            userService.changePassword(loginUser.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được thay đổi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể đổi mật khẩu: " + e.getMessage());
        }

        return "redirect:/users/profile";
    }
}
