package com.blackmoonit.androidbits.filesystem;


/**
 * Alphabetical sort that first removes the extension before comparing.
 *
 * @author Ryan Fischbach
 */
public class FileComparatorAlpha extends FileComparator {

	public FileComparatorAlpha(boolean bReverseSort, boolean bFoldersFirst) {
		super(bReverseSort,bFoldersFirst);
	}

	@Override
	protected int compareFiles(FileListAdapterElement f1, FileListAdapterElement f2) {
		return sortFileName(f1, f2)*mSorterReverseFactor;
	}

}
