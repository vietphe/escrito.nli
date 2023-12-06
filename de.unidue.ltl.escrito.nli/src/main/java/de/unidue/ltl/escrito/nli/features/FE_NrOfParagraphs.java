package de.unidue.ltl.escrito.nli.features;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.TypeCapability;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Counts the number of paragraphs
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"})
@LanguageCapability({ "de","en" })
public class FE_NrOfParagraphs 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{
	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget) throws TextClassificationException {
		Set<Feature> features = new HashSet<>();
		int numOfParagraph = 0;
		//TODO: They are "\n" (Linux and MacOS X), "\r" (MacOS 9 and older) and "\r\n" (Windows).
		String[] lines = jcas.getDocumentText().split("\r\n|\n|\r");
		for (String line : lines) {
		   if(!line.equals("")) {
			   numOfParagraph++;
		   }
		}		
//		System.out.println("Num of Paragraph:" +numOfParagraph);
		features.add(new Feature("NrOfParagraphs", (double) numOfParagraph, FeatureType.NUMERIC));
		return features;
	}
}

