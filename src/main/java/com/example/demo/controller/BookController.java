package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

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

import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.service.AuthorService;
import com.example.demo.service.BookService;

@Controller
@RequestMapping("/manager/books")
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
            bookService.createBook(
                    book.getTitle(),
                    book.getIsbn(),
                    book.getDescription(),
                    book.getTotalCopies(),
                    authorIds);
            redirectAttributes.addFlashAttribute("success", "Sách đã được thêm thành công!");
            return "redirect:/manager/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm sách: " + e.getMessage());
            return "redirect:/manager/books/create";
        }
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "books/detail";
                })
                .orElse("redirect:/manager/books");
    }

    @GetMapping("/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("authors", authorService.getAllAuthors());
                    // Pre-compute selected author IDs for the view to avoid complex Thymeleaf
                    // expressions
                    List<Long> selectedAuthorIds = book.getAuthors() != null
                            ? book.getAuthors().stream().map(Author::getId).toList()
                            : java.util.List.of();
                    model.addAttribute("selectedAuthorIds", selectedAuthorIds);
                    return "books/edit";
                })
                .orElse("redirect:/manager/books");
    }

    @PostMapping("/{id}/edit")
    public String updateBook(@PathVariable Long id,
            @ModelAttribute Book book,
            @RequestParam(required = false) List<Long> authorIds,
            RedirectAttributes redirectAttributes) {
        try {

            Book existingBook = bookService.getBookById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sách"));

            System.out.println(existingBook.getId());

            // Cập nhật thông tin cơ bản
            existingBook.setTitle(book.getTitle());
            existingBook.setIsbn(book.getIsbn());
            existingBook.setDescription(book.getDescription());

            // Cập nhật số lượng sách một cách an toàn (tránh NullPointerException)
            Integer previousTotal = existingBook.getTotalCopies() != null ? existingBook.getTotalCopies() : 0;
            Integer previousAvailable = existingBook.getAvailableCopies() != null ? existingBook.getAvailableCopies()
                    : 0;
            int borrowedCopies = Math.max(0, previousTotal - previousAvailable);

            Integer newTotal = book.getTotalCopies() != null ? book.getTotalCopies() : previousTotal;
            existingBook.setTotalCopies(newTotal);
            existingBook.setAvailableCopies(Math.max(0, newTotal - borrowedCopies));

            // Cập nhật danh sách tác giả bằng cách load chính xác theo ID đã chọn
            if (authorIds != null && !authorIds.isEmpty()) {
                List<Author> authors = authorService.getAuthorsByIds(authorIds);
                existingBook.setAuthors(authors);
            } else {
                existingBook.setAuthors(java.util.List.of()); // Không có tác giả nào được chọn
            }

            System.out.println(existingBook);
            System.out.println(existingBook.getAuthors());

            bookService.saveBook(existingBook);
            redirectAttributes.addFlashAttribute("success", "Sách đã được cập nhật thành công!");
            return "redirect:/manager/books/" + id;
        } catch (Exception e) {
            System.out.println("sao tao lại ở đây ");
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sách: " + e.getMessage());
            return "redirect:/manager/books/" + id + "/edit";
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
        return "redirect:/manager/books";
    }
}
