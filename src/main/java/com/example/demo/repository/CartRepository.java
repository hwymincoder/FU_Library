package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.book.id = :bookId")
    Optional<Cart> findByUserIdAndBookId(Long userId, Long bookId);
    
    void deleteByUserId(Long userId);
}


