package com.merlin.digitalbanking.ebankingbackend.mappers;

import com.merlin.digitalbanking.ebankingbackend.dto.AccountOperationDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.CustomerDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.UserDTO;
import com.merlin.digitalbanking.ebankingbackend.entities.AccountOperation;
import com.merlin.digitalbanking.ebankingbackend.entities.Customer;
import com.merlin.digitalbanking.ebankingbackend.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OperationsMapper {

    User fromUserDTO(UserDTO currentUser);

    default UserDTO userToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }

    default CustomerDTO mapToCustomerDTO(Customer customer) {
        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getCreatedBy() != null ? userToUserDTO(customer.getCreatedBy()) : null,
                customer.getUpdatedBy() != null ? userToUserDTO(customer.getUpdatedBy()) : null
        );
    }

    default AccountOperationDTO mapToOperationDTO(AccountOperation operation) {
        return new AccountOperationDTO(
                operation.getId(),
                operation.getOperationDate(),
                operation.getAmount(),
                operation.getType(),
                operation.getDescription(),
                operation.getBankAccount().getId(),
                operation.getPerformedBy() != null ? userToUserDTO(operation.getPerformedBy()) : null
        );
    }
}
