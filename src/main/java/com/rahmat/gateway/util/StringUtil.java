package com.rahmat.gateway.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class StringUtil {
	public Map<String, String[]> findUsingStream(String search, HashMap<String, String[]> list) {
		Map<String, String[]> matchingElements = list.entrySet() 
		          .stream() 
		          .filter(map -> map.getKey().trim().contains(search)) 
		          .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));  
		
	    return matchingElements;
	}
	
	public String extractURL(String path) {
		String uri = path.replace("/"+path.substring(path.lastIndexOf('/') + 1), "");
		return uri;
	}
}
