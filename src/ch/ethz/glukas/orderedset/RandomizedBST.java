package ch.ethz.glukas.orderedset;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;


/**
 * 
 * Implements a randomized sorted set structure introduced by Conrado Martinez and Salvador Roura:
 * http://www.cis.temple.edu/~wolfgang/cis551/martinez.pdf
 * 
 * This class conforms to the java.util.NavigableSet interface. It is thus very similar in behavior to java.util.TreeSet, but it is more space efficient and provides access by rank.
 * 
 * Expected add, contains and remove performance is O(logn) for all input distributions
 * In addition to access by key, the structure provides O(logn) access and deletion by rank
 * 
 * 
 * @author Lukas Gianinazzi
 *
 */
public class RandomizedBST<T> extends RankedTree<T> implements NavigableSet<T>, RangeSet<T> {

	///
	//COLLECTION
	///
	
	@Override
	public boolean add(T value)
	{
		assert isInOrder();
		
		if (value == null) throw new NullPointerException();
		
		Out<Boolean> modified = new Out<Boolean>();

		setRoot(internalAdd(value, getRoot(), modified));
		//internalAdd(value);
		
		assert subtreeSizeConsistent(getRoot());
		assert isInOrder();
		assert contains(value);
	
		return modified.get();
	}
	
	
	@Override
	public boolean remove(Object arg0)
	{
		assert isInOrder();
		assert subtreeSizeConsistent(getRoot());
		
		if (arg0 == null) return false;
		
		@SuppressWarnings("unchecked")
		T value = (T)arg0;
		Out<Boolean> modified = new Out<Boolean>();
		
		setRoot(internalRemove(value, getRoot(), modified));

		assert subtreeSizeConsistent(getRoot());
		assert isInOrder();
		assert (!contains(value));
		
		return modified.get();
	}
	
	@Override
	public int size()
	{
		return ((RankedTreeNode<T>) metaRoot).size()-1;
	}
	
	@Override
	public void clear() {
		metaRoot = new RankedTreeNode<T>(null);
		
		assert isEmpty();
	}
	
	


	
	////
	//NAVIGABLE SET
	///
	
	public T pollFirst()
	{
		if (isEmpty()) return null;
		T first = first();
		remove(first);
		return first;
	}
	
	public T poll()
	{
		return pollFirst();
	}
	
	public T pollLast()
	{
		if (isEmpty()) return null;
		T last = last();
		remove(last);
		return last;
	}
	
	
	
	//NAVIGATE:
	//the algorithms are based on split and join operations
	//this maintains randomness and gives them O(log n) expected performance
	//Discussion: other methods such as threading or parent pointers might increase the performance, but increase complexity and overhead for other methods
	
	@Override
	public T floor(T e) {
		if (contains(e)) return e;
		return internalLower(e);
	}
	

	@Override
	public T lower(T e) {
		if (contains(e)) {
			int index = indexOf(e);
			if (!isEmpty() && index > 0) {
				return get(index-1);
			} else {
				return null;
			}
		} else {
			return internalLower(e);
		}
	}
	
	
	@Override
	public T ceiling(T e) {
		if (contains(e)) return e;
		return internalHigher(e);
	}

	@Override
	public T higher(T e) {
		if (contains(e)) {
			int index = indexOf(e);
			if (index < size()-1) {
				return get(index+1);
			} else {
				return null;
			}
		} else {
			return internalHigher(e);
		}
	}
	
	private T internalLower(T e)
	{
		Out<T> smaller = new Out<T>();
		getNeighborhood(e, smaller, null);
		return smaller.get();
	}
	
	private T internalHigher(T e)
	{
		Out<T> greater = new Out<T>();
		getNeighborhood(e, null, greater);
		return greater.get();
	}
	
	//reverse order

	@Override
	public Iterator<T> descendingIterator() {
		return descendingSet().iterator();
	}


	@Override
	public NavigableSet<T> descendingSet() {
		// TODO Auto-generated method stub
		return null;
	}


	


	///
	//DESTRUCTIVE SUBSET METHODS : elements are removed from this set and added to a new set
	///
	
	
	public RandomizedBST<T> cutHeadSet(T toElement, boolean inclusive)
	{
		//partition the tree around the value
		Out<TreeNode<T>> smaller = new Out<TreeNode<T>>();
		Out<TreeNode<T>> greater = new Out<TreeNode<T>>();
		TreeNode<T> equal = split(toElement, getRoot(), smaller, greater);
		
		//create the new set
		RandomizedBST<T> headSet = new RandomizedBST<T>();
		
		//assign the partitions of the split
		headSet.setRoot(smaller.get());
		setRoot(greater.get());
		if (equal != null) {
			if (inclusive) {
				headSet.add(equal.getValue());
			} else {
				add(equal.getValue());
			}
		}
		return headSet;
	}
	
	
	public RandomizedBST<T> cutTailSet(T fromElement, boolean inclusive)
	{
		//partition the tree around the value
		Out<TreeNode<T>> smaller = new Out<TreeNode<T>>();
		Out<TreeNode<T>> greater = new Out<TreeNode<T>>();
		TreeNode<T> equal = split(fromElement, getRoot(), smaller, greater);
		
		//create the new set
		RandomizedBST<T> tailSet = new RandomizedBST<T>();
		
		//assign the partitions of the split
		tailSet.setRoot(greater.get());
		setRoot(smaller.get());
		if (equal != null) {
			if (inclusive) {
				tailSet.add(equal.getValue());
			} else {
				add(equal.getValue());
			}
		}
		return tailSet;
	}
	
	////
	//IMPLEMENTATION
	////
	
	private TreeNode<T> internalAdd(T value, TreeNode<T> r, Out<Boolean> modified)
	{
		int size = size(r);

		int rand = random.nextInt(size+1);
		
		
		if (size == rand) {//base case: insert here, restructure r
			return insertAtRoot(value, r, modified);
		}
		
		int comparison = compareValues(value, r.getValue());
		
		if (comparison < 0) {
			r.setLeftChild(internalAdd(value, r.getLeftChild(),  modified));	
		} else if (comparison > 0) {
			r.setRightChild(internalAdd(value, r.getRightChild(),  modified));
		} else {//base case: already present
			modified.set(false);
		}
		
		return r;
	}
	
	

	//precondition: !contains(value)
	/*
	private void internalAdd(T value)
	{
		assert !contains(value);
		
		TreeNode<T> parent = metaRoot;
		TreeNode<T> current = getRoot();
		int rand = 0;
		int currentSize = 0;
		while (true) {
			
			currentSize = size(current);
			rand = random.nextInt(currentSize+1);
			
			if (currentSize == rand) break;
			
			
			int comparison = compareValues(value, current.getValue());
			//update size
			((RankedTreeNode<T>)current).setSize(currentSize + 1);
			//advance pointers
			parent = current;
			if (comparison < 0) {
				current = current.getLeftChild();
			} else if (comparison > 0) {
				current = current.getRightChild();
			}
			
		}
		assert parent != current;
		Out<Boolean> modified = new Out<Boolean>();
		TreeNode<T> newNode = insertAtRoot(value, current, modified);
		
		parent.replaceChild(current, newNode);
		assert contains(value);
		assert subtreeSizeConsistent(getRoot());
	}*/
	
	
	private TreeNode<T> internalRemove(T value, TreeNode<T> r, Out<Boolean> modified)
	{
		if (r == null) {//base case 1 : value is not present
			modified.set(false);
			return null; 
		}
		
		
		int comparison = compareValues(value, r.getValue());
		
		if (comparison < 0) {
			r.setLeftChild(internalRemove(value, r.getLeftChild(), modified));
		} else if (comparison > 0) {
			r.setRightChild(internalRemove(value, r.getRightChild(), modified));
		} else {//base case 2 : value is present : remove using join
			assert (value.equals(r.getValue()));
			modified.set(true);
			r = join(r.getLeftChild(), r.getRightChild());
		}
		
		return r;
	}
	
	//returns the value if it is contained in the structure, sets 'greater' to the next highest and 'smaller' to the next lowest value contained in the tree
	//smaller and greater can be null
	//Algorithm: split around the value and join the subtrees back
	public T getNeighborhood(T value, Out<T> smaller, Out<T> greater) {
		//partition tree around the value e
		Out<TreeNode<T>> greaterTree = new Out<TreeNode<T>>();
		Out<TreeNode<T>> smallerTree = new Out<TreeNode<T>>();
		TreeNode<T> equal = split(value, getRoot(), smallerTree, greaterTree);

		//extract result values if needed
		if (greaterTree.get() != null && greater != null) {
			greater.set(findFirst(greaterTree.get()).getValue());
		}
		if (smallerTree.get() != null && smaller != null) {
			smaller.set(findLast(smallerTree.get()).getValue());
		}
		
		//restore the tree
		TreeNode<T> newRoot = join(smallerTree.get(), greaterTree.get());
		setRoot(newRoot);
		if (equal != null) {
			add(value);
		}
		
		assert isInOrder();
		assert subtreeSizeConsistent(getRoot());
		return valueOrNull(equal);
	}
	


	
	//insert the value here: restructure the subtree rooted at 'r' so that value is the the root of this subtree, return the new root
	//if the value was already present, modified will be set to false, else if will be set to true
	private TreeNode<T> insertAtRoot(T value, TreeNode<T> r, Out<Boolean> modified)
	{

		Out<TreeNode<T>> less = new Out<TreeNode<T>>();
		Out<TreeNode<T>> greater = new Out<TreeNode<T>>();
		
		TreeNode<T> equal = split(value, r, less, greater);
		
		assert less.get() == null || compareValues(value, findLast(less.get()).getValue()) > 0;
		assert greater.get() == null || compareValues(value, findFirst(greater.get()).getValue()) < 0;
		
		
		if (equal == null) {
			modified.set(true);
			equal = new RankedTreeNode<T>(value);
		} else {
			assert equal.getValue().equals(value);
			modified.set(false);
		}
		
		equal.setLeftChild(less.get());
		equal.setRightChild(greater.get());
		
		assert subtreeSizeConsistent(equal);
		assert modified.get() != null;
		return equal;
	}

	
	//randomized join operation
	private TreeNode<T> join(TreeNode<T> L, TreeNode<T> R)
	{
		assert descendantsAreSmaller(L, valueOrNull(R));
		assert descendantsAreGreater(R, valueOrNull(L));
		
		int sizeL = size(L);
		int sizeR = size(R);
		int total = sizeL+sizeR;
		
		if (total == 0) return null;
		
		int r = random.nextInt(total);
		
		if (r < sizeL) {
			L.setRightChild(join(L.getRightChild(), R));
			return L;
			
		} else {
			R.setLeftChild(join(L, R.getLeftChild()));
			return R;
		}
		
	}
	
	
	///
	//INSTANCE VARIABLES
	///
	private Random random = new Random(91);



	
	
}
