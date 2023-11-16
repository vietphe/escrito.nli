package de.unidue.ltl.escrito.nli.features;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class BasicFeature
    extends FeatureExtractorResource_ImplBase
    implements FeatureExtractor
{
    
    @Override
    public Set<Feature> extract(JCas jcas, TextClassificationTarget target)
        throws TextClassificationException
    {
    	
    	double numTokens = JCasUtil.selectCovered(jcas, Token.class, target).size();
    	System.out.println(numTokens);
    	
        Set<Feature> featList = new HashSet<Feature>();

        DocumentMetaData meta = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
        String text = meta.getDocumentTitle();
        System.out.println(text);
        String[] features = text.split(" ");
        for (int i = 0; i < features.length; i++) {
        	double fValue = Double.parseDouble(features[i]);
        	featList.add(new Feature("Features_"+i, fValue, FeatureType.NUMERIC));
		}        
        return featList;
    }
}