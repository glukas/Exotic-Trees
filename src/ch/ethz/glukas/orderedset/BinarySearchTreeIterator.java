package ch.ethz.glukas.orderedset;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Stack;

import javax.naming.OperationNotSupportedException;



public class BinarySearchTreeIterator<T> implements Iterator<T> {

	
	public BinarySearchTreeIterator(TreeNode<T> node) {
		addSubtreeToDeque(node);
	}

	@Override
	public boolean hasNext() {
		return !deque.isEmpty();
	}

	@Override
	public T next() {
		return deque.pollFirst().getValue();
	}
	
	public void addSubtreeToDeque(TreeNode<T> node) {
		if (node == null) return;
		
		addSubtreeToDeque(node.getLeftChild());
		deque.addLast(node);
		addSubtreeToDeque(node.getRightChild());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();	
	}

	Deque<TreeNode<T>> deque = new ArrayDeque<TreeNode<T>>();
	
	
}
