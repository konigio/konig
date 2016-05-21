package io.konig.appengine.ldp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import io.konig.ldp.LdpHeader;

public class ServletHeader implements LdpHeader {
	
	private HttpServletResponse response;
	private Map<String, String> map = new HashMap<>();

	public ServletHeader(HttpServletResponse request) {
		this.response = request;
	}

	@Override
	public Collection<String> getHeaderNames() {
		return map.keySet();
	}

	@Override
	public void put(String name, String value) {
		name = name.toLowerCase();
		map.put(name, value);
		response.setHeader(name, value);
	}

	@Override
	public String getHeader(String name) {
		name = name.toLowerCase();
		return map.get(name);
	}

}
