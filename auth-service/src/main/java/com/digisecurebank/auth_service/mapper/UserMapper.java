package com.digisecurebank.auth_service.mapper;


import com.digisecurebank.auth_service.dto.UsersDTO;
import com.digisecurebank.auth_service.entity.Users;
import org.springframework.security.core.userdetails.User;

public class UserMapper {

    public static UsersDTO mapToUsersDTO(Users user){
        UsersDTO userDTO = new UsersDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFullName(user.getFullName());
        userDTO.setPassword(user.getPassword());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    public static Users mapToUser(UsersDTO userDTO){
        Users user = new Users();
        if(userDTO.getId() != null){
            user.setId(userDTO.getId());
        }
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setPassword(userDTO.getPassword());
        userDTO.setRole(userDTO.getRole());
        return user;
    }

}
