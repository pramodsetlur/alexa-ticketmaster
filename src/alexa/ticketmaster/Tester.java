package alexa.ticketmaster;

import java.util.ArrayList;

public class Tester {

	public static void main(String[] args) {
		Ticketmaster ticketmaster = new Ticketmaster();
		ArrayList<EventsEntity> eventsEntities = ticketmaster.getEventDetails("los angeles", "2015-11-14");
		for (EventsEntity e:eventsEntities) {
			System.out.println(e.getEventName());
		}
	}

}
