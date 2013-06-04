package ch.ethz.glukas.orderedset;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 
 * An abstract Binary Search Tree
 * Provides find operations, iteration and splits
 * 
 * @author Lukas Gianinazzi
 *
 * @param <T>
 */

abstract class BinarySearchTree<T> extends AbstractCollection<T> implements Set<T>{
	
	
	///
	//CONSTRUCTION
	//clear() is guaranteed to be called by the constructors and can be used for initialization
	///
	
	protected BinarySearchTree(Comparator<? super T> comparator)
	{
		clear();
		internalComparator = comparator;
	}
	
	protected BinarySearchTree()
	{
		clear();
		internalComparator = new Comparator<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(T arg0, T arg1) {
				return ((Comparable<T>)arg0).compareTo(arg1);
			}
		};
	}
	
	
	/////
	//ABSTRACT COLLECTION
	////
	
	
	@Override
	public boolean contains(Object arg0) {
		if (arg0 == null) return false;

		@SuppressWarnings("unchecked")
		T value = (T)arg0;
		return internalContains(value);
	}
	

	@Override
	public Iterator<T> iterator() {
		return new BinarySearchTreeIterator<T>(metaRoot.getLeftChild());
	}
	
	
	@Override
	public void clear() {
		metaRoot = new TreeNode<T>(null);
	}
	
	
	////
	//ORDERING
	////

	
	public T first() {
		if (isEmpty()) throw new NoSuchElementException();
		TreeNode<T> node = findFirst(getRoot());
		return node.getValue();
	}
	
	
	public T last() {
		if (isEmpty()) throw new NoSuchElementException();
		TreeNode<T> node = findLast(getRoot());
		return node.getValue();
	}
	
	//TODO : comparator should return null if instanciated using the natural ordering?
	public Comparator<? super T> comparator() {
		return internalComparator;
	}

	
	/////
	//IMPLEMENTATION :: TREE ROTATIONS
	/////
	
	protected void treeRotateLeft(TreeNode<T> child, TreeNode<T> parent)
	{
		parent.setRightChild(child.getLeftChild());
		child.setLeftChild(parent);
	}
	
	protected void treeRotateRight(TreeNode<T> child, TreeNode<T> parent)
	{
		parent.setLeftChild(child.getRightChild());
		child.setRightChild(parent);
	}
	
	protected void treeRotateUp(TreeNode<T> child, TreeNode<T> parent, TreeNode<T> grandmother)
	{
		assert child != parent;
		assert parent != grandmother;
		
		grandmother.replaceChild(parent, child);
		if (parent.getLeftChild() == child) {
			treeRotateRight(child, parent);
		} else {
			assert parent.getRightChild() == child;
			treeRotateLeft(child, parent);
		}
		
		assert child.getLeftChild() != child.getRightChild();
		assert grandmother.getLeftChild() == child || grandmother.getRightChild() == child;
		assert child.getLeftChild() == parent || child.getRightChild() == parent;
	}
	
	
	
	//////
	///IMPLEMENTATION :: FIND
	//////

	/**
	 * Returns if the value is contained in the BST
	 */
	protected boolean internalContains(T value)
	{
		return findNodeWithValueStartingFrom(metaRoot, value) != null;
	}
	
	/**
	 * Returns the node with the smallest value (the leftmost node in the subtree rooted at 'node')
	 * precondition: node != null
	 */
	protected TreeNode<T> findFirst(TreeNode<T> node)
	{
		while (node.getLeftChild() != null) {
			node = node.getLeftChild();
		}
		return node;
	}
	
	/**
	 * Returns the node with the largest value (the rightmost node in the subtree rooted at 'node')
	 * precondition: node != null
	 */
	protected TreeNode<T> findLast(TreeNode<T> node)
	{
		while (node.getRightChild() != null) {
			node = node.getRightChild();
		}
		return node;
	}
	
	
	//the first element of the list is always the metaRoot
	//else if the value is present in the collection, the last element of the returned list is the node containing the value
	//else the last element of the list is the next highest node
	protected ArrayList<TreeNode<T>> find(T value)
	{
		return traceNodeWithValueStartingFrom(metaRoot, value);
	}
	
	protected Buffer<TreeNode<T>> find(T value, int capacity)
	{
		return traceNodeWithValueStartingFrom(metaRoot, value, capacity);
	}
	
	protected TreeNode<T> findNodeWithValueStartingFrom(TreeNode<T> currentNode, T valueToFind)
	{
		int comparison = -1;
		while (currentNode != null) {
			comparison = compareValues(valueToFind, currentNode.getValue());
			if (comparison < 0) {
				currentNode = currentNode.getLeftChild();
			} else if (comparison > 0) {
				currentNode = currentNode.getRightChild();
			} else {
				break;
			}
		}
		return currentNode;
	}
	
	
	//result contains 2 elements: the successors parent at 0 and the successor at 1
	protected Buffer<TreeNode<T>> findSuccessor(TreeNode<T> node)
	{
		assert node.getRightChild() != null;
		
		Buffer<TreeNode<T>> trace = traceNodeWithValueStartingFrom(node.getRightChild(), node.getValue(), 2);
		if (trace.numberOfUsedSlots() == 1) {//if the successor is the immediate right child, the node will need to be added to the trace
			TreeNode<T> successor = trace.get(0);
			trace.add(node);
			trace.add(successor);
		}
		return trace;
	}
	
	
	protected ArrayList<TreeNode<T>> traceNodeWithValueStartingFrom(TreeNode<T> startingNode, T valueToFind)
	{
		ArrayList<TreeNode<T>> trace = new ArrayList<TreeNode<T>>();

		TreeNode<T> currentNode = startingNode;
		
		int comparison = -1;
		
		while (currentNode != null) {
			trace.add(currentNode);
			
			comparison = compareValues(valueToFind, currentNode.getValue());
			if (comparison < 0) {
				currentNode = currentNode.getLeftChild();
			} else if (comparison > 0) {
				currentNode = currentNode.getRightChild();
			} else {
				break;
			}
		}
		
		return trace;
	}
	
	protected Buffer<TreeNode<T>> traceNodeWithValueStartingFrom(TreeNode<T> startingNode, T valueToFind, int maximumParentBufferSize)
	{

		Buffer<TreeNode<T>> trace = new Buffer<TreeNode<T>>(maximumParentBufferSize);

		TreeNode<T> currentNode = startingNode;
		int comparison = -1;
		while (currentNode != null) {
			trace.add(currentNode);
			comparison = compareValues(valueToFind, currentNode.getValue());
			if (comparison < 0) {
				currentNode = currentNode.getLeftChild();
			} else if (comparison > 0) {
				currentNode = currentNode.getRightChild();
			} else {
				break;
			}
		}
		return trace;
	}
	
	
	////
	//IMPLEMENTATION : SPLIT
	///
	
	protected TreeNode<T> split(T value, TreeNode<T> r, Out<TreeNode<T>> less, Out<TreeNode<T>> greater)
	{
		assert isInOrder(r);
		
		
		if (r == null) {//base case 1
			less.set(null);
			greater.set(null);
			return null;
		}
		
		
		int comparison = compareValues(value, r.getValue());
		
		TreeNode<T> equal = null;
		
		if (comparison < 0) {
			
			equal = split(value, r.getLeftChild(), less, greater);
			r.setLeftChild(greater.get());
			greater.set(r);
			
		} else if (comparison > 0) {
			equal = split(value, r.getRightChild(), less, greater);
			r.setRightChild(less.get());
			less.set(r);
			
		} else {//base case 2
			equal = r;
			less.set(r.getLeftChild());
			greater.set(r.getRightChild());
		}
		
		
		assert descendantsAreSmaller(less.get(), value);
		assert descendantsAreGreater(greater.get(), value);
		assert isInOrder(less.get());
		assert isInOrder(greater.get());
		return equal;
	}
	
	/////
	//IMPLEMENTATION : HELPER METHODS
	/////
	
	protected T valueOrNull(TreeNode<T> node)
	{
		if (node == null) return null;
		return node.getValue();
	}
	
	protected TreeNode<T> getRoot()
	{
		return metaRoot.getLeftChild();
	}
	
	protected void setRoot(TreeNode<T> root)
	{
		metaRoot.setLeftChild(root);
	}
	
	//wraps the comparator to make it partially null-safe: null is interpreted as +infinity
	//if both arguments are 0 the value is undefined
	protected int compareValues(T v1 ,T v2) {
		assert v1 != null || v2 != null;
		if (v2 == null) return -1;
		if (v1 == null) return +1;
		return comparator().compare(v1, v2);
	}
	
	
	protected int compareValues(TreeNode<T> n1, TreeNode<T> n2) {
		return compareValues(n1.getValue(), n2.getValue());
	}
	
	///
	//INVARIANTS & ASSERTIONS
	///
	
	protected boolean descendantsAreSmaller(TreeNode<T> node, T value)
	{
		if (value == null || node == null) return true;
		boolean result = compareValues(node.getValue(), value) < 0 && descendantsAreSmaller(node.getLeftChild(), value) && descendantsAreSmaller(node.getRightChild(), value);
		assert result;//fail fast
		return result;
	}
	
	protected boolean descendantsAreGreater(TreeNode<T> node, T value)
	{
		if (value == null || node == null) return true;
		boolean result = compareValues(node.getValue(), value) > 0 && descendantsAreGreater(node.getLeftChild(), value) && descendantsAreGreater(node.getRightChild(), value);
		assert result;//fail fast
		return result;
	}
	
	protected boolean isInOrder(TreeNode<T> node)
	{
		if (node == null) return true;
		
		
		boolean result = true;
		if (node.getLeftChild() != null) {
			result = result && compareValues(node.getLeftChild(), node) < 0 && isInOrder(node.getLeftChild());
			assert result;
		}
		if (node.getRightChild() != null) {
			result = result && compareValues(node.getRightChild(), node) > 0 && isInOrder(node.getRightChild());
			assert result;
		}
		return result;
	}
	
	protected boolean isInOrder()
	{
		return isInOrder(getRoot());
	}
	
	
	////
	//INSTANCE VARIABLES
	////
	

	

	private Comparator<? super T> internalComparator;
	protected TreeNode<T> metaRoot;
}
