package com.rahmat.gateway.provider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.rahmat.gateway.model.Auth;
import com.rahmat.gateway.model.ValidateResponse;



@Component
public class ClientService {
	@Value("${host.validate}")
	private String hostValidate;
	
	private Logger logger = LoggerFactory.getLogger(ClientService.class);
	
	public Authentication tokenValidateClient(String token, HttpServletResponse response) throws IOException {
		HttpClient client = HttpClient.newHttpClient();
		
		if(token == null) {
			logger.error("Token is "+token);
			response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED
                );
			return null;
		}
        // Create HTTP request object
		logger.info(hostValidate);
        HttpRequest httpRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:8181/api/auth/validate"))
                .uri(URI.create(hostValidate))
                .GET()
                .header("Authorization", "Bearer "+token)
                .header("Content-Type", "application/json")
                .build();
        // Send HTTP request
        HttpResponse<String> httpResponse = null;
        
        String body = null;
        List<String> authList = new ArrayList<String>();
        String statusCode = null;
        String statusMessage = null;
        Boolean isAuth = false;
        String username = null;
        Boolean credentialIsNotExpired = false;
        Boolean accountIsNotExpired = false;
        Boolean accountNotLocked = false;
        String email = null;
        String remoteAdd = null;
        ValidateResponse val = new ValidateResponse();
        Auth authData = new Auth();
        String sessionId = null;
        
        try {
			httpResponse = client.send(httpRequest,
			        HttpResponse.BodyHandlers.ofString());
			body = httpResponse.body();
        } catch (IOException e) {
			logger.error("Http client Rejected || "+ e.getMessage());
			response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED
                );
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED
                );
			return null;
		}
        try {
        	JSONObject data = new JSONObject(body);
        	
        	statusCode = data.getString("statusCode");
        	statusMessage = data.getString("statusMessage");
        	
        	if(statusCode.equals("200") && statusMessage.equals("Success")) {
        		JSONArray authorities = data.getJSONObject("data").getJSONArray("authorities");
            	for(int i = 0;i<authorities.length();i++) {
            		JSONObject auth = new JSONObject();
            		auth = authorities.getJSONObject(i);
            		authList.add(auth.getString("authority"));
            	}
        	} else {
        		logger.error("HTTP Status Code : "+statusCode);
        		response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED
                    );
        		return null;
        	}
        	
        	isAuth = Boolean.valueOf(data.getJSONObject("data").get("authenticated").toString());
        	JSONObject principal = data.getJSONObject("data").getJSONObject("principal");
        	JSONObject details = data.getJSONObject("data").getJSONObject("details");
        	username = principal.get("username").toString();
        	email = principal.get("email").toString();
        	credentialIsNotExpired = Boolean.valueOf(principal.get("credentialsNonExpired").toString());
        	accountIsNotExpired = Boolean.valueOf(principal.get("accountNonExpired").toString());
        	sessionId = details.get("sessionId").toString();
        	remoteAdd = details.get("remoteAddress").toString();
        	
        	authData.setUsername(username);
        	authData.setEmail(email);
        	authData.setAccountIsNotExpired(accountIsNotExpired);
        	authData.setAccountNotLocked(accountNotLocked);
        	authData.setCredentialIsNotExpired(credentialIsNotExpired);
        	authData.setSessionId(sessionId);
        	authData.setRemoteAdd(remoteAdd);
        	authData.setIsAuth(isAuth);
        	authData.setCredential(authList);
        	
        	val.setStatusCode(statusCode);
        	val.setStatusMessage(statusMessage);
        	val.setAuthData(authData);
        	
        	return new UsernamePasswordAuthenticationToken(username, "",this.getAuthority(authList));
        } catch (Exception e) {
			// TODO: handle exception
        	logger.error(e.getMessage());
			response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED
                );
			return null;
		}
	}
	
	private Collection<? extends GrantedAuthority> getAuthority(List<String> roles){
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		for(String item : roles) {
			list.add(new SimpleGrantedAuthority(item));
		}
		return list;
	}
}
