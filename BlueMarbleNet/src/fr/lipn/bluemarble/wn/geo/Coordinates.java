package fr.lipn.bluemarble.wn.geo;

public class Coordinates {
	private String lat;
	private String lon;
	private String offset;
	private String geonamesID;
	
	public Coordinates(String offset, String geonamesID, String lat, String lon) {
		this.offset=offset;
		this.geonamesID=geonamesID;
		this.lat=lat;
		this.lon=lon;
	}
	
	public String getLatLon(){
		return "("+this.lat+":"+this.lon+")";
	}
	
	public String getOffset(){
		return this.offset;
	}
	
	public String getGeonamesID(){
		return this.geonamesID;
	}

}
