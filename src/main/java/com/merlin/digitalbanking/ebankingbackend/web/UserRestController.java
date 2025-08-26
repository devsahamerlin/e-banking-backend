package com.merlin.digitalbanking.ebankingbackend.web;


import com.merlin.digitalbanking.ebankingbackend.dto.ChangePasswordDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.CreateUserDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.UpdateUserDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.UserDTO;
import com.merlin.digitalbanking.ebankingbackend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserRestController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserDTO createUserDTO) {
        UserDTO user = userService.createUser(createUserDTO);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody UpdateUserDTO updateUserDTO) {
        UserDTO user = userService.updateUser(updateUserDTO);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN') or @userService.getCurrentUser().id() == #userId")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String keyword) {
        List<UserDTO> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{userId}/change-password")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN') or @userService.getCurrentUser().id() == #userId")
    public ResponseEntity<Void> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(userId, changePasswordDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
}