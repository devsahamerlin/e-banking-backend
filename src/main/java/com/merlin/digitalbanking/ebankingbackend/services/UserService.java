package com.merlin.digitalbanking.ebankingbackend.services;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import java.util.List;

public interface UserService {
    UserDTO createUser(CreateUserDTO createUserDTO);
    UserDTO updateUser(UpdateUserDTO updateUserDTO);
    void deleteUser(Long userId);
    UserDTO getUser(Long userId);
    List<UserDTO> getAllUsers();
    List<UserDTO> searchUsers(String keyword);
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
    void activateUser(Long userId);
    void deactivateUser(Long userId);
    UserDTO getCurrentUser();
    UserDTO findByUsername(String username);
}