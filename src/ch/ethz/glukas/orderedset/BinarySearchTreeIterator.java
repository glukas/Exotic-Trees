package ch.ethz.glukas.orderedset;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Stack;

import javax.naming.OperationNotSupportedException;



public class BinarySearchTreeIterator<T> implements Iterator<T> {

	public BinarySearchTreeIterator(TreeNode<T> node, boolean descending) {
		init(node, descending);
	}
	
	public BinarySearchTreeIterator(TreeNode<T> node) {
		init(node, false);
	}
	
	private void init(TreeNode<T> node, boolean descending)
	{
		if (descending) {
			parity = 1;
		} else {
			parity = -1;
		}
		push(node);
	}

	@Override
	public boolean hasNext() {
		return !deque.isEmpty();
	}

	@Override
	public T next() {
		
		TreeNode<T> current = deque.pop();
		push(current.getChild(-parity));
		return current.getValue();
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();	
	}
	
	private void push(TreeNode<T> node)
	{
		while(node != null) {
			deque.push(node);
			node = node.getChild(parity);
		}
	}

	Deque<TreeNode<T>> deque = new ArrayDeque<TreeNode<T>>();
	int parity;//if parity is negative the left subtree is traversed first. otherwise the right subtree is traversed first ('descending order')
}
