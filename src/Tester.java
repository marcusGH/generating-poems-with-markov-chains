import edu.stanford.nlp.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Tester {
    /**
     * These poem files should have been ran through the python formatter such that
     * each end of line word is followed by the string EOL
     */
    static final Path dataDirectory = Paths.get("data/formatted-poems/");

    /**
     * Trains the markov chain models based on presented training data and generates
     * javascript dictionaries with the weights for use in the website demo
     * @param args  not supported
     * @throws IOException if training data cannot be located
     */
    public static void main(String[] args) throws IOException {
        // gets the set of paths to all files in the data directory
        Set<Path> poemFiles = Files.walk(dataDirectory).filter(Files::isRegularFile).collect(Collectors.toSet());
        // tokenizes the different poems to get the training data
        Set<List<String>> trainingData = DataPreparation.loadPoems(poemFiles);

        // debug message
        System.out.println(trainingData.size() + " poems have now been loaded.");

        // count number of distinct words (for debugging)
        Map<String, Integer> wordCount = new HashMap<>();
        for (List<String> p : trainingData)
            for (String s : p) {
                if (!wordCount.containsKey(s))
                    wordCount.put(s, 0);
                wordCount.put(s, wordCount.get(s) + 1);
            }

        // sort by frequency with a very *simple* stream ;) and print word frequencies (for debugging)
        wordCount = wordCount.entrySet().stream().sorted((x, y) -> y.getValue() - x.getValue()).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println(wordCount);
        System.out.println(wordCount.size());

        // picks out a subset of poems (for use if we have more training data than what we want to use)
        int numPoems = 180;
        Set<List<String>> partialTrainingData =
                new HashSet<>(new ArrayList<>(trainingData).subList(0, numPoems));

        // instantiate our implementation with the training data
        MarkovChain mc = new MarkovChain(trainingData);
        // train the model
        mc.generateTransitionProbabilities();
        // extract the "weights"
        Map<String, List<Pair<String, Double>>> firstOrders = mc.firstOrders();
        Map<MCState, List<Pair<String, Double>>> secondOrders = mc.secondOrders();

        // print them out for debugging
        System.out.println(firstOrders);
        System.out.println(secondOrders);

        // generate the javascript JSON dictionaries
        mc.generateJSDictionary();
    }
}
