package de.unidue.ltl.escrito.nli.basics;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.ml.experiment.ExperimentCrossValidation;
import org.dkpro.tc.ml.experiment.ExperimentTrainTest;
import org.dkpro.tc.ml.report.CrossValidationReport;
import org.dkpro.tc.ml.report.RuntimeReport;
import org.dkpro.tc.ml.report.TrainTestReport;
import org.dkpro.tc.ml.weka.WekaAdapter;

import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpParser;
import de.tudarmstadt.ukp.dkpro.core.clearnlp.ClearNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.unidue.ltl.escrito.core.clustering.BatchTaskClusterClassificationExemplar;
import de.unidue.ltl.escrito.core.clustering.BatchTaskClusterLabelPropagation;
import de.unidue.ltl.escrito.core.clustering.BatchTaskClustering;
import de.unidue.ltl.escrito.core.learningcurve.CvLearningCurveReport;
import de.unidue.ltl.escrito.core.learningcurve.LearningCurveAdapter;
import de.unidue.ltl.escrito.core.learningcurve.LearningCurveReport;
import de.unidue.ltl.escrito.core.report.CvEvaluationReport;
import de.unidue.ltl.escrito.core.report.GradingEvaluationReport;
import de.unidue.ltl.escrito.core.report.GradingEvaluationReportClusteringCurve;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMO;

public abstract class Experiments_ImplBase
	implements Constants
{


	public static Dimension<Map<String, Object>> getStandardWekaClassificationArgsDim()
	{	
		Map<String, Object> config = new HashMap<>();
		config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new WekaAdapter(), SMO.class.getName()});
		// config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new WekaAdapterConfidenceScores(), J48.class.getName()});
		//config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new WekaAdapterConfidenceScores(), J48.class.getName()});
		config.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
		config.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());
		Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);					
		return mlas;
	}	

	public static Dimension<Map<String, Object>> getStandardWekaRegressionArgsDim()
	{	
		Map<String, Object> config = new HashMap<>();
		config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new WekaAdapter(), LinearRegression.class.getName()});
		//config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new WekaAdapter(), weka.classifiers.functions.Logistic.class.getName()});
		config.put(DIM_DATA_WRITER, new WekaAdapter().getDataWriterClass());
		config.put(DIM_FEATURE_USE_SPARSE, new WekaAdapter().useSparseFeatures());
		Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);					
		return mlas;
	}
	

	public static Dimension<Map<String, Object>> getWekaLearningCurveClassificationArgsDim()
	{	
		Map<String, Object> config = new HashMap<>();
		config.put(DIM_CLASSIFICATION_ARGS, new Object[] { new LearningCurveAdapter(), SMO.class.getName()});
		config.put(DIM_DATA_WRITER, new LearningCurveAdapter().getDataWriterClass());
		config.put(DIM_FEATURE_USE_SPARSE, new LearningCurveAdapter().useSparseFeatures());
		Dimension<Map<String, Object>> mlas = Dimension.createBundle("config", config);					
		return mlas;
	}

	// ######### PREPROCESSING ##########//

	// TODO: make preprocessing dependent on the feature extraction used
	public static AnalysisEngineDescription getPreprocessing(String languageCode) throws ResourceInitializationException {
		AnalysisEngineDescription tagger       = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription lemmatizer   = createEngineDescription(NoOpAnnotator.class);
		AnalysisEngineDescription parser       = createEngineDescription(NoOpAnnotator.class);

		tagger = createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, languageCode);
		lemmatizer = createEngineDescription(ClearNlpLemmatizer.class);
		parser = createEngineDescription(
				ClearNlpParser.class,
				ClearNlpParser.PARAM_LANGUAGE, languageCode,
				ClearNlpParser.PARAM_VARIANT, "ontonotes"
				);


		if (languageCode.equals("en")){
			return createEngineDescription(
					createEngineDescription(
							ClearNlpSegmenter.class
							),
					tagger,
					lemmatizer,
					parser
					);
		} else if (languageCode.equals("de")){
			return createEngineDescription(
					createEngineDescription(
							OpenNlpSegmenter.class
							),
					tagger,
					lemmatizer,
					createEngineDescription(NoOpAnnotator.class)
					);
		} else {
			System.err.println("Unknown language code "+languageCode+". We currently support: en, de");
			System.exit(-1);
		}
		return null;
	}
	
	public static AnalysisEngineDescription getPreprocessingSimple(String languageCode) throws ResourceInitializationException {
		if (languageCode.equals("en")){
			return createEngineDescription(
					createEngineDescription(
							ClearNlpSegmenter.class
							)
					);
		} else if (languageCode.equals("de")){
			return createEngineDescription(
					createEngineDescription(
							OpenNlpSegmenter.class
							)
					);
		} else {
			System.err.println("Unknown language code "+languageCode+". We currently support: en, de");
			System.exit(-1);
		}
		return null;
	}

	public static AnalysisEngineDescription getEmptyPreprocessing() throws ResourceInitializationException {
		
			return createEngineDescription(
					createEngineDescription(
							NoOpAnnotator.class
							)
					);
	}


	// ######### EXPERIMENTAL SETUPS ##########
	// ##### TRAIN-TEST #####
	protected static void runTrainTest(ParameterSpace pSpace, String name, AnalysisEngineDescription aed)
			throws Exception
	{
		System.out.println("Running experiment "+name);
		ExperimentTrainTest batch = new ExperimentTrainTest(name + "-TrainTest");
		batch.setPreprocessing(aed);
		//batch.addInnerReport(GradingEvaluationReport.class);
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(GradingEvaluationReport.class);
		batch.addReport(TrainTestReport.class);
		// Run
		Lab.getInstance().run(batch);
	}

	// ##### CV #####
	protected static void runCrossValidation(ParameterSpace pSpace, String name, AnalysisEngineDescription aed, int numFolds)
			throws Exception
	{
		ExperimentCrossValidation batch = new ExperimentCrossValidation(name + "-CV", numFolds);
		batch.setPreprocessing(aed);
		// TODO: adapt so that it also works from this slightly different context
		//	batch.addInnerReport(GradingEvaluationReport.class);
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(CrossValidationReport.class);
		batch.addReport(CvEvaluationReport.class);

		// Run
		Lab.getInstance().run(batch);
	}



	// ##### LEARNING-CURVE #####
	public static void runLearningCurve(ParameterSpace pSpace, String name, String languageCode)
			throws Exception
	{
		System.out.println("Running experiment "+name);
		ExperimentTrainTest batch = new ExperimentTrainTest(name + "-LearningCurve");
		batch.setPreprocessing(getPreprocessing(languageCode));
		batch.addInnerReport(new LearningCurveReport());    
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		// TODO: wieso wird der nicht ausgeführt?
		batch.addReport(new RuntimeReport());
		// Run
		Lab.getInstance().run(batch);
	}
	
	
	public static void runLearningCurveCV(ParameterSpace pSpace, String name, String languageCode, int numFolds)
			throws Exception
	{
		System.out.println("Running experiment "+name);
		ExperimentCrossValidation batch = new ExperimentCrossValidation(name + "-LearningCurve", numFolds);
		batch.setPreprocessing(getPreprocessing(languageCode));
		batch.addInnerReport(new LearningCurveReport());    
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		// TODO: wieso wird der nicht ausgeführt?
		batch.addReport(new RuntimeReport());
		batch.addReport(CvLearningCurveReport.class);
		//batch.addReport(CrossValidationReport.class); // generischer Report, bringt hier vermutlich nix
		//batch.addReport(CvEvaluationReport.class); // unser CV report, bringt wohl auch nix
		// Run
		Lab.getInstance().run(batch);
	}
	
	

	// ##### CLUSTERING #####
	protected static void runClustering(ParameterSpace pSpace, String name, String languageCode)
			throws Exception
	{
		BatchTaskClustering batch = new BatchTaskClustering(name + "-Clustering");   
		batch.setPreprocessing(getPreprocessing(languageCode));
		//   System.out.println(batch.getPreprocessing());
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(TrainTestReport.class);
		//   batch.addReport(BatchOutcomeIDReport.class);
		batch.addReport(RuntimeReport.class);

		// Run
		Lab.getInstance().run(batch);
	}


	// ##### CLUSTERING WITH LABEL PROPAGATION #####
	protected static void runClusteringLabelPropagation(ParameterSpace pSpace, String name, String languageCode)
			throws Exception
	{
		BatchTaskClusterLabelPropagation batch = new BatchTaskClusterLabelPropagation(name + "-Clustering");    
		batch.setPreprocessing(getPreprocessing(languageCode));
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addReport(new TrainTestReport());
		//   batch.addReport(BatchOutcomeIDReport.class);
		batch.addReport(new RuntimeReport());

		// Run
		Lab.getInstance().run(batch);
	}


	// ##### CLUSTERING + CLASSIFICATION WITH CENTROIDS #####
	protected static void runClusterClassificationCentroids(ParameterSpace pSpace, String name, String languageCode)
			throws Exception
	{
		BatchTaskClusterClassificationExemplar batch = new BatchTaskClusterClassificationExemplar(name + "-ClusterClassification");    
		batch.setPreprocessing(getPreprocessing(languageCode));
		batch.setParameterSpace(pSpace);
		batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
		batch.addInnerReport(GradingEvaluationReportClusteringCurve.class);   
		//   batch.addInnerReport(KappaReport.class);
		//   batch.addReport(BatchTrainTestReport.class);
		//    batch.addReport(BatchOutcomeIDReport.class);
		batch.addReport(new RuntimeReport());

		// Run
		Lab.getInstance().run(batch);
	}
}
