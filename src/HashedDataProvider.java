public class HashedDataProvider implements DataProvider<String, Integer> {
    private int _queries = 0;
    public Integer get(String key) {
        _queries++;
        return key.hashCode();
    }
    public int getQueries() {
        return _queries;
    }
}
