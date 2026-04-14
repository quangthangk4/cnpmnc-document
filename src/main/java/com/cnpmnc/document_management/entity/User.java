package com.cnpmnc.document_management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Table(name = "users")
@Entity
@EntityListeners(AuditingEntityListener.class) // Cần cái này để @CreatedDate hoạt động
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Tự động tạo UUID nếu bạn không truyền vào
    private String id;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;

    // ĐÃ XÓA: firstName và lastName bị trùng lặp ở đây
    
    private Long departmentId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createAt;

    @ManyToMany(fetch = FetchType.EAGER) // Thêm EAGER để lấy roles ngay khi load User (giúp fix lỗi Auth)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"), // Đổi thành user_id cho đúng chuẩn naming
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}