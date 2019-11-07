import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * An implementation of <tt>Cache</tt> that uses a least-recently-used (LRU)
 * eviction policy.
 */
public class LRUCache<T, U> implements Cache<T, U> {

	/**
	 * Serves as a node in a linked list, with data equal to the key (type T) of its corresponding CacheData object
	 */
	private class Node {
		final private T _data;
		private Node _next;
		private Node _previous;
		private Node(T key) {
			_data = key;
		}
	}

	/**
	 * Serves as a linked list to hold the ordered list of keys stored in our Cache, and manipulated to maintain last-used order.
	 */
	private class LinkedList {
		private Node _head;
		private Node _tail;
		private int _height = 0;
	}

	/**
	 * Serves as a single object to store the result (type U) of a key query (type T) given by a provider, as well as its corresponding Node
	 * in the ordered linked list.
	 */
	private class CacheData {
		final private U _cacheData;
		final private Node _orderPointer;
		private CacheData(U d, Node pointer) {
			_cacheData = d;
			_orderPointer = pointer;
		}
	}

	private int _numMisses;
	final private int _capacity;
	final private DataProvider<T, U> _provider;
	final private Hashtable<T, CacheData> _table;
	final private LinkedList _keyOrder = new LinkedList();
	/**
	 * Constructs the LRUCache
	 * @param provider the data provider to consult for a cache miss
	 * @param capacity the exact number of (key,value) pairs to store in the cache
	 */
	public LRUCache (DataProvider<T, U> provider, int capacity) {
		_table = new Hashtable<>(capacity+1);
		_provider = provider;
		_capacity = capacity;
	}

	/**
	 * Returns the value associated with the specified key.
	 * @param key the key
	 * @return the value associated with the key
	 */
	public U get (T key) {
		//See if our key exists
		U finalProduct;
		Node orderingNode;
		if(_table.containsKey(key)) {
			//Utilize cache
			CacheData atKey = _table.get(key);
			finalProduct = atKey._cacheData;
			orderingNode = atKey._orderPointer;
		} else {
			//get the result
			finalProduct = _provider.get(key);
			_numMisses++;
			orderingNode = new Node(key);
			_keyOrder._height++;
			CacheData hashBlob = new CacheData(finalProduct, orderingNode);
			_table.put(key, hashBlob);
		}
		//Order LL
		if(orderingNode != _keyOrder._head) {
			//delink
			final Node previous = orderingNode._previous;
			final Node next = orderingNode._next;
			if(previous != null)
				previous._next = next;
			if(next != null)
				next._previous = previous;

			orderingNode._next = _keyOrder._head;
			if(orderingNode._next != null)
				orderingNode._next._previous = orderingNode;

			orderingNode._previous = null;
			_keyOrder._head = orderingNode;
			if(_keyOrder._tail == orderingNode)
				_keyOrder._tail = previous;

			if(_keyOrder._tail == null)
				_keyOrder._tail = orderingNode;

		}
		pruneStructure();
		return finalProduct;
	}

	/**
	 * Prunes the LRUCache, if over capacity, by removing the least recently used element.
	 */
	private void pruneStructure() {
		//if we're over capacity, trim the end
		if(_keyOrder._height > _capacity) {
			//prune the tail
			Node toBeRemoved = _keyOrder._tail;
			_table.remove(toBeRemoved._data);
			_keyOrder._tail = toBeRemoved._previous;
			toBeRemoved._previous = null;
			toBeRemoved._next = null;
			_keyOrder._height--;
		}
	}
	/**
	 * Returns the number of cache misses since the object's instantiation.
	 * @return the number of cache misses since the object's instantiation.
	 */
	public int getNumMisses () {
		return _numMisses;
	}
}
