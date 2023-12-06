package de.unidue.ltl.escrito.nli.features;

import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Extracts the total number of characters.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"})
public class FE_NrOfChars 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	public static String NR_OF_CHARS = "NrOfChars";
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget) throws TextClassificationException {
		String text = jcas.getDocumentText();
		double nrOfChars = text.length();
		
		return new Feature(NR_OF_CHARS, nrOfChars, FeatureType.NUMERIC).asSet();
	}
	
}
