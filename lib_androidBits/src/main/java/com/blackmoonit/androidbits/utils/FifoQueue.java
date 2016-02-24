package com.blackmoonit.androidbits.utils;
/*
 * Copyright (C) 2014 Blackmoon Info Tech Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Queue that implements FIFO (first in, first out)
 *
 * @author baracudda
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
