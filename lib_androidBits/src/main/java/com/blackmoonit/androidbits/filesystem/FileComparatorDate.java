package com.blackmoonit.androidbits.filesystem;
/*
 * Copyright (C) 2015 Blackmoon Info Tech Services
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

/**
 * Date (oldest first), followed by Alphabetical sort.
 * @see com.blackmoonit.androidbits.filesystem.FileComparatorAlpha
 *
 * @author Ryan Fischbach
 */
public class FileComparatorDate extends FileComparator {

	public FileComparatorDate(boolean bReverseSort, boolean bFoldersFirst) {
		super(bReverseSort,bFoldersFirst);
	}

	@Override
	protected int compareFiles(FileListAdapterElement f1, FileListAdapterElement f2) {
		long lm1 = f1.mLastModified;
		long lm2 = f2.mLastModified;
		if (lm1<lm2) {
			return mSorterReverseFactor;
		} else if (lm1>lm2) {
			return -mSorterReverseFactor;
		} else {
			return sortFileName(f1, f2);
		}
	}

}
