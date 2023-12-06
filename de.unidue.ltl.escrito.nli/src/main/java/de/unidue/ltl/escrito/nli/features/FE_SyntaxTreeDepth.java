package de.unidue.ltl.escrito.nli.features;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.features.FeatureType;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Language;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class FE_SyntaxTreeDepth 
	extends FeatureExtractorResource_ImplBase
	implements FeatureExtractor
{
	
	public static final String AVG_SYNTAX_TREE_DEPTH = "syntaxTreeDepthAvg";
	public static final String TOTAL_SYNTAX_TREE_DEPTH = "syntaxTreeDepthMax";
	
	private Language language;

	public FE_SyntaxTreeDepth(Language language) {
		this.language = language;
	}
	
	public int depthOfTree(Constituent constituent,String rootText) throws Exception
	{
		String type = constituent.getConstituentType();
		String text = constituent.getCoveredText().replaceAll("\\s*\\p{Punct}+\\s*$", "");
		
		//Filter the constituent type S and PSEUDO(potential non-tree structures in German)
		//Because they duplicate the ROOT
		boolean duplicate=false;
		
		if(language.equals("en")) {
			// Filter the constituent type S in English, because it will cause duplication
			duplicate = type.equals("S");
		}
		else if(language.equals("de")) {
			// Filter the constituent type S, which is the duplication of ROOT
			// Filter the constituent typ PSEUDO(potential non-tree structures in German)
			duplicate = (type.equals("S") && text.equals(rootText)) || type.equals("PSEUDO");
		} else{
			throw new Exception("Unsupported language: " + language);
		}
		
		int maxDepth = maxDepthOfSubtree(constituent.getChildren().toArray(), rootText);
		if (duplicate) {
			return maxDepth;
		} else{
			return maxDepth + 1;
		}
	}
	
	private int maxDepthOfSubtree(FeatureStructure[] children, String rootText) throws Exception
	{
		int max = 0;
		for (FeatureStructure child : children) {
			if (!child.getType().getShortName().equals("Token")) {
				int tmp = depthOfTree((Constituent) child,rootText);
				if (max < tmp)
					max = tmp;
			}
		}
		return max;
	}

	@Override
	public Set<Feature> extract(JCas jcas, TextClassificationTarget aTarget) throws TextClassificationException {
		double totalTreeDepth = 0;
		Collection<PennTree> trees = JCasUtil.select(jcas, PennTree.class);
		// check every penntree for the root element and calculate the depth of the tree from there
		for (PennTree tree : trees) {
			for (Constituent constituent : JCasUtil.selectCovered(Constituent.class, tree)) {
				if (constituent.getConstituentType().equals("ROOT")) {
					String rootText = constituent.getCoveredText().replaceAll("\\s*\\p{Punct}+\\s*$", "");
					try {
						totalTreeDepth += depthOfTree(constituent, rootText);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//Normalization on total count of trees
		double avgTreeDepth = (double) totalTreeDepth / trees.size();

		Set<Feature> featList = new HashSet<Feature>();
		featList.add(new Feature(AVG_SYNTAX_TREE_DEPTH, avgTreeDepth, FeatureType.NUMERIC));
		featList.add(new Feature(TOTAL_SYNTAX_TREE_DEPTH, totalTreeDepth, FeatureType.NUMERIC));
		return featList;
	}
}
