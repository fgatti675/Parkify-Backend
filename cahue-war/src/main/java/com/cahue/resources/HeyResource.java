package com.cahue.resources;

import org.glassfish.jersey.server.mvc.Template;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/Hey")
public class HeyResource {
	

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/template")
	@Template(name="/foo.ftl")
	public Map<String, Object> template() {
		Map<String, Object> map = new HashMap<>();
		map.put("foo", "Akshay");
		map.put("bar", "Yo Yo Whatsup?");
		return map;
	}


}
