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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackmoonit.androidbits.R;
import com.blackmoonit.androidbits.widget.BitsArrayAdapter;

/**
 * Adapter for a list of files.
 *
 * @author Ryan Fischbach
 */
abstract public class FileListAdapter extends BitsArrayAdapter<FileListAdapterElement> {
	protected final FileIcons mFileIcons;
	protected final Drawable mFolderIcon;
	public boolean bShowFolderInfo = false;

	public FileListAdapter(Context aContext, FileListDataSource aDataSource, FileIcons aFileIcons,
			int aContentLayoutResourceId) {
		this(aContext, aContentLayoutResourceId, aDataSource, aFileIcons);
	}

	protected FileListAdapter(Context aContext, int aLayoutResId,
			FileListDataSource aDataSource, FileIcons aFileIcons) {
		super(aContext, aLayoutResId, aDataSource);
		mFileIcons = aFileIcons;
		mFolderIcon = aContext.getResources().getDrawable(R.drawable.item_folder);
		mResizableTextViews = getResizeableTextViews();
	}

	/**
	 * Sample method code: <pre>
	   return new int[] {
	       R.id.tv_filename
	   };
	 * </pre>
	 * @return Returns the ResourceIDs of the resizeable text views.
	 */
	abstract protected int[] getResizeableTextViews();

	public void applyFileName(final FileListAdapterElement anItem, TextView v) {
		v.setText(anItem.getDisplayName());
		//since Accessibility reads the text and ignores the content desc, this is useless
		//setViewContentDesc(v,anItem.mViewDesc);
	}

	public void applyFileFolder(final FileListAdapterElement anItem, TextView v) {
		if (bShowFolderInfo) {
			v.setVisibility(View.VISIBLE);
			v.setText(anItem.mExternalStorageRelativePath);
		}
	}

	public void applyFileInfo(final FileListAdapterElement anItem, TextView v) {
		Long displayDate = anItem.mLastModified;
		String theFileInfoText = "";
		if (anItem.isDirectory()) {
			if (anItem.isFileJumpPoint())
				theFileInfoText += "§";
			if (displayDate!=0L) {
				if (theFileInfoText.length()>0)
					theFileInfoText += ", ";
				theFileInfoText += v.getContext().getString(R.string.folder_info_fmtstr,displayDate,displayDate);
			}
		} else {
			String displaySize = android.text.format.Formatter.formatFileSize(v.getContext(),anItem.mSize);
			theFileInfoText = v.getContext().getString(R.string.file_info_fmtstr,displaySize,displayDate,displayDate);
		}
		v.setText(theFileInfoText);
	}

	protected void applyFolderImg(final FileListAdapterElement anItem, ImageView v) {
		if (mFileIcons.mOnSetThumbnail!=null)
			mFileIcons.mOnSetThumbnail.onSetThumbnail(v,mFolderIcon);
		else
			v.setImageResource(R.drawable.item_folder);
	}

	protected void applyFileImg(final FileListAdapterElement anItem, ImageView v) {
		mFileIcons.setFileIcon(v,anItem,mFileIcons.scaleFactor);
	}

	public void applyFileIcon(final FileListAdapterElement anItem, ImageView v) {
		mFileIcons.checkRecycleView(v,anItem);
		//v.setVisibility(View.VISIBLE);
		if (anItem.isDirectory()) {
			applyFolderImg(anItem,v);
		} else {
			applyFileImg(anItem,v);
		}
	}

	public void applyFileEmblem(final FileListAdapterElement anItem, ImageView v) {
		if (!anItem.canRead()) {
			v.setImageResource(R.drawable.emblem_unreadable);
			v.setVisibility(View.VISIBLE);
		} else if (!anItem.canWrite()) {
			v.setImageResource(R.drawable.emblem_readonly);
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.INVISIBLE);
		}
	}

	public void applyFileMark(final FileListAdapterElement anItem, ImageView v) {
		v.setVisibility(anItem.isMarked()||anItem.containsMarked()?View.VISIBLE:View.INVISIBLE);
		if (!anItem.containsMarked())
			v.setImageResource(R.drawable.emblem_check);
		else
			v.setImageResource(R.drawable.emblem_check_partial);
		//v.setAlpha(anItem.containsMarked()?0x90:0xFF); //affects all checks if done this way
	}

	/**
	 * Set all the various pieces of the item view.
	 * Sample method contents: <pre>
		applyFileName(anItem,(TextView)getViewHandle(anItemView,R.id.list_item_filename));
		applyFileFolder(anItem,(TextView)getViewHandle(anItemView,R.id.list_item_filefolder));
		applyFileInfo(anItem,(TextView)getViewHandle(anItemView,R.id.list_item_fileinfo));
		applyFileIcon(anItem,(ImageView)getViewHandle(anItemView,R.id.ImageFileThumb));
		applyFileEmblem(anItem,(ImageView)getViewHandle(anItemView,R.id.ImageReadOnlyEmblem));
		applyFileMark(anItem,(ImageView)getViewHandle(anItemView,R.id.ImageSelectedEmblem));
		return anItemView;
	 * </pre>
	 * @param anItemView - View containing all views needed to display an item
	 * @return Returns the View passed in.
	 */
	abstract public View applyItemView(FileListAdapterElement anItem, View anItemView);

	/**
	 * During the list's onRecycleView callback, recycle our icon/thumbnail.
	 * Sample method contents: <pre>
		ImageView theImageView = (ImageView)getViewHandle(anItemView,R.id.ImageFileIcon);
		mFileIcons.recycleView(theImageView);
	 * </pre>
	 * @param anItemView - View containing all views needed to display an item.
	 */
	abstract public void onRecycleView(View anItemView);

}
