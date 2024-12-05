package com.rahmat.gateway.model;

import lombok.Data;

@Data
public class ValidateResponse {
	private String statusCode;
	private String statusMessage;
	private Auth authData;
}
