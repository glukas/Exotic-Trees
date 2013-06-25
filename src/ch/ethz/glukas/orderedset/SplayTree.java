package ch.ethz.glukas.orderedset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

/* Implements a Splay Tree as introduced in http://www.cs.cmu.edu/~sleator/papers/self-adjusting.pdf
 * 
 * Guarantees O(log n) amortized performance for contains, add and delete.
 * For any input distribution, this tree is only off by a constant factor from an optimal search tree.
 * 
 * Splay trees are especially fast if some elements are accessed much more frequently than others or if elements are accessed repeatedly in a short time interval.
 * This is because after accessing an element it will be at the root of the tree. Also, the nodes encountered on the way to the node will be much closer to the root.
 * This second fact is crucial and is what makes the trees also viable in the worst case.
 */


public class SplayTree<E> extends BinarySearchTree <E> implements NavigableSet<E> {

	@Override
	public boolean add(E val)
	{
		assert sizeIsConsistent();
		boolean modified = false;
		
		TreeNode<E> tail = splitOffTail(val);
		if (tail == null || compareValues(tail, val) != 0) {
			tail = prepend(tail, val);
			modified = true;
			count++;
		}
		tail.setLeftChild(getRoot());
		setRoot(tail);
		
		/*equivalently:
		if (!internalContains(val)) {
		//we assume that the tree is splayed around val by the internalContains call
		assert treeIsSplayedAroundValue(getRoot(), val);
		
		//do the insertion
		TreeNode<E> node = new TreeNode<E>(val);
		TreeNode<E> tail = splitOffTail(val, false);//we just splayed, no need to splay again
		node.setRightChild(tail);
		node.setLeftChild(getRoot());
		setRoot(node);
		
		incrementCount();
		modified = true;
		}*/
		
		assert sizeIsConsistent();
		assert contains(val);
		assert checkInvariants();
		
		return modified;
	}
	
	protected TreeNode<E> prepend(TreeNode<E> subtree, E newValue)
	{
		TreeNode<E> node = new TreeNode<E>(newValue);
		node.setRightChild(subtree);
		return node;
	}
	
	@Override
	//throws if val == null
	public boolean remove(Object val)
	{
		@SuppressWarnings("unchecked")
		E value = (E)val;
		boolean modified = false;
		
		TreeNode<E> tail = splitOffTail(value);
		
		if (tail != null) {
			if (compareValues(tail, value) == 0) {
				assert tail.getLeftChild() == null;
				//the root of the tail has 'val' at the root. cut it off by not joining it back in.
				tail = tail.getRightChild();
				modified = true;
				count--;
			}
			//reassemble
			joinIn(tail);
		}
		
		assert sizeIsConsistent();
		assert checkInvariants();
		return modified;
	}
	
	@Override
	public int size() {
		return count;
	}
	
	
	/////
	//IMPLEMENTATION
	////
	
	@Override
	protected boolean internalContains(E val)
	{
		assert sizeIsConsistent();
		if (isEmpty()) return false;
		//splaying moves an element to the root of the tree
		splay(val);
		return compareValues(val, getRoot()) == 0;
	}
	
	
	@Override
	public void clear()
	{
		super.clear();
		count = 0;
		assert checkInvariants();
		assert sizeIsConsistent();
	}
	
	/////
	//Navigable set
	////
	
	public E floor(E value)
	{
		return precedingValue(value, true);
	}

	public E lower(E value)
	{
		return precedingValue(value, false);
	}
	
	public E higher(E value)
	{
		return succedingValue(value, false);
	}
	
	public E ceiling(E value)
	{
		return succedingValue(value, true);
	}
	
	public E pollFirst()
	{
		E first = first();
		remove(first);
		return first;
	}
	
	public E pollLast()
	{
		E last = last();
		remove(last);
		return last;
	}
	
	@Override
	public Iterator<E> descendingIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<E> descendingSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<E> headSet(E arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<E> headSet(E arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<E> subSet(E arg0, E arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<E> subSet(E arg0, boolean arg1, E arg2, boolean arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<E> tailSet(E arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<E> tailSet(E arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	////
	//IMPLEMENTATION :: NAVIGATION
	////

	
	public E succedingValue(E value, boolean inclusive)
	{
		TreeNode<E> tail = splitOffTail(value);
		if (tail == null) return null;
		
		E result = null;
		if (!inclusive && compareValues(tail, value) == 0) {//the value might be part of the tail, but not wanted
			assert tail.getLeftChild() == null;
			if (tail.getRightChild() != null) {
				result = findFirst(tail.getRightChild()).getValue();
			}
		} else {
			result = tail.getValue();
		}
		joinIn(tail);
		
		return result;
	}
	
	protected E precedingValue(E value, boolean inclusive)
	{	
		E result = null;
		
		TreeNode<E> tail = splitOffTail(value);
		if (inclusive && tail != null && compareValues(tail, value) == 0) {//the value, if present is always part of the tail
			result = tail.getValue();
		} else if (getRoot()!= null) {
			result = last();
		}
		joinIn(tail);//reassmble
		
		return result;
	}
	
	////
	//IMPLEMENTATION : SPLITS & JOINS
	////
	
	//precondition: all elements in r are larger than all elements in this tree
	//the subtrees rooted at r2 and r1 must not share nodes, meaning r2 is not a subtree of r1 and vice versa
	protected void joinIn(TreeNode<E> r)
	{
		//if either this tree or 'r' is null, the result is simple:
		if (r == null) {
			return;
		}
		if (getRoot() == null) {
			setRoot(r);
			return;
		}
		
		assert compareValues(findLast(getRoot()), findFirst(r)) < 0;
		splay(last());
		//since the largest element is now at the root, its right child is null
		assert getRoot().getRightChild() == null;

		getRoot().setRightChild(r);
		
		assert checkInvariants();
	}
	
	
	//returns the root of the subtree containg values greater than or equal to val and removes it from this tree
	//if val is contained in the tree, it will be the root of the result, and its left child will be null
	protected TreeNode<E> splitOffTail(E val)
	{
		if (getRoot() == null) return null;
		//int countBefore = size();
		
		TreeNode<E> result;
		splay(val);
		assert treeIsSplayedAroundValue(getRoot(), val);
		
		//if the value is not contained in the tree, it is not clear if the root is now bigger or smaller than 'val'
		if (compareValues(getRoot(), val) >= 0) {
			result = getRoot();
			setRoot(getRoot().getLeftChild());
			result.setLeftChild(null);
		} else {
			result = getRoot().getRightChild();
			getRoot().setRightChild(null);
		}

		assert getRoot() != result;
		assert getRoot() == null || compareValues(last(), val) < 0;//the remaining values are smaller
		assert result == null || compareValues(findFirst(result), val) >= 0;//the resulting values are larger
		//assert result == null || (countBefore -= exhaustiveCount(result)) > -1;
		//assert getRoot() == null || (countBefore -= exhaustiveCount(getRoot())) == 0; 
		return result;
	}
	

	
	
	/////
	//IMPLEMENTATION: SPLAYING
	/////
	

	//if the value is contained in the tree, it will be the root of the tree after this operation
	//else, the root will either be the higher or lower value to 'val'
	private void splay(E val)
	{
		if (getRoot() == null) return;
		assert isInOrder();
		
		ArrayList<TreeNode<E>> trace = find(val);
		TreeNode<E> found = trace.get(trace.size()-1);
		
		//perform zig-zig and zig-zags
		int current = trace.size()-1;
		
		for (; current >= 3; current-=2) {
			TreeNode<E>	parent = trace.get(current-1);
			TreeNode<E> grandparent = trace.get(current-2);
			TreeNode<E> grandgrandparent = trace.get(current-3);
			
			splayStep(found, parent, grandparent, grandgrandparent);
		}
		
		//in the odd case, a final zig is necessary to promote the node with value 'val' to the root
		if (current == 2) {
			zig(found, trace.get(1), trace.get(0));
		}
		
		
		assert treeIsSplayedAroundValue(getRoot(), val);
		assert getRoot() == found;
		assert checkInvariants();
	}
	
	
	private void splayStep(TreeNode<E> current, TreeNode<E> parent, TreeNode<E> grandparent, TreeNode<E> grandgrandparent)
	{
		//calculate which case we are in:
		int directionSum = grandparent.childDirection(parent)+2*parent.childDirection(current);
		
		if (Math.abs(directionSum) == 3) {
			//if the direction sum is -3 the current and parent are both left children of their parents
			//if the direction sum is 3 the current and parent are both right children of their parents
			zigZig(current, parent, grandparent, grandgrandparent, directionSum);
		} else {
			//if the direction sum is 1, the current node is the left child of its parent and the parent is the right child of the grandparent
			//if the direction sum is -1, the current node is the right child of its parent and the parent is the left child of the grandparent
			zigZag(current, parent, grandparent, grandgrandparent, directionSum);
		}
	}
	
	
	//if parity > 0:
	//        z                  x
	//     x      --->        y    z
	//        y
	
	//if parity < 0:
	//     z                    x
	//        x  --->        z     y
	//     y
	
	//gpp must be the parent of z
	private void zigZag(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, TreeNode<E> ggp, int parity)
	{
		int antiparity = -parity;

		assert z.getChild(antiparity) == y;
		assert y.getChild(parity) == x;
		
		ggp.replaceChild(z, x);
		y.setChild(parity, x.getChild(antiparity));
		z.setChild(antiparity, x.getChild(parity));
		x.setChild(antiparity, y);
		x.setChild(parity, z);
		
		assert x.getChild(antiparity) == y;
		assert x.getChild(parity) == z;
	}
	
	//if parity > 0:
	//  	z                   x
	//        y    --->       y
	//          x           z
	
	//if parity < 0:
	//          z          x
	//        y    --->      y
	//     x                  z
	
	//gpp must be the parent of z
	private void zigZig(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, TreeNode<E> ggp, int parity)
	{
		int antiparity = -parity;
		
		assert z.getChild(parity) == y;
		assert y.getChild(parity) == x;
		
		ggp.replaceChild(z, x);
		y.setChild(parity, x.getChild(antiparity));
		x.setChild(antiparity, y);
		z.setChild(parity, y.getChild(antiparity));
		y.setChild(antiparity, z);
		
		assert x.getChild(antiparity) == y;
		assert y.getChild(antiparity) == z;
	}
	
	//single rotation 'upwards'
	private void zig(TreeNode<E> child, TreeNode<E> parent, TreeNode<E> grandparent)
	{
		treeRotateUp(child, parent, grandparent);
	}
	
	
	/////
	//ASSERTIONS
	/////
	
	private boolean checkInvariants()
	{
		boolean isInOrder =  isInOrder();
		assert isInOrder;
		assert metaRoot.getRightChild() == null;
		return isInOrder;
	}
	

	protected boolean treeIsSplayedAroundValue(TreeNode<E> root, E val)
	{
		if (root == null) return true;
		boolean leftsmaller =  root.getLeftChild() == null || compareValues(val, findLast(root.getLeftChild())) > 0;
		assert leftsmaller;
		boolean rightsmaller = root.getRightChild() == null || compareValues(val, findFirst(root.getRightChild())) < 0;
		assert rightsmaller;
		return leftsmaller && rightsmaller;
	}
	
	
	/////
	//INSTANCE VARIABLES
	/////
	
	int count = 0;



}
