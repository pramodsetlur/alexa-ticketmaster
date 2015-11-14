//package alexa.ticketmaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.*;

class Events {
	String eventName;
	String description; // eg: this event starts at <startTime> and it is happening at <address>
	String latitude;
	String longitude;
}

public class HackscTicketMaster {
	
	static int HOURS_INDEX = 0;
	static int MINUTES_INDEX = 1;
	
	static ArrayList<String> eventNamesList = new ArrayList<String>();

	public static String convertTimeToText(String time) {
		String[] timeStrArray = time.split(":");
		
		String formattedTime = "";
		String timeUnit = "";
		
		int hours = Integer.parseInt(timeStrArray[HOURS_INDEX]);
		int minutes = Integer.parseInt(timeStrArray[MINUTES_INDEX]);
		
		if (hours > 12) {
			hours = hours % 12;
			timeUnit = "P M ";
		}
		else {
			timeUnit = "A M ";
		}
		
		if (hours == 0) { //convert 00 hours as 12 AM
			hours = 12;
		}
		
		formattedTime += String.valueOf(hours);
		if (minutes != 0) {
			formattedTime += " " + String.valueOf(minutes);
		}
		formattedTime += " " + timeUnit;
		
		return formattedTime;
	}
	
	public static void log(String msg) {
		System.out.println(msg);
	}
	
	public static String convertStringToSpacedOutInt(int number) {
		
		String spacedOutAddr = "";
		while (number !=0 ) {
			int digit = number % 10;
			spacedOutAddr += String.valueOf(digit) + " ";
			number = number/10;
		}
		
		return new StringBuilder(spacedOutAddr).reverse().toString() + " ";
	}
	
	public static String SpaceOutAdress(String address) {
		
		String streetNumber = address.split(" ")[0];
		int stNumberInt = 0;
		try {
			stNumberInt = Integer.parseInt(streetNumber);
		} catch (NumberFormatException e) {
			stNumberInt = 0;
		}
		
		if (stNumberInt != 0) {
			String spacedOutAddr = convertStringToSpacedOutInt(stNumberInt);
			return spacedOutAddr;
		}
		return null;
	} 
	
	public static String getEventAddress(JSONObject addressObj) throws JSONException {
		String eventAddress = "";
		
		eventAddress += addressObj.getJSONObject("address").getString("line1");
		//log(eventAddress);
		return eventAddress;
	}
	
	public static String getLongitude(JSONObject addressObj) throws JSONException {
		String longitude = addressObj.getJSONObject("location").getString("longitude");
		return longitude;
	}
	
	public static String getLatitude(JSONObject addressObj) throws JSONException {
		String latitude = addressObj.getJSONObject("location").getString("latitude");
		return latitude;
	}
	
	public static String getEventTime(JSONObject timeObj) throws JSONException {
		JSONObject startTimeObj;
		String startTime = "";

		startTimeObj = timeObj.getJSONObject("start");
		startTime = startTimeObj.getString("localTime");	
	
		String formattedTime = convertTimeToText(startTime);
		
		return formattedTime;
	}
	
	public static String getEventDescription(String eventName, JSONObject eventJsonObj) throws JSONException {
		String eventDesc = "";
		
		JSONObject timeObj = eventJsonObj.getJSONObject("dates");
		JSONArray addressObjArray = eventJsonObj.getJSONObject("_embedded").getJSONArray("venue");
		
		
		String eventStartTime = "";
		eventStartTime = getEventTime(timeObj);
		String eventAddress = "";
		eventAddress = getEventAddress(addressObjArray.getJSONObject(0)); // default taking only the first venue
			
		if (!eventStartTime.equals("")) {
			eventDesc += eventName + " is starting at ";
			eventDesc += eventStartTime;
		}
		
		if (!eventAddress.equals("")) {
			eventDesc += "at ";
			String spacedOutAddr = SpaceOutAdress(eventAddress);
			if (spacedOutAddr == null) {
				eventDesc += eventAddress;
			}
			else {
				// remove street number and add spacedoutAddress
				String[] addrElemArray = eventAddress.split(" ");
				String formattedAddr = spacedOutAddr;
				for (int i=1; i < addrElemArray.length; i++) {
					formattedAddr += addrElemArray[i];
				}
				eventDesc += formattedAddr;
			}
		}
		
		if (eventDesc.equals("")) {
			eventDesc += "Sorry, Event information is not available for the event " + eventName;
		}
		
		return eventDesc;
	}
	
	public static Boolean addToEventsList(String eventName) {
		if (eventNamesList.size() == 0 || !eventNamesList.contains(eventName)) {
			eventNamesList.add(eventName);
			return true;
		}
		return false;
	}
	
	public static void parseJsonObj(JSONObject mainJsonObj) throws JSONException {
		
		JSONObject _embeddedObj = mainJsonObj.getJSONObject("_embedded");
		JSONArray jsonArray = _embeddedObj.getJSONArray("events");
		
		System.out.println(jsonArray.length());
		
		ArrayList<Events> eventsArrList = new ArrayList<Events>();
		for (int i=0; i<jsonArray.length(); i++) {
			Events eventDetails = new Events();
			JSONObject jsonEventObject = jsonArray.getJSONObject(i);
			String eventName = jsonEventObject.getString("name");

			if (addToEventsList(eventName)) {
				//event description
				String eventDesc = getEventDescription(eventName, jsonEventObject);
				//log(eventDesc);
				
				//latitude
				JSONArray addrArray = jsonEventObject.getJSONObject("_embedded").getJSONArray("venue");
				//get the first venue at 0th index
				String latitude = getLatitude(addrArray.getJSONObject(0));
				String longitude = getLongitude(addrArray.getJSONObject(0));
						
				eventDetails.eventName = eventName;
				eventDetails.description = eventDesc;
				eventDetails.latitude = latitude;
				eventDetails.longitude = longitude;
				
				eventsArrList.add(eventDetails);

			}
		}
	} 
	
	public static void main(String[] args) throws JSONException {
		try {
			
			URL url = new URL(
					"https://app.ticketmaster.com/discovery/v1/events.json?apikey=7elxdku9GGG5k8j0Xm8KWdANDgecHMV0&keyword=new%20york");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			if (conn.getResponseCode() != 200) {
				throw new IOException(conn.getResponseMessage());
			}

			// Buffer the result into a string
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			

			JSONObject mainJsonObj = new JSONObject(sb.toString());
			parseJsonObj(mainJsonObj);

			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
