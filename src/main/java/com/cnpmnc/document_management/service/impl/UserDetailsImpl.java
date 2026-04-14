package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // Thêm cái này để tránh lỗi "required: no arguments, found: User"
public class UserDetailsImpl implements UserDetails {
    
    private transient User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Duyệt qua Set<Role> trong User để map sang SimpleGrantedAuthority
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getId();
    }

    // Các method bắt buộc của UserDetails (nên trả về true nếu bạn chưa làm logic khóa tài khoản)
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}