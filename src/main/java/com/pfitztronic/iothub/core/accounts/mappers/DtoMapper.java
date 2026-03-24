package com.pfitztronic.iothub.core.accounts.mappers;

import com.pfitztronic.iothub.core.accounts.dto.NewUserResponse;
import com.pfitztronic.iothub.core.accounts.models.User;

/**
 * Mapper class for converting domain models to Data Transfer Objects (DTOs).
 * This utility class provides static methods to transform User domain models into their corresponding response DTOs.
 * This facilitates the separation of concerns between the business logic layer and the API response layer.
 */
public class DtoMapper {

    /**
     * Maps a User domain model to a NewUserResponse DTO.
     * Extracts the relevant user information and formats it for API responses.
     *
     * @param user the User domain model to convert
     * @return a NewUserResponse DTO containing the user's phone number, name, verification status, and account status
     * @throws NullPointerException if the user or user's userId is null
     */
    public static NewUserResponse toNewUserResponse(User user) {
        return new NewUserResponse(
                user.getUserId().number(),
                user.getName(),
                user.isVerified(),
                user.getStatus().name()
        );
    }
}

