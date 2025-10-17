package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Book;
import com.example.demo.entity.Cart;
import com.example.demo.entity.User;
import com.example.demo.repository.CartRepository;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    public List<Cart> getUserCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public boolean addToCart(Long userId, Long bookId) {
        Optional<User> userOpt = userService.getUserById(userId);
        Optional<Book> bookOpt = bookService.getBookById(bookId);

        if (userOpt.isPresent() && bookOpt.isPresent()) {
            // Kiểm tra xem sách đã có trong giỏ chưa
            Optional<Cart> existingCart = cartRepository.findByUserIdAndBookId(userId, bookId);
            if (existingCart.isPresent()) {
                return false; // Đã có trong giỏ
            }

            Cart cart = new Cart(userOpt.get(), bookOpt.get());
            cartRepository.save(cart);
            return true;
        }
        return false;
    }

    public boolean removeFromCart(Long cartId) {
        if (cartRepository.existsById(cartId)) {
            cartRepository.deleteById(cartId);
            return true;
        }
        return false;
    }

    public void clearUserCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    public int getCartCount(Long userId) {
        return cartRepository.findByUserId(userId).size();
    }
}


