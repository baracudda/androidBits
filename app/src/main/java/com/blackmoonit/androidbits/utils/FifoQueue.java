package com.blackmoonit.androidbits.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Queue that implements FIFO (first in, first out)
 *
 * @author Ryan Fischbach
 */
public class FifoQueue<E> extends LinkedList<E> implements Queue<E> {
	static private final long serialVersionUID = 1409271926562953991L;

	public FifoQueue() {
		//nothing to do
	}

	public FifoQueue(E[] aArray) {
		addAll(aArray);
	}

	public FifoQueue(List<E> aList) {
		addAll(aList);
	}

	@Override
	public E element() {
		return peek();
	}

	@Override
	public boolean offer(E o) {
		try {
			addLast(o);
			return true;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	@Override
	public E peek() {
		if (size()>0) {
			return get(0);
		} else {
			return null;
		}
	}

	@Override
	public E poll() {
		return remove();
	}

	@Override
	public E remove() {
		if (size()>0) {
			try {
				return removeFirst();
			} catch (NoSuchElementException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public void addAll(E[] aArray) {
		if (aArray!=null) {
			for (E aItem:aArray) {
				offer(aItem);
			}
		}
	}

	public void addAll(List<E> aList) {
		if (aList!=null) {
			for (E aItem:aList) {
				offer(aItem);
			}
		}
	}
}
