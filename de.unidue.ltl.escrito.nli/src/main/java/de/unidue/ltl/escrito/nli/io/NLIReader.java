package de.unidue.ltl.escrito.nli.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationTarget;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;


public class NLIReader extends JCasResourceCollectionReader_ImplBase {

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	protected String inputFileString;
	protected URL inputFileURL;

	public static final String PARAM_LABEL_FILE = "LabelFile";
	@ConfigurationParameter(name = PARAM_LABEL_FILE, mandatory = true)
	protected String labelFileString;
	protected URL labelFileURL;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue = "en")
	protected String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
	private String encoding;

	protected int currentIndex;

	protected Queue<QueueItem> items;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		items = new LinkedList<QueueItem>();		
		Map<String, String> labels = new HashMap<>();
		try {
			labelFileURL = ResourceUtils.resolveLocation(labelFileString, this, aContext);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							labelFileURL.openStream(),
							encoding
							)
					);
			String nextLine;
			System.out.println("Reading the label file...");
			while ((nextLine = reader.readLine()) != null) {
				if (nextLine.startsWith("test_taker_id")){
					continue;
				}
				String[] nextItem = nextLine.split(",");
				//System.out.println(nextItem.length);
				int id = Integer.parseInt(nextItem[0]); 
				// format to 5 digits to match with textId of .txt-files
				String textId = String.format("%05d", id);
				textId = textId + ".txt";
				String label  = nextItem[3];
				labels.put(textId, label);				
			}
			System.out.println("Reading the label file is done!");
		}catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
		
		try {
			inputFileURL = ResourceUtils.resolveLocation(inputFileString, this, aContext);
			File file = new File(inputFileString);
			//UTF-8 for German
			Charset inputCharset = Charset.forName("UTF-8");
			File[] fileArray = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.indexOf(".txt") != -1;
				}
			});
			for (File f : fileArray) {
				String id = f.getName();
				String text = cleanString(String.join("\n", FileUtils.readLines(f,inputCharset)));
//				System.out.println(String.join(" ", FileUtils.readLines(f,inputCharset)));
//				System.out.println(text);
//				System.out.println( );
				if (text.startsWith("missing data") || text.equals("")) {
					continue;
				}
				QueueItem item = new QueueItem(id, text, labels.get(id));
				items.add(item);
				System.out.println(item.toString());
			}
	      	
	    }
	    catch(IOException ioe) {
	      ioe.printStackTrace();
	    }
		System.out.println("Read "+items.size()+" items.");
		currentIndex = 0;
	}
	// HOTFIX for Issue 445 in DKPro Core
	private static String cleanString(String textForCas) {
		textForCas = textForCas.replaceAll("´", "'");
		textForCas = textForCas.replaceAll("…", "...");						
		textForCas = textForCas.replaceAll("`", "'");
		textForCas = textForCas.replaceAll("’", "'");		
//		textForCas = textForCas.replaceAll("[^a-zA-Z0-9\\-\\.,:;\\(\\)\\'´’…`@/?! ]", "");
		//to add space after a dot if not
//		textForCas = textForCas.replaceAll("[,.!?;:]", "$0 ").replaceAll("\\s+", " "); 
		return textForCas;
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
	public void getNext(JCas jcas) throws IOException, CollectionException {
		System.out.println("GETNEXT");
		QueueItem item = items.poll();
		getLogger().debug(item);
			
		jcas.setDocumentLanguage(language);
		jcas.setDocumentText(item.getText());
		
		DocumentMetaData dmd = DocumentMetaData.create(jcas);
		//TODO: The name of the getters und setters must be meaningful
		dmd.setDocumentId(item.getId()); 
		dmd.setDocumentTitle(item.getText());
		dmd.setDocumentUri(inputFileString);
		dmd.setCollectionId(item.getId());
		
		TextClassificationTarget unit = new TextClassificationTarget(jcas, 0, jcas.getDocumentText().length());
		unit.setSuffix(item.getId());
		unit.addToIndexes();
		
		TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, 0, jcas.getDocumentText().length());
		outcome.setOutcome(item.getLabel());
		outcome.addToIndexes();
		currentIndex++;
	}
	
	
	class QueueItem{
		private String id;
		private String text;
		private String label;
		
		
		public QueueItem(String id, String text, String label) {
			super();
			this.id = id;
			this.text = text;
			this.label = label;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(id);
			sb.append("-");
			sb.append(" ");
			String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
			sb.append(subStringText);
			sb.append(" ...");
			sb.append("-");
			sb.append(" ");
			sb.append(label);
			
			
			return sb.toString();        
		}
		
		
	}
	
}