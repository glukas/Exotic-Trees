package ch.ethz.glukas.orderedset;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Stack;

import javax.naming.OperationNotSupportedException;



public class BinarySearchTreeIterator<T> implements Iterator<T> {

	public BinarySearchTreeIterator(TreeNode<T> node) {
		push(node);
	}

	@Override
	public boolean hasNext() {
		return !deque.isEmpty();
	}

	@Override
	public T next() {
		
		TreeNode<T> current = deque.pop();
		push(current.getRightChild());
		
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
			node = node.getLeftChild();
		}
	}

	Deque<TreeNode<T>> deque = new ArrayDeque<TreeNode<T>>();
	
}
