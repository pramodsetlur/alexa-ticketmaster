package alexa.ticketmaster;

import com.sun.jersey.api.client.ClientResponse;

public class Tester {

	public static void main(String[] args) {
		Ticketmaster ticketmaster = new Ticketmaster();
		String responses = ticketmaster.getEventDetails("los angeles", "2015-12-15");
		System.out.println(responses);
	}

}
