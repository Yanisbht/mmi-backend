package com.mmi.service;

import com.mmi.entity.User;
import com.mmi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrPseudo) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrPseudo(emailOrPseudo, emailOrPseudo)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + emailOrPseudo));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}