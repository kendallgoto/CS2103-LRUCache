import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
public class WebQueryDataProvider implements DataProvider<String, Integer> {
    private int _queries = 0;
    public int getQueries() {
        return _queries;
    }

    public Integer get(String key) {
        int responseCode;
        _queries++;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(key).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            responseCode = connection.getResponseCode();
        }
        catch(IOException | NullPointerException | ClassCastException e ) {
            return -1; //IO exception if the openConnection fails, NPE if "URL(key)" fails to parse, CCE if file:// is accessed
        }
        return responseCode;
    }
}
