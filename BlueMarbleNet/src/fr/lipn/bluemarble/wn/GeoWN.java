package fr.lipn.bluemarble.wn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import fr.lipn.bluemarble.wn.geo.Coordinates;

public class GeoWN {
	private static HashMap<String, Coordinates> geoMap;
	private static String GEOWN_HOME="res/GeoWN";
	
	public static void init(){
		geoMap = new HashMap<String, Coordinates>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(GEOWN_HOME+"/mapping.dat"));
			String line;
		    while ((line = reader.readLine()) != null) {
		    	if(!line.startsWith("#")){
		    		String [] elements = line.split("\t");
		    		String offset =elements[0];
		    		Coordinates coord = new Coordinates(elements[0], elements[1], elements[2], elements[3]);
		    		geoMap.put(offset, coord);
		    	}
		    }
		    reader.close();
		} catch (Exception e){
			System.err.println("Error reading GeoWN mappings:");
			e.printStackTrace();
		}
	}
	
	public static Coordinates getCoord(String offset){
		return geoMap.get(offset);
	}
}
