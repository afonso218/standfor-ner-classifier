import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifierEvaluator;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.WordShapeClassifier;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.Timing;
import edu.stanford.nlp.util.Triple;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Afonso on 05/04/17.
 */
public class StanfordNER_API {

    /**
     * Directory of classifier files
     */
    private static final String DIRECTORY = "stanford-ner/classifiers/";

    /**
     * Classes: Location, Person, Organization
     */
    public static final String CLASSIFIER_FILENAME_3CLASS = DIRECTORY + "english.all.3class.distsim.crf.ser.gz";

    /**
     * Classes: Location, Person, Organization, Misc
     * Type: CONLL
     */
    public static final String CLASSIFIER_FILENAME_4CLASS = DIRECTORY + "english.conll.4class.distsim.crf.ser.gz";

    /**
     * Classes: Time, Location, Organization, Person, Money, Percent, Date
     * Type: MUC
     */
    public static final String CLASSIFIER_FILENAME_7CLASS = DIRECTORY + "english.muc.7class.distsim.crf.ser.gz";


    public static LinkedHashMap<String, LinkedHashSet<String>> identifyNER(String text, String classifier_filename) {
        return identifyNER(text, classifier_filename, true);
    }

    public static LinkedHashMap<String, LinkedHashSet<String>> identifyNER(String text, String classifier_filename, boolean print_predictions) {

        System.out.println(classifier_filename);

        CRFClassifier<CoreLabel> classifier = CRFClassifier.getClassifierNoExceptions(classifier_filename);

        List<List<CoreLabel>> classify = classifier.classify(text);

        return printPredictedEntities(classify);

    }



    /** The main method. See the class documentation. */
    public static void advanceClassifyCoNLL(String content_filename, String classifier_filename) {

        Properties props = new Properties();
        props.setProperty("loadClassifier", classifier_filename);
        props.setProperty("testFile", content_filename);
        props.setProperty("inputEncoding", "iso-8859-1");

        /*
        props.setProperty("mergeTags", "false"); // Show all tags used
        props.setProperty("printFeatures", "true");
        props.setProperty("iobTags", "true");
        props.setProperty("iobWrapper", "false");
        props.setProperty("keepOBInMemory", "true");
        */

        //props.setProperty("printProbs", "true");

        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
        String testFile = flags.testFile;
        String loadPath = flags.loadClassifier;

        if (loadPath != null) {
            crf.loadClassifierNoExceptions(loadPath, props);
        } else {
            crf.loadDefaultClassifier();
        }

        crf.loadTagIndex();

        Triple<Double,Double,Double> results = null;

        try {

            if (testFile != null) {
                DocumentReaderAndWriter<CoreLabel> readerAndWriter = crf.defaultReaderAndWriter();
                if (crf.flags.searchGraphPrefix != null) {
                    crf.classifyAndWriteViterbiSearchGraph(testFile, crf.flags.searchGraphPrefix, readerAndWriter);
                } else if (crf.flags.printFirstOrderProbs) {
                    crf.printFirstOrderProbs(testFile, readerAndWriter);
                } else if (crf.flags.printFactorTable) {
                    crf.printFactorTable(testFile, readerAndWriter);
                } else if (crf.flags.printProbs) {
                    crf.printProbs(testFile, readerAndWriter);
                } else if (crf.flags.useKBest) {
                    int k = crf.flags.kBest;
                    crf.classifyAndWriteAnswersKBest(testFile, k, readerAndWriter);
                } else if (crf.flags.printLabelValue) {
                    crf.printLabelInformation(testFile, readerAndWriter);
                } else {
                    results = crf.classifyAndWriteAnswers(testFile, readerAndWriter, true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * Advances Function
     *
     * @param content_filename
     * @param classifier_filename
     * @return
     */
    public static void advanceIdentifyNER(String content_filename, String classifier_filename) {

        try {
            AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(classifier_filename);

            // Get Content
            String fileContents = IOUtils.slurpFile(content_filename);

            // Classify content
            List<List<CoreLabel>> results = classifier.classify(fileContents);

            printPredictedEntities(results);

            /*

            System.out.println("---");
            List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);
            for (Triple<String, Integer, Integer> item : list) {
                System.out.println(item.first() + ": " + fileContents.substring(item.second(), item.third()));
            }
            System.out.println("---");
            System.out.println("Ten best entity labelings");
            DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.makePlainTextReaderAndWriter();
            classifier.classifyAndWriteAnswersKBest(content_filename, 10, readerAndWriter);

            DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.makePlainTextReaderAndWriter();
            System.out.println("---");
            System.out.println("Per-token marginalized probabilities");
            classifier.printProbs(content_filename, readerAndWriter);

            */

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Print List<List<CoreLabel>> classify
     * @param classify
     * @return
     */
    private static LinkedHashMap<String, LinkedHashSet<String>> printPredictedEntities(List<List<CoreLabel>> classify) {

        LinkedHashMap<String, LinkedHashSet<String>> map = new LinkedHashMap<>();

        for (List<CoreLabel> coreLabels : classify) {

            for (CoreLabel coreLabel : coreLabels) {

                String word = coreLabel.word();
                String category = coreLabel.get(CoreAnnotations.AnswerAnnotation.class);

                System.out.println(word + "\t" + category);

                if (!"O".equals(category)) {
                    if (map.containsKey(category)) {
                        map.get(category).add(word);
                    } else {
                        LinkedHashSet<String> temp = new LinkedHashSet<>();
                        temp.add(word);
                        map.put(category, temp);
                    }
                }

            }

        }

        System.out.println(map.toString());
        return map;
    }

    /**
     * Create Properties Object for Conll dataset
     * @return
     */
    public static Properties getPropertiesForCoNLL(){
         Properties props = new Properties();
         props.setProperty("conllNoTags", "true");
         props.setProperty("readerAndWriter", "edu.stanford.nlp.sequences.ColumnDocumentReaderAndWriter");
         props.setProperty("map", "word=0,answer=1");
         props.setProperty("useObservedSequencesOnly","true");
         props.setProperty("useLongSequences", "true");
         props.setProperty("useNGrams", "true");
         props.setProperty("usePrev", "true");
         props.setProperty("useNext", "true");
         props.setProperty("useWordPairs", "true");
         props.setProperty("useSequences", "true");
         props.setProperty("usePrevSequences", "true");
         props.setProperty("noMidNGrams", "true");
         props.setProperty("useReverse", "false");
         props.setProperty("useTypeSeqs", "true");
         props.setProperty("useTypeSeqs2", "true");
         props.setProperty("useTypeySequences", "true");
         props.setProperty("wordShape", "WordShapeClassifier.WORDSHAPEDAN2USELC");
         props.setProperty("useLastRealWord", "true");
         props.setProperty("useNextRealWord", "true");
         props.setProperty("sigma", "20.0");
         props.setProperty("adaptSigma", "20.0");
         props.setProperty("normalize", "true");
         props.setProperty("normalizeTimex", "true");
         props.setProperty("maxLeft", "2");
         props.setProperty("useDisjunctive", "true");
         props.setProperty("disjunctionWidth", "4");
         props.setProperty("useBoundarySequences", "true");
         props.setProperty("inputEncoding", "iso-8859-1");
         props.setProperty("useQN", "true");
         props.setProperty("QNsize", "15");

         return props;
     }

}

