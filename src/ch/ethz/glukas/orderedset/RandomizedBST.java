package ch.ethz.glukas.orderedset;

import java.util.Iterator;
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
public class RandomizedBST<T> extends BinarySearchTree<T> implements NavigableSet<T>, RangeSet<T> {

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
	
	
	///
	//ACCESS BY RANK 
	///
	
	/**
	 * Returns the k'th-smallest element from the set
	 */
	public T get(int index)
	{
		if (index < 0) throw new IndexOutOfBoundsException();
		if (index >= size()) throw new IndexOutOfBoundsException();
		
		
		TreeNode<T> result = getByRank(getRoot(), index);
		if (result == null) return null;
		return result.getValue();
	}
	
	/**
	 * Retrieves and removes the k'th smallest element from the set
	 * @param index
	 * @return  the value that has been removed
	 * @throws IndexOutOfBoundsException
	 */
	public T poll(int index)
	{
		T value = get(index);
		remove(value);
		assert (!contains(value));
		return value;
	}
	
	/**
	 * Removes the k'th smallest element from the set
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(int index)
	{
		poll(index);
	}
	
	
	/**
	 * If 'value' is the k'th smallest element in the set, this method returns 'k'
	 * @param value
	 * @return the rank of 'value'
	 */
	public int indexOf(T value)
	{
		return internalIndexOf(value, getRoot());
	}
	
	/**
	 * Since values are unique, this method is equivalent to indexOf
	 * @param value
	 * @return the rank of 'value'
	 */
	public int lastIndexOf(T value)
	{
		return indexOf(value);
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
		
		Out<T> greater = new Out<T>();
		Out<T> smaller = new Out<T>();
		T equal = getNeighborhood(e, greater, smaller);

		if (equal != null) return equal;
		return smaller.get();
	}
	

	@Override
	public T lower(T e) {
		Out<T> greater = new Out<T>();
		Out<T> smaller = new Out<T>();
		T equal = getNeighborhood(e, greater, smaller);

		return smaller.get();
	}
	
	
	@Override
	public T ceiling(T e) {
		Out<T> greater =new Out<T>();
		Out<T> smaller = new Out<T>();
		T equal = getNeighborhood(e, greater, smaller);

		if (equal != null) return equal;
		return greater.get();
	}

	@Override
	public T higher(T e) {
		Out<T> greater =new Out<T>();
		Out<T> smaller = new Out<T>();
		T equal = getNeighborhood(e, greater, smaller);

		return greater.get();
	}
	
	
	//reverse order

	@Override
	public Iterator<T> descendingIterator() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public NavigableSet<T> descendingSet() {
		// TODO Auto-generated method stub
		return null;
	}




	
	//Non-destructive subset methods : returned sets are backed by this set so changes in one set are reflected in the other set

	@Override
	public NavigableSet<T> headSet(T toElement) {
		return headSet(toElement, false);
	}


	@Override
	public NavigableSet<T> headSet(T toElement, boolean inclusive) {
		return new SortedSubset<T>(this, null, toElement, false, inclusive);
	}
	
	
	@Override
	public NavigableSet<T> tailSet(T fromElement) {
		return tailSet(fromElement, true);
	}


	@Override
	public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
		return new SortedSubset<T>(this, fromElement, null, inclusive, false);
	}


	@Override
	public NavigableSet<T> subSet(T fromElement, T toElement) {
		return subSet(fromElement, true, toElement, false);
	}


	@Override
	public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
		return new SortedSubset<T>(this, fromElement, toElement, fromInclusive, toInclusive);
	}


	///
	//DESTRUCTIVE SUBSET METHODS
	///
	
	
	public RandomizedBST<T> cutHeadSet(T toElement, boolean inclusive)
	{
		return null;
	}
	
	
	public RandomizedBST<T> cutTailSet(T fromElement, boolean inclusive)
	{
		return null;
	}
	
	
	///
	//RANGE SET
	///
	
	
	@Override
	public int sizeOfRange(T lowerbound, T upperbound, boolean fromInclusive, boolean toInclusive) {
		if (compareValues(lowerbound, upperbound) > 0) throw new IllegalArgumentException();
		
		T lower;
		T upper;
		if (fromInclusive) {
			lower = ceiling(lowerbound);
		} else {
			lower = higher(lowerbound);
		}
		if (toInclusive) {
			upper = floor(upperbound);
		} else {
			upper = lower(upperbound);
		}
		if (compareValues(lower, upper) > 0) return 0;
		
		int lowerIndex = indexOf(lower);
		int upperIndex = indexOf(upper);
		
		return upperIndex-lowerIndex+1;
	}


	@Override
	public void removeRange(T lowerbound, T upperbound, boolean fromInclusive, boolean toInclusive) {
		//could be done faster
		NavigableSet<T> subset = subSet(lowerbound, fromInclusive, upperbound, toInclusive);
		while(subset.pollFirst() != null) {
		}
	}

	
	////
	//IMPLEMENTATION
	////
	
	private TreeNode<T> internalAdd(T value, TreeNode<T> r, Out<Boolean> modified)
	{
		int size = size(r);

		int rand = 0;
		if (size > 0) {
			rand = random.nextInt(size+1);
		}
		
		
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
	
	//index is 0 based: smallest element has index '0'
	private TreeNode<T> getByRank(TreeNode<T> root, int index)
	{
		assert index < size(root);
		
		
		int leftChildren = size(root.getLeftChild());
		
		//base case
		if (leftChildren == index) return root;
		
		//recursive step
		if (index < leftChildren) {
			return getByRank(root.getLeftChild(), index);
		} else {
			return getByRank(root.getRightChild(), index-leftChildren-1);
		}
	}
	
	
	//returns the index of the value relative to the current node, or -1 if the value is not present
	private int internalIndexOf(T value, TreeNode<T> current)
	{
		//base case 1 : not found
		if (current == null) 
		{
			return -1;
		}
		
		int indexOfValue = 0;
		
		int comparison = compareValues(value, current.getValue());
		
		if (comparison < 0) {//continue searching left
			indexOfValue = internalIndexOf(value, current.getLeftChild());
		} else if (comparison > 0) {//continue searching right
			indexOfValue = internalIndexOf(value, current.getRightChild());
			if (indexOfValue != -1) {//the index is relative to the index of the child: calculate index relative to the current node
				indexOfValue += size(current.getLeftChild())+1;
			}
		} else {//base case 2: found
			assert comparison == 0;
			indexOfValue = size(current.getLeftChild());
		}
		
		return indexOfValue;
	}

	//returns the value if it is contained in the structure, sets 'greater' to the next highest and 'smaller' to the next lowest value contained in the tree
	//Algorithm: split around the value and join the subtrees back
	public T getNeighborhood(T value, Out<T> greater, Out<T> smaller) {
		//partition tree around the value e
		Out<TreeNode<T>> greaterTree = new Out<TreeNode<T>>();
		Out<TreeNode<T>> smallerTree = new Out<TreeNode<T>>();
		TreeNode<T> equal = split(value, getRoot(), smallerTree, greaterTree);

		//extract result values
		if (greaterTree.get() != null) {
			greater.set(findFirst(greaterTree.get()).getValue());
		}
		if (smallerTree.get() != null) {
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
	

	
	//all nodes of this tree are ranked
	private int size(TreeNode<T> node)
	{
		if (node == null) return 0;
		return ((RankedTreeNode<T>)node).size();
	}
	
	

	///
	//INVARIANTS
	///

	
	public boolean subtreeSizeConsistent(TreeNode<T> node)
	{
		Out<Boolean> consistent = new Out<Boolean>();
		subtreeSize(node, consistent);
		return consistent.get();
	}
	
	
	//count size of subtree by exhaustion, place into consistent if the values agree with the cached values
	public int subtreeSize(TreeNode<T> node, Out<Boolean> consistent)
	{
		if (node == null) {
			if (consistent.get() == null) consistent.set(true);
			return 0;
		}
		
		int total = 1 + subtreeSize(node.getLeftChild(), consistent) + subtreeSize(node.getRightChild(), consistent);
		consistent.set(consistent.get() && total == size(node));		
		
		return total;
	}
	
	
	///
	//INSTANCE VARIABLES
	///
	private Random random = new Random(91);



	
	
}
