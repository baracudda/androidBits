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

import android.view.View;
import android.widget.ImageView;

import com.blackmoonit.androidbits.utils.BitsStringUtils;

import java.io.File;

/**
 * Thumbnail queue item holding all data needed to retrieve and then set an image file's thumbnail.
 *
 * @author Ryan Fischbach
 */
public class FileIconThumbnailQueueItem implements Comparable<FileIconThumbnailQueueItem> {
	//private static final AtomicLong seq = new AtomicLong();
	//private final long seqNum;  //used to determine FIFO/FILO, but calc screen coordinates instead now
	public final ImageView iconView;
	public final File imageFile;
	public final String imageTag;
	public final int imageSize;
	public final String mimeType;
	public final String mimeCategory;
	private final int mInitialViewRank;
	public int mThumbnailScaleFactor; //cannot be made "final" since we decrease this in some OOM cases

	public FileIconThumbnailQueueItem(ImageView aIconView, File aImageFile, String aMimeType, int aImageSize,
			int aScaleFactor) {
		//seqNum = seq.getAndIncrement();
		iconView = aIconView;
		imageFile = aImageFile;
		imageTag = imageFile.getPath();
		iconView.setTag(imageTag);
		imageSize = aImageSize;
		mimeType = aMimeType;
		if (mimeType!=null)
			mimeCategory = mimeType.substring(0,mimeType.lastIndexOf("/"))+"/*";
		else
			mimeCategory = "*/*";
		String theParentTag = (String)((View)iconView.getParent()).getTag();
		if (BitsStringUtils.isEqual(theParentTag, "GridDetail") ||
				(BitsStringUtils.isEqual(theParentTag,"JumpPoint")) ) {
			mInitialViewRank = 0;
		} else {
			mInitialViewRank = 1;
		}
		mThumbnailScaleFactor = aScaleFactor;

	}

	public Boolean viewHasNotBeenRecycled() {
		String theTag = (String)(iconView.getTag());
		if (theTag!=null) {
			return (theTag.equals(imageTag));
		} else {
			return false;
		}
	}

	public int calcViewRank() {
			int[] q = new int[2];
			iconView.getLocationOnScreen(q);
			int theResult = q[0]+q[1];
			return (theResult>0) ? theResult : Integer.MAX_VALUE;
	}

	@Override
	public int compareTo(FileIconThumbnailQueueItem another) {
		if (mInitialViewRank==another.mInitialViewRank) {
			int theViewRank = calcViewRank();
			return theViewRank-another.calcViewRank();
		} else {
			return mInitialViewRank-another.mInitialViewRank;
		}
	}

}
