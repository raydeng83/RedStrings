package com.redstrings.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redstrings.backend.model.Session;
import com.redstrings.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private RequestCache requestCache = new HttpSessionRequestCache();
    private final ObjectMapper mapper;

    @Autowired
    private UserService userService;

    @Autowired
    AuthenticationSuccessHandler(MappingJackson2HttpMessageConverter messageConverter) {
        this.mapper = messageConverter.getObjectMapper();
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        HttpSession httpSession = request.getSession();

        String username = (String) authentication.getPrincipal();
        String sessionId = httpSession.getId();
        String token = CustomAuthenticationProvider.getToken();
        Session session = userService.setUserSession(username, sessionId, token);
        System.out.println(session);
        //super.onAuthenticationSuccess( request, response, authentication );
        /*SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest == null) {
            clearAuthenticationAttributes(request);
            return;
        }
        String targetUrlParam = getTargetUrlParameter();
        if (isAlwaysUseDefaultTargetUrl() || (targetUrlParam != null && StringUtils.hasText(request.getParameter(targetUrlParam)))) {
            requestCache.removeRequest(request, response);
            clearAuthenticationAttributes(request);
            return;
        }
        clearAuthenticationAttributes(request);
        */
    }

    @Override
    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
