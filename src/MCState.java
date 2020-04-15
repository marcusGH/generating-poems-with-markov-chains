import java.util.stream.IntStream;

public class MCState {
    // should support up to 4 words
    private String[] words;

    /**
     * Creates an instance with as many words as we need
     * @param words should contain no more than 4 string elements
     */
    public MCState(String ... words) {
        this.words = words;
    }

    /**
     * @return the first two words stored (if the
     * state contains two or more words)
     */
    public MCState firstTwo() {
        return new MCState(words[0], words[1]);
    }

    /**
     * @return the last two words (if the state
     * contains 4 or more words)
     */
    public MCState lastTwo() {
        return new MCState(words[2], words[3]);
    }

    /**
     * @return the first word stored
     */
    public String first() {
        if (words != null && words.length > 0)
            return words[0];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    /**
     * @return the second word stored
     */
    public String second() {
        if (words != null && words.length > 1)
            return words[1];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    /**
     * @return the third word stored
     */
    public String third() {
        if (words != null && words.length > 2)
            return words[2];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    /**
     * @return the fourth word stored
     */
    public String fourth() {
        if (words != null && words.length > 3)
            return words[3];
        else
            throw new RuntimeException("MCState does not contain enough words");
    }

    /**
     * Creates a unique hash based on the stored words by
     * multiplying the words' indices with their respective hash codes
     * @return an integer hashcode
     */
    @Override
    public int hashCode() {
        // sum over the hash-codes and multiply by index
        return IntStream.range(0, words.length)
                .map(i -> (i + 1) * words[i].hashCode())
                .reduce(0, Integer::sum);
    }

    /**
     * Compares to objects
     * @param o the object to compare with
     * @return whether this object is equal to the other object
     */
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

    /**
     * splits the different words by a space character to get
     * a string representation of the state
     * @return the string representation of the state
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (String s : this.words)
            ret.append(s).append(" ");
        return ret.toString();
    }

}
