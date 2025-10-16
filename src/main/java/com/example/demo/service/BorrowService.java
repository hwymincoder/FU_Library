package com.example.demo.service;

import com.example.demo.entity.BorrowRecord;
import com.example.demo.entity.User;
import com.example.demo.entity.Book;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.entity.Membership;
import com.example.demo.repository.BorrowRecordRepository;
import com.example.demo.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BorrowService {

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    private static final double DAILY_FINE_RATE = 30000.0; // 30k VND per day per book

    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordRepository.findAll();
    }

    public Optional<BorrowRecord> getBorrowRecordById(Long id) {
        return borrowRecordRepository.findById(id);
    }

    public List<BorrowRecord> getUserBorrowRecords(Long userId) {
        return borrowRecordRepository.findByUserId(userId);
    }

    public List<BorrowRecord> getActiveBorrowsByUser(Long userId) {
        return borrowRecordRepository.findActiveBorrowsByUser(userId);
    }

    public List<BorrowRecord> getOverdueBorrows() {
        return borrowRecordRepository.findOverdueBorrows(LocalDateTime.now());
    }

    @Transactional
    public boolean borrowBook(Long userId, Long bookId, LocalDateTime dueDate) {
        Optional<User> userOpt = userService.getUserById(userId);
        Optional<Book> bookOpt = bookService.getBookById(bookId);

        if (userOpt.isPresent() && bookOpt.isPresent()) {
            User user = userOpt.get();
            Book book = bookOpt.get();

            // Kiểm tra điều kiện mượn sách
            if (!canUserBorrowBook(user, book)) {
                return false;
            }

            // Tạo borrow record
            BorrowRecord borrowRecord = new BorrowRecord(user, book, LocalDateTime.now(), dueDate);
            borrowRecordRepository.save(borrowRecord);

            // Giảm số lượng sách có sẵn
            bookService.decreaseAvailableCopies(bookId);

            return true;
        }
        return false;
    }

    @Transactional
    public boolean returnBook(Long borrowRecordId) {
        Optional<BorrowRecord> borrowOpt = borrowRecordRepository.findById(borrowRecordId);

        if (borrowOpt.isPresent()) {
            BorrowRecord borrowRecord = borrowOpt.get();

            if (borrowRecord.getStatus() == BorrowRecord.Status.BORROWED) {
                // Cập nhật trạng thái
                borrowRecord.setStatus(BorrowRecord.Status.RETURNED);
                borrowRecord.setReturnDate(LocalDateTime.now());

                // Tính phạt nếu trả muộn
                double fineAmount = calculateFine(borrowRecord);
                borrowRecord.setFineAmount(fineAmount);

                borrowRecordRepository.save(borrowRecord);

                // Tăng số lượng sách có sẵn
                bookService.increaseAvailableCopies(borrowRecord.getBook().getId());

                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean payFine(Long borrowRecordId) {
        Optional<BorrowRecord> borrowOpt = borrowRecordRepository.findById(borrowRecordId);

        if (borrowOpt.isPresent()) {
            BorrowRecord borrowRecord = borrowOpt.get();
            User user = borrowRecord.getUser();

            if (borrowRecord.getFineAmount() > 0 && user.getBalance() >= borrowRecord.getFineAmount()) {
                // Trừ tiền từ tài khoản
                user.setBalance(user.getBalance() - borrowRecord.getFineAmount());
                userService.saveUser(user);

                // Tạo transaction record
                PaymentTransaction transaction = new PaymentTransaction(
                        user,
                        PaymentTransaction.TransactionType.FINE_PAYMENT,
                        borrowRecord.getFineAmount(),
                        "Thanh toán phạt trả muộn cho sách: " + borrowRecord.getBook().getTitle());
                paymentTransactionRepository.save(transaction);

                // Reset fine amount
                borrowRecord.setFineAmount(0.0);
                borrowRecordRepository.save(borrowRecord);

                return true;
            }
        }
        return false;
    }

    private boolean canUserBorrowBook(User user, Book book) {
        // Kiểm tra user có bị ban không
        if (user.getIsBanned()) {
            return false;
        }

        // Kiểm tra sách có sẵn không
        if (book.getAvailableCopies() <= 0) {
            return false;
        }

        // Kiểm tra membership
        if (user.getMembership() == null || user.getMembershipExpiresAt() == null ||
                user.getMembershipExpiresAt().isBefore(LocalDateTime.now())) {
            return false; // Không có membership hoặc đã hết hạn
        }

        // Kiểm tra số sách đã mượn trong tháng
        List<BorrowRecord> activeBorrows = getActiveBorrowsByUser(user.getId());
        Membership membership = user.getMembership();

        if (membership.getMaxBooksPerMonth() != null &&
                activeBorrows.size() >= membership.getMaxBooksPerMonth()) {
            return false; // Đã mượn đủ số sách cho phép
        }

        return true;
    }

    private double calculateFine(BorrowRecord borrowRecord) {
        if (borrowRecord.getReturnDate().isAfter(borrowRecord.getDueDate())) {
            long daysLate = java.time.Duration.between(
                    borrowRecord.getDueDate(),
                    borrowRecord.getReturnDate()).toDays();
            return daysLate * DAILY_FINE_RATE;
        }
        return 0.0;
    }

    @Transactional
    public void updateOverdueStatus() {
        List<BorrowRecord> overdueBorrows = getOverdueBorrows();
        for (BorrowRecord borrowRecord : overdueBorrows) {
            borrowRecord.setStatus(BorrowRecord.Status.OVERDUE);
            borrowRecordRepository.save(borrowRecord);
        }
    }
}
