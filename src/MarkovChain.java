import edu.stanford.nlp.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MarkovChain {
    private final double alpha = 0.9;

    /**
     * The training data is stored here as well.
     * The first order transition probabilities only use a single word as a key,
     *      so the corresponding map only uses a string.
     * The second order transition probabilities use the previous two words as a key,
     *      so a custom MCState is used. {@see MCState}
     */
    private Set<List<String>> trainingData;
    private Map<String, List<Pair<String, Double>>> firstOrderTransitionProbabilities;
    private Map<MCState, List<Pair<String, Double>>> secondOrderTransitionProbabilities;

    /**
     * Trains the model by calculating first- and second order transition probabilities
     * based on the poems passed to it
     * @param poems the set of poems that will be used as training data. Must already be tokenized and
     *              processed
     */
    public MarkovChain(Set<List<String>> poems) {
        this.trainingData = poems;
    }

    /**
     * Auxiliary predicative that tells us if a word is an EOL word
     * @param s the string to test for
     * @return whether the string is an EOL word
     */
    private boolean isEOL(String s) {
        return s.endsWith("\n");
    }

    /**
     * @return the first order transition probabilities
     */
    public Map<String, List<Pair<String, Double>>> firstOrders() {
        return this.firstOrderTransitionProbabilities;
    }

    /**
     * @return the second order transition probabilities
     */
    public Map<MCState, List<Pair<String, Double>>> secondOrders() {
        return this.secondOrderTransitionProbabilities;
    }

    /**
     * Creates a javascript file with a single variable that stored the
     * transition probabilities of both the first- and second-order model
     * in JSON format.
     * @throws IOException if some bad stuff happens
     */
    public void generateJSDictionary() throws IOException {
        // create the files
        final String dataDirectory = "website/data2/";
        File f1 = new File(dataDirectory + "firstOrder.js");
        File f2 = new File(dataDirectory + "secondOrder.js");
        if (f1.createNewFile()) {
            System.out.println("File created: " + f1.getName());
        }
        else
            System.out.println("Did not create file because it already exists");
        if (f2.createNewFile()) {
            System.out.println("File created: " + f2.getName());
        }
        else
            System.out.println("Did not create file because it already exists");

        // write to the files
        FileWriter fw1 = new FileWriter(f1.getAbsolutePath());
        FileWriter fw2 = new FileWriter(f2.getAbsolutePath());

        // get the dictionaries
        Map<String, List<Pair<String, Double>>> firstOrders = this.firstOrders();
        Map<MCState, List<Pair<String, Double>>> secondOrders = this.secondOrders();

        // make first orders first
        fw1.write("var firstOrder = {\n");
        for (String key : firstOrders.keySet()) {
            fw1.write('"' + key.replace("\n","\\n") + "\":");
            StringBuilder value = new StringBuilder("[");
            for (Pair<String,Double> val : firstOrders.get(key)) {
                value.append("[\"").append(val.first.replace("\n","\\n")).append("\",").append(val.second).append("],");
            }
            // delete last comma
            value.deleteCharAt(value.length() - 1);
            value.append("]");
            // write the value to the key
            fw1.write(value.toString() + ",\n");
        }
        fw1.write("\"REMOVEME\":[[\"WILL DO\", 1.0]]\n");
        fw1.write("};\n\n\n\n\n");
        fw1.close();

        // no write secondOrders
        fw2.write("var secondOrder = {\n");
        for (MCState key : secondOrders.keySet()) {
            // write the key
            fw2.write("\"" + key.first().replace("\n","\\n") + " " +
                    key.second().replace("\n", "\\n") + "\":");
            // build the value
            StringBuilder value = new StringBuilder("[");
            for (Pair<String,Double> val : secondOrders.get(key)) {
                value.append("[\"").append(val.first.replace("\n","\\n")).append("\",").append(val.second).append("],");
            }
            // delete last comma
            value.deleteCharAt(value.length() - 1);
            value.append("]");
            // write the value to the key
            fw2.write(value.toString() + ",\n");
        }
        fw2.write("\"REMOVEME REMOVEME\":[[\"WILL DO\", 1.0]]\n");
        fw2.write("};\n\n\n\n\n");
        fw2.close();
    }

    /**
     * Trains the first- and second-order model using the training data given
     * during construction. {@see #MarkovChain}
     */
    public void generateTransitionProbabilities() {
        // transitions from this state
        Map<MCState, Double> transFromWordState = new HashMap<>();
        // transitions from state i to state j
        Map<MCState, Double> transFromToWordState = new HashMap<>();
        // separate transitions for EOL words
        Map<MCState, Double> transFromEOLWord = new HashMap<>();
        Map<MCState, Double> transFromToEOLWord = new HashMap<>();
        // first order markov chain transitions
        Map<String, Map<String, Double>> firstOrderTrans = new HashMap<>();
        // count vocabulary
        Map<String, Integer> regVocab = new HashMap<>();
        Map<String, Integer> eolVocab = new HashMap<>();
        // need this special state
        regVocab.put("", 1);
        eolVocab.put("", 1);


        // NOTE: Counting necessary transitions --------------------------------------------------------------

        int i = 0;
        // go through training set
        for (List<String> p : trainingData) {
            System.out.println(i++ + " poems out of " + trainingData.size() + " scanned."); // for debugging
            // special EOL state
            String eolWord = "";
            // special state for first two words;
            String prevPrevWord = "";
            String prevWord = "";

            // go through poem
            for (String s : p) {
                if (!eolVocab.containsKey(s)) {
                    eolVocab.put(s, 0);
                    regVocab.put(s, 0);
                }
                // count vocabulary
                if (isEOL(s)) {
                    eolVocab.put(s, eolVocab.get(s) + 1);
                }
                else {
                    regVocab.put(s, regVocab.get(s) + 1);
                }

                // record previous state
                MCState prevWordState = new MCState(prevPrevWord, prevWord);
                // figure out current state
                MCState curWordState = new MCState(prevWord, s);

                // count first order transitions, by first handling KeyErrors...
                if (!firstOrderTrans.containsKey(prevWord))
                    firstOrderTrans.put(prevWord, new HashMap<>());
                if (!firstOrderTrans.get(prevWord).containsKey(s))
                    firstOrderTrans.get(prevWord).put(s, 0.0);
                // then actually incrementing the counter
                firstOrderTrans.get(prevWord).put(s, firstOrderTrans.get(prevWord).get(s) + 1);

                // count transition from prevState, by handling KeyErrors...
                if (!transFromWordState.containsKey(prevWordState))
                    transFromWordState.put(prevWordState, 0.0);
                // then incrementing
                transFromWordState.put(prevWordState, transFromWordState.get(prevWordState) + 1.0);

                // count second order transitions, by first handling KeyErrors....
                MCState mergedState = new MCState(prevPrevWord, prevWord, prevWord, s);
                if (!transFromToWordState.containsKey(mergedState))
                    transFromToWordState.put(mergedState, 0.0);
                // then incrementing
                transFromToWordState.put(mergedState, transFromToWordState.get(mergedState) + 1.0);

                // count trans from EOL words, by handling KeyErrors
                MCState eolWordState = new MCState(eolWord);
                if (!transFromEOLWord.containsKey(eolWordState))
                    transFromEOLWord.put(eolWordState, 0.0);
                // then incrementing
                transFromEOLWord.put(eolWordState, transFromEOLWord.get(eolWordState) + 1.0);

                // count trans from EOL to current word
                MCState eolToCurState = new MCState(eolWord, s);
                if (!transFromToEOLWord.containsKey(eolToCurState))
                    transFromToEOLWord.put(eolToCurState, 0.0);
                // increment
                transFromToEOLWord.put(eolToCurState, transFromToEOLWord.get(eolToCurState) + 1.0);

                // update words for the next iteration
                if (isEOL(prevWord))
                    eolWord = prevWord;
                prevPrevWord = prevWord;
                prevWord = s;
            }
        }

        // NOTE: calculating transition probabilities -----------------------------------------

        // second order transition probabilities
        Map<MCState, List<Pair<String, Double>>> secondOrderTransitionProbs = new HashMap<>();

        int counter = 0;    // for debugging
        for (MCState ABBC : transFromToWordState.keySet()) {
            // debug purposes
            if (counter % 1000 == 0)
                System.out.println("Calculated probabilities from " + (counter++) +
                        " states out of " + transFromToWordState.size() + "\n\t" +
                        " heapspace used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) +
                        " free heapspace available: " + (Runtime.getRuntime().maxMemory() -
                        Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()));
            else
                counter++;    // debugging

            // first two words
            MCState AB = ABBC.firstTwo();
            // cur word
            String C = ABBC.fourth();

            // calculate 2nd order transition probability without smoothing
            double transToCProb = (transFromToWordState.get(ABBC) / transFromWordState.get(AB));
            // store it
            if (!secondOrderTransitionProbs.containsKey(AB))
                secondOrderTransitionProbs.put(AB, new ArrayList<>());
            secondOrderTransitionProbs.get(AB).add(new Pair<>(C, transToCProb));
        }

        // first order transition probabilities
        Map<String, List<Pair<String, Double>>> firstOrderTransitionProbs = new HashMap<>();

        for (String s : firstOrderTrans.keySet()) {
            for (String w : firstOrderTrans.get(s).keySet()) {
                // calculate transition probability without smoothing
                double prob = firstOrderTrans.get(s).get(w) / (regVocab.get(s) + eolVocab.get(s));
                // nothing added before
                if (!firstOrderTransitionProbs.containsKey(s))
                    firstOrderTransitionProbs.put(s, new ArrayList<>());
                // store it
                firstOrderTransitionProbs.get(s).add(new Pair<>(w, prob));
            }
        }

        // finally store the results
        this.firstOrderTransitionProbabilities = firstOrderTransitionProbs;
        this.secondOrderTransitionProbabilities = secondOrderTransitionProbs;
    }
}
