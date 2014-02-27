package fr.lipn.bluemarble;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetComparator;
import it.uniroma1.lcl.jlt.util.Language;
import edu.mit.jwi.item.IPointer;
import fr.lipn.bluemarble.wn.WordNet;

public class MakeWorld {

	public static void main(String[] args) throws IOException {
		WordNet.init();
		BabelNet bn = BabelNet.getInstance();
		//TODO: browse all synsets and find those related to Geography (place names, demonyms, languages, etc...)
		
		System.out.println("SYNSETS WITH English word: \"bank\"");
		List<BabelSynset> synsets = bn.getSynsets(Language.EN, "bank");
		Collections.sort(synsets, new BabelSynsetComparator("bank"));
		for (BabelSynset synset : synsets)
		{
			System.out.print("  =>(" + synset.getId() + ") SOURCE: " + synset.getSynsetSource() +
							 "; TYPE: " + synset.getSynsetType() + 
							 "; WN SYNSET: " + synset.getWordNetOffsets() + ";\n" +
							 "  MAIN LEMMA: " + synset.getMainSense() + 
							 ";\n  IMAGES: " + synset.getImages() + 
							 ";\n  CATEGORIES: " + synset.getCategories() + 
							 ";\n  SENSES (German): { ");
			for (BabelSense sense : synset.getSenses(Language.DE))
				System.out.print(sense.toString()+" ");
			System.out.println("}\n  -----");
			Map<IPointer, List<BabelSynset>> relatedSynsets = synset.getRelatedMap(); 
			for (IPointer relationType : relatedSynsets.keySet())
			{
				List<BabelSynset> relationSynsets = relatedSynsets.get(relationType);
				for (BabelSynset relationSynset : relationSynsets)
				{
					System.out.println("    EDGE " + relationType.getSymbol() +
									   " " + relationSynset.getId() +
									   " " + relationSynset.toString(Language.EN));
				}
			}
			System.out.println("  -----");
		}
	}

}
