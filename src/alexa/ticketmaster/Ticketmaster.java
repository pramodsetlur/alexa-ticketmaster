package alexa.ticketmaster;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class Ticketmaster {
	private static final String hostServer = "https://app.ticketmaster.com";
	
	private static final String API_KEY = "apikey";
	private static final String apiValue = "7elxdku9GGG5k8j0Xm8KWdANDgecHMV0";
	
	private static final String discoveryApi = "/discovery/v1";
	private static final String eventsEndPoint = "/events.json";
	
	public String getEventDetails(String location, String date) {
		WebResource resource = setupClient();
		resource = resource.path(eventsEndPoint);
		
		//Creating request parameters
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(API_KEY, apiValue);
		params.add("keyword", URLEncoder.encode(location));
		params.add("startDateTime", constructDateTimeQueryParam(date));
				
		resource = resource.queryParams(params);
		
		ClientResponse clientResponse = resource.get(ClientResponse.class);
		String response = null;
		if (clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			response = clientResponse.getEntity(String.class);
		}
		
		return response;
	}
	
	private String constructDateTimeQueryParam(String date) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String time = dateFormat.format(cal.getTime());
		
		String timeQuery = "T" + time + "Z";
		System.out.println(date+timeQuery);
		return date+timeQuery;
	}
	
	private WebResource setupClient() {
		Client client = Client.create();
		WebResource webResource = client.resource(hostServer).path(discoveryApi);
		return webResource;
	}
	
	
}
