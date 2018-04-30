package com.redstrings.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redstrings.backend.fr.openam.AMUserService;
import com.redstrings.backend.model.*;
import com.redstrings.backend.repository.RoleRepository;
import com.redstrings.backend.repository.UserRepository;
import com.redstrings.backend.service.OtpRefService;
import com.redstrings.backend.service.UserService;
import org.apache.http.cookie.Cookie;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class LoginController {

    @Autowired
    private AMUserService amUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private OtpRefService otpRefService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @RequestMapping("/")
    public ResponseEntity<String> logout(
            @RequestParam("logout") String logout
    ){
        return new ResponseEntity<>("Logout success.", HttpStatus.OK);
    }

    @RequestMapping(value = "/user/logout", method = RequestMethod.POST)
    public void logout(HttpServletRequest request, Principal principal) {
        String username = principal.getName();
        String sessionId = request.getSession().getId();

        userService.invalidateUserSession(username, sessionId);

    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public void authenticate(
            @RequestBody HashMap<String, Object> mapper
    ){
        ObjectMapper om = new ObjectMapper();
        String username = (String) mapper.get("username");
        String password = (String) mapper.get("password");

        amUserService.authenticateUser(username, password);
    }

    @RequestMapping("/test")
    public String test() {
        return "test";
    }

    @RequestMapping("/checkSession")
    public String checkSession() {
        return "Session Active";
    }

    @RequestMapping("/otp")
    public String otp(@RequestParam("otpId") String otpId) {
        return "otpId:"+otpId;
    }

    @RequestMapping(value = "/otp", method = RequestMethod.POST)
    public void otpPost(@RequestBody HashMap<String, Object> mapper, HttpServletRequest request){
        ObjectMapper om = new ObjectMapper();
        String otpId = (String) mapper.get("otpId");
        String passcode = (String) mapper.get("passcode");

        String token = amUserService.sendOtp(otpId, passcode);
        if (token != null) {

            OtpRef otpRef = otpRefService.findById(Long.parseLong(otpId));
            User user = otpRef.getUser();

            HttpSession httpSession = request.getSession();
            Set<GrantedAuthority> authorities = new HashSet<>();
            Set<UserRole> userRoles = user.getUserRoles();
            userRoles.forEach(ur -> authorities.add(new Authority(ur.getRole().getName())));
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String sessionId = httpSession.getId();
            Session session = userService.setUserSession(user.getUsername(), sessionId, token);

            otpRefService.deleteById(otpRef.getId());
        } else {
            throw new BadCredentialsException("Passcode is not valid");
        }
    }

    @RequestMapping("/googleLogin")
    public String googleLogin(HttpServletResponse response) {
        HashMap map = amUserService.googleLogin();

        List<Cookie> cookies = (List<Cookie>) map.get("cookies");
        Cookie NTID = cookies.get(0);
        Cookie ORIG_URL = cookies.get(2);

        System.out.println(NTID.getValue());
        System.out.println(ORIG_URL.getValue());
        System.out.println(map.get("authId"));

        javax.servlet.http.Cookie ntidCookie = new javax.servlet.http.Cookie("NTID", NTID.getValue());
        javax.servlet.http.Cookie orig_urlCookie = new javax.servlet.http.Cookie("ORIG_URL", ORIG_URL
                .getValue());
        javax.servlet.http.Cookie authIdCookie = new javax.servlet.http.Cookie("authId", map.get("authId").toString
                ());

        ntidCookie.setDomain("example.com");
        orig_urlCookie.setDomain("example.com");
        authIdCookie.setDomain("example.com");
        response.addCookie(ntidCookie);
        response.addCookie(orig_urlCookie);
        response.addCookie(authIdCookie);

        JSONObject jo = new JSONObject();
        jo.put("authId", map.get("authId"));
        jo.put("googleSsoUrl", map.get("googleSsoUrl"));
        return jo.toString();
    }

    @RequestMapping(value = "/googleLogin", method = RequestMethod.POST)
    public String googleLoginPost(HttpServletRequest request, @RequestBody HashMap mapper) throws UnsupportedEncodingException {

        javax.servlet.http.Cookie[] cookies = request.getCookies();
        String ORIG_URL = null;
        String NTID = null;

        for (int i = 0; i < cookies.length; i++) {
            if(cookies[i].getName().equalsIgnoreCase("ORIG_URL")) {
                ORIG_URL = cookies[i].getValue();
            }

            if(cookies[i].getName().equalsIgnoreCase("NTID")) {
                NTID = cookies[i].getValue();
            }
        }

        mapper.put("ORIG_URL", ORIG_URL);
        mapper.put("NTID", NTID);


        JSONObject result = amUserService.googleLoginPost(mapper);
        String tokenId = result.get("tokenId").toString();

        if (tokenId != null) {

            JSONObject sessionInfo = amUserService.retrieveIdFromSession(tokenId);
            String userId = sessionInfo.get("id").toString();

            User user = userService.getUserByUsername(userId);
            Set<UserRole> userRoles = new HashSet<>();

            if(user==null) {

                User newUser = new User();
                newUser.setUsername(userId);
                newUser.setAccountType("regular");
                Role role1 = new Role();
                int roleId = roleRepository.findByName("ROLE_USER").getRoleId();
                role1.setRoleId(roleId);
                role1.setName("ROLE_USER");
                userRoles.add(new UserRole(newUser, role1));
                newUser.getUserRoles().addAll(userRoles);
                user = userRepository.save(newUser);
            } else {
                userRoles=user.getUserRoles();
            }

            HttpSession httpSession = request.getSession();
            Set<GrantedAuthority> authorities = new HashSet<>();
            userRoles.forEach(ur -> authorities.add(new Authority(ur.getRole().getName())));
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String sessionId = httpSession.getId();
            Session session = userService.setUserSession(user.getUsername(), sessionId, tokenId);

        } else {
            throw new InternalError("Something went wrong. Can't create user session.");
        }

        return result.toString();
    }

    @RequestMapping("/oauth")
    public String oauthPost(@RequestBody String code){
        JSONObject jo = amUserService.getOauthToken(code);

        return jo.toString();
    }
}
