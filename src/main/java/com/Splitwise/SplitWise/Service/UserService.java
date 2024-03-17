package com.Splitwise.SplitWise.Service;

import com.Splitwise.SplitWise.Exceptions.NotFoundException;
import com.Splitwise.SplitWise.payload.UserDTO;

public interface UserService {
    UserDTO addUser(UserDTO userDTO);

    UserDTO getUserById(Long userId) throws NotFoundException;

    void deleteUserById(Long userId) throws NotFoundException;
}
