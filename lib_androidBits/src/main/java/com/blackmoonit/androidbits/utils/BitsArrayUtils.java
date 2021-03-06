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

import java.util.Arrays;

/**
 * Extending the static array functions.
 *
 * @author baracudda
 */
public final class BitsArrayUtils {

	private BitsArrayUtils() {} //do not instanciate this class

	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching
	 *
	 * @param aHaystack - the array to search through
	 * @param aNeedle - the collection of bytes to find
	 * @return Returns the index into aHaystack where aNeedle is found, else -1.
	 */
	public static int indexOf(byte[] aHaystack, byte[] aNeedle) {
		return indexOf(aHaystack,aNeedle,0,aHaystack.length);
	}

	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching
	 *
	 * @param aHaystack - the array to search through
	 * @param aNeedle - the collection of bytes to find
	 * @param idxStart - start searching at this index
	 * @param idxEnd - stop searching once this index is reached
	 * @return Returns the index into aHaystack where aNeedle is found, else -1.
	 */
	public static int indexOf(byte[] aHaystack, byte[] aNeedle, int idxStart, int idxEnd) {
		if (aNeedle!=null && aNeedle.length>0 && idxStart>=0 && idxEnd>=0 && idxEnd<=aHaystack.length &&
				idxEnd-idxStart>=aNeedle.length) {
			idxEnd = Math.min(aHaystack.length,idxEnd);
			int[] failure = KMP_computeFailure(aNeedle);
			int j = 0;
			for (int i=idxStart; i<idxEnd; i++) {
				while (j>0 && aNeedle[j]!=aHaystack[i]) {
					j = failure[j-1];
				}
				if (aNeedle[j] == aHaystack[i]) {
					j++;
				}
				if (j==aNeedle.length) {
					return i-aNeedle.length+1;
				}
			}
		}
		return -1;
	}

	/**
	 * Computes the failure function using a boot-strapping process,
	 * where the pattern is matched against itself.
	 * FOR USE WITH {@link #indexOf}
	 *
	 * @param - the pattern to match, usually aNeedle
	 * @return - returns the anti-pattern needed for the Knuth-Morris-Pratt algorithm.
	 */
	private static int[] KMP_computeFailure(byte[] aPattern) {
		int[] failure = new int[aPattern.length];
		int j = 0;
		for (int i=1; i<aPattern.length; i++) {
			while (j>0 && aPattern[j]!=aPattern[i]) {
				j = failure[j-1];
			}
			if (aPattern[j]==aPattern[i]) {
				j++;
			}
			failure[i] = j;
		}
		return failure;
	}

	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching, modified to search backwards from end.
	 *
	 * @param aHaystack - the array to search through
	 * @param aNeedle - the collection of bytes to find
	 * @return Returns the index into aHaystack where aNeedle is found, else -1.
	 */
	public static int lastIndexOf(byte[] aHaystack, byte[] aNeedle) {
		return lastIndexOf(aHaystack,aNeedle,0,aHaystack.length);
	}

	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching, modified to search backwards from end.
	 * Note that despite the param names, this will search starting from end and ending at start.
	 *
	 * @param aHaystack - the array to search through
	 * @param aNeedle - the collection of bytes to find
	 * @param idxStart - start searching at this index
	 * @param idxEnd - stop searching once this index is reached
	 * @return Returns the index into aHaystack where aNeedle is found, else -1.
	 */
	public static int lastIndexOf(byte[] aHaystack, byte[] aNeedle, int idxStart, int idxEnd) {
		if (aNeedle!=null && aNeedle.length>0 && idxStart>=0 && idxEnd>=0 && idxEnd<=aHaystack.length &&
				idxEnd-idxStart>=aNeedle.length) {
			idxEnd = Math.min(aHaystack.length,idxEnd);
			int[] failure = KMP_lastComputeFailure(aNeedle);
			int j = aNeedle.length-1;
			for (int i=idxEnd-1; i>=idxStart; i--) {
				while (j<aNeedle.length-1 && aNeedle[j]!=aHaystack[i]) {
					j = failure[j+1];
				}
				if (aNeedle[j] == aHaystack[i]) {
					j--;
				}
				if (j==-1) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Computes the failure function using a boot-strapping process,
	 * where the pattern is matched against itself.
	 * FOR USE WITH {@link #lastIndexOf}
	 *
	 * @param - the pattern to match, usually aNeedle
	 * @return - returns the anti-pattern needed for the Knuth-Morris-Pratt algorithm.
	 */
	private static int[] KMP_lastComputeFailure(byte[] aPattern) {
		int[] failure = new int[aPattern.length];
		Arrays.fill(failure,aPattern.length-1);
		int j = aPattern.length-1;
		for (int i=aPattern.length-2; i>=0; i--) {
			while (j<aPattern.length-1 && aPattern[j]!=aPattern[i]) {
				j = failure[j+1];
			}
			if (aPattern[j]==aPattern[i]) {
				j--;
			}
			failure[i] = j;
		}
		return failure;
	}

}
