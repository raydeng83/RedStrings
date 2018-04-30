package com.redstrings.backend.config;

import com.redstrings.backend.fr.openam.AMUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CustomAuthenticationProvider
        implements AuthenticationProvider {

    static private String token = null;

    @Autowired
    private AMUserService amUserService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        token = amUserService.authenticateUser(username, password);
        if ( token !=null) {
            if(token.startsWith("OTP")) {
                throw new BadCredentialsException("otpId:"+token.substring(4));
            } else {
                return new UsernamePasswordAuthenticationToken(
                        username, password, new ArrayList<>());
            }

        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }

    public static String getToken (){
        return token;
    }
}
