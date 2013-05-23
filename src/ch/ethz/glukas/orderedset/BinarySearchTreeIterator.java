package ch.ethz.glukas.orderedset;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Stack;

import javax.naming.OperationNotSupportedException;



public class BinarySearchTreeIterator<T> implements Iterator<T> {
	//idea for traversal from http://leetcode.com/2010/04/binary-search-tree-in-order-traversal.html
	
	public BinarySearchTreeIterator(TreeNode<T> node) {
		deque.addLast(node);
	}

	@Override
	public boolean hasNext() {
		return !deque.isEmpty();
	}

	@Override
	public T next() {
		T result = null;
		if (current != null) {
			deque.addLast(current);
			current = current.getLeftChild();
		} else {
			current = deque.pollLast();
			result = current.getValue();
			current = current.getRightChild();
		 }
		return result;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();	
	}

	Deque<TreeNode<T>> deque = new ArrayDeque<TreeNode<T>>();
	TreeNode<T> current;
	
}
