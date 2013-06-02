package ch.ethz.glukas.orderedset;

class TreapNode <T> extends TreeNode<T> {
	//Treap node with explicit priority
	
	public TreapNode(T value, int nodePriority)
	{
		super(value);
		priority = nodePriority;
	}
	
	
	public void setPriority(int newPriority)
	{
		priority = newPriority;
	}
	
	public int getPriority()
	{
		return priority;
	}
	
	private int priority;
}
