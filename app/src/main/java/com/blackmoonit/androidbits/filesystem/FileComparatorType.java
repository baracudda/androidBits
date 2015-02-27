package com.blackmoonit.androidbits.filesystem;


/**
 * Type, followed by Alphabetical sort.
 * @see FileComparatorAlpha
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
