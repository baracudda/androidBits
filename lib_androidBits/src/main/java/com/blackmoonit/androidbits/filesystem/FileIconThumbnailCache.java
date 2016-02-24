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

import java.io.File;
import java.util.HashMap;

import android.graphics.drawable.Drawable;


/**
 * Thumbnail cache holding image file's thumbnail. Hashmap wrapper to help facilitate using file cache.
 *
 * @author baracudda
 */
public class FileIconThumbnailCache extends HashMap<String, Drawable> {
	static private final long serialVersionUID = 786992526469278470L;

	public String getCacheKey(File aFile, int aScaleFactor) {
		return Integer.toString(aScaleFactor)+aFile.getPath();
	}

	public boolean containsFile(File aFile, int aScaleFactor) {
		if (aFile!=null) {
			if (containsKey(getCacheKey(aFile,aScaleFactor))) {
				return true;
			} else
				return false;
		}
		return false;
	}

	public Drawable getFile(File aFile, int aScaleFactor) {
		Drawable theResult = null;
		if (aFile!=null) {
			theResult = get(getCacheKey(aFile,aScaleFactor));
		}
		return theResult;
	}

	public void putFile(File aFile, int aScaleFactor, Drawable aThumbnail) {
		if (aFile!=null && aThumbnail!=null) {
			put(getCacheKey(aFile,aScaleFactor),aThumbnail);
		}
	}

	public void removeFile(File aFile, int aScaleFactor) {
		if (aFile!=null) {
			remove(getCacheKey(aFile,aScaleFactor));
		}
	}

}
