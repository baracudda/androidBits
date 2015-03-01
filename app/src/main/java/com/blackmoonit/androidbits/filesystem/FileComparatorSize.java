package com.blackmoonit.androidbits.filesystem;


/**
 * Size, followed by Alphabetical sort.
 * @see com.blackmoonit.androidbits.filesystem.FileComparatorAlpha
 *
 * @author Ryan Fischbach
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
