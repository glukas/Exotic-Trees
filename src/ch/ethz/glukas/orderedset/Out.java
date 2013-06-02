package ch.ethz.glukas.orderedset;

public class Out<T> {
	//Can be used to return multiple results from a method
	
	
	public Out()
	{
	}
	
	public Out(T value)
	{
		set(value);
	}
	
	public void set(T value)
	{
		internalReference = value;
	}
	
	public T get()
	{
		return internalReference;
	}
	
	
	private T internalReference;
	
}
