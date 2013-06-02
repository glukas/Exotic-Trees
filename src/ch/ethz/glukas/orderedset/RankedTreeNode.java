package ch.ethz.glukas.orderedset;

class RankedTreeNode<T> extends TreeNode<T> {

	
	public RankedTreeNode(T value)
	{
		super(value);
		size = 1;
	}
	
	public int size()
	{
		return size;
	}
	
	public void setSize(int newSize)
	{
		size = newSize;
	}
	
	public void adjustSize()
	{
		size = 1;
		if (getLeftChild() != null) {
			size += ((RankedTreeNode<T>) getLeftChild()).size();
		}
		if (getRightChild() != null) {
			size += ((RankedTreeNode<T>)getRightChild()).size();
		}	
	}
	
	public void setLeftChild(TreeNode<T> node)
	{
		super.setLeftChild(node);
		adjustSize();
	}
	
	public void setRightChild(TreeNode<T> node)
	{
		super.setRightChild(node);
		adjustSize();
	}
	
	
	private int size;
	
	
}
