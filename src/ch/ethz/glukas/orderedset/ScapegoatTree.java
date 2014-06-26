package ch.ethz.glukas.orderedset;

import java.util.ArrayList;

public class ScapegoatTree<T> extends BinarySearchTree<T>{

	protected int size;
	private boolean didChange;
	private boolean needsRebalance;
	private int subtreeSize;
	
	private int maxSize = 1;
	
	private static final double alpha = 0.6;//closer to 0.5 gives smaller height, more restructuring
	private static final double logalphainverse = Math.log(1/alpha);
	
	@Override
	protected boolean internalAdd(T value)
	{
		return internalAddIteratively(value);
		//setRoot(internalAddRecursively(getRoot(), value, 0));
		//return didChange;
	}
	
	@Override
	protected boolean internalRemove(T value) {
		setRoot(internalRemoveRecursively(getRoot(), value));
		if (size <= alpha*maxSize) {
			setRoot(rebalance(size, getRoot()));
			maxSize = size;
		}
		return didChange;
	}
	
	//standard remove from bst algorithm (replace with successor)
	private TreeNode<T> internalRemoveRecursively(TreeNode<T> root, T value) {
		if (root == null) {
			didChange = false;
			return null;
		}
		
		int comparison = compareValues(value, root);
		if (comparison < 0) {
			root.setLeftChild(internalRemoveRecursively(root.getLeftChild(), value));
		} else if (comparison > 0) {
			root.setRightChild(internalRemoveRecursively(root.getRightChild(), value));
		} else {
			didChange = true;
			size--;
			
			if (root.getRightChild() == null) {//no right child, can remove by returning left child
				root = root.getLeftChild();
			} else {//has right child: replace with successor
				Buffer<TreeNode<T>> successor = findSuccessor(root);
				TreeNode<T> succ = successor.get(1);
				//remove the successor from its old parent & replace root with successor
				successor.get(0).replaceChild(succ, succ.getRightChild());
				succ.setLeftChild(root.getLeftChild());
				succ.setRightChild(root.getRightChild());
				root = succ;
			}
		}
		return root;
	}

	@Override
	public void clear() {
		size = 0;
		super.clear();
	}
	
	private TreeNode<T> internalAddRecursively(TreeNode<T> currentNode, T value, int depth) {
		if (currentNode == null) {
			didChange = true;
			size++;
			if (depth > maximumHeight()) {
				needsRebalance = true;
				subtreeSize = 1;
				//System.out.println("rebalance triggered at depth " + depth);
			}
			return newNode(value);
		}
		
		int comparison = compareValues(value, currentNode);
		int leftSize;
		int rightSize;
		if (comparison < 0) {
			currentNode.setLeftChild(internalAddRecursively(currentNode.getLeftChild(), value, depth+1));
			if (needsRebalance) {
				leftSize = subtreeSize;
				assert leftSize == exhaustiveCount(currentNode.getLeftChild());
				rightSize = exhaustiveCount(currentNode.getRightChild());
				currentNode = tryRebalance(leftSize, rightSize, currentNode);
			}
		} else if (comparison > 0) {
			currentNode.setRightChild(internalAddRecursively(currentNode.getRightChild(), value, depth+1));
			if (needsRebalance) {
				leftSize = exhaustiveCount(currentNode.getLeftChild());
				rightSize = subtreeSize;
				assert rightSize == exhaustiveCount(currentNode.getRightChild());
				currentNode = tryRebalance(leftSize, rightSize, currentNode);
			}
		} else {
			didChange = false;
		}
		
		return currentNode;
	}
	
	private boolean internalAddIteratively(T value) {
		
		ArrayList<TreeNode<T>> trace = find(value);
	
		TreeNode<T> currentNode = trace.get(trace.size()-1);
		int comparison = compareValues(value, currentNode);
		
		if (comparison == 0) {
			return false;
		} else {
			TreeNode<T> newNode = newNode(value);
			currentNode.setChild(comparison, newNode);
			size++;
			if (trace.size()-2 > maximumHeight()) {
				trace.add(newNode);
				rebalanceAlongTrace(trace);
			}
			return true;
		}
	}
	
	private void rebalanceAlongTrace(ArrayList<TreeNode<T>> trace) {
		if (trace.size() <= 2) return;
		
		TreeNode<T> currentNode;
		int leftSize;
		int rightSize;
		int subtreeSize = 1;
		int cur = trace.size()-2;
		do {
			currentNode = trace.get(cur);
			if (currentNode.childDirection(trace.get(cur+1)) < 0) {
				leftSize = subtreeSize;
				rightSize = exhaustiveCount(currentNode.getRightChild());
			} else {
				leftSize = exhaustiveCount(currentNode.getLeftChild());
				rightSize = subtreeSize;
			}
			subtreeSize = leftSize+rightSize+1;
			cur--;
		} while (cur > 0 && isAlphaWeightBalanced(leftSize, rightSize));
		
		trace.get(cur).replaceChild(trace.get(cur+1), rebalance(subtreeSize, currentNode));
	}

	private TreeNode<T> tryRebalance(int leftSize, int rightSize, TreeNode<T> currentNode) {
		assert didChange;
		subtreeSize = leftSize+rightSize+1;	
		if (!isAlphaWeightBalanced(leftSize, rightSize)) {
			currentNode = rebalance(subtreeSize, currentNode);
			needsRebalance = false;
		}
		return currentNode;
	}
	
	private TreeNode<T> rebalance(int size, TreeNode<T> currentNode) {
		//System.out.println("scapegoat is " + currentNode);
		TreeNode<T> dummy = newNode(null);
		TreeNode<T> inOrderTraversal = flatten(currentNode, dummy);
		build(size, inOrderTraversal);
		return dummy.getLeftChild();
	}
	
	private TreeNode<T> build(final int n, TreeNode<T> current) {
		assert n>= 0;
		if (n == 0) {
			current.setLeftChild(null);
			return current;
		}
		
		int half = (n-1)/2;
		TreeNode<T> r = build(n-1-half, current);
		TreeNode<T> s =  build(half, r.getRightChild());
		r.setRightChild(s.getLeftChild());
		s.setLeftChild(r);
		return s;
	}

	//returns a tree which is the result of recursively flattening the currentNode and appending 'toAppend'
	private TreeNode<T> flatten(TreeNode<T> currentNode, TreeNode<T> toAppend) {
		if (currentNode == null) {
			return toAppend;
		}
		
		currentNode.setRightChild(flatten(currentNode.getRightChild(), toAppend));
		return flatten(currentNode.getLeftChild(), currentNode);
	}

	private int maximumHeight() {
		return (int)(Math.log(size)/logalphainverse);
	}
	
	private boolean isAlphaWeightBalanced(int leftSubtreeSize, int rightSubtreeSize) {
		int totalSize = leftSubtreeSize+rightSubtreeSize+1;
		return (leftSubtreeSize <= alpha*totalSize) && (rightSubtreeSize <= alpha*totalSize);
	}
	
	@Override
	public int size() {
		return size;
	}

}
