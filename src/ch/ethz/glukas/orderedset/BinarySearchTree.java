package ch.ethz.glukas.orderedset;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 
 * An abstract Binary Search Tree
 * Provides find operations, iteration and splits
 * 
 * @author Lukas Gianinazzi
 *
 * @param <T>
 */

class BinarySearchTree<T> extends AbstractCollection<T> {
	
	
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
	public boolean containsAll(Collection<?> arg0) {
		boolean contains = true;
		for (Object o : arg0) {
			if (!contains(o)) {
				contains = false;
				break;
			}
		}
		return contains;
	}

	@Override
	public Iterator<T> iterator() {
		return new BinarySearchTreeIterator<T>(metaRoot.getLeftChild());
	}
	
	
	@Override
	public int size() {
		return count;
	}
	
	@Override
	public void clear() {
		count = 0;
		metaRoot = new TreeNode<T>(null);
	}
	
	
	////
	//ORDERING
	////
	
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
	//IMPLEMENTATION :: SPLIT
	////
	
	
	/*
	//Algorithm from http://www.cs.cmu.edu/~scandal/papers/treaps-spaa98.pdf
	protected TreeNode<T> persistentSplit(TreeNode<T> root, T value, Ref<TreeNode<T>> greater, Ref<TreeNode<T>> less)
	{
		//base case 1
		if (root == null) {
			greater.set(null);
			less.set(null);
			return null;
		}
		
		
		TreeNode<T> newRoot = new TreeNode<T>(root.getValue());
		int comparison = compareValues(root.getValue(), value);
		
		if (comparison < 0) {//recursive step 1
			
			less.set(newRoot);
			Ref<TreeNode<T>> newRootLess = new Ref<TreeNode<T>>();
			newRoot = persistentSplit(root.getRightChild(), value, greater, newRootLess);
			if (newRoot != null) {
				newRoot.setRightChild(newRootLess.get());
			}
			return newRoot;
			
		} else if (comparison > 0) {//recursive step 2
			greater.set(newRoot);
			Ref<TreeNode<T>> newRootGreater = new Ref<TreeNode<T>>();
			newRoot = persistentSplit(root.getLeftChild(), value, newRootGreater, less);
			if (newRoot != null) {
				newRoot.setLeftChild(newRootGreater.get());
			}
			return newRoot;
			
		} else {//base case 2
			
			assert comparison == 0;
			less.set(root.getLeftChild());
			greater.set(root.getRightChild());
			return newRoot;
		}
		
		
	}*/
	
	
	
	/*
	 * Iterative top down non-persistent split
	 * 
	 * returns the node that has equal value to 'value'
	 * less and greater contain the headSet and tailSet respectively
	 */
	protected TreeNode<T> split(TreeNode<T> root, TreeNode<T> parent, T value, Ref<TreeNode<T>> less, Ref<TreeNode<T>> greater)
	{
		
		if (root == null) {
			greater.set(null);
			less.set(null);
			return null;
		}
		
		assert parent != null;
		
		TreeNode<T> equal = null;
		
		TreeNode<T> currentNode = root;
		TreeNode<T> currentParent = parent;
		
		TreeNode<T> splitMetaRoot = new TreeNode<T>(null);
		TreeNode<T> currentSplitNode = splitMetaRoot;
		
		int comparison = -1;
		while(currentNode != null) {
			
			comparison = compareValues(value, currentNode.getValue());
			
			if (comparison > 0) {//value is greater than current node: don't include the current root, continue looking for larger values
				currentParent = currentNode;
				currentNode = currentNode.getRightChild();
			} else if (comparison < 0) {//value is smaller than current node: include current node into the large value subtree, continue looking for smaller values
				
				//append to large value tree
				currentSplitNode.setLeftChild(currentNode);
				currentSplitNode = currentNode;
				
				//remove from small value tree
				currentParent.replaceChild(currentNode, currentNode.getLeftChild());
				//advance search
				currentNode = currentNode.getLeftChild();
				

			} else if (comparison == 0) {
				
				equal = currentNode;
				
				currentSplitNode.setLeftChild(currentNode.getRightChild());
				
				//remove from smaller valued tree
				currentParent.replaceChild(currentNode, currentNode.getLeftChild());
				
				currentNode.isolate();
				break;
			}
			
		}

		if (comparison != 0) {
			currentSplitNode.setLeftChild(null);
		}
		
		
		greater.set(splitMetaRoot.getLeftChild());
		
		if (compareValues(root.getValue(), parent.getValue()) < 0) {
			less.set(parent.getLeftChild());
			parent.setLeftChild(null);
		} else {
			less.set(parent.getRightChild());
			parent.setRightChild(null);
		}
		
		return equal;
	}
	
	
	/////
	//IMPLEMENTATION : HELPER METHODS
	/////
	
	protected void decrementCount()
	{
		assert count > 0;
		count--;
	}
	
	protected void incrementCount()
	{
		count++;
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
	
	
	///
	//INVARIANT
	///
	
	
	protected boolean isInOrder()
	{
		@SuppressWarnings("unchecked")
		T[] sorted = (T[]) toArray();
		for (int i=1; i<sorted.length; i++) {
			if (compareValues(sorted[i], sorted[i-1]) < 0) return false;
		}
		return true;
	}
	
	
	////
	//INSTANCE VARIABLES
	////
	

	
	private int count = 0;
	private Comparator<? super T> internalComparator;
	protected TreeNode<T> metaRoot;
}
