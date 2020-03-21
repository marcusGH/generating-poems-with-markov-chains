
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class DataPreparation {

    //Set<String> ignoreList = new HashSet<>(Arrays.asList(".", ":", ";", "", "--", ))
    public static Set<List<String>> loadPoems(Set<Path> paths) throws IOException {
        Set<List<String>> result = new HashSet<>();

        for (Path p : paths) {
            List<String> wordsSource = Tokenizer.tokenize(p);
            List<String> wordsProcessed = new ArrayList<>();
            // process the string
            for (String word : wordsSource) {
                // is a proper word
                if (word.length() > 0 && Character.isAlphabetic(word.charAt(0))) {
                    // it is an EOL char
                    if (word.equals("eol") && wordsProcessed.size() > 0)
                        wordsProcessed.set(wordsProcessed.size() - 1,
                                wordsProcessed.get(wordsProcessed.size() - 1) + "\n");
                    else
                        wordsProcessed.add(word);
                }
            }
            result.add(wordsProcessed);
        }
        return result;
    }
}
