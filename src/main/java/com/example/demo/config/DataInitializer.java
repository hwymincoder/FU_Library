package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired
        private LibraryRepository libraryRepository;

        @Autowired
        private ManagerRepository managerRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AuthorRepository authorRepository;

        @Autowired
        private BookRepository bookRepository;

        @Autowired
        private MembershipRepository membershipRepository;

        @Override
        public void run(String... args) throws Exception {
                initializeData();
        }

        private void initializeData() {
                // Create default library if not exists
                Library library = libraryRepository.findAll().stream()
                                .findFirst()
                                .orElseGet(() -> {
                                        Library defaultLibrary = new Library("Thư viện trung tâm TP.HCM",
                                                        "123 Nguyễn Huệ, Q1, TP.HCM",
                                                        "028-1234-5678");
                                        return libraryRepository.save(defaultLibrary);
                                });

                // Create default manager if not exists
                if (managerRepository.count() == 0) {
                        Manager manager = new Manager(
                                        "Admin Manager",
                                        "admin@library.com",
                                        "admin123",
                                        "0123456789",
                                        library);
                        managerRepository.save(manager);
                }

                // Create sample users if not exists
                if (userRepository.count() == 0) {
                        User user1 = new User("Nguyễn Văn A", "user1@email.com", "123456", "0123456789",
                                        library);
                        user1.setBalance(50000.0);
                        // Gán membership mặc định và hạn
                        membershipRepository.findByType(Membership.MembershipType.ORDINARY).ifPresent(m -> {
                                user1.setMembership(m);
                                user1.setMembershipExpiresAt(LocalDateTime.now().plusMonths(1));
                        });
                        userRepository.save(user1);

                        User user2 = new User("Trần Thị B", "user2@email.com", "123456", "0987654321",
                                        library);
                        user2.setBalance(100000.0);
                        membershipRepository.findByType(Membership.MembershipType.ADVANCE).ifPresent(m -> {
                                user2.setMembership(m);
                                user2.setMembershipExpiresAt(LocalDateTime.now().plusMonths(1));
                        });
                        userRepository.save(user2);
                }

                // Create sample authors if not exists
                if (authorRepository.count() == 0) {
                        Author author1 = new Author("Nguyễn Nhật Ánh",
                                        "Nhà văn Việt Nam nổi tiếng với các tác phẩm viết cho thiếu nhi và thanh thiếu niên",
                                        "Việt Nam");
                        authorRepository.save(author1);

                        Author author2 = new Author("Paulo Coelho",
                                        "Nhà văn Brazil nổi tiếng với tác phẩm 'Nhà giả kim'",
                                        "Brazil");

                        Author author3 = new Author("Haruki Murakami", "Nhà văn Nhật Bản với phong cách viết độc đáo",
                                        "Nhật Bản");
                        authorRepository.save(author2);
                        authorRepository.save(author3);
                }

                // Create sample books if not exists
                if (bookRepository.count() == 0) {
                        Library defaultLibrary = libraryRepository.findAll().get(0);

                        Book book1 = new Book("Tôi thấy hoa vàng trên cỏ xanh", "978-604-1-12345-6",
                                        "Câu chuyện về tuổi thơ và tình bạn", 5, defaultLibrary);
                        bookRepository.save(book1);

                        Book book2 = new Book("Nhà giả kim", "978-604-1-12345-7",
                                        "Câu chuyện về hành trình tìm kiếm ý nghĩa cuộc sống", 3, defaultLibrary);
                        bookRepository.save(book2);

                        Book book3 = new Book("Norwegian Wood", "978-604-1-12345-8",
                                        "Tiểu thuyết về tình yêu và mất mát", 4,
                                        defaultLibrary);
                        bookRepository.save(book3);
                }

                // Initialize memberships (handled by MembershipService @PostConstruct)
        }
}
