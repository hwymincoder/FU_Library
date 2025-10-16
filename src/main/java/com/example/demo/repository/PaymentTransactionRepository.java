package com.example.demo.repository;

import com.example.demo.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByUserId(Long userId);

    List<PaymentTransaction> findByType(PaymentTransaction.TransactionType type);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.user.id = :userId ORDER BY pt.transactionDate DESC")
    List<PaymentTransaction> findUserTransactionsOrderByDate(@Param("userId") Long userId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.transactionDate BETWEEN :startDate AND :endDate")
    List<PaymentTransaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
