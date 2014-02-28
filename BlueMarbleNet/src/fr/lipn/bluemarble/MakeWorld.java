package fr.lipn.bluemarble;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetComparator;
import it.uniroma1.lcl.babelnet.iterators.BabelSynsetIterator;
import it.uniroma1.lcl.jlt.util.Language;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.POS;
import fr.lipn.bluemarble.wn.GeoWN;
import fr.lipn.bluemarble.wn.WordNet;
import fr.lipn.bluemarble.wn.geo.Coordinates;

public class MakeWorld {

	public static void main(String[] args) throws IOException {
		WordNet.init();
		GeoWN.init();
		BabelNet bn = BabelNet.getInstance();
		//TODO: browse all synsets and find those related to Geography (place names, demonyms, languages, etc...)
		BabelSynsetIterator itr = bn.getSynsetIterator();
		System.out.println("<synsets>");
		while(itr.hasNext()) {
			BabelSynset syn = itr.next();
			HashMap<String, HashSet<Coordinates>> relatedCoords= new HashMap<String, HashSet<Coordinates>>();
			HashSet<Coordinates> coords = new HashSet<Coordinates>();
			if(syn.getPOS().equals(POS.NOUN) || syn.getPOS().equals(POS.ADJECTIVE)){
				//System.err.println(syn.getId());
				//System.err.println(syn.getWordNetOffsets());
				boolean hasGeo=false;
				boolean isGeo=checkGeoSynset(syn);
				if(isGeo) {
					hasGeo=true;
					coords.add(getCoords(syn));
					relatedCoords.put("direct", coords);
				}
				//if(isGeo){
					/*System.err.println(syn.getSynsetType());
					System.err.println(syn.getMainSense());
					System.err.println("-----");*/
					Map<IPointer, List<BabelSynset>> relatedSynsets = syn.getRelatedMap(); 
					for (IPointer relationType : relatedSynsets.keySet())
					{
						//System.err.println("relationName: "+relationType.getName()+" symbol: "+relationType.getSymbol());
						if(!relationType.getSymbol().equals("r")) {
							List<BabelSynset> relationSynsets = relatedSynsets.get(relationType);
							for (BabelSynset relationSynset : relationSynsets)
							{
								isGeo=checkGeoSynset(relationSynset);
								if(isGeo){
									hasGeo=true;
									HashSet<Coordinates> currCoords=relatedCoords.get(relationType.getSymbol());
									if (currCoords == null) currCoords = new HashSet<Coordinates>();
									currCoords.add(getCoords(relationSynset));
									relatedCoords.put(relationType.getSymbol(), currCoords);
									/*System.err.println("    EDGE " + relationType.getSymbol() +
												   " " + relationSynset.getId() +
												   " " + relationSynset.getWordNetOffsets() +
												   " " + relationSynset.getMainSense());
												   */
								}
							}
						}
					}
				//}
				//System.err.println("  -----");
				if(hasGeo) {
					List<BabelSense> sensesES = syn.getSenses(Language.ES);
					List<BabelSense> sensesFR = syn.getSenses(Language.FR);
					dumpSynsetData(syn, relatedCoords, sensesES, sensesFR);
				}
			}
		}
		System.out.println("</synsets>");
		
	}
	
	private static boolean checkGeoSynset(BabelSynset syn) {
		List<String> offsets = syn.getWordNetOffsets();
		for(String off : offsets){
			off=off.replaceAll("n", "");
			Coordinates coords = GeoWN.getCoord(off);
			if(coords != null){
				//System.err.println("GEO: "+coords.getLatLon());
				return true;
			}
		}
		return false;
	}
	
	private static Coordinates getCoords(BabelSynset syn) {
		List<String> offsets = syn.getWordNetOffsets();
		for(String off : offsets){
			off=off.replaceAll("n", "");
			Coordinates coords = GeoWN.getCoord(off);
			if(coords != null){
				return coords;
			}
		}
		return null;
	}
	
	private static void dumpSynsetData(BabelSynset syn, HashMap<String,HashSet<Coordinates>> relatedCoords, List<BabelSense> sensesES, List<BabelSense> sensesFR){
		System.out.println("<synset id=\""+syn.getId()+"\" off=\""+syn.getWordNetOffsets().get(0)+"\" wn=\""+syn.getMainSense()+"\" >");
		try {
			System.out.println("\t<lemma lang=\"es\">"+sensesES.get(0).getLemma()+"</lemma>");
		} catch(Exception e) {
			//no Spanish lemma
		}
		
		try{
			System.out.println("\t<lemma lang=\"fr\">"+sensesFR.get(0).getLemma()+"</lemma>");
		} catch (Exception e) {
			//no French lemma
		}
		System.out.println("\t<lemma lang=\"en\">"+syn.getSenses().get(0).getLemma()+"</lemma>");
		for(String s : relatedCoords.keySet()) {
			for(Coordinates c : relatedCoords.get(s)) {
				System.out.println("\t<geo source=\""+s+"\" geonamesid=\""+c.getGeonamesID()+"\">"+c.getLatLon()+"</geo>");
			}
		}
		System.out.println("</synset>");
		
	}

}
