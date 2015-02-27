package com.blackmoonit.androidbits.filesystem;


/**
 * Date (oldest first), followed by Alphabetical sort.
 * @see FileComparatorAlpha
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
