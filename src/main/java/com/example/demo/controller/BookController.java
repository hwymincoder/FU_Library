package com.example.demo.controller;

import com.example.demo.entity.Book;
import com.example.demo.entity.Author;
import com.example.demo.service.BookService;
import com.example.demo.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @GetMapping
    public String listBooks(Model model, @RequestParam(required = false) String search) {
        List<Book> books;
        if (search != null && !search.trim().isEmpty()) {
            books = bookService.searchBooksByTitle(search);
        } else {
            books = bookService.getAllBooks();
        }
        model.addAttribute("books", books);
        model.addAttribute("search", search);
        return "books/list";
    }

    @GetMapping("/available")
    public String availableBooks(Model model) {
        model.addAttribute("books", bookService.getAvailableBooks());
        return "books/available";
    }

    @GetMapping("/create")
    public String createBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorService.getAllAuthors());
        return "books/create";
    }

    @PostMapping("/create")
    public String createBook(@ModelAttribute Book book,
            @RequestParam(required = false) List<Long> authorIds,
            RedirectAttributes redirectAttributes) {
        try {
            Book savedBook = bookService.createBook(
                    book.getTitle(),
                    book.getIsbn(),
                    book.getDescription(),
                    book.getTotalCopies(),
                    authorIds);
            redirectAttributes.addFlashAttribute("success", "Sách đã được thêm thành công!");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm sách: " + e.getMessage());
            return "redirect:/books/create";
        }
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "books/detail";
                })
                .orElse("redirect:/books");
    }

    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("authors", authorService.getAllAuthors());
                    return "books/edit";
                })
                .orElse("redirect:/books");
    }

    @PostMapping("/{id}/edit")
    public String updateBook(@PathVariable Long id,
            @ModelAttribute Book book,
            @RequestParam(required = false) List<Long> authorIds,
            RedirectAttributes redirectAttributes) {
        try {
            book.setId(id);
            if (authorIds != null && !authorIds.isEmpty()) {
                List<Author> authors = authorService.getAllAuthors().stream()
                        .filter(author -> authorIds.contains(author.getId()))
                        .toList();
                book.setAuthors(authors);
            }
            bookService.saveBook(book);
            redirectAttributes.addFlashAttribute("success", "Sách đã được cập nhật thành công!");
            return "redirect:/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sách: " + e.getMessage());
            return "redirect:/books/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Sách đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa sách: " + e.getMessage());
        }
        return "redirect:/books";
    }
}
