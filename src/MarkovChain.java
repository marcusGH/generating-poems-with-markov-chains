import edu.stanford.nlp.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MarkovChain {
    private final double alpha = 0.9;

    private Pair<Map<String, List<Pair<String, Double>>>, Map<MCState, ArrayList<Pair<String, Double>>>> transitionProbabilities;

    public MarkovChain(Set<List<String>> poems) {
        this.transitionProbabilities = getTransitionProbabilities(poems);
    }

    private boolean isEOL(String s) {
        return s.endsWith("\n");
    }

//    private ArrayList<String> makeList(String ... strings) {
//        return new ArrayList<>(Arrays.asList(strings));
//    }

    public Map<String, List<Pair<String, Double>>> firstOrders() {
        return this.transitionProbabilities.first;
    }

    public Map<MCState, ArrayList<Pair<String, Double>>> secondOrders() {
        return this.transitionProbabilities.second;
    }

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
        Map<MCState, ArrayList<Pair<String, Double>>> secondOrders = this.secondOrders();

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

    public Pair<Map<String, List<Pair<String, Double>>>, Map<MCState, ArrayList<Pair<String, Double>>>> getTransitionProbabilities(Set<List<String>>  poems) {
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

        int i = 0;
        // go through training set
        for (List<String> p : poems) {
            System.out.println(i++ + " poems out of " + poems.size() + " scanned.");
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
//                List<String> prevWordState = new ArrayList<String>(Arrays.asList(prevPrevWord,prevWord));
                // figure out current state
                MCState curWordState = new MCState(prevWord, s);
//                List<String> curWordState = new ArrayList<String>(Arrays.asList(prevWord,s));

                // remember to -debug !!
                //System.out.println("|" +curState+"|");

                // count first order transitions
                if (!firstOrderTrans.containsKey(prevWord))
                    firstOrderTrans.put(prevWord, new HashMap<>());
                if (!firstOrderTrans.get(prevWord).containsKey(s))
                    firstOrderTrans.get(prevWord).put(s, 0.0);
                firstOrderTrans.get(prevWord).put(s, firstOrderTrans.get(prevWord).get(s) + 1);

                // count transition from prevState
                if (!transFromWordState.containsKey(prevWordState))
                    transFromWordState.put(prevWordState, 0.0);
                transFromWordState.put(prevWordState, transFromWordState.get(prevWordState) + 1.0);

                // count trans from and to
                MCState mergedState = new MCState(prevPrevWord, prevWord, prevWord, s);
//                List<String> mergedState = new ArrayList<>(prevWordState);
//                mergedState.addAll(curWordState);
                if (!transFromToWordState.containsKey(mergedState))
                    transFromToWordState.put(mergedState, 0.0);
                transFromToWordState.put(mergedState, transFromToWordState.get(mergedState) + 1.0);

                // count trans from EOL words
                MCState eolWordState = new MCState(eolWord);
                if (!transFromEOLWord.containsKey(eolWordState))
                    transFromEOLWord.put(eolWordState, 0.0);
                transFromEOLWord.put(eolWordState,
                        transFromEOLWord.get(eolWordState) + 1.0);

                // count trans from EOL to current word
                MCState eolToCurState = new MCState(eolWord, s);
//                List<String> eolToCurState = new ArrayList<>(Arrays.asList(eolWord, s));
                if (!transFromToEOLWord.containsKey(eolToCurState))
                    transFromToEOLWord.put(eolToCurState, 0.0);
                transFromToEOLWord.put(eolToCurState, transFromToEOLWord.get(eolToCurState) + 1.0);

                // update words
                if (isEOL(prevWord))
                    eolWord = prevWord;
                prevPrevWord = prevWord;
                prevWord = s;
            }
        }

        // NOTE: calculating transition probabilities

        Map<MCState, ArrayList<Pair<String, Double>>> secondOrderTransitionProbs = new HashMap<>();

        int counter = 0;
        for (MCState ABBC : transFromToWordState.keySet()) {
            if (counter % 1000 == 0)
                System.out.println("Calculated probabilities from " + (counter++) +
                        " states out of " + transFromToWordState.size() + "\n\t" +
                        " heapspace used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) +
                        " free heapspace available: " + (Runtime.getRuntime().maxMemory() -
                        Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()));
            else
                counter++;
            // first two words
            MCState AB = ABBC.firstTwo();
            // cur word
            String C = ABBC.fourth();

            // calculate 2nd order markov transition
            double transToCProb = (transFromToWordState.get(ABBC) / transFromWordState.get(AB));
            if (!secondOrderTransitionProbs.containsKey(AB))
                secondOrderTransitionProbs.put(AB, new ArrayList<>());
            secondOrderTransitionProbs.get(AB).add(new Pair<>(C, transToCProb));

            // calculate transitions from EOLs
//            for (String eol : eolVocab) {
//                MCState eolCurWord = new MCState(eol, C);
//                MCState eolState = new MCState(eol);
//
//                double transEolCProb;
//                if (transFromToEOLWord.containsKey(eolCurWord))
//                    transEolCProb = (transFromToEOLWord.get(eolCurWord) + 1) /
//                            (transFromEOLWord.get(eolState) + (double)regVocab.size());
//                // no transition but there is something from the eol word, so apply smoothing
//                else if (transFromEOLWord.containsKey(eolState)) {
//                    transEolCProb = 0 / (transFromEOLWord.get(eolState) + (double)regVocab.size());
//                }
//                else
//                    transEolCProb = 0;
//
//                // fuzzy compare
//                if (transEolCProb > 0.00001) {
//                    //record prob
//                    MCState fromState = new MCState(eol, AB.first(), AB.second());
//                    if (!transitionProbabilities.containsKey(fromState))
//                        transitionProbabilities.put(fromState, new ArrayList<>());
//                    transitionProbabilities.get(fromState).
//                            add(new Pair<>(C, transToCProb * transEolCProb));
//                }
//            }
        }

        // NOTE : calculate first order transitions
        Map<String, List<Pair<String, Double>>> firstOrderTransitionProbs = new HashMap<>();
        for (String s : firstOrderTrans.keySet()) {
            for (String w : firstOrderTrans.get(s).keySet()) {
                double prob = firstOrderTrans.get(s).get(w) / (regVocab.get(s) + eolVocab.get(s));
                // nothing added before
                if (!firstOrderTransitionProbs.containsKey(s))
                    firstOrderTransitionProbs.put(s, new ArrayList<>());
                firstOrderTransitionProbs.get(s).add(new Pair<>(w, prob));
            }
        }
        return new Pair<>(firstOrderTransitionProbs, secondOrderTransitionProbs);
    }

    public String generatePoem(int len, double alpha) {
        // previous words
        String prevPrevWord = "";
        String prevWord = "";
        String eolWord = "";

        StringBuilder poem = new StringBuilder("");

        for (int i = 0; i < len; i++) {
            // determine if we're doing first order or second order
            double rand = Math.random();
            // possible next words
            List<Pair<String, Double>> nextWords;
            // use first order
            if (rand < alpha)
                nextWords = this.transitionProbabilities.second.get(new MCState(prevPrevWord, prevWord));
            else
                nextWords = this.transitionProbabilities.first.get(prevWord);
            // the next word
            String next = "";

            if (nextWords == null)
                break;
            // debug the size
            System.out.print(nextWords.size() + " ");

            // total probability
            double totalProb = nextWords.stream().map(p -> p.second).reduce(0.0, Double::sum);
            // generate random number in range [0, totalProb)
            rand = Math.random() * totalProb;
            // go through list and decide which word to use
            double prevAccumProb = 0.0;
            for (Pair<String, Double> p : nextWords) {
                // use the word
                if (rand < prevAccumProb + p.second) {
                    next = p.first;
                    break;
                }
                else
                    prevAccumProb += p.second;
            }

            // add the word
            poem.append(next).append(" ");
            // update words
            prevPrevWord = prevWord;
            prevWord = next;
            if (isEOL(next))
                eolWord = next;
        }
        System.out.println("");

        return poem.toString();
    }

    public static void main(String[] args) {
    }
}
