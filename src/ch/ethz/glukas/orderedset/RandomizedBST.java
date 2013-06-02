package ch.ethz.glukas.orderedset;

import java.util.Random;
import java.util.Set;


/**
 * 
 * Implements a randomized sorted set structure introduced by Conrado Martinez and Salvador Roura:
 * http://www.cis.temple.edu/~wolfgang/cis551/martinez.pdf
 * 
 * Expected add, contains and remove performance is O(logn) for all input distributions
 * In addition to access by key, the structure provides O(logn) access and deletion by rank
 * 
 * @author Lukas Gianinazzi
 *
 */
public class RandomizedBST<T> extends BinarySearchTree<T> {

	
	
	@Override
	public boolean add(T value)
	{
		Out<Boolean> modified = new Out<Boolean>();

		setRoot(internalAdd(value, getRoot(), modified));
		
		assert subtreeSizeConsistent(getRoot());
		assert isInOrder();
		
		return modified.get();
	}
	
	
	@Override
	public boolean remove(Object arg0)
	{
		if (arg0 == null) return false;
		
		@SuppressWarnings("unchecked")
		T value = (T)arg0;
		Out<Boolean> modified = new Out<Boolean>();
		
		setRoot(internalRemove(value, getRoot(), modified));

		assert subtreeSizeConsistent(getRoot());
		assert isInOrder();
		
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
	}
	
	
	///
	//ACCESS BY RANK : Expected O(logn)
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
	 * Removes the k'th smallest element from the set
	 * @param index
	 * @return  true if the set has been modified, false otherwise
	 */
	public boolean remove(int index)
	{
		
		T value = get(index);
		boolean modified = false;
		if (value != null) {
			modified = true;
			remove(value);
		}
		return modified;
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
			
		} else {
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
		
		//base case 1
		//base case 2
		int leftChildren = size(root.getLeftChild());
		if (leftChildren == index) return root;
		
		//recursive step
		if (index < leftChildren) {
			return getByRank(root.getLeftChild(), index);
		} else {
			return getByRank(root.getRightChild(), index-leftChildren-1);
		}
	}
	
	
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
			if (indexOfValue != -1) {
				indexOfValue += size(current.getLeftChild())+1;
			}
			
		} else {//base case 2: found
			assert comparison == 0;
			indexOfValue = size(current.getLeftChild());
		}
		
		return indexOfValue;
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
	
	

	
	//INVARIANTS
	
	public boolean subtreeSizeConsistent(TreeNode<T> node)
	{
		Out<Boolean> consistent = new Out<Boolean>();
		subtreeSize(node, consistent);
		return consistent.get();
	}
	
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
	
	//INSTANCE VARIABLES
	
	private Random random = new Random(91);
	
	
}
