import java.util.Arrays;
import java.util.stream.IntStream;

public class MCState {
    // should support up to 4 words
    private String[] words;

    public MCState(String ... words) {
        this.words = words;
    }

    public MCState firstTwo() {
        return new MCState(words[0], words[1]);
    }

    public MCState lastTwo() {
        return new MCState(words[2], words[3]);
    }

    public String first() {
        if (words != null && words.length > 0)
            return words[0];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    public String second() {
        if (words != null && words.length > 1)
            return words[1];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    public String third() {
        if (words != null && words.length > 2)
            return words[2];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    public String fourth() {
        if (words != null && words.length > 3)
            return words[3];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    @Override
    public int hashCode() {
        // sum over the hash-codes and multiply by index
        return IntStream.range(0, words.length)
                .map(i -> (i + 1) * words[i].hashCode())
                .reduce(0, Integer::sum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (this.getClass() != o.getClass())
            return false;
        MCState other = (MCState)o;
        if (other.words == null || other.words.length != this.words.length)
            return false;
        for (int i = 0; i < this.words.length; i++) {
            if (!other.words[i].equals(this.words[i]))
                return false;
        }
        return true;
    }

}
