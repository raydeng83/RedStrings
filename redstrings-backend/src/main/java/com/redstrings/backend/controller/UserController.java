package com.redstrings.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redstrings.backend.fr.openam.AMUserService;
import com.redstrings.backend.model.*;
import com.redstrings.backend.repository.RoleRepository;
import com.redstrings.backend.service.UserService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AMUserService amUserService;

    @RequestMapping(method = RequestMethod.POST)
    public User createUser (@RequestBody User user) {
        Role role = new Role();
        role.setName("ROLE_USER");
        int roleId = roleRepository.findByName("ROLE_USER").getRoleId();
        role.setRoleId(roleId);
        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(new UserRole(user, role));
        return userService.createUser(user, userRoles, "regular");
    }

    @RequestMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @RequestMapping(value = "/forgetpassword", method = RequestMethod.POST)
    public String forgetPassword(@RequestBody HashMap<String, Object> mapper) throws NotFoundException {
        ObjectMapper om = new ObjectMapper();

        HashMap<String, String> input = (HashMap<String, String>) om.convertValue(mapper.get("input"), Object.class);
        String username = om.convertValue(input.get("queryFilter"), String.class).replace("uid eq ", "");

        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new NotFoundException("Username "+username+" not found");
        }

        return amUserService.forgetPassword(username);
    }

    @RequestMapping(value = "/oauthUser", method = RequestMethod.POST)
    public void oauthLogin(@RequestBody User tempUser, HttpServletRequest request) throws Exception {
        User user = userService.getUserByEmailAndAccountType(tempUser.getEmail(), "google");

        if(user == null) {

            Role role = new Role();
            int roleId = roleRepository.findByName("ROLE_USER").getRoleId();
            role.setRoleId(roleId);
            role.setName("ROLE_USER");
            Set<UserRole> userRoles = new HashSet<>();
            userRoles.add(new UserRole(tempUser, role));
            user = userService.createUser(tempUser, userRoles, "google");
        }

        user.setPassword(tempUser.getPassword());

        String token = amUserService.authenticateUser(user.getUsername(), user.getPassword());

        if (token != null && !token.startsWith("OTP")) {
            HttpSession httpSession = request.getSession();
            Set<GrantedAuthority> authorities = new HashSet<>();
            Set<UserRole> userRoles = user.getUserRoles();
            userRoles.forEach(ur -> authorities.add(new Authority(ur.getRole().getName())));
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String sessionId = httpSession.getId();
            Session session = userService.setUserSession(user.getUsername(), sessionId, token);
        } else {
            throw new Exception("Google authentication failed.");
        }

    }

    @RequestMapping("/")
    public User getUserByCurrentSession(Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByUsername(username);

        return user;
    }

}

