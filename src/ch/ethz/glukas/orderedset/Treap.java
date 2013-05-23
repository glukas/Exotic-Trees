package ch.ethz.glukas.orderedset;
import java.util.*;

public class Treap<T> implements SortedSet<T> {
	

	
	////
	//CONSTRUCTION
	////
	
	
	public Treap(Comparator<T> comparator)
	{
		clear();
		internalComparator = comparator;
	}
	

	
	/////
	///SORTED SET INTERFACE
	/////
	
	@Override
	public boolean add(T arg0) {
		if (arg0.equals(null)) throw new NullPointerException();
		
		//return internalAddRotatingDownwards(arg0, getPriorityForValue(arg0), metaRoot);
		return internalAddAtLeafAndRebalanceUpwards(arg0);
	}

	
	
	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		boolean changed = false;
		for (T elem : arg0) {
			changed = changed || add(elem);
		}
		return changed;
	}

	@Override
	public void clear() {
		count = 0;
		metaRoot = new TreeNode<T>(null);
		a0 = random.nextInt();
		a1 = random.nextInt();
		a2 = random.nextInt();
		a3 = random.nextInt();
		//a4 = random.nextInt();
	}

	
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
	public boolean isEmpty() {
		return size()==0;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * if arg0 is null, false is returned and the collection is not modified
	 */
	@Override
	public boolean remove(Object arg0) {
		
		if (arg0 == null) return false;

		@SuppressWarnings("unchecked")
		T value = (T)arg0;

		Buffer<TreeNode<T>> trace = find(value, 2);
		
		boolean modified = false;
		TreeNode<T> toRemove = trace.get(trace.size()-1);
		
		if (compareValues(toRemove.getValue(), value) == 0) {
			assert trace.size() >= 2;
			
			modified = true;
			TreeNode<T> parent = trace.get(trace.size()-2);
			
			internalRemove(toRemove, parent);
			assert !contains(value);
			internalRebalanceDownwards(toRemove, parent);
			
			decrementCount();
		}
		
		
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean modified = false;
		for (Object o : arg0) {
			modified = modified || remove(o);
		}
		return modified;
	}
	

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		return count;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<? super T> comparator() {
		return internalComparator;
	}

	@Override
	public T first() {
		//TODO: test
		ArrayList<TreeNode<T>> node = traceNodeWithValueStartingFrom(metaRoot.getLeftChild(), null);
		
		return node.get(0).getValue();
	}

	@Override
	public SortedSet<T> headSet(T arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T last() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<T> subSet(T arg0, T arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<T> tailSet(T arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/////
	///IMPLEMENTATION
	//////
	
	
	private boolean internalAddRotatingDownwards(T value, int p, TreeNode<T> node)
	{
		int comparison = compareValues(value, node.getValue());
		boolean modified = false;
		if (comparison < 0) {
			if (node.getLeftChild() == null) {
				node.setLeftChild(new TreeNode<T>(value));
				incrementCount();
				modified = true;
			} else {
				modified = internalAddRotatingDownwards(value, p, node.getLeftChild());
			}
			
			if (modified && getPriorityForNode(node.getLeftChild()) < p) {
				treeRotateUpwards(node.getLeftChild(), node);
			}
			
		} else if (comparison > 0) {
			if (node.getRightChild() == null) {
				node.setRightChild(new TreeNode<T>(value));
				incrementCount();
				modified = true;
			} else {
				modified = internalAddRotatingDownwards(value, p, node.getRightChild());
			}
			
			if (modified && getPriorityForNode(node.getRightChild()) < p) {
				treeRotateUpwards(node.getLeftChild(), node);
			}
		}
		
		return modified;
	}
	
	private void treeRotateUpwards(TreeNode<T> leftChild, TreeNode<T> node) {
		// TODO Auto-generated method stub
		
	}



	//insertion algorithm 1
	private boolean internalAddAtLeafAndRebalanceUpwards(T arg0)
	{
		ArrayList<TreeNode<T>> trace = find(arg0);
		
		boolean modified = false;
		
		if (trace.size() == 1 || compareValues( trace.get(trace.size()-1).getValue(), arg0) != 0) {
			modified = true;
			
			TreeNode<T> newNode = internalAdd(trace.get(trace.size()-1), arg0);
			trace.add(newNode);
			internalRebalanceUpwards(trace);
			
			incrementCount();
		}
		
		return modified;
	}
	
	private TreeNode<T> internalAdd(TreeNode<T> parent, T newValue) {
		
		TreeNode<T> newNode = new TreeNode<T>(newValue);
		
		int comparison = compareValues(newValue, parent.getValue());
		
		if (comparison < 0) {
			parent.setLeftChild(newNode);
		} else {
			parent.setRightChild(newNode);
		}
		
		return newNode;
	}
	

	private boolean internalContains(T value)
	{
		//return traceNodeWithValueStartingFrom(metaRoot.getLeftChild(), value, false).size() > 0;
		return findNodeWithValueStartingFrom(metaRoot, value) != null;
	}
	

	
	

	//if toRemove has 1 or 0 children it is removed and toRemove.getRightChild()==toRemove.getLeftChild()==null
	//else the value and priority it holds is swapped with its successor : a rebalance operation may be required
	private void internalRemove(TreeNode<T> toRemove, TreeNode<T> parent)
	{
		
		if (!toRemove.hasChildren()) {
			parent.replaceChild(toRemove, null);
			
		} else if (toRemove.getRightChild() == null) {
			assert toRemove.getLeftChild() != null;
			parent.replaceChild(toRemove, toRemove.getLeftChild());
			toRemove.setLeftChild(null);
			
		} else if (toRemove.getLeftChild() == null) {
			assert toRemove.getRightChild() != null;
			parent.replaceChild(toRemove, toRemove.getRightChild());
			toRemove.setRightChild(null);
			
		} else {
			Buffer<TreeNode<T>> trace = findSuccessor(toRemove);
			assert trace.size() >= 2;
			
			TreeNode<T> successor = trace.get(trace.size()-1);
			TreeNode<T> successorsParent = trace.get(trace.size()-2);
			assert successor != successorsParent;
			swapNodeValues(successor, toRemove);
			
			internalRemove(successor, successorsParent);
		}
		
	}
	
	
	
	//////
	///IMPLEMENTATION :: FIND
	//////
	
	
	
	//the first element of the list is always the metaRoot
	//else if the value is present in the collection, the last element of the returned list is the node containing the value
	//else the last element of the list is the next highest node
	private ArrayList<TreeNode<T>> find(T value)
	{
		return traceNodeWithValueStartingFrom(metaRoot, value);
	}
	
	private Buffer<TreeNode<T>> find(T value, int capacity)
	{
		return traceNodeWithValueStartingFrom(metaRoot, value, capacity);
	}
	
	
	
	//Basically identical to traceNodeWithValueStartinFrom, but has less overhead, since only the last node is returned:
	private TreeNode<T> findNodeWithValueStartingFrom(TreeNode<T> currentNode, T valueToFind)
	{
		int comparison = -1;
		while (currentNode != null && comparison != 0) {
			comparison = compareValues(valueToFind, currentNode.getValue());
			if (comparison < 0) {
				currentNode = currentNode.getLeftChild();
			} else if (comparison > 0) {
				currentNode = currentNode.getRightChild();
			}
		}
		return currentNode;
	}
	
	
	/*
	private ArrayList<TreeNode<T>> findSuccessor(TreeNode<T> node)
	{
		assert node.getRightChild() != null;
		
		ArrayList<TreeNode<T>> trace = traceNodeWithValueStartingFrom((TreeNode<T>)node.getRightChild(), node.getValue());
		if (trace.size() == 1) {//if the successor is the immediate right child, the node will need to be added to the trace
			trace.add(0, node);
		}
		return trace;
	}
	*/
	
	
	private Buffer<TreeNode<T>> findSuccessor(TreeNode<T> node)
	{
		assert node.getRightChild() != null;
		
		Buffer<TreeNode<T>> trace = traceNodeWithValueStartingFrom((TreeNode<T>)node.getRightChild(), node.getValue(), 2);
		if (trace.numberOfUsedSlots() == 1) {//if the successor is the immediate right child, the node will need to be added to the trace
			TreeNode<T> successor = trace.get(0);
			trace.add(node);
			trace.add(successor);
		}
		return trace;
	}
	
	
	private ArrayList<TreeNode<T>> traceNodeWithValueStartingFrom(TreeNode<T> startingNode, T valueToFind)
	{
		int initialCapacity = Integer.numberOfLeadingZeros(count);
		
		ArrayList<TreeNode<T>> trace = new ArrayList<TreeNode<T>>(initialCapacity);

		TreeNode<T> currentNode = startingNode;
		
		int comparison = -1;
		
		while (currentNode != null) {
			trace.add((TreeNode<T>)currentNode);
			
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
	
	private Buffer<TreeNode<T>> traceNodeWithValueStartingFrom(TreeNode<T> startingNode, T valueToFind, int maximumParentBufferSize)
	{

		Buffer<TreeNode<T>> trace = new Buffer<TreeNode<T>>(maximumParentBufferSize);

		TreeNode<T> currentNode = startingNode;
		int comparison = -1;
		while (currentNode != null) {
			trace.add((TreeNode<T>)currentNode);
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
	
	//////
	////IMPLEMENTATION :: BALANCE
	//////
	
	private void internalRebalanceUpwards(Buffer<TreeNode<T>> trace)
	{
		assert trace.size() > 0;
		
		int currentIndex = trace.size()-1;
		
		while (currentIndex > 1 && getPriorityForNode(trace.get(currentIndex)) < getPriorityForNode(trace.get(currentIndex-1))) {
			
			treeRotateUp(trace.get(currentIndex), trace.get(currentIndex-1), trace.get(currentIndex-2));
			trace.swap(currentIndex, currentIndex-1);//the tree rotation needs to be reflected in the trace
			currentIndex = currentIndex - 1;
		}
	}
	
	
	private void internalRebalanceUpwards(ArrayList<TreeNode<T>> trace)
	{
		assert trace.size() > 0;
		
		int currentIndex = trace.size()-1;
		
		while (currentIndex > 1 && getPriorityForNode(trace.get(currentIndex)) < getPriorityForNode(trace.get(currentIndex-1))) {
			
			treeRotateUp(trace.get(currentIndex), trace.get(currentIndex-1), trace.get(currentIndex-2));
			swap(trace, currentIndex, currentIndex-1);//the tree rotation needs to be reflected in the trace
			currentIndex = currentIndex - 1;
		}
	}
	
	
	private void internalRebalanceDownwards(TreeNode<T> currentNode, TreeNode<T> parent)
	{
		TreeNode<T> localMin = nodeWithLocallyMinimalPriority(currentNode);
		while (localMin != currentNode) {
			
			treeRotateUp(localMin, currentNode, parent);
			
			parent = localMin;
			localMin = nodeWithLocallyMinimalPriority(currentNode);
		}
	}
	
	private void treeRotateLeft(TreeNode<T> child, TreeNode<T> parent)
	{
		parent.setRightChild(child.getLeftChild());
		child.setLeftChild(parent);
	}
	
	private void treeRotateRight(TreeNode<T> child, TreeNode<T> parent)
	{
		parent.setLeftChild(child.getRightChild());
		child.setRightChild(parent);
	}
	
	private void treeRotateUp(TreeNode<T> child, TreeNode<T> parent, TreeNode<T> grandmother)
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
	

	////
	//IMPLEMENTATION : HELPERS
	/////
	
	//wraps the comparator to make it partially null-safe: null is interpreted as +infinity
	//if both arguments are 0 the value is undefined
	private int compareValues(T v1 ,T v2) {
		assert v1 != null || v2 != null;
		if (v2 == null) return -1;
		if (v1 == null) return +1;
		return comparator().compare(v1, v2);
	}
	
	
	//precondition: node.hasChildren()
	private int compareChildrenPriorities(TreeNode<T> node)
	{
		assert node.hasChildren();
		
		if (node.getLeftChild() == null) return 1;
		if (node.getRightChild() == null) return -1;
		return (getPriorityForNode((TreeNode<T>) node.getLeftChild())) - (getPriorityForNode((TreeNode<T>) node.getRightChild()));
	}
	
	
	private void swapNodeValues(TreeNode<T> node1, TreeNode<T> node2)
	{
		T value1 = node1.getValue();
		node1.setValue(node2.getValue());
		node2.setValue(value1);
	}
	
	private TreeNode<T> nodeWithLocallyMinimalPriority(TreeNode<T> currentNode)
	{
		TreeNode<T> min = currentNode;
		if (currentNode.getLeftChild() != null && getPriorityForNode(((TreeNode<T>) currentNode.getLeftChild())) < getPriorityForNode(currentNode)) {
			min = currentNode;
		}
		if (currentNode.getRightChild() != null && getPriorityForNode((TreeNode<T>) currentNode.getRightChild()) < getPriorityForNode(currentNode)) {
			min = currentNode;
		}
		return min;
	}
	
	private <S> void swap(List<S> list, int index1, int index2)
	{
		S temp = list.get(index1);
		list.set(index1, list.get(index2));
		list.set(index2, temp);
	}
	
	
	private int getPriorityForNode(TreeNode<T> node)
	{
		return getPriorityForValue(node.getValue());
	}
	private int getPriorityForValue(T value)
	{
		int c = value.hashCode();
		int cc = c*c;
		int ccc = cc*c;
		return a0+c*a1+cc*a2+ccc*a3;
	}
	
	
	private void decrementCount()
	{
		assert count > 0;
		count--;
	}
	
	private void incrementCount()
	{
		count++;
	}


	
	
	//////
	//INSTANCE VARIABLES
	////
	
	private int count = 0;
	private Comparator<? super T> internalComparator;
	private TreeNode<T> metaRoot;
	
	private Random random = new Random(1);
	
	//random polynomial coefficients (for priority hash function)
	private int a0;
	private int a1;
	private int a2;
	private int a3;
	//private int a4;
}
