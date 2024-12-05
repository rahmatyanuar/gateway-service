package com.rahmat.gateway.model;

import java.util.Collection;

import lombok.Data;

@Data
public class Auth {
	private String username;
	private String email;
	private Boolean isAuth;
	private Boolean credentialIsNotExpired;
	private Boolean accountIsNotExpired;
	private Boolean accountNotLocked;
	private String sessionId;
	private String remoteAdd;
	private Collection<String> credential;
}
