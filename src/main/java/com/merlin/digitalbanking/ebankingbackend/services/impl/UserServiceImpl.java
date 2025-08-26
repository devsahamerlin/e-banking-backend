package com.merlin.digitalbanking.ebankingbackend.services.impl;

import com.merlin.digitalbanking.ebankingbackend.dto.ChangePasswordDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.CreateUserDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.UpdateUserDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.UserDTO;
import com.merlin.digitalbanking.ebankingbackend.entities.User;
import com.merlin.digitalbanking.ebankingbackend.mappers.BankAccountMapper;
import com.merlin.digitalbanking.ebankingbackend.repositories.UserRepository;
import com.merlin.digitalbanking.ebankingbackend.services.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankAccountMapper bankAccountMapper;

    @Override
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        log.info("Creating user: {}", createUserDTO.username());

        if (userRepository.existsByUsername(createUserDTO.username())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserDTO.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(createUserDTO.username());
        user.setPassword(passwordEncoder.encode(createUserDTO.password()));
        user.setEmail(createUserDTO.email());
        user.setFirstName(createUserDTO.firstName());
        user.setLastName(createUserDTO.lastName());
        user.setRole(createUserDTO.role());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        return bankAccountMapper.fromUser(savedUser);
    }

    @Override
    public UserDTO updateUser(UpdateUserDTO updateUserDTO) {
        log.info("Updating user: {}", updateUserDTO.id());

        User user = userRepository.findById(updateUserDTO.id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(updateUserDTO.email());
        user.setFirstName(updateUserDTO.firstName());
        user.setLastName(updateUserDTO.lastName());
        user.setRole(updateUserDTO.role());
        user.setIsActive(updateUserDTO.isActive());

        User updatedUser = userRepository.save(user);
        return bankAccountMapper.fromUser(updatedUser);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        log.info("Changing password for user: {}", userId);

        if (!changePasswordDTO.newPassword().equals(changePasswordDTO.confirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(changePasswordDTO.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);
        userRepository.deleteById(userId);
    }

    @Override
    public UserDTO getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bankAccountMapper.fromUser(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(bankAccountMapper::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword).stream()
                .map(bankAccountMapper::fromUser)
                .collect(Collectors.toList());
    }

    @Override
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        return bankAccountMapper.fromUser(user);
    }

    @Override
    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return bankAccountMapper.fromUser(user);
    }
}
