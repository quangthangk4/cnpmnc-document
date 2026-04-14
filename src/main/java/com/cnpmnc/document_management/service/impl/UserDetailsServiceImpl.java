package com.cnpmnc.document_management.service.impl;

import com.cnpmnc.document_management.entity.User;
import com.cnpmnc.document_management.exception.AppException;
import com.cnpmnc.document_management.exception.ErrorCode;
import com.cnpmnc.document_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByIdWithRoles(username).orElseThrow(
                () -> new BadCredentialsException("" ,new AppException(ErrorCode.USER_NOT_FOUND))
        );

        return new UserDetailsImpl(user);
    }
}
