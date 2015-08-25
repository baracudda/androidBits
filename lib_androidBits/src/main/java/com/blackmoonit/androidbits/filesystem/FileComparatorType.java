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
 * Type, followed by Alphabetical sort.
 * @see com.blackmoonit.androidbits.filesystem.FileComparatorAlpha
 *
 * @author Ryan Fischbach
 */
public class FileComparatorType extends FileComparator {

	public FileComparatorType(boolean bReverseSort, boolean bFoldersFirst) {
		super(bReverseSort,bFoldersFirst);
	}

	@Override
	protected int compareFiles(FileListAdapterElement f1, FileListAdapterElement f2) {
		if (f1.bIsFile && f2.bIsFile) {
			String e1 = f1.mExtPart;
			String e2 = f2.mExtPart;
			int theResult = (e1==e2)?0:2; //aka is both e1 and e2 null?
			if (theResult!=0) {
				if (e1!=null && e2!=null)
					theResult = e1.compareToIgnoreCase(e2);
				else
					theResult = (e1!=null)?-1:1;
			}
			if (theResult!=0)
				return theResult*mSorterReverseFactor;
			else
				return sortFileName(f1, f2);
		} else if (f1.bIsFile != f2.bIsFile) {
			return (f1.bIsFile)?1:-1;
		} else
			return sortFileName(f1, f2);
	}

}
