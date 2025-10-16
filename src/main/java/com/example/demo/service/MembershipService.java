package com.example.demo.service;

import com.example.demo.entity.Membership;
import com.example.demo.repository.MembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class MembershipService {

    @Autowired
    private MembershipRepository membershipRepository;

    public List<Membership> getAllMemberships() {
        return membershipRepository.findAll();
    }

    public Optional<Membership> getMembershipById(Long id) {
        return membershipRepository.findById(id);
    }

    public Optional<Membership> getMembershipByType(Membership.MembershipType type) {
        return membershipRepository.findByType(type);
    }

    public Membership saveMembership(Membership membership) {
        return membershipRepository.save(membership);
    }

    public void deleteMembership(Long id) {
        membershipRepository.deleteById(id);
    }

    @PostConstruct
    public void initializeDefaultMemberships() {
        // Tạo các gói membership mặc định nếu chưa có
        if (membershipRepository.count() == 0) {
            Membership ordinary = new Membership(
                    Membership.MembershipType.ORDINARY,
                    5, // 5 sách/tháng
                    0.0, // Miễn phí
                    "Gói thường - 5 sách/tháng, miễn phí");

            Membership advance = new Membership(
                    Membership.MembershipType.ADVANCE,
                    10, // 10 sách/tháng
                    20000.0, // 20k VND
                    "Gói nâng cao - 10 sách/tháng, 20k VND");

            Membership premium = new Membership(
                    Membership.MembershipType.PREMIUM,
                    999, // Không giới hạn (sử dụng số lớn để đại diện)
                    100000.0, // 100k VND
                    "Gói cao cấp - Không giới hạn sách/tháng, 100k VND");

            membershipRepository.save(ordinary);
            membershipRepository.save(advance);
            membershipRepository.save(premium);
        }
    }
}
