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
import android.widget.ImageView;

/**
 * Adapter for a grid-style view of files.
 *
 * @author baracudda
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
