package com.rahmat.gateway.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rahmat.gateway.provider.ClientService;
import com.rahmat.gateway.util.StringUtil;

@Component
@PropertySource("classpath:bootstrap.properties")
public class JwtRequestFilter extends OncePerRequestFilter {
	@Value("#{PropertySplitter.mapOfList('${access.uri}')}")
	HashMap<String, String[]> access;
	
	@Autowired
	ClientService client;
	
	@Autowired
	StringUtil util;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	throws ServletException, IOException {		
		String path = request.getRequestURI();
		
	    String contentType = request.getContentType();
	    String token = this.tokenresolver(request,response);
	    
	    Map<String, String[]> matchList = util.findUsingStream(path, access);
		Authentication auth = client.tokenValidateClient(token, response);
		if (auth != null) {
			this.authInterceptor(response,auth,matchList.get(path),access);
			//Setter Allow or Access
		    SecurityContextHolder.getContext().setAuthentication(auth);
			logger.info("Request URL path : {"+path+"}, Request content type: {"+contentType+"}");
		}
		chain.doFilter(request, response);
	}
	
	private String tokenresolver(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String prefix = null;
		try {
			prefix = request.getHeader("JWT");
			if(prefix == null)
				prefix = request.getHeader("Authorization");
			if(prefix.startsWith("Bearer")) {
				return prefix.substring(7);
			}
			return null;
		} catch (Exception e) {
			logger.info("Don't need Authorization || "+e.getMessage());
			return null;
		}
	}
	
	private void authInterceptor(HttpServletResponse response,Authentication auth, String[] authorizations, HashMap<String, String[]> roles ) throws IOException {
		Boolean authRes = false;
		Boolean authMatch = false;
		try {
			List<String> authStr = auth.getAuthorities().stream().map(n->String.valueOf(n)).collect(Collectors.toList());
			for(String str : authStr ) {
				for (Map.Entry<String, String[]> set :
					roles.entrySet()) {
					for(String role:set.getValue()) {
						authMatch = role.equals(str);
						if(authMatch)
							break;
					}
					if(authMatch)
						break;
		        }
				
				if(authMatch)
					break;
			}
			
			for(String authorization : authorizations) {
				authRes = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authorization));
				if(authRes)
					break;
			}
			if (!authRes||!authMatch)
				response.sendError(
	                    HttpServletResponse.SC_UNAUTHORIZED
	                );
		} catch (NullPointerException e) {
			logger.error("Cannot get Authorization from Server || Auth value : "+auth+"||"+e.getMessage());
			response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED
                );
		}
	}
}