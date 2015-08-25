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
import java.net.URI;

/**
 * File object that will always only be a folder. If you try to assign a File object to it that is an
 * an actual file, it will instead assign itself to its parent folder containing said file.
 *
 * @author Ryan Fischbach
 */
public class Folder extends File {
	static private final long serialVersionUID = -7182124359991533310L;

	public Folder(String aPath) {
		super(ensureFolder(new File(aPath)).getPath());
	}

	public Folder(URI aURI) {
		super(ensureFolder(new File(aURI)).getPath());
	}

	public Folder(File aParentFile, String aFileName) {
		super(ensureFolder(new File(aParentFile,aFileName)).getPath());
	}

	public Folder(String aParentPath, String aFileName) {
		super(ensureFolder(new File(aParentPath,aFileName)).getPath());
	}

	public Folder(File aFile) {
		super(ensureFolder(aFile).getPath());
	}

	static public File ensureFolder(File aFile) {
		if (aFile!=null && aFile.exists()) {
			if (!aFile.isDirectory() && aFile.isFile()) {
				return ensureFolder(aFile.getParentFile());
			}
		}
		return aFile;
	}

}
