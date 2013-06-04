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
	
	//precondition: node.size is consistent, getRightChild.size is consistent
	//postcondition: this.size is consistent
	public void setLeftChild(TreeNode<T> node)
	{
		super.setLeftChild(node);
		adjustSize();
	}
	
	//precondition: node.size is consistent, getLeftChild.size is consistent
	//postcondition: this.size is consistent
	public void setRightChild(TreeNode<T> node)
	{
		super.setRightChild(node);
		adjustSize();
	}
	
	
	private int size;
	
	
}
