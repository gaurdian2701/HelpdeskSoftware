package com.helpdesk.service;

import com.helpdesk.model.Role;
import com.helpdesk.model.User;

public interface UserService {

    User findUserByEmail(String email);

    boolean findRoleByUser(String role, User user);

    boolean isAdmin(User user);

    boolean isUser(User user);

    void saveUser(User user);

}
