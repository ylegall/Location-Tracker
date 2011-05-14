package edu.pitt.cs.exposure;

import java.util.Iterator;

/**
 * 
 * @author ylegall
 *
 */
public class RingBuffer<T> implements Iterable<T> {

	private T[] data;
	private int size;
	private int head;
	
	/**
	 * Constructs a new RingBuffer with
	 * the given fixed size.
	 * @param size
	 */
	@SuppressWarnings("unchecked")
	public RingBuffer(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("RingBuffer capacity must be positive.");
		}
		this.size = 0;
		this.head = 0;
		data = (T[])(new Object[size]);
	}
	
	/**
	 * gets the number of items in this RingBuffer
	 * @return
	 */
	public int size() {
		return this.size;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return size == 0;
	}
	
	/**
	 * gets the capacity of this RingBuffer
	 * @return
	 */
	public int capacity() {
		return data.length;
	}
	
	/**
	 * 
	 */
	public void clear() {
		size = head = 0;
	}
	
	/**
	 * Adds a the item at the head of this buffer.
	 * If this buffer was full, then the oldest item
	 * is overwritten.
	 * @param item
	 */
	public void add(T item) {
		if (size < data.length) {
			head = size;
			size++;
		} else {
			head = (head + 1) % data.length;
		}
		data[head] = item;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder('[');
		
		for (int i=0; i < size; i++) {
			int index = head - i;
			index = (index < 0)? size + index : index;
			sb.append(data[index]);
			sb.append(",");
		}
		sb.replace(sb.length()-1, sb.length(),"]");
		return sb.toString();
	}
	
	/**
	 * Gets the item stored at the specified
	 * index in the buffer.
	 * @param i the 
	 * @return
	 */
	public T get(int i) {
		if (i >= size || i < 0) {
			throw new IndexOutOfBoundsException(i + "");
//			return null;
		}
		
		int index = head - i;
		if (index < 0) {
			index = size + index;
		}
		return data[index];
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < size;
			}

			@Override
			public T next() {
				return get(index++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
