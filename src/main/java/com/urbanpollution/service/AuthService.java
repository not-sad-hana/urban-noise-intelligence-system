package com.urbanpollution.service;

import com.urbanpollution.dto.AuthResponse;
import com.urbanpollution.dto.LoginRequest;
import com.urbanpollution.dto.SignupRequest;
import com.urbanpollution.model.Role;
import com.urbanpollution.model.User;
import com.urbanpollution.repository.UserRepository;
import com.urbanpollution.security.CustomUserDetailsService;
import com.urbanpollution.security.JwtTokenUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Initializes default demo users if not already present.
     * Also updates passwords for existing users to ensure consistency.
     */
    @PostConstruct
    @Transactional
    public void initDefaultUsers() {
        // Always ensure users have the correct password encoding
        // This handles both fresh installs and existing databases
        
        userRepository.findByUsername("citizen").ifPresentOrElse(
            user -> {
                user.setPasswordHash(passwordEncoder.encode("password123"));
                user.setRole(Role.CITIZEN);
                user.setFullName("Ramapuram Resident");
                userRepository.save(user);
                logger.info("User 'citizen' password updated: 'password123' (Role: CITIZEN)");
            },
            () -> {
                User citizen = new User("citizen", passwordEncoder.encode("password123"), Role.CITIZEN, "Ramapuram Resident");
                userRepository.save(citizen);
                logger.info("Default user created: 'citizen' / 'password123' (Role: CITIZEN)");
            }
        );

        userRepository.findByUsername("authority").ifPresentOrElse(
            user -> {
                user.setPasswordHash(passwordEncoder.encode("admin123"));
                user.setRole(Role.AUTHORITY);
                user.setFullName("TNPCB Ward Officer");
                userRepository.save(user);
                logger.info("User 'authority' password updated: 'admin123' (Role: AUTHORITY)");
            },
            () -> {
                User authority = new User("authority", passwordEncoder.encode("admin123"), Role.AUTHORITY, "TNPCB Ward Officer");
                userRepository.save(authority);
                logger.info("Default user created: 'authority' / 'admin123' (Role: AUTHORITY)");
            }
        );
    }

    /**
     * Authenticates existing user and generates JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails, user.getRole().name());

        return new AuthResponse(token, user.getUsername(), user.getRole(), user.getFullName(), "Login successful");
    }

    /**
     * Registers a new user with chosen role (CITIZEN or AUTHORITY).
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.CITIZEN;
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = new User(request.getUsername(), encodedPassword, role, request.getFullName());
        User savedUser = userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails, savedUser.getRole().name());

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole(), savedUser.getFullName(), "User registered successfully");
    }

    /**
     * Retrieves currently logged in user info.
     */
    public User getProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
