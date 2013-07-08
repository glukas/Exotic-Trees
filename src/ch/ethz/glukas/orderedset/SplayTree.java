package ch.ethz.glukas.orderedset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Random;
import java.util.SortedSet;

/* Implements a SplayTree as introduced in http://www.cs.cmu.edu/~sleator/papers/self-adjusting.pdf
 * 
 * Guarantees O(log n) amortized performance for contains, add and delete.
 * For any input distribution, this tree is only off by a constant factor from an optimal search tree.
 * 
 * SplayTrees are especially fast if some elements are accessed more frequently than others or if elements are accessed repeatedly in a short time interval.
 * This is because after accessing an element it will be at the root of the tree. Also, the nodes encountered on the way to the node will be much closer to the root.
 * This second fact is crucial and is what makes the trees also viable in the worst case.
 * 
 * As a subclass of RankedTree, this SplayTree also provides dynamic order statistics.
 */


public class SplayTree<E> extends RankedTree <E> {
	
	/////
	//IMPLEMENTATION
	////
	
	@Override
	protected boolean internalContains(E val)
	{
		//splaying moves an element to the root of the tree
		splay(val);
		return compareValues(val, getRoot()) == 0;
	}
	
	
	@Override
	protected boolean internalAdd(E val)
	{
		boolean modified = false;
		
		TreeNode<E> tail = splitOffTail(val);
		if (compareValues(tail, val) != 0) {//if tail is null, the comparison will return +1
			tail = prepend(tail, val);
			modified = true;
		}

		joinUp(tail);
		
		return modified;
	}
	
	@Override
	protected boolean internalRemove(E value)
	{
		boolean modified = false;

		TreeNode<E> tail = splitOffTail(value);
		if (compareValues(tail, value) == 0) {//if tail is null, the comparison will return +1
			assert tail.getLeftChild() == null;
			//the root of the tail has 'val' at the root. cut it off by not joining it back in.
			tail = tail.getRightChild();
			modified = true;
		}
		//reassemble
		joinIn(tail);
		
		return modified;
	}
	
	
	/////
	//Navigable set
	////
	
	public E floor(E value)
	{
		if (internalContains(value)) return value;
		E flo = lower(value);
		return flo;
	}

	public E lower(E value)
	{
		E lo = precedingValue(value);
		E hilo = null;
		assert lo == null || (hilo = succedingValue(lo)) == null || true;
		assert hilo == null || compareValues(hilo, value) == 0;
		return lo;
	}
	
	public E higher(E value)
	{
		E hi = succedingValue(value);
		return hi;
	}
	
	public E ceiling(E value)
	{
		if (internalContains(value)) return value;
		return higher(value);
	}
	
	
	////
	//IMPLEMENTATION :: NAVIGATION
	////

	
	//not inclusive
	public E succedingValue(E value)
	{
		assert getRoot() != null;
		E result;
		
		splay(value);
		if (compareValues(getRoot(), value) > 0) {
			result = getRoot().getValue();
		} else {
			result = valueOrNull(successor(getRoot()));
			if (result!= null && rand.nextFloat() < 0.1) {//splay on the value sometimes to prevent bad worst case
				splay(result);
			}
		}
		
		assert result == null || compareValues(result, value) > 0;
		assert result != null || getRoot() == null || compareValues(value, last()) >= 0;
		
		assert checkInvariants();
		return result;
	}
	
	
	//not inclusive
	protected E precedingValue(E value)
	{
		assert getRoot() != null;
		E result;
		
		splay(value);
		if (compareValues(getRoot(), value) < 0) {
			result = getRoot().getValue();
		} else {
			result = valueOrNull(predecessor(getRoot()));
			if (result!= null && rand.nextFloat() < 0.1) {//splay on the value sometimes to prevent bad worst case
				splay(result);
			}
		}
		
		assert result == null || compareValues(result, value) < 0;
		assert result != null || getRoot() == null || compareValues(value, first()) <= 0;
		
		assert checkInvariants();
		return result;
	}
	
	
	////
	//IMPLEMENTATION : SPLITS & JOINS
	////
	
	protected TreeNode<E> prepend(TreeNode<E> subtree, E newValue)
	{
		assert compareValues(subtree, newValue) > 0;
		
		TreeNode<E> node = newNode(newValue);
		node.setRightChild(subtree);
		return node;
	}
	
	protected void joinUp(TreeNode<E> tail)
	{
		assert tail.getLeftChild() == null;
		assert getRoot() == null || compareValues(last(), tail) < 0;
		
		tail.setLeftChild(getRoot());
		setRoot(tail);
	}
	
	//precondition: all elements in r are larger than all elements in this tree
	//the subtrees rooted at r2 and r1 must not share nodes, meaning r2 is not a subtree of r1 and vice versa
	protected void joinIn(TreeNode<E> r)
	{
		//if either this tree or 'r' is null, the result is simple:
		if (r == null) {
			return;
		} else if (getRoot() == null) {
			setRoot(r);
			return;
		}
		
		assert isInOrder(r);
		assert compareValues(findLast(getRoot()), findFirst(r)) < 0;
		
		splay(last());
		
		//since the largest element is now at the root, its right child is null
		assert getRoot().getRightChild() == null;
		getRoot().setRightChild(r);
		((RankedTreeNode<E>)metaRoot).setSize(size(getRoot())+1);//the size of the metaRoot needs to be updated
		
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
			setRoot(getRoot());
		}

		assert getRoot() != result;
		assert getRoot() == null || compareValues(last(), val) < 0;//the remaining values are smaller
		assert result == null || compareValues(findFirst(result), val) >= 0;//the resulting values are larger
		//assert result == null || (countBefore -= exhaustiveCount(result)) > -1;
		//assert getRoot() == null || (countBefore -= exhaustiveCount(getRoot())) == 0; 
		return result;
	}
	
	////
	//Access by rank : add splaying operation where needed
	///
	
	@Override
	public E get(int index)
	{
		E result = super.get(index);
		splay(result);
		return result;
	}
	
	@Override
	public int indexOf(E value)
	{
		if (!isEmpty()) {
			splay(value);
		}
		int result = super.indexOf(value);
		return result;
	}
	
	/////
	//IMPLEMENTATION: SPLAYING
	/////
	
	
	//if the value is contained in the tree, it will be the root of the tree after this operation
	//else, the root will either be the higher or lower value to 'val'
	private void splay(E val)
	{
		assert (getRoot() != null);
		
		ArrayList<TreeNode<E>> trace = find(val);
		splay(trace);
	}
	
	
	//splay using the trace provided. the root of the subtree to be modified must be at index 1, its parent at index 0. The node to be splayed is at the last index.
	private void splay(ArrayList<TreeNode<E>> trace)
	{
		assert isInOrder();
		
		int current = trace.size()-1;
		TreeNode<E> found = trace.get(current);
		
		//perform zig-zig and zig-zags
		for (; current >= 3; current-=2) {
			TreeNode<E>	parent = trace.get(current-1);
			TreeNode<E> grandparent = trace.get(current-2);
			TreeNode<E> grandgrandparent = trace.get(current-3);
			
			splayStep(found, parent, grandparent);
			grandgrandparent.replaceChild(grandparent, found);
		}
		
		//in the odd case, a final zig is necessary to promote the node with value 'val' to the root
		if (current == 2) {
			zig(found, trace.get(1), trace.get(0));
		}
		
		assert treeIsSplayedAroundValue(getRoot(), found.getValue());
		assert getRoot() == found;
		assert checkInvariants();
	}
	
	
	private void splayStep(TreeNode<E> current, TreeNode<E> parent, TreeNode<E> grandparent)
	{
		//calculate which case we are in:
		int directionSum = grandparent.childDirection(parent)+2*parent.childDirection(current);
		
		if (Math.abs(directionSum) == 3) {
			//if the direction sum is -3 the current and parent are both left children of their parents
			//if the direction sum is 3 the current and parent are both right children of their parents
			zigZig(current, parent, grandparent, directionSum);
		} else {
			//if the direction sum is -1, the current node is the left child of its parent and the parent is the right child of the grandparent
			//if the direction sum is 1, the current node is the right child of its parent and the parent is the left child of the grandparent
			zigZag(current, parent, grandparent, directionSum);
		}
	}
	
	
	//if parity > 0:
	//        z                  x
	//     y      --->        y    z
	//        x
	
	//if parity < 0:
	//     z                    x
	//        y  --->        z     y
	//     x
	
	//gpp must be the parent of z
	private void zigZag(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, int parity)
	{
		int antiparity = -parity;

		assert z.getChild(antiparity) == y;
		assert y.getChild(parity) == x;
		
		y.setChild(parity, x.getChild(antiparity));
		z.setChild(antiparity, x.getChild(parity));
		x.setChild(antiparity, y);
		x.setChild(parity, z);
		
		assert x.getChild(antiparity) == y;
		assert x.getChild(parity) == z;
		assert subtreeSizeConsistent(x);
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
	private void zigZig(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, int parity)
	{
		int antiparity = -parity;
		
		assert z.getChild(parity) == y;
		assert y.getChild(parity) == x;
		
		y.setChild(parity, x.getChild(antiparity));
		z.setChild(parity, y.getChild(antiparity));
		y.setChild(antiparity, z);
		x.setChild(antiparity, y);
		
		assert x.getChild(antiparity) == y;
		assert y.getChild(antiparity) == z;
		assert subtreeSizeConsistent(x);
	}
	
	//single rotation 'upwards'
	private void zig(TreeNode<E> child, TreeNode<E> parent, TreeNode<E> grandparent)
	{
		treeRotateUp(child, parent, grandparent);
	}
	
	
	/////
	//ASSERTIONS
	/////

	
	protected boolean treeIsSplayedAroundValue(TreeNode<E> root, E val)
	{
		if (root == null) return true;
		boolean leftsmaller =  root.getLeftChild() == null || compareValues(val, predecessor(root)) > 0;
		assert leftsmaller;
		boolean rightsmaller = root.getRightChild() == null || compareValues(val, successor(root)) < 0;
		assert rightsmaller;
		return leftsmaller && rightsmaller;
	}
	
	
	/////
	//INSTANCE VARIABLES
	/////
	
	final Out<TreeNode<E>> lower = new Out<TreeNode<E>>();
	final Out<TreeNode<E>> higher = new Out<TreeNode<E>>();
	final Random rand = new Random();
}
