package ch.ethz.glukas.orderedmap;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

public class Trie<T> {
	//TODO: create iterator
	//TODO: investigate if behaves properly unicode characters requiring more than 16bits (i think it still works)
	
	public Trie()
	{
		//TODO: consider a special purpose sparse table based map
		children = new CharMap<Trie<T>>();
	}
	
	public T put(String key, T newValue) {
		if (key.isEmpty()) {
			//insert here
			T oldValue = value;
			value = newValue;
			return oldValue;
		}
		assert key.length() > 0;
		//recursively insert into the appropriate child
		char firstCharacter = key.charAt(0);
		if (!children.containsKey(firstCharacter)) {
			children.put(firstCharacter, new Trie<T>());
		}
		assert children.containsKey(firstCharacter);
		
		return children.get(firstCharacter).put(key.substring(1), newValue);
	}
	
	public boolean contains(String key)
	{
		return get(key) != null;
	}
	
	public T remove(String key)
	{
		//TODO: clean up unnecessary leaves
		return put(key, null);
	}
	
	public T get(String key)
	{
		Trie<T> subtree = subtrieWithPrefix(key);
		if (subtree != null) {
			return subtree.value;
		}
		return null;
	}
	
	//returns a Trie containing all keys starting with prefix
	//changes in the returned Trie are reflected in the original Trie and vice-versa
	public Trie<T> subtrieWithPrefix(String prefix)
	{
		if (prefix.isEmpty()) {
			return this;
		}
		char firstCharacter = prefix.charAt(0);
		Trie<T> subtrie = children.get(firstCharacter);
		if (subtrie != null) {
			return subtrie.subtrieWithPrefix(prefix.substring(1));
		}
		return null;
	}
	
	///
	//INVARIANTS AND ASSERTIONS
	///
	
	protected boolean checkInvariants()
	{
		boolean result = children != null;
		assert result;
		return result;
	}
	
	////
	//Instance variables
	////
	private T value;
	private CharMap<Trie<T>> children;
	
}
