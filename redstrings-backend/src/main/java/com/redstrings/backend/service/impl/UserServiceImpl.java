package com.redstrings.backend.service.impl;

import com.redstrings.backend.fr.openam.AMUserService;
import com.redstrings.backend.model.Session;
import com.redstrings.backend.model.User;
import com.redstrings.backend.model.UserRole;
import com.redstrings.backend.repository.RoleRepository;
import com.redstrings.backend.repository.SessionRepository;
import com.redstrings.backend.repository.UserRepository;
import com.redstrings.backend.service.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private AMUserService amUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Override
    public User createUser(User user, Set<UserRole> userRoles, String accountType) {
        User localUser = userRepository.findByEmail(user.getEmail());

        if (localUser != null) {
            LOG.info("User with username {} already exist. Nothing will be done. ", user.getUsername());
        } else {
            for (UserRole ur : userRoles) {
                roleRepository.save(ur.getRole());
            }

            user.getUserRoles().addAll(userRoles);

            user.setAccountType(accountType);

            localUser = userRepository.save(user);

            amUserService.createUser(user);
        }

        return localUser;
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Session setUserSession (String username, String sessionId, String tokenId) {
        Session session = new Session();
        session.setJsessionId(sessionId);
        session.setTokenId(tokenId);

        User user = getUserByUsername(username);
        session.setUser(user);
        return sessionRepository.save(session);
    }

    @Override
    public JSONObject invalidateUserSession (String username, String sessionId) {
        Session session = sessionRepository.findByJsessionId(sessionId);
        String tokenId = session.getTokenId();
        SecurityContextHolder.clearContext();
        sessionRepository.delete(session);
        return amUserService.invalidateSession(tokenId);
    }

    @Override
    public User getUserByEmailAndAccountType(String email, String accountType) {
        return userRepository.findByEmailAndAccountType(email, accountType);
    }

}
