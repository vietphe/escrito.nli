package de.unidue.ltl.escrito.nli.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

// TODO ASAP and UDE reader seem to be very similar (use base class or even same reader?)
public class NLIFeatureReader
extends JCasResourceCollectionReader_ImplBase
{

	public enum RatingBias {
		high,
		low
	}

	/**
	 * Language
	 */
	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true,defaultValue = "en")
	private String language;

	/**
	 * Target criterium that the learner should make use of
	 */
	public static final String PARAM_TARGET_LABEL = "TargetLabel";
	@ConfigurationParameter(name = PARAM_TARGET_LABEL, mandatory = true)
	protected String targetLabel;

	/**
	 * In case of two coders with different ratings, use lower rating/higher rating bias.
	 */
	public static final String PARAM_RATING_BIAS = "RatingBias";
	@ConfigurationParameter(name = PARAM_RATING_BIAS, mandatory = true)
	protected RatingBias ratingBias;

	public static final String PARAM_DO_SPARSECLASSMERGING = "DoSparseClassMerging";
	@ConfigurationParameter(name = PARAM_DO_SPARSECLASSMERGING, mandatory = true)
	private boolean doSparseClassMerging;


	public static final String PARAM_DO_NORMALIZATION = "doNormalization";
	@ConfigurationParameter(name = PARAM_DO_NORMALIZATION, mandatory = true)
	private boolean doNormalization;

	public static final String PARAM_ASAP_NUMBER = "asapNumber";
	@ConfigurationParameter(name = PARAM_ASAP_NUMBER, mandatory = false,defaultValue="0")
	private int asapNumber;

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_SEPARATOR = "Separator";
	@ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "\t")
	private String separator;

	public static final String PARAM_QUESTION_ID = "QuestionId";
	@ConfigurationParameter(name = PARAM_QUESTION_ID, mandatory = false, defaultValue = "-1")
	protected Integer requestedQuestionId; 

	protected Queue<NLIFeatureItem> items;
	protected URL inputFileURL;
	int currentIndex;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException
	{
		items = new LinkedList<NLIFeatureItem>();
		try {
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							inputFileURL.openStream(),
							encoding
							)
					);
			String nextLine;
			System.out.println("Initialize");
			while ((nextLine = reader.readLine()) != null) {
				if (nextLine.startsWith("textId")){
					continue;
				}
				String[] nextItem = nextLine.split(",");
				//System.out.println(nextItem.length);
				String textId = nextItem[0]; 
				String text  = "";
				String label  = nextItem[24];
				for (int i = 1; i < 24; i++) {
					text = text+nextItem[i]+" ";
				}
				
//				System.out.println(text);
				
				NLIFeatureItem newItem = new NLIFeatureItem(textId, label, text);				
				items.add(newItem);
			}   
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		System.out.println("Read "+items.size()+" items.");
		currentIndex =0;
	}



	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return !items.isEmpty();
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(currentIndex, items.size(), Progress.ENTITIES) };
	}

	@Override
	public void getNext(JCas jcas)
			throws IOException, CollectionException
	{    
		//System.out.println("GETNEXT");
		NLIFeatureItem item = items.poll();
		getLogger().debug(item);
		jcas.setDocumentText(item.getText());
		jcas.setDocumentLanguage(language);

		DocumentMetaData dmd = DocumentMetaData.create(jcas);
		dmd.setDocumentId(item.getTextId()); 
		dmd.setDocumentTitle(item.getText());
		dmd.setDocumentUri(inputFileString);
		dmd.setCollectionId(item.getTextId());

		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
//		System.out.println("target:"+jcas.getDocumentText());
		
		//will add the token content as a suffix to the ID of this unit 
		//	System.out.println("ItemId: "+item.getId());
		unit.setSuffix(item.getTextId());
		unit.addToIndexes();		 
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(item.getLabel());
		outcome.addToIndexes();
		currentIndex++;
	}
}

class NLIFeatureItem{

	protected String textId;
	protected String text;
	protected String label;


	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(textId);
		sb.append("-");
		sb.append(label);
		sb.append(" ");
		String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
		sb.append(subStringText);
		sb.append(" ...");
		return sb.toString();        
	}

	public NLIFeatureItem(String essayId, String label, String text)
	{
		this.textId = essayId;		
		this.label = label;
		this.text = text;

	}
	
	public String getTextId()
	{
		return textId;
	}

	public void setTextId(String textId)
	{
		this.textId = textId;
	}
	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}