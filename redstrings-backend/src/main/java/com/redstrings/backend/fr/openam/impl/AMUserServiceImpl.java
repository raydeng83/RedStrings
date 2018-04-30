package com.redstrings.backend.fr.openam.impl;

import com.redstrings.backend.fr.openam.AMUserService;
import com.redstrings.backend.model.OtpRef;
import com.redstrings.backend.model.User;
import com.redstrings.backend.service.OtpRefService;
import com.redstrings.backend.service.UserService;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.util.*;

@Service
public class AMUserServiceImpl implements AMUserService {

    @Value("${openam-server.address}")
    private String openamUrl;

    @Autowired
    private OtpRefService otpRefService;

    @Autowired
    private UserService userService;

    @Override
    public void createUser(User user) {
        String createUserUrl = openamUrl+"/json/realms/phoenix-dev/selfservice/userRegistration?_action=submitRequirements";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost httpPost = new HttpPost(createUserUrl);
            StringEntity entity = new StringEntity("{ \"input\": { \"user\": { \"username\": \""+user.getUsername()+"\", \"givenName\": \""+user.getFirstName()+"\", \"sn\": \""+user.getLastName()+"\", \"mail\":\""+user.getEmail()+"\", \"userPassword\": \""+user.getPassword()+"\", \"inetUserStatus\": \"Active\" } } }");
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            System.out.println(httpResponse);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String authenticateUser(String username, String password) {
        boolean status = false;

        String token = null;

        String authenticateUserUrl = openamUrl+"/json/realms/phoenix-dev/authenticate";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost httpPost = new HttpPost(authenticateUserUrl);
            httpPost.setHeader("X-OpenAM-Username", username);
            httpPost.setHeader("X-OpenAM-Password", password);
            httpPost.setHeader("Content-Type", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONObject o = new JSONObject(result.toString());

            if (o.has("tokenId")) {
                token = (String) o.getString("tokenId");

            } else if (o.has("authId")){
                OtpRef otpRef = new OtpRef();
                otpRef.setOtpString(o.toString());
                User user = userService.getUserByUsername(username);
                otpRef.setUser(user);
                otpRef = otpRefService.save(otpRef);
                token = "OTP-" + otpRef.getId();

            } else {
                token = null;
            }

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return token;
    }

    @Override
    public JSONObject invalidateSession(String tokenId) {
        JSONObject result = null;

        String authenticateUserUrl = openamUrl+"/json/sessions?_action=logout";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost httpPost = new HttpPost(authenticateUserUrl);
            httpPost.setHeader("iplanetDirectoryPro", tokenId);
            httpPost.setHeader("Cache-Control", "no-cache");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept-API-Version", "resource=2.0");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer rb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                rb.append(line);
            }

           result = new JSONObject(rb.toString());



        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String fetchOIDCToken(String tokenId) {

        String stsUrl = openamUrl+"/rest-sts/phoenix-dev/access_token?_action=translate";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        String issued_token = null;

        try {
            HttpPost httpPost = new HttpPost(stsUrl);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("iPlanetDirectoryPro", tokenId);

            String body = "{ \"input_token_state\" : { \"token_type\" : \"OPENAM\", \"session_id\" : \""+tokenId+"\" }, \"output_token_state\" : { \"token_type\" : \"OPENIDCONNECT\", \"nonce\" : \"123456\", \"allow_access\" : true } }";
            StringEntity entity = new StringEntity(body);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer rb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                rb.append(line);
            }

            JSONObject o = new JSONObject(rb.toString());

            if (o.has("issued_token")) {
                issued_token = (String) o.getString("issued_token");
                System.out.println(issued_token);
            } else {
                issued_token = null;
            }



        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return issued_token;
    }

    @Override
    public String forgetPassword (String username) {
        String stsUrl = openamUrl+"/json/realms/phoenix-dev/selfservice/forgottenPassword?_action=submitRequirements";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost httpPost = new HttpPost(stsUrl);

            httpPost.setHeader("Content-Type", "application/json");

            String body = "{ \"input\": { \"queryFilter\": \"uid eq \\\""+username+"\\\"\" } }";
            StringEntity entity = new StringEntity(body);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if (httpResponse.getStatusLine().getStatusCode()== HttpStatus.SC_OK) {
                return "Email sent";
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String sendOtp(String otpId, String passcode) {
        OtpRef otpRef = otpRefService.findById(Long.parseLong(otpId));
//        JSONObject joLevel1 = new JSONObject(otpRef.getOtpString());
//        System.out.println(joLevel1.get("callbacks").toString());
//
//        String str = joLevel1.get("callbacks").toString();
//        JSONObject joLevel2 = new JSONObject(str.substring(1, str.length()-1));
//
//        str = joLevel2.get("input").toString();
//        JSONObject joLevel3 = new JSONObject(str.substring(1, str.length()-1));
//
//        joLevel3.put("value", passcode);
        String otpString = otpRef.getOtpString();

        int index = otpString.indexOf("IDToken1");
        String original = otpString.substring(index-1, index+20);
        String newString = otpString.substring(0,index+19)+passcode+otpString.substring(index+19);
        System.out.println(newString);

        String token = null;

        String authenticateUserUrl = openamUrl+"/json/realms/phoenix-dev/authenticate";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost httpPost = new HttpPost(authenticateUserUrl);
            httpPost.setHeader("Content-Type", "application/json");

            String body = newString;
            StringEntity entity = new StringEntity(body);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONObject o = new JSONObject(result.toString());

            if (o.has("tokenId")) {
                token = (String) o.getString("tokenId");
            } else {
                token = null;
            }

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return token;
    }

    public HashMap googleLogin() {
        JSONObject result = null;

        String googleSocialLoginUrl = openamUrl+"/json/realms/root/realms/phoenix-dev/authenticate?authIndexType" +
                "=service&authIndexValue=GoogleSocialAuthenticationService";

        String googleSsoUrl = null;
        HashMap map = new HashMap();


        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            HttpPost httpPost = new HttpPost(googleSocialLoginUrl);
            httpPost.setHeader("Content-Type", "application/json");

            HttpClientContext context = HttpClientContext.create();

            HttpResponse httpResponse = httpClient.execute(httpPost, context);

            CookieStore cookieStore = context.getCookieStore();
            List<Cookie> cookies = cookieStore.getCookies();
            map.put("cookies", cookies);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer rb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                rb.append(line);
            }

            result = new JSONObject(rb.toString());
            String authId = result.get("authId").toString();
            map.put("authId", authId);

            String str = result.get("callbacks").toString();
            JSONObject jo1 = new JSONObject(str.substring(1,str.length()-1));
            str = jo1.get("output").toString();
            JSONObject jo2 = new JSONObject(str.substring(1,str.length()-1));
            googleSsoUrl = jo2.get("value").toString();

            System.out.println(googleSsoUrl);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        map.put("googleSsoUrl", googleSsoUrl);

        return map;
    }

    @Override
    public JSONObject googleLoginPost(HashMap map) throws UnsupportedEncodingException {
        JSONObject result = null;
        String tokenId = null;

        String authId = map.get("authId").toString();
        String NTID = map.get("NTID").toString();
        String code = map.get("code").toString();
        String session_state = map.get("session_state").toString();
        String state = map.get("state").toString();
        String ORIG_URL = map.get("ORIG_URL").toString();

        String googleSocialLoginUrl = openamUrl+"/json/realms/root/realms/phoenix-dev/authenticate?service=GoogleSocialAuthenticationService&=&authIndexType=service&authIndexValue=GoogleSocialAuthenticationService&state="+state+"&code="+code+
        "&authuser=0&session_state="+session_state+"&prompt=none";

        String googleSsoUrl = null;

        BasicCookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie ntidCookie = new BasicClientCookie("NTID", NTID);
        ntidCookie.setDomain(".example.com");
        ntidCookie.setAttribute(ClientCookie.DOMAIN_ATTR,"true");
        cookieStore.addCookie(ntidCookie);

        BasicClientCookie origUrlCookie = new BasicClientCookie("ORIG_URL", ORIG_URL);
        origUrlCookie.setDomain(".example.com");
        origUrlCookie.setAttribute(ClientCookie.DOMAIN_ATTR,"true");
        cookieStore.addCookie(origUrlCookie);

        JSONObject jo = new JSONObject();
        jo.put("authId", authId);
        jo.put("authIndexType", "service");
        jo.put("authIndexValue", "GoogleSocialAuthenticationService");
        jo.put("authuser", 0);
        jo.put("code", code);
        jo.put("prompt", "none");
        jo.put("realm", "/phoenix-dev");
        jo.put("session_state", session_state);
        jo.put("state", state);
        String content = jo.toString();
//        String entityStr = StringEscapeUtils.escapeJava(content);
        StringEntity entity = new StringEntity(content);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        try {
            HttpPost httpPost = new HttpPost(googleSocialLoginUrl);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost, localContext);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer rb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                rb.append(line);
            }

            System.out.println(rb.toString());

            result = new JSONObject(rb.toString());


        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return result;
    }

    @Override
    public JSONObject retrieveIdFromSession(String tokeId){
        String stsUrl = openamUrl+"/json/users?_action=idFromSession";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        JSONObject result = null;


        try {
            HttpPost httpPost = new HttpPost(stsUrl);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("iplanetDirectoryPro", tokeId);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer rb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                rb.append(line);
            }

            System.out.println(rb.toString());

            result = new JSONObject(rb.toString());

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public JSONObject accessEvaluation(String resource, String adminTokenId, String tokenId) throws
            UnsupportedEncodingException {
        String stsUrl = openamUrl+"/json/phoenix-dev/policies?_action=evaluate";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        JSONObject result = null;
        JSONObject jo = new JSONObject();
        List <String> list = new ArrayList <String>();
        list.add(resource);
        jo.put("resources", list);
        jo.put("application", "phoenixPolicySet");
        JSONObject ssoToken = new JSONObject();
        ssoToken.put("ssoToken", tokenId);
        jo.put("subject", ssoToken);
        String content = jo.toString();
        StringEntity entity = new StringEntity(content);

        try {
            HttpPost httpPost = new HttpPost(stsUrl);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("iplanetDirectoryPro", adminTokenId);
            httpPost.setEntity(entity);
            HttpResponse httpResponse = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer rb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                rb.append(line);
            }

            System.out.println(rb.toString());
            String rbStr = rb.toString();
            result = new JSONObject(rbStr.substring(1, rbStr.length()-1));

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;

    }

    @Override
    public JSONObject getOauthToken(String code) {
        String stsUrl = openamUrl+"/oauth2/realms/phoenix-dev/access_token";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        JSONObject result = null;


        try {
            HttpPost httpPost = new HttpPost(stsUrl);
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("code", code));
            nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nvps.add(new BasicNameValuePair("client_id", "phoenix-client"));
            nvps.add(new BasicNameValuePair("redirect_uri", "http://localhost:4200/oauth"));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            String encoding = Base64.getEncoder().encodeToString(("phoenix-client:password").getBytes());
            httpPost.setHeader("Authorization", "Basic " + encoding);


            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            HttpResponse httpResponse = httpClient.execute(httpPost);

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

            StringBuffer rb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                rb.append(line);
            }

            System.out.println(rb.toString());
            String rbStr = rb.toString();
            result = new JSONObject(rbStr);

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
