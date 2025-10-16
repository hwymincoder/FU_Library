package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.Membership;
import com.example.demo.entity.PaymentTransaction;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.MembershipRepository;
import com.example.demo.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void banUser(Long id) {
        User user = userRepository.findById(id).get();
        user.setIsBanned(true);
        userRepository.save(user);
    }

    public void unbanUser(Long id) {
        User user = userRepository.findById(id).get();
        user.setIsBanned(false);
        userRepository.save(user);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getBannedUsers() {
        return userRepository.findByIsBanned(true);
    }

    public List<User> getUsersWithLowBalance(Double amount) {
        return userRepository.findUsersWithLowBalance(amount);
    }

    @Transactional
    public boolean depositMoney(Long userId, Double amount) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setBalance(user.getBalance() + amount);
            userRepository.save(user);

            // Tạo transaction record
            PaymentTransaction transaction = new PaymentTransaction(
                    user,
                    PaymentTransaction.TransactionType.DEPOSIT,
                    amount,
                    "Nạp tiền vào tài khoản");
            paymentTransactionRepository.save(transaction);

            return true;
        }
        return false;
    }

    @Transactional
    public boolean purchaseMembership(Long userId, Long membershipId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Membership> membershipOpt = membershipRepository.findById(membershipId);

        if (userOpt.isPresent() && membershipOpt.isPresent()) {
            User user = userOpt.get();
            Membership membership = membershipOpt.get();

            // Kiểm tra số dư
            if (user.getBalance() >= membership.getPrice()) {
                // Trừ tiền
                user.setBalance(user.getBalance() - membership.getPrice());

                // Cập nhật membership
                user.setMembership(membership);
                user.setMembershipExpiresAt(LocalDateTime.now().plusMonths(1));

                userRepository.save(user);

                // Tạo transaction record
                PaymentTransaction transaction = new PaymentTransaction(
                        user,
                        PaymentTransaction.TransactionType.MEMBERSHIP_PURCHASE,
                        membership.getPrice(),
                        "Mua gói membership: " + membership.getType());
                paymentTransactionRepository.save(transaction);

                return true;
            }
        }
        return false;
    }

    @Transactional
    public void checkAndBanUsers() {
        List<User> usersWithLowBalance = getUsersWithLowBalance(-100.0); // Số dư âm
        for (User user : usersWithLowBalance) {
            user.setIsBanned(true);
            userRepository.save(user);
        }
    }

    public User updateUserProfile(Long userId, String name, String phone) {
        User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id: " + userId));

        user.setName(name);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id: " + userId));

        if (currentPassword == null || !currentPassword.equals(user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }
}
