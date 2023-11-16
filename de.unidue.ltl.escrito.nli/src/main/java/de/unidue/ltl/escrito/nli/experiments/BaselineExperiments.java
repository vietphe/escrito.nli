package de.unidue.ltl.escrito.nli.experiments;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.corenlp.CoreNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.BinaryCasReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.unidue.ltl.escrito.nli.annotators.Analyzer;
import de.unidue.ltl.escrito.nli.basics.Experiments_ImplBase;
import de.unidue.ltl.escrito.nli.basics.FeatureSettings;
import de.unidue.ltl.escrito.nli.io.NLIFeatureReader;
import de.unidue.ltl.escrito.nli.io.NLIFeatureReader.RatingBias;

public class BaselineExperiments extends Experiments_ImplBase implements Constants{

	public static void main(String[] args) throws Exception {
		//TODO: set system property
		System.setProperty("DKPRO_HOME", "C:\\Users\\vietphe\\workspace\\DKPRO_HOME");
	
		String path = "src\\main\\resources\\featureFile\\NLIFeaturesSorted.csv";
		runBasicAsapExperiment(path);	
	}

	private static void runBasicAsapExperiment(String trainData) throws Exception {
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(NLIFeatureReader.class,
				NLIFeatureReader.PARAM_QUESTION_ID,1, NLIFeatureReader.PARAM_TARGET_LABEL, "label",
				NLIFeatureReader.PARAM_RATING_BIAS, RatingBias.low, NLIFeatureReader.PARAM_DO_SPARSECLASSMERGING, false,
				NLIFeatureReader.PARAM_DO_NORMALIZATION, false, NLIFeatureReader.PARAM_INPUT_FILE, trainData);
		runBaselineExperiment("NLI", reader, reader, "en");
	}
	private static void runBaselineExperiment(String experimentName, CollectionReaderDescription readerTrain,
			CollectionReaderDescription readerTest, String languageCode) throws Exception {
		Map<String, Object> dimReaders = new HashMap<String, Object>();
		dimReaders.put(DIM_READER_TRAIN, readerTrain);
		dimReaders.put(DIM_READER_TEST, readerTest);

		Dimension<String> learningDims = Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL);
		Dimension<Map<String, Object>> learningsArgsDims = getStandardWekaClassificationArgsDim();

		ParameterSpace pSpace = null;
		pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders), learningDims,
				Dimension.create(DIM_FEATURE_MODE, FM_UNIT), 
				FeatureSettings.getBasicFeatureSet(),
				learningsArgsDims);

		runCrossValidation(pSpace, experimentName, getEmptyPreprocessing(), 10);
	}

	 public static AnalysisEngineDescription getPreprocessing(String languageCode) throws ResourceInitializationException {
		 
		 AnalysisEngineDescription seg = createEngineDescription(CoreNlpSegmenter.class,CoreNlpSegmenter.PARAM_LANGUAGE, "en");
		 AnalysisEngineDescription tagger       = createEngineDescription(CoreNlpPosTagger.class, CoreNlpPosTagger.PARAM_LANGUAGE, "en");

	     AnalysisEngineDescription lemmatizer   = createEngineDescription(CoreNlpLemmatizer.class);

	     AnalysisEngineDescription analyzer = createEngineDescription(Analyzer.class);
	        
	     return createEngineDescription(
	    		 	seg
//	                tagger,
//	                lemmatizer

//	                analyzer

	            );
	}


}