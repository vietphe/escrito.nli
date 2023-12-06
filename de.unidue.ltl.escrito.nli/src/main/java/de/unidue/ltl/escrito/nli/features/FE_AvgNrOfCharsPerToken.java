package de.unidue.ltl.escrito.nli.features;

import java.util.Collection;
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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Extracts the ratio of characters per token.
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class FE_AvgNrOfCharsPerToken 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{

	public static final String AVG_NR_OF_CHARS_PER_TOKEN = "avgNumCharsPerToken";
    public static final String STANDARD_DEVIATION_OF_CHARS_PER_TOKEN = "standardDevCharsPerToken";

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget) throws TextClassificationException {
		Set<Feature> featureList = new HashSet<Feature>();

		Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
		double numOfTokens = 0;
		double tempSize = 0;

		for (Token token : tokens) {
			if (token.getPos() == null) {
				System.err.println("No POS for token " + token.getCoveredText() + " in essay "
						+ jcas.getDocumentText().substring(0, 100));
			} else {
				if (!token.getPos().getPosValue().equals("$.") && !token.getPos().getPosValue().equals(".")) {
					tempSize += token.getCoveredText().length();
					numOfTokens++;
				}
			}
		}
		double avgSize = tempSize / numOfTokens;

		tempSize = 0;
		for (Token token : tokens) {
			if (token.getPos() == null) {
				System.err.println("No POS for token " + token.getCoveredText() + " in essay "
						+ jcas.getDocumentText().substring(0, 100));
			} else {
				if (!token.getPos().getPosValue().equals("$.") && !token.getPos().getPosValue().equals(".")) {
					double tempAdd = token.getCoveredText().length() - avgSize;
					tempSize += Math.pow(tempAdd, 2);
				}
			}
		}

		double stndDeviation = Math.sqrt(tempSize / numOfTokens);
		featureList.add(new Feature(AVG_NR_OF_CHARS_PER_TOKEN, avgSize, FeatureType.NUMERIC));
		featureList.add(new Feature(STANDARD_DEVIATION_OF_CHARS_PER_TOKEN, stndDeviation, FeatureType.NUMERIC));
		return featureList;
	}

}
