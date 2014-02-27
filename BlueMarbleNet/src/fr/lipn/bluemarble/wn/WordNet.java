package fr.lipn.bluemarble.wn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.SynsetID;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordNet {
	private static IDictionary dict;
	public static WordnetStemmer stemmer;
	private static String wnhome="res/WN3.0";
	private static String icFile="res/ic-bnc.dat";
	private static HashMap <String, Double> icMap;
	private static double maxIC=0d;
	
	public static void init(){
		//String wnhome = System . getenv (" WNHOME ");
		String path = wnhome + File.separator + "dict";
		URL url;
		try {
			url = new URL("file", null , path );
			dict = new Dictionary ( url);
			dict.open ();
			stemmer = new WordnetStemmer(dict);
			
			//now init Resnik information content
			icMap=new HashMap<String, Double>();
			File f = new File(icFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader((new FileInputStream(f))));
			String line;
			while ((line = reader.readLine()) != null) {
				if(line.startsWith("wnver::")) continue;
				
				String [] toks = line.trim().split(" ");
				String offset_str =toks[0].substring(0, toks[0].length()-1);
				Integer offset = Integer.parseInt(offset_str);
				char pos_chr = toks[0].charAt(toks[0].length()-1);
				POS pos=null;
				switch(pos_chr) {
					case 'n' : pos=POS.NOUN; break;
					case 'v' : pos=POS.VERB; break;
					case 'a' : pos=POS.ADJECTIVE; break;
					case 'r' : pos=POS.ADVERB; break;
					default: pos=POS.NOUN;
				}
				
				ISynsetID sid = new SynsetID(offset.intValue(), pos);
				Double score = Double.parseDouble(toks[1]);
				
				if(score > maxIC) maxIC=score;
				
				icMap.put(sid.toString(), score);
		    }
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		/*
		IIndexWord idxWord = dict . getIndexWord ("dog", POS. NOUN );
		IWordID wordID = idxWord . getWordIDs ().get (0) ;
		IWord word = dict . getWord ( wordID );
		System .out . println ("Id = " + wordID );
		System .out . println (" Lemma = " + word . getLemma ());
		System .out . println (" Gloss = " + word . getSynset (). getGloss ());
		*/
	}
	
	public static void init(String wn){
		wnhome = wn;
		init();
	}
	
	/**
	 * returns all noun synsets corresponding to a given word (if  
	 * @param text
	 * @param pos
	 * @return
	 */
	public static HashSet<ISynsetID> getSynsets(String text, String pos){
		HashSet<ISynsetID> synsets = new HashSet<ISynsetID>();
		POS cpos;
		if(pos.startsWith("N")) cpos = POS.NOUN;
		else if(pos.startsWith("V")) cpos = POS.VERB;
		else if(pos.startsWith("J")) cpos = POS.ADJECTIVE;
		else if(pos.startsWith("R")) cpos = POS.ADVERB;
		else return synsets; //don't deal with other stuff
		text=text.toLowerCase(); //FIXME: check that is ok
		try {
			List<String> stems = stemmer.findStems(text, cpos);
			IIndexWord idxWord = null;
			if(stems.size()>0){
				for(String stem : stems){
					idxWord = dict.getIndexWord(stem, cpos);
					if(idxWord != null){
						if(!idxWord.getLemma().equals("be") && !idxWord.getLemma().equals("have")) {
							List<IWordID> words = idxWord.getWordIDs();
							for(IWordID wid : words){
								ISynsetID isyn= wid.getSynsetID();
								if(cpos==POS.VERB || cpos==POS.ADJECTIVE || cpos==POS.ADVERB){ //try to get a derived noun
									IWord wd = dict.getWord(wid);
									List<IWordID> related = wd.getRelatedWords(Pointer.DERIVATIONALLY_RELATED);
									for(IWordID rel: related) {
										if(rel.getPOS()==POS.NOUN) {
											ISynsetID irelsyn=rel.getSynsetID();
											//ISynset syn = dict.getSynset(isyn);
											synsets.add(irelsyn);
										}
									}
								} else {
									synsets.add(isyn);
								}
							}
						}
					}
				}
			}
		} catch (IllegalArgumentException e){
			//If the WordNet stemmer is not able to return stems
			return synsets;
		}
		
		return synsets;
	}
	/*
	public static boolean areComparable(IIndexWord a, IIndexWord b){
		
	}*/
	
	/**
	 * returns all hypernym paths from a given synset
	 * @param syn
	 * @return
	 */
	public static Vector<String> getHypernyms(ISynsetID syn){
		ISynset ssyn = dict.getSynset(syn);
		List<ISynsetID> hypernyms = ssyn.getRelatedSynsets(Pointer.HYPERNYM);
		if(hypernyms.size()==0){
			//try instances
			hypernyms = ssyn.getRelatedSynsets(Pointer.HYPERNYM_INSTANCE);
		}
		Vector<String> result = new Vector<String>();
		result.add(""+syn.getOffset()); //added to avoid cycles
		if(hypernyms.size()>0){
			ISynsetID hypeID = hypernyms.get(0); //don't deal with multiple inheritance for simplicity
			Vector<String> inherited = getHypernyms(hypeID);
			result.addAll(inherited);
		}
		
		return result;
	}
	
	/**
	 * returns all holonym paths from a given synset
	 * @param syn
	 * @return
	 */
	public static Vector<String> getHolos(ISynsetID syn){
		ISynset ssyn = dict.getSynset(syn);
		List<ISynsetID> holos = ssyn.getRelatedSynsets(Pointer.HOLONYM_PART);
		if(holos.size()==0){
			//try parts
			holos = ssyn.getRelatedSynsets(Pointer.HOLONYM_MEMBER);
		}
		if(holos.size()==0){
			//try substance
			holos = ssyn.getRelatedSynsets(Pointer.HOLONYM_SUBSTANCE);
		}
		Vector<String> result = new Vector<String>();
		result.add(""+syn.getOffset()); //added to avoid cycles
		if(holos.size()>0){
			ISynsetID hypeID = holos.get(0); //don't deal with multiple inheritance for simplicity
			Vector<String> inherited = getHolos(hypeID);
			result.addAll(inherited);
		}
		
		return result;
	}
	
	/**
	 * Checks whether two synsets are antonyms or not
	 * @param syn
	 * @return
	 */
	public static boolean checkAntonym(ISynsetID syn, ISynsetID syn2){
		ISynset ssyn = dict.getSynset(syn);
		
		List<ISynsetID> ants = ssyn.getRelatedSynsets(Pointer.ANTONYM);
		if(ants.contains(syn2)) return true;
		else return false;
	}
	
	public static String getNameForSynset(ISynsetID syn){
		ISynset ssyn = dict.getSynset(syn);
		List<IWord> words = ssyn.getWords();
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(IWord iw : words){
			buf.append(iw.getLemma());
			buf.append(",");
		}
		buf.deleteCharAt(buf.lastIndexOf(","));
		buf.append(")");
		return buf.toString();
	}
	
	public static String getNameFromOffset(String off){
		ISynsetID isyn = new SynsetID(Integer.parseInt(off), POS.NOUN); 
		ISynset ssyn = dict.getSynset(isyn);
		List<IWord> words = ssyn.getWords();
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for(IWord iw : words){
			buf.append(iw.getLemma());
			buf.append(",");
		}
		buf.deleteCharAt(buf.lastIndexOf(","));
		buf.append(")");
		return buf.toString();
	}
	/**
	 * returns the current WordNet home directory
	 * @return
	 */
	public static String getWNHome(){
		return wnhome;
	}
	
	public static double getIC(ISynsetID syn){
		double res=0f;
		Double v =icMap.get(syn.toString());
		//System.err.println(syn+", icMap val:"+v);
		if(v!=null) res=v.doubleValue();
		
		return res;
		//return res/maxIC; //added normalisation
	}
	
	public static double getMaxIC(){
		return maxIC;
	}
}
