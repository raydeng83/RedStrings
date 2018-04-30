package com.redstrings.backend.service;

import com.redstrings.backend.model.Session;
import com.redstrings.backend.model.User;
import com.redstrings.backend.model.UserRole;
import org.json.JSONObject;

import java.util.Set;

public interface UserService {
    User createUser(User user, Set<UserRole> userRoles, String accountType);

    User getUserByUsername(String username);

    Session setUserSession(String username, String sessionId, String tokenId);

    JSONObject invalidateUserSession(String username, String sessionId);

    User getUserByEmail(String email);

    User getUserByEmailAndAccountType(String email, String accountType);
}
