package ch.ethz.glukas.orderedset;

import java.util.ArrayList;

/* Implements a Splay Tree as introduced in http://www.cs.cmu.edu/~sleator/papers/self-adjusting.pdf
 * 
 * Guarantees O(log n) amortized performance for contains, add and delete.
 * For any input distribution, this tree is only off by a constant factor from an optimal search tree.
 * 
 * Splay trees are especially fast if some elements are accessed much more frequently than others or if elements are accessed repeatedly in a short time interval.
 * This is because after accessing an element it will be at the root of the tree. Also, the nodes encountered on the way to the node will be much closer to the root.
 * This second fact is crucial and is what makes the trees also viable in the worst case.
 */


public class SplayTree<E> extends BinarySearchTree <E> {

	@Override
	public boolean add(E val)
	{
		assert sizeIsConsistent();
		boolean modified = false;
		
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
		}
		
		assert contains(val);
		assert checkInvariants();
		
		return modified;
	}
	
	@Override
	//throws if val == null
	public boolean remove(Object val)
	{
		@SuppressWarnings("unchecked")
		E value = (E)val;
		
		TreeNode<E> tail = splitOffTail(value);
		boolean modified = false;
		if (tail != null) {
				//the values is greater than any value in the tree. do nothing
			 if (compareValues(tail.value, value) == 0) {
				assert tail.leftChild == null;
				
				joinIn(tail.rightChild);
				modified = true;
				decrementCount();
			} else {
				joinIn(tail);
			}
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
	
	protected void decrementCount()
	{
		count--;
		assert sizeIsConsistent();
	}
	
	protected void incrementCount()
	{
		count++;
		assert sizeIsConsistent();
	}
	
	@Override
	protected boolean internalContains(E val)
	{
		assert sizeIsConsistent();
		if (isEmpty()) return false;
		//splaying moves an element to the root of the tree
		splay(val);
		return compareValues(val, getRoot().value) == 0;
	}
	
	
	@Override
	public void clear()
	{
		super.clear();
		count = 0;
		assert checkInvariants();
		assert sizeIsConsistent();
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
		
		TreeNode<E> largest = findLast(getRoot());
		splay(largest.value);
		//since the largest element is now at the root, its right child is null
		assert getRoot().rightChild == null;

		getRoot().setRightChild(r);
		
		assert checkInvariants();
	}
	
	protected TreeNode<E> splitOffTail(E val)
	{
		return splitOffTail(val, true);
	}
	//returns the root of the subtree containg values greater than or equal to val and removes it from this tree
	//if val is contained in the tree, it will be the root of the result
	protected TreeNode<E> splitOffTail(E val, boolean shouldSplay)
	{
		assert sizeIsConsistent();
		if (isEmpty()) return null;
		//int countBefore = size();
		
		TreeNode<E> result;
		if (shouldSplay) {
			splay(val);
		}
		
		//if the value is not contained in the tree, it is not clear if the root is now bigger or smaller than 'val'
		//we need to handle those cases seperately
		if (compareValues(getRoot().value, val) >= 0) {
			result = getRoot();
			setRoot(getRoot().leftChild);
			result.setLeftChild(null);
		} else {
			result = getRoot().rightChild;
			getRoot().setRightChild(null);
		}

		assert getRoot() != result;
		assert getRoot() == null || compareValues(last(), val) < 0;//the remaining values are smaller
		assert result == null || compareValues(findFirst(result).value, val) >= 0;//the resulting values are larger
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
			
			//calculate which case we are in:
			int directionSum = childDirection(parent, grandparent)+2*childDirection(found, parent);;
			
			if (directionSum == 0) {
				//if the direction sum is 0 the current and parent are both left children of their parents
				//->perform zig zig
				zigZigRightwards(found, parent, grandparent, grandgrandparent);
			} else if (directionSum == 1) {
				//if the direction sum is 1, the current node is the left child of its parent and the parent is the right child of the grandparent
				//->perform zag zig (zag zig is the symmetrical operation to zig zag)
				zagZig(found, parent, grandparent, grandgrandparent);
			} else if (directionSum == 2) {
				//if the direction sum is 2, the current node is the right child of its parent and the parent is the left child of the grandparent
				//->perform zig zag
				zigZag(found, parent, grandparent, grandgrandparent);
			} else {
				assert directionSum == 3;
				//if the direction sum is 3 the current and parent are both right children of their parents
				//->perform zig zig
				zigZigLeftwards(found, parent, grandparent, grandgrandparent);
			}
		}
		
		//in the odd case, a zig is necessary to promote the node with value 'val' to the root
		if (current == 2) {
			zig(found, trace.get(1), trace.get(0));
		}
		
		
		assert treeIsSplayedAroundValue(getRoot(), val);
		assert getRoot() == found;
		assert checkInvariants();
	}
	
	
	//double rotation: brings up the current and sets its parent and grandparent as children
	
	//        z                  x
	//     x      --->        y    z
	//        y
	private void zigZag(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, TreeNode<E> ggp)
	{
		assert z.leftChild == y;
		assert y.rightChild == x;
		
		ggp.replaceChild(z, x);
		y.setRightChild(x.leftChild);
		z.setLeftChild(x.rightChild);
		x.setLeftChild(y);
		x.setRightChild(z);
		
		assert x.leftChild == y;
		assert x.rightChild == z;
	}
	
	
	//symmetric to zigZag
	
	//     z                    x
	//        x  --->        z     y
	//     y
	private void zagZig(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, TreeNode<E> ggp)
	{
		assert z.rightChild == y;
		assert y.leftChild == x;
		
		ggp.replaceChild(z, x);
		y.setLeftChild(x.rightChild);
		z.setRightChild(x.leftChild);
		x.setRightChild(y);
		x.setLeftChild(z);

		assert x.rightChild == y;
		assert x.leftChild == z;
	}
	
	
	//Two rotations leftwards:
	
	//	z                   x
	//    y    --->       y
	//      x           z
	private void zigZigLeftwards(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, TreeNode<E> ggp)
	{
		assert z.rightChild == y;
		assert y.rightChild == x;
		
		ggp.replaceChild(z, x);
		y.setRightChild(x.leftChild);
		x.setLeftChild(y);
		z.setRightChild(y.leftChild);
		y.setLeftChild(z);
		
		assert x.leftChild == y;
		assert y.leftChild == z;
	}
	
	//two rotations rightwards:
	
	//      z         x
	//    y    --->     y
	//  x                 z
	private void zigZigRightwards(TreeNode<E> x, TreeNode<E> y, TreeNode<E> z, TreeNode<E> ggp)
	{
		assert z.leftChild == y;
		assert y.leftChild == x;
		
		ggp.replaceChild(z, x);
		y.setLeftChild(x.rightChild);
		x.setRightChild(y);
		z.setLeftChild(y.rightChild);
		y.setRightChild(z);
		
		assert x.rightChild == y;
		assert y.rightChild == z;
	}
	
	//single rotation 'upwards'
	private void zig(TreeNode<E> child, TreeNode<E> parent, TreeNode<E> grandparent)
	{
		treeRotateUp(child, parent, grandparent);
	}

	
	//precondition: child is a child of parent
	//returns 0 if left child, 1 if right child
	private int childDirection(TreeNode<E> child, TreeNode<E> parent)
	{
		if (parent.leftChild == child) {
			return 0;
		} else {
			assert parent.rightChild == child;
			return 1;
		}
	}
	
	
	/////
	//ASSERTIONS
	/////
	
	private boolean checkInvariants()
	{
		boolean isInOrder =  isInOrder();
		assert isInOrder;
		assert metaRoot.rightChild == null;
		return isInOrder;
	}
	

	protected boolean treeIsSplayedAroundValue(TreeNode<E> root, E val)
	{
		if (root == null) return true;
		boolean leftsmaller =  root.leftChild == null || compareValues(val, findLast(root.leftChild).value) > 0;
		assert leftsmaller;
		boolean rightsmaller = root.rightChild == null || compareValues(val, findFirst(root.rightChild).value) < 0;
		assert rightsmaller;
		return leftsmaller && rightsmaller;
	}
	
	
	/////
	//INSTANCE VARIABLES
	/////
	
	int count = 0;
}
