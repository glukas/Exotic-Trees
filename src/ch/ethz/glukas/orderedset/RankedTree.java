package ch.ethz.glukas.orderedset;

import java.util.ListIterator;
import java.util.NavigableSet;

public abstract class RankedTree<T> extends BinarySearchTree<T> implements RangeSet<T> {
	//Augments the binary search tree with dynamic order statistics
	//all methods provided here are 'readonly'
	//the implementer is responsible of mainting the tree and especially the size counts
	//all nodes are assumed to conform to RankedTreeNode
	
	
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
		//could be done faster, but this is easy
		NavigableSet<T> subset = subSet(lowerbound, fromInclusive, upperbound, toInclusive);
		while(subset.pollFirst() != null) {
		}
	}

	
	public ListIterator<T> listIterator()
	{
		return listIterator(0);
	}
	
	public ListIterator<T> listIterator(int index)
	{
		if (index >= size()) return  new RangeSetIterator<T>();
		return new RangeSetIterator<T>(this, get(index), last());
	}
	
	/////
	//NAVIGABLE SET : Subsets
	/////
	
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
	
	
	////
	//IMPLEMENTATION
	////
	
	
	//index is 0 based: smallest element has index '0'
	protected TreeNode<T> getByRank(TreeNode<T> root, int index)
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
	protected int internalIndexOf(T value, TreeNode<T> current)
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
	

	//all nodes of this tree are ranked
	protected int size(TreeNode<T> node)
	{
		if (node == null) return 0;
		return ((RankedTreeNode<T>)node).size();
	}
	
	

	///
	//INVARIANTS
	///

	protected boolean subtreeSizeConsistent(TreeNode<T> node)
	{
		Out<Boolean> consistent = new Out<Boolean>();
		subtreeSize(node, consistent);
		return consistent.get();
	}
	
	
	//count size of subtree by exhaustion, place into consistent if the values agree with the cached values
	protected int subtreeSize(TreeNode<T> node, Out<Boolean> consistent)
	{
		if (node == null) {
			if (consistent.get() == null) consistent.set(true);
			return 0;
		}
		
		int total = 1 + subtreeSize(node.getLeftChild(), consistent) + subtreeSize(node.getRightChild(), consistent);
		consistent.set(consistent.get() && total == size(node));		
		
		return total;
	}
	
	
}
