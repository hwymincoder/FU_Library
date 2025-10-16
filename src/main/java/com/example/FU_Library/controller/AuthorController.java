package com.example.demo.controller;

import com.example.demo.entity.Author;
import com.example.demo.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/authors")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping
    public String listAuthors(Model model, @RequestParam(required = false) String search) {
        List<Author> authors;
        if (search != null && !search.trim().isEmpty()) {
            authors = authorService.searchAuthorsByName(search);
        } else {
            authors = authorService.getAllAuthors();
        }
        model.addAttribute("authors", authors);
        model.addAttribute("search", search);
        return "authors/list";
    }

    @GetMapping("/create")
    public String createAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return "authors/create";
    }

    @PostMapping("/create")
    public String createAuthor(@ModelAttribute Author author, RedirectAttributes redirectAttributes) {
        try {
            authorService.saveAuthor(author);
            redirectAttributes.addFlashAttribute("success", "Tác giả đã được thêm thành công!");
            return "redirect:/authors";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm tác giả: " + e.getMessage());
            return "redirect:/authors/create";
        }
    }

    @GetMapping("/{id}")
    public String viewAuthor(@PathVariable Long id, Model model) {
        return authorService.getAuthorById(id)
                .map(author -> {
                    model.addAttribute("author", author);
                    return "authors/detail";
                })
                .orElse("redirect:/authors");
    }

    @GetMapping("/{id}/edit")
    public String editAuthorForm(@PathVariable Long id, Model model) {
        return authorService.getAuthorById(id)
                .map(author -> {
                    model.addAttribute("author", author);
                    return "authors/edit";
                })
                .orElse("redirect:/authors");
    }

    @PostMapping("/{id}/edit")
    public String updateAuthor(@PathVariable Long id,
            @ModelAttribute Author author,
            RedirectAttributes redirectAttributes) {
        try {
            author.setId(id);
            authorService.saveAuthor(author);
            redirectAttributes.addFlashAttribute("success", "Tác giả đã được cập nhật thành công!");
            return "redirect:/authors/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật tác giả: " + e.getMessage());
            return "redirect:/authors/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            authorService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("success", "Tác giả đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa tác giả: " + e.getMessage());
        }
        return "redirect:/authors";
    }
}
