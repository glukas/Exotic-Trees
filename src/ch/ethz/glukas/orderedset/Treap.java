package ch.ethz.glukas.orderedset;
import java.lang.reflect.Array;
import java.util.*;

public class Treap<T> extends BinarySearchTree<T> implements SortedSet<T>{
	

	
	////
	//CONSTRUCTION
	////
	
	
	public Treap(Comparator<? super T> comparator)
	{
		super(comparator);
		
	}
	
	public Treap()
	{
		super();
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
		super.clear();
		random = new Random(1);
		a0 = random.nextInt();
		a1 = random.nextInt();
		a2 = random.nextInt();
		a3 = random.nextInt();
		//a4 = random.nextInt();
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
	public T first() {
		if (isEmpty()) throw new NoSuchElementException();
		
		TreeNode<T> node = findFirst(getRoot());
		
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
		TreeNode<T> node = findLast(getRoot());
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
	//MORE SORTED SET OPERATIONS
	////
	

	public Treap<T> splitHeadSet(T value, boolean includeValue)
	{
		//TODO: count is not updated
		//it may be necessary to make each node hold the number of descendants it has
		
		Treap<T> treap = new Treap<T>();
		
		Ref<TreeNode<T>> less = new Ref<TreeNode<T>>();
		Ref<TreeNode<T>> greater  = new Ref<TreeNode<T>>();
		
		TreeNode<T> equal = split(getRoot(), metaRoot, value, less, greater);
		
		treap.setRoot(less.get());
		this.setRoot(greater.get());
		if (includeValue) {
			treap.addBottomUp(value);
		} else {
			this.addBottomUp(value);
		}
		
		return treap;
	}
	

	
	/////
	///IMPLEMENTATION
	//////
	
	
	/*
	private boolean addTopDown(T value)
	{
		
		boolean alreadyContained = true;
		
		
		Ref<TreeNode<T>> less = new Ref<TreeNode<T>>();
		Ref<TreeNode<T>> greater = new Ref<TreeNode<T>>();
		
		TreeNode<T> eq = persistentSplit(metaRoot.getLeftChild(), value, less, greater);
		
		if (eq == null) {
			alreadyContained = false;
			
			TreeNode<T> newNode = new TreeNode<T>(value);

			newNode.setLeftChild(less.get());
			newNode.setRightChild(greater.get());
			
			metaRoot.setLeftChild(newNode);
			
			rebalanceDownwards(newNode, metaRoot);
			
			incrementCount();
			
		}
		
		assert isInOrder();
		assert isHeapOrdered();
		
		return !alreadyContained;
	
	}*/
	
	

	
	
	private boolean addTopDown(T value)
	{
		

		boolean alreadyContained = true;
		TreeNode<T> valueNode;

		Ref<TreeNode<T>> tailSet = new Ref<TreeNode<T>>();
		Ref<TreeNode<T>> headSet = new Ref<TreeNode<T>>();
		
		valueNode = split(getRoot(), metaRoot, value, headSet, tailSet);
		if (valueNode == null) {
			alreadyContained = false;
			valueNode = new TreeNode<T>(value);
			incrementCount();
		}
		
		valueNode.setLeftChild(headSet.get());
		valueNode.setRightChild(tailSet.get());
		
		setRoot(valueNode);
		
		rebalanceDownwards(valueNode, metaRoot);

		
		assert isInOrder();
		assert isHeapOrdered();
		
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
	


	////
	//IMPLEMENTATION : HELPERS
	/////
	
	
	
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
		//int cc = c*c;
		//int ccc = cc*c;
		
		return a0+c*a1;
		//return a0+c*a1+cc*a2+ccc*a3;
	}


	//INVARIANTS
	
	
	
	private boolean isHeapOrdered()
	{
		return isHeapOrdered(getRoot());
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
	
	private Random random;
	
	//random polynomial coefficients (for priority hash function)
	private int a0;
	private int a1;
	private int a2;
	private int a3;
	//private int a4;
}
