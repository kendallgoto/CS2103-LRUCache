import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Code to test an <tt>LRUCache</tt> implementation.
 */
public class CacheTest {
	//DataProvider testing suite
	@Test
	public void WebQueryDP_TestCaptive() { //Internet connection required
		DataProvider<String, Integer> provider = new WebQueryDataProvider();
		int captive_apple = provider.get("http://captive.apple.com/");
		int captive_micro = provider.get("http://www.msftconnecttest.com/");
		int captive_google = provider.get("http://connectivitycheck.gstatic.com/generate_204");
		int captive_firefox = provider.get("http://detectportal.firefox.com/success.txt");
		int captive_https = provider.get("https://httpstat.us/999");
		assertEquals(captive_apple, 200);
		assertEquals(captive_micro, 200);
		assertEquals(captive_google, 204);
		assertEquals(captive_firefox, 200);
		assertEquals(captive_https, 999);
		//fail cases:
		int malformedURL = provider.get("htt//");
		int noURL = provider.get("");
		int relativeURL = provider.get("/something/");
		int filesystem = provider.get("file://");
		assertEquals(malformedURL, -1);
		assertEquals(noURL, -1);
		assertEquals(relativeURL, -1);
		assertEquals(filesystem, -1);
	}
	@Test
	public void WebQueryDP_TestFail() {
		DataProvider<String, Integer> provider = new WebQueryDataProvider();
		//fail cases:
		int malformedURL = provider.get("htt//");
		int noURL = provider.get("");
		int relativeURL = provider.get("/something/");
		int filesystem = provider.get("file://");
		assertEquals(malformedURL, -1);
		assertEquals(noURL, -1);
		assertEquals(relativeURL, -1);
		assertEquals(filesystem, -1);
	}
	@Test
	public void HashedDP_TestString() {
		DataProvider<String, Integer> provider = new HashedDataProvider();
		assertEquals(provider.get("hello"), (Integer)("hello".hashCode()));
		assertEquals(provider.get("bye"), (Integer)("bye".hashCode()));
		assertEquals(provider.get("123"), (Integer)("123".hashCode()));
		assertEquals(provider.get(""), (Integer)("".hashCode()));
	}
	@Test
	public void WebQueryDP_QueryCount() {
		WebQueryDataProvider provider = new WebQueryDataProvider(); //weakest would be DataProvider, but we're using unique methods
		assertEquals(provider.getQueries(), 0);
		provider.get("http://captive.apple.com/");
		assertEquals(provider.getQueries(), 1);
		provider.get("http://captive.apple.com/");
		provider.get("http://captive.apple.com/");
		assertEquals(provider.getQueries(), 3);

		WebQueryDataProvider provider2 = new WebQueryDataProvider();
		assertEquals(provider2.getQueries(), 0); //test nonstatic incrementation
	}
	@Test
	public void HashedDP_QueryCount() {
		HashedDataProvider provider = new HashedDataProvider(); //weakest would be DataProvider, but we're using unique methods
		assertEquals(provider.getQueries(), 0);
		provider.get("hello");
		assertEquals(provider.getQueries(), 1);
		provider.get("world");
		provider.get("hello");
		assertEquals(provider.getQueries(), 3);
		HashedDataProvider provider2 = new HashedDataProvider();
		assertEquals(provider2.getQueries(), 0); //test nonstatic incrementation
	}

	//Cache Test Suite
	@Test
	public void leastRecentlyUsedIsCorrect () {
		DataProvider<String,Integer> provider = new WebQueryDataProvider();
		Cache<String,Integer> cache = new LRUCache<String,Integer>(provider, 3);
		assertEquals(cache.getNumMisses(), 0);

		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //miss
		assertEquals(cache.getNumMisses(), 1);

		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //hit
		assertEquals(cache.getNumMisses(), 1);

		assertEquals(cache.get("2"), (Integer)("2".hashCode())); //miss
		assertEquals(cache.get("3"), (Integer)("3".hashCode())); //miss
		assertEquals(cache.get("4"), (Integer)("4".hashCode())); //miss
		//since cap=3, this is our 4th item so we have to evict the first (1)
		assertEquals(cache.getNumMisses(), 4);
		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //miss (previously evicted)
		assertEquals(cache.getNumMisses(), 5);
	}

}
