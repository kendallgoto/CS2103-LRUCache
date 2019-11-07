public class HashedDataProvider implements DataProvider<String, Integer> {
    private int _queries = 0;

    /**
     * Returns the hashCode of a given string.
     * @param key the string to hash
     * @return the hashCode of the given string
     */
    public Integer get(String key) {
        _queries++;
        return key.hashCode();
    }

    /**
     * Returns the total number of queries ran
     * @return the total number of queries ran
     */
    public int getQueries() {
        return _queries;
    }
}
