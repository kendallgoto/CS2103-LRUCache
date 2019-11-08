import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;

/**
 * Code to test an <tt>LRUCache</tt> implementation.
 */
public class CacheTest {
	//DataProvider testing suite

    /**
     * Test to ensure that the provider produces the correct value
     */
	@Test
	public void HashedDP_TestString() {
		DataProvider<String, Integer> provider = new HashedDataProvider();
		assertEquals(provider.get("hello"), (Integer)("hello".hashCode()));
		assertEquals(provider.get("bye"), (Integer)("bye".hashCode()));
		assertEquals(provider.get("123"), (Integer)("123".hashCode()));
		assertEquals(provider.get(""), (Integer)("".hashCode()));
	}

    /**
     * Test to ensure that the provider's query count accurately reflects the number of queries
     */
	@Test
	public void HashedDP_QueryCount() {
		HashedDataProvider provider = new HashedDataProvider(); //weakest would be DataProvider, but we're using unique methods (getQueries)
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

    /**
     * Test to ensure that the provider's query count matches the cache's "miss" count
     */
    @Test
    public void testNumMissLies() {
        HashedDataProvider provider = new HashedDataProvider();
        Cache<String,Integer> cache = new LRUCache<String,Integer>(provider, 3);
        assertEquals(cache.getNumMisses(), 0);
        assertEquals(cache.getNumMisses(), provider.getQueries());
        cache.get("1");
        assertEquals(cache.getNumMisses(), provider.getQueries());
        cache.get("2");
        assertEquals(cache.getNumMisses(), provider.getQueries());
        cache.get("1");
        assertEquals(cache.getNumMisses(), provider.getQueries());
    }

    /**
     * Basic test to ensure that the last recently used item is properly sequenced (and thus evicted)
     */
	@Test
	public void leastRecentlyUsedIsCorrect () {
		DataProvider<String,Integer> provider = new HashedDataProvider();
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

    /**
     * Test basic eviction of the last element, without reordering the cache's internal storage.
     */
	@Test
	public void testBasicEviction() {
		DataProvider<String,Integer> provider = new HashedDataProvider();
		Cache<String,Integer> cache = new LRUCache<String,Integer>(provider, 2);
		assertEquals(cache.getNumMisses(), 0);

		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //miss
		assertEquals(cache.get("2"), (Integer)("2".hashCode())); //miss
		assertEquals(cache.getNumMisses(), 2);

		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //hit
		assertEquals(cache.get("2"), (Integer)("2".hashCode())); //hit
		assertEquals(cache.getNumMisses(), 2);

		assertEquals(cache.get("3"), (Integer)("3".hashCode())); //miss (evicts 1)
		assertEquals(cache.get("4"), (Integer)("4".hashCode())); //miss (evicts 2)
		assertEquals(cache.getNumMisses(), 4);

		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //miss (evicted previously) (evicts 3)
		assertEquals(cache.get("2"), (Integer)("2".hashCode())); //miss (evicted previously) (evicts 4)
		assertEquals(cache.getNumMisses(), 6);
		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //hit
		assertEquals(cache.get("2"), (Integer)("2".hashCode())); //hit
		assertEquals(cache.getNumMisses(), 6);

		assertEquals(cache.get("3"), (Integer)("3".hashCode())); //miss (evicted previously) (evicts 1)
		assertEquals(cache.get("4"), (Integer)("4".hashCode())); //miss (evicted previously) (evicts 2)
		assertEquals(cache.getNumMisses(), 8);
	}

    /**
     * Test eviction in the case of resorted elements from the middle of a cache.
     */
	@Test
	public void testComplexEviction() {
		DataProvider<String,Integer> provider = new HashedDataProvider();
		Cache<String,Integer> cache = new LRUCache<String,Integer>(provider, 3);
		assertEquals(cache.getNumMisses(), 0);

		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //miss
		assertEquals(cache.get("2"), (Integer)("2".hashCode())); //miss
		assertEquals(cache.get("3"), (Integer)("3".hashCode())); //miss
		assertEquals(cache.getNumMisses(), 3);
		//Test that we appropriately adjust the order
		//More Recent -> Least Recent
		//3 2 [1]
		assertEquals(cache.get("4"), (Integer)("4".hashCode())); //miss (evict 1)
		assertEquals(cache.getNumMisses(), 4);

		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //miss (evict 2))
		assertEquals(cache.getNumMisses(), 5);
		//1 4 [3]  -- test that we can reuse 4, evict 2 objects and ensure 4 remains

		assertEquals(cache.get("4"), (Integer)("4".hashCode())); //hit
		assertEquals(cache.getNumMisses(), 5);
		//4 1 [3]
		assertEquals(cache.get("5"), (Integer)("5".hashCode())); //miss (evict 3))
		assertEquals(cache.get("6"), (Integer)("6".hashCode())); //miss (evict 1))
		assertEquals(cache.getNumMisses(), 7);
		//6 5 [4] - ensure 4 remains
		assertEquals(cache.get("4"), (Integer)("4".hashCode())); //hit
		assertEquals(cache.getNumMisses(), 7);
		//4 6 [5]
		//clear entirely
		assertEquals(cache.get("1"), (Integer)("1".hashCode())); //miss (evict 5)
		assertEquals(cache.get("2"), (Integer)("2".hashCode())); //miss (evict 6)
		assertEquals(cache.get("3"), (Integer)("3".hashCode())); //miss (evict 4)
		assertEquals(cache.getNumMisses(), 10);
		//ensure refetching 564 -> 3 misses
		assertEquals(cache.get("5"), (Integer)("5".hashCode())); //miss (evict 1)
		assertEquals(cache.get("6"), (Integer)("6".hashCode())); //miss (evict 2)
		assertEquals(cache.get("4"), (Integer)("4".hashCode())); //miss (evict 3)
		assertEquals(cache.getNumMisses(), 13);
		//back to 4 6 [5].
	}

    /**
     * Tests a LRUCache using a simple LinkedHashMap equivalent implementation for proper eviction, results, and numMisses() counting.
     * @param capacity max capacity of the LRUCache
     */
	public void testBruteForceEviction(int capacity) {
		final int NUM_BRUTE_FORCE_ATTEMPTS = 10000; // we need to _at least_ have capacity# of brute force attempts
		// Use a linkedhashmap as "ideal example" and test it alongside LRUCache to see if we wrongly evict / track LRU
		// in NUM_BRUTE_FORCE_ATTEMPTS number of random tests
		DataProvider<String,Integer> provider = new HashedDataProvider();
		Cache<String,Integer> cache = new LRUCache<String,Integer>(provider, capacity);
		assertEquals(cache.getNumMisses(), 0);
		LinkedHashMap<String,Integer> ideal = new LinkedHashMap<>(capacity+1);

		for(int i = 0; i < NUM_BRUTE_FORCE_ATTEMPTS; i++) {
			String thisEntry = ""+(int)(Math.random() * (capacity * 2.0));
			int current = cache.getNumMisses();
			if(ideal.containsKey(thisEntry)) { //we should be cached, thus cache.get() doesn't fire a miss
				int result = cache.get(thisEntry);
				assertEquals(result, (int) ideal.get(thisEntry));
				assertEquals(cache.getNumMisses(), current);
				//Update order (reinsert)
				ideal.remove(thisEntry);
				ideal.put(thisEntry, result);
			}
			else {
				int result = cache.get(thisEntry);
				ideal.put(thisEntry, result);
				assertEquals(result, thisEntry.hashCode());
				assertEquals(cache.getNumMisses(), current+1);
			}
			if(ideal.size() > capacity) {
				String toEvict = ideal.keySet().iterator().next();
				ideal.remove(toEvict); //remove last key
			}
		}
	}

    /**
     * Rapidly tests collections of sizes 0-100 to ensure for accurate eviction
     */
	@Test
	public void testBruteForceAnyCapacity() {
		final int MAX_CAPACITY = 100;
		for(int i = 0; i < MAX_CAPACITY; i++) {
			testBruteForceEviction(i);
		}
	}

    /**
     * Tests that high capacities do not take unruly amounts of time.
     */
	@Test
	public void testHighCapacity() {
		//in o(n) or slower, this will be a very very slow test (19720ms on my machine vs 61ms in O(1)).
		//Our "BruteForceEviction" method will not actually evict anything here since the capacity is so high.
		//As thus, this test will not necessarily fail even if the eviction process is faulty. It is only provided
		//as a demonstration of the speed of the LRUCache.
		testBruteForceEviction(50000);
	}

}
