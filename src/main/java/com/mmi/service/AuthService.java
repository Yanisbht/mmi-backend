package com.mmi.service;

import com.mmi.dto.*;
import com.mmi.entity.User;
import com.mmi.enums.Role;
import com.mmi.repository.UserRepository;
import com.mmi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        if (userRepository.existsByPseudo(request.getPseudo())) {
            throw new RuntimeException("Pseudo déjà utilisé");
        }

        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .pseudo(request.getPseudo())
                .classePromo(request.getClassePromo())
                .adresse(request.getAdresse())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telephone(request.getTelephone())
                .matiereEnseignee(request.getMatiereEnseignee())
                .specialisation(request.getSpecialisation())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getRole().name(), user.getPseudo(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        // Chercher par email ou pseudo
        User user = userRepository.findByEmailOrPseudo(request.getIdentifiant(), request.getIdentifiant())
                .orElseThrow(() -> new BadCredentialsException("Identifiant ou mot de passe incorrect"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getRole().name(), user.getPseudo(), user.getId());
    }
}