package ch.ethz.glukas.orderedset;

public class Ref<T> {
	//Can be used to return multiple results from a method
	
	
	public Ref()
	{
	}
	
	public Ref(T value)
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
