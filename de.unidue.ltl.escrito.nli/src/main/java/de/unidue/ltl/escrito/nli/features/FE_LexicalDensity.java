package de.unidue.ltl.escrito.nli.features;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

/**
 * Calculates the lexical density by extracting the ratio of defined content words per overall POS tags.
 * The default defines adjectives, verbs and nouns as content words. The forwarded JCas has to be POS tagged.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class FE_LexicalDensity 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{
	public static String LEXICAL_DENSITY = "LexicalDensity";
	// TODO: Are those all content words? What about adverbs?
	// TODO: make parametrizable
	private boolean isContentWord(String coarseValue) {
		if (coarseValue != null && (coarseValue.equals("ADJ") || coarseValue.equals("VERB") || coarseValue.startsWith("N"))){
			return true;
		} else {
			return false;
		}
	}
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget) throws TextClassificationException {
		int numberOfContentWords = 0;

		int n=0;
		for (POS pos : JCasUtil.select(jcas, POS.class)) {
			if (isContentWord(pos.getCoarseValue())){
				numberOfContentWords++;
			}
			n++;
		}

		double ld = (double) numberOfContentWords / n ;
		
		Set<Feature> features = new HashSet<Feature>();
		features.add(new Feature(LEXICAL_DENSITY, ld, FeatureType.NUMERIC));

		return features;
	}

}
