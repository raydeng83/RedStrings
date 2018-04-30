package com.redstrings.backend.fr.openam;

import com.redstrings.backend.model.User;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public interface AMUserService {
    void createUser(User user);

    String authenticateUser(String usernamne, String password);

    JSONObject invalidateSession(String tokenId);

    String fetchOIDCToken(String tokenId);

    String forgetPassword(String username);

    String sendOtp(String otpId, String passcode);

    HashMap googleLogin();

    JSONObject googleLoginPost(HashMap map)  throws UnsupportedEncodingException;

    JSONObject retrieveIdFromSession(String tokeId);

    JSONObject accessEvaluation(String resource, String adminTokenId, String tokenId) throws
            UnsupportedEncodingException;

    JSONObject getOauthToken(String code);
}
