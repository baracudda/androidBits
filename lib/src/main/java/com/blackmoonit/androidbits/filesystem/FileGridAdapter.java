package com.blackmoonit.androidbits.filesystem;

import android.content.Context;
import android.widget.ImageView;

/**
 * Adapter for a grid-style view of files.
 *
 * @author Ryan Fischbach
 */
abstract public class FileGridAdapter extends FileListAdapter {

	public FileGridAdapter(Context aContext, FileListDataSource aDataSource, FileIcons aFileIcons,
			int aContentLayoutResourceId) {
		super(aContext, aDataSource, aFileIcons, aContentLayoutResourceId);
	}

	protected void applyFileImg(final FileListAdapterElement anItem, ImageView v) {
		//we do not want to scale the thumbnail any larger than a normal icon in grid view
		mFileIcons.setFileIcon(v, anItem, 1);
	}

}
