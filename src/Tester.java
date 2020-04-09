import edu.stanford.nlp.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Tester {
    static final Path dataDirectory = Paths.get("data/formatted-poems/");

    public static void main(String[] args) throws IOException {
        Set<Path> poemFiles = Files.walk(dataDirectory).filter(Files::isRegularFile).collect(Collectors.toSet());
//        poemFiles = new HashSet<Path>(Arrays.asList(Paths.get("data/0")));
        // get the poems
        Set<List<String>> poems = DataPreparation.loadPoems(poemFiles);

        System.out.println(poems.size() + " poems loaded.");

//        System.out.println(poems.toArray()[0]); // for -debug

        // count number of distinct words
        Map<String, Integer> wordCount = new HashMap<>();

        for (List<String> p : poems)
            for (String s : p) {
                if (!wordCount.containsKey(s))
                    wordCount.put(s, 0);
                wordCount.put(s, wordCount.get(s) + 1);
            }

        // sort by frequency with a very simple stream ;)
        wordCount = wordCount.entrySet().stream().sorted((x, y) -> y.getValue() - x.getValue()).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        System.out.println(wordCount); // for -debug
        System.out.println(wordCount.size()); // for -debug

        Set<List<String>> somePoems = new HashSet<>(new ArrayList<List<String>>(poems).subList(0, 180));

        // implementation
        MarkovChain mc = new MarkovChain(poems);
//        System.out.println(mc.generatePoem(100000, 0.9));

        Map<String, List<Pair<String, Double>>> firstOrders = mc.firstOrders();
        Map<MCState, ArrayList<Pair<String, Double>>> secondOrders = mc.secondOrders();

        System.out.println(firstOrders);
        System.out.println(secondOrders);

        mc.generateJSDictionary();

    }


}
