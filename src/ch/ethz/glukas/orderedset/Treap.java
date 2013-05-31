package ch.ethz.glukas.orderedset;
import java.lang.reflect.Array;
import java.util.*;

public class Treap<T> extends AbstractCollection<T> implements SortedSet<T>{
	

	
	////
	//CONSTRUCTION
	////
	
	
	public Treap(Comparator<? super T> comparator)
	{
		clear();
		internalComparator = comparator;
	}
	
	public Treap()
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
	///SORTED SET INTERFACE
	/////
	
	@Override
	public boolean add(T arg0) {
		if (arg0.equals(null)) throw new NullPointerException();
		
		//return addTopDown(arg0);
		return addBottomUp(arg0);
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
	public Iterator<T> iterator() {
		return new BinarySearchTreeIterator<T>(metaRoot.getLeftChild());
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
			
			rebalanceDownwards(toRemove, parent);
			
			decrementCount();
			
			assert isInOrder();
		}
		
		assert !contains(value);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return count;
	}
	
	@Override
	public Comparator<? super T> comparator() {
		return internalComparator;
	}

	@Override
	public T first() {
		if (isEmpty()) throw new NoSuchElementException();
		
		TreeNode<T> node = findFirst(metaRoot.getLeftChild());
		
		return node.getValue();
	}
	


	@Override
	public SortedSet<T> headSet(T arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T last() {
		if (isEmpty()) throw new NoSuchElementException();
		TreeNode<T> node = metaRoot.getLeftChild();
		while (node.getRightChild() != null) {
			node = node.getRightChild();
		}
		return node.getValue();
	}

	@Override
	public SortedSet<T> subSet(T arg0, T arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<T> tailSet(T arg0) {
		//TODO
		return null;
	}
	
	
	////
	//NAVIGABLE SET
	////
	
	
	public T upper(T value)
	{
		//TODO: test / only works if value present
		TreeNode<T> node = findNodeWithValueStartingFrom(metaRoot.getLeftChild(), value);
		TreeNode<T> successor = findNodeWithValueStartingFrom(node.getRightChild(), node.getValue());
		return successor.getValue();
	}
	
	
	/////
	///IMPLEMENTATION
	//////
	
	
	private boolean addTopDown(T value)
	{
		
		boolean alreadyContained = internalContains(value);
		
		if (!alreadyContained) {
			
			TreeNode<T> newNode = new TreeNode<T>(value);
			TreeNode<T> tailSet = split(metaRoot.getLeftChild(), metaRoot, value);
			
			newNode.setLeftChild(metaRoot.getLeftChild());
			newNode.setRightChild(tailSet);
			
			metaRoot.setLeftChild(newNode);
			
			rebalanceDownwards(newNode, metaRoot);
			
			incrementCount();
			assert isInOrder();
			assert isHeapOrdered();
		}
		
		return !alreadyContained;
	
	}

	//analogous to insertion into an unbalanced BST, but restore heap property afterwards
	private boolean addBottomUp(T arg0)
	{
		ArrayList<TreeNode<T>> trace = find(arg0);
		
		boolean modified = false;
		
		if (trace.size() == 1 || compareValues( trace.get(trace.size()-1).getValue(), arg0) != 0) {
			modified = true;
			
			TreeNode<T> newNode = makeChildWithValue(trace.get(trace.size()-1), arg0);
			trace.add(newNode);
			rebalanceUpwards(trace);
			
			incrementCount();
			assert isInOrder();
		}
		
		return modified;
	}
	
	private TreeNode<T> makeChildWithValue(TreeNode<T> parent, T newValue) {
		
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
	
	
	////
	//IMPLEMENTATION :: SPLIT
	////
	
	
	/**
	 * Splits of the right subtree and returns it
	 * @param parent
	 * @return the root of the subtree, parent.getRightChild()
	 */
	/*private TreeNode<T> splitRight(TreeNode<T> parent)
	{
		assert parent.getRightChild() != null;
		TreeNode<T> rightChild = parent.getRightChild();
		parent.setRightChild(null);
		return rightChild;
	}*/
	
	
	/**
	 * preconditions:	 the search tree invariant holds
	 *   			     the heap invariant holds 
	 * 					 all values in node are larger than metaRoot.getLeftChild(), or metaRoot.getLeftChild() == null
	 * 					 the tree referred to by node is a valid treap
	 * postcondition:	 node (and its children) are part of the treap.
	 * 					 the search tree invariant holds
	 *   			     the heap invariant holds
	 *
	 * @param node
	 */
	/*
	private void joinInRightwards(TreeNode<T> node)
	{
		node.setLeftChild(metaRoot.getLeftChild());
		metaRoot.setLeftChild(node);
		if (node.getLeftChild() != null) {
			rebalanceDownwards(node.getLeftChild(), node);
		}
		assert isInOrder();
		assert isHeapOrdered();
	}*/

	
	private TreeNode<T> split(TreeNode<T> root, TreeNode<T> parent, T value)
	{
		//splitMetaRoot.getLeftChild()  will contain the subtree with values greater than or equal to the value
		//if the value is contained in the subtree rooted at root, it will be the smallest/leftmost node of the returned tree
		TreeNode<T> splitMetaRoot = new TreeNode<T>(null);
		TreeNode<T> currentSplitNode = splitMetaRoot;
		
		int comparison = -1;
		while(root != null && comparison != 0) {
			
			comparison = compareValues(value, root.getValue());
			
			if (comparison > 0) {//value is greater than current node: don't include the current root, continue looking for larger values
				parent = root;
				root = root.getRightChild();
			} else if (comparison <= 0) {//value is smaller than current node: include current node into the large value subtree, continue looking for smaller values
				
				//append to large value tree
				currentSplitNode.setLeftChild(root);
				currentSplitNode = root;
				
				//remove from small value tree
				parent.replaceChild(root, root.getLeftChild());
				//advance search
				root = root.getLeftChild();
				
			}
			
		}
		
		currentSplitNode.setLeftChild(null);
		
		assert isInOrder();
		assert isHeapOrdered();
		assert isHeapOrdered(splitMetaRoot.getLeftChild());
		
		return splitMetaRoot.getLeftChild();
	}
	
	
	
	//////
	///IMPLEMENTATION :: FIND
	//////
	
	
	/**
	 * Finds the parent of the first value greater than or equal to the value provided
	 * @param value
	 * @return
	 */
	/*
	private TreeNode<T> findCeil(T value)
	{
		return findNodeWithValueStartingFrom(metaRoot.getLeftChild(), value);
	}*/
	
	
	/**
	 * Returns the node with the smallest value (the leftmost node in the subtree rooted at 'node')
	 */
	private TreeNode<T> findFirst(TreeNode<T> node)
	{
		while (node.getLeftChild() != null) {
			node = node.getLeftChild();
		}
		return node;
	}
	
	
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
	
	
	
	private TreeNode<T> findNodeWithValueStartingFrom(TreeNode<T> currentNode, T valueToFind)
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
	private Buffer<TreeNode<T>> findSuccessor(TreeNode<T> node)
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
	
	
	private ArrayList<TreeNode<T>> traceNodeWithValueStartingFrom(TreeNode<T> startingNode, T valueToFind)
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
	
	private Buffer<TreeNode<T>> traceNodeWithValueStartingFrom(TreeNode<T> startingNode, T valueToFind, int maximumParentBufferSize)
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
	
	//////
	////IMPLEMENTATION :: BALANCE
	//////

	
	private void rebalanceUpwards(ArrayList<TreeNode<T>> trace)
	{
		assert trace.size() > 0;
		
		int currentIndex = trace.size()-1;
		
		while (currentIndex > 1 && getPriorityForNode(trace.get(currentIndex)) < getPriorityForNode(trace.get(currentIndex-1))) {
			
			treeRotateUp(trace.get(currentIndex), trace.get(currentIndex-1), trace.get(currentIndex-2));
			swap(trace, currentIndex, currentIndex-1);//the tree rotation needs to be reflected in the trace
			currentIndex = currentIndex - 1;
		}
		
		assert isHeapOrdered(trace.get(0));
	}
	
	
	private void rebalanceDownwards(TreeNode<T> currentNode, TreeNode<T> parent)
	{
		assert currentNode != null;
		assert parent != null;
		
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
		return (getPriorityForNode(node.getLeftChild())) - (getPriorityForNode(node.getRightChild()));
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
		int currentMinPriority = getPriorityForNode(min);
		if (currentNode.getLeftChild() != null && getPriorityForNode((currentNode.getLeftChild())) < currentMinPriority) {
			min = currentNode.getLeftChild();
			currentMinPriority = getPriorityForNode(currentNode.getLeftChild());
		}
		if (currentNode.getRightChild() != null && getPriorityForNode(currentNode.getRightChild()) < currentMinPriority) {
			min = currentNode.getRightChild();
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

	//INVARIANTS
	
	
	@SuppressWarnings("unchecked")
	private boolean isInOrder()
	{
		T[] sorted = (T[]) toArray();
		for (int i=1; i<sorted.length; i++) {
			if (compareValues(sorted[i], sorted[i-1]) < 0) return false;
		}
		return true;
	}
	
	private boolean isHeapOrdered()
	{
		return isHeapOrdered(metaRoot.getLeftChild());
	}
	
	private boolean isHeapOrdered(TreeNode<T> node)
	{
		//base case
		if (node == null) return true;
		
		//recursive step
		boolean result = true;
		if (node != metaRoot && nodeWithLocallyMinimalPriority(node) != node) {
			result = false;
		} else {
			result = result && isHeapOrdered(node.getLeftChild());
			result = result && isHeapOrdered(node.getRightChild());
		}
		return result;
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
