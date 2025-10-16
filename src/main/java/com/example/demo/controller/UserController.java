package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.service.MembershipService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/manager/users")
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
                return "redirect:/manager/users/create";
            }

            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được thêm thành công!");
            return "redirect:/manager/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm người dùng: " + e.getMessage());
            return "redirect:/manager/users/create";
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
                .orElse("redirect:/manager/users");
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "users/edit";
                })
                .orElse("redirect:/manager/users");
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
            @ModelAttribute User user,
            RedirectAttributes redirectAttributes) {
        try {
            user.setId(id);
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Người dùng đã được cập nhật thành công!");
            return "redirect:/manager/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật người dùng: " + e.getMessage());
            return "redirect:/manager/users/" + id + "/edit";
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
        return "redirect:/manager/users";
    }

    @GetMapping("/{id}/deposit")
    public String depositForm(@PathVariable Long id, Model model) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    return "users/deposit";
                })
                .orElse("redirect:/manager/users");
    }

    @PostMapping("/{id}/deposit")
    public String depositMoney(@PathVariable Long id,
            @RequestParam Double amount,
            RedirectAttributes redirectAttributes) {
        try {
            if (amount <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số tiền phải lớn hơn 0!");
                return "redirect:/manager/users/" + id + "/deposit";
            }

            boolean success = userService.depositMoney(id, amount);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Nạp tiền thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng!");
            }
            return "redirect:/manager/users/" + id + "/deposit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi nạp tiền: " + e.getMessage());
            return "redirect:/manager/users/" + id + "/deposit";
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
            return "redirect:/manager/users/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi mua membership: " + e.getMessage());
            return "redirect:/manager/users/" + id;
        }
    }
}
