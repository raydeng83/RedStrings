package com.redstrings.backend.controller;

import com.redstrings.backend.fr.openam.AMUserService;
import com.redstrings.backend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AMUserService amUserService;

    @RequestMapping("/client1SSO")
    public String fetchOIDCToken(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String tokenId = sessionService.getTokenIdBySessionId(sessionId);

        String issuedToken = amUserService.fetchOIDCToken(tokenId);

        return issuedToken;
    }
}
