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
 * Size, followed by Alphabetical sort.
 * @see com.blackmoonit.androidbits.filesystem.FileComparatorAlpha
 *
 * @author baracudda
 */
public class FileComparatorSize extends FileComparator {

	public FileComparatorSize(boolean bReverseSort, boolean bFoldersFirst) {
		super(bReverseSort,bFoldersFirst);
	}

	@Override
	protected int compareFiles(FileListAdapterElement f1, FileListAdapterElement f2) {
		long l1 = f1.mSize;
		long l2 = f2.mSize;
		boolean bIsFile = f1.isFile();
		if (bIsFile && l1<l2) {
			return -mSorterReverseFactor;
		} else if (bIsFile && l1>l2) {
			return mSorterReverseFactor;
		} else {
			return sortFileName(f1, f2);
		}
	}


}
