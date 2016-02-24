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
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

import android.net.Uri;

/**
 * MIME type map class returning MIME types given filenames based mostly on the extension.<br>
 *
 * @author baracudda
 */
public final class MIMEtypeMap extends TreeMap<String, Integer> {
	static private final long serialVersionUID = 6907911910402699271L;

    /**
	 * >>>>> DO NOT FORGET TO UPDATE THIS NUMBER WHEN ADDING NEW TYPES <<<<<<<<br>
	 */
	private static final int NUM_MIMETYPES = 124;
	private String[] mMIMEtypes = new String[NUM_MIMETYPES];
    private String[] mMIMEtypeDefExt = new String[NUM_MIMETYPES];

    /**
     * Given a file object, determine the MIME type.
     *
     * @param aFile - may be either a Directory or a File.
     * @return Returns "container/directory" if aFile exists and is a Directory,
     *     otherwise returns the MIME type based on the filename.
     *     May return null if no MIME type was found.
     *     ".bak" extensions are ignored and the next viable extension used instead.<br>
     *     e.g. "somefile.mp3.bak" will return "audio/mpeg"
     */
    public String guessMIMEtype(File aFile) {
    	if (aFile!=null) {
	    	if (aFile.isDirectory())
	    		return BitsFileUtils.MIMETYPE_FOLDER;
	    	else
	    		return guessMIMEtype(aFile.getName());
    	} else
    		return null;
    }

    /**
     * Same as {@link #guessMIMEtype(java.io.File)} except that it won't return null.
     * @param aFile - may be either a directory or a file.
     * @return Returns empty string ("&#42/&#42") instead of null.
     * @see #guessMIMEtype(java.io.File)
     */
    public String getMIMEtype(File aFile) {
    	String theResult = guessMIMEtype(aFile);
    	if (theResult!=null)
    		return theResult;
   		else
   			return "*/*";
    }

    private String getExtension(String aFilename) {
    	if (aFilename!=null) {
        	int i = aFilename.lastIndexOf(".");
    		if (i>0) {
    			String theExtension = aFilename.substring(i).toLowerCase(Locale.getDefault());
    			if (theExtension!=null && theExtension.equals(".bak")) {
    				theExtension = getExtension(aFilename.substring(0,i));
    				if (theExtension==null) {
    					theExtension = ".bak";
    				}
    			}
    			return theExtension;
    		}
    	}
    	return null;
    }

    /**
     * Given a filename, determine the MIME type.  Cannot return "container/directory" as a
     * MIME type as this funtion does not query the file system.  Use {@link guessMIMEtype( java.io.File aFile)}
     * instead if you need to determine directory status. <br>
     * <b>NOTE:</b> ".bak" extensions are ignored and the next viable extension used instead.
     * e.g. "somefile.mp3.bak" will return "audio/mpeg" <br>
     * <b>NOTE:</b>"._somefile.jpg" will return "application/applefile" as the "._" prefix is how Apple
     * handles the AppleDouble file resource storage on non-native file systems.
     *
     * @param aFilename - a string representing a file. The string may be a fully qualified path
     *     and may not exist.
     * @return Returns the found MIME type.
     *     May return null if no MIME type was found.
     */
	public String guessMIMEtype(String aFilename) {
		MIMEtypeMap mExtensions = this;
		String theGuess = null;
		if (aFilename!=null) {
			if (aFilename.startsWith("._")) {
				return BitsFileUtils.MIMETYPE_APPLEDOUBLE_RES;
			}
			try {
				theGuess = URLConnection.guessContentTypeFromName(aFilename);
			} catch (StringIndexOutOfBoundsException e) {
				theGuess = null;
			}
			if (theGuess==null || theGuess.equals("")) {
				theGuess = null;
				String theExtension = getExtension(aFilename);
				if (theExtension!=null) {
					if (mExtensions.containsKey(theExtension)) {
						theGuess = mMIMEtypes[mExtensions.get(theExtension)];
					}
				}
			}
		}
		return theGuess;
	}

	/**
	 * Given the MIME type, return only the category prefix with "/*" appended to it.
	 *
	 * @param aMIMEtype - is a MIME type.  All MIME types are of the form "category/type"
	 * @return Returns "category/*".  Returns null if aMIMEtype is null or equal to "&#42/&#42".
	 */
	public String getMIMEcategory(String aMIMEtype) {
		if (aMIMEtype!=null && !aMIMEtype.equals("*/*")) {
			return aMIMEtype.substring(0,aMIMEtype.lastIndexOf("/"))+"/*";
		}
		return null;
	}

	/**
	 * Guesses the MIME type of aFile and then returns only the category
	 * prefix with "/*" appended to it.
	 *
	 * @param aFile - is a File that may not exist.
	 * @return Returns "category/*".  Returns null if aFile is null or it's MIME type returned null.
	 */
	public String getMIMEcategory(File aFile) {
		return getMIMEcategory(guessMIMEtype(aFile));
	}

	/**
	 * Compares aMIMEcategory with the category returned for guessMIMEtype(aFile).
	 *
	 * @param aMIMEcategory - a MIME category in the form "category"
	 * @param aFile - a file in which to determine the MIME category to compare against aMIMEcategory
	 * @return Returns true if the categories are equal.
	 *
	 * @see com.blackmoonit.androidbits.filesystem.MIMEtypeMap#guessMIMEtype(java.io.File) guessMIMEtype(File)
	 */
	public boolean isCategory(String aMIMEcategory, File aFile) {
		String theMIMEcat = getMIMEcategory(aFile);
		return (theMIMEcat!=null && theMIMEcat.equalsIgnoreCase(aMIMEcategory+"/*"));
	}

	/**
	 * Compares aMIMEtype with the MIME type returned for guessMIMEtype(aFile).
	 *
	 * @param aMIMEtype - a MIME type in the form "category/type"
	 * @param aFile - a file in which to determine the MIME type to compare against aMIMEtype
	 * @return Returns true if the types are equal.
	 *
	 * @see com.blackmoonit.androidbits.filesystem.MIMEtypeMap#guessMIMEtype(java.io.File) guessMIMEtype(File)
	 */
	public boolean isType(String aMIMEtype, File aFile) {
		String theMIMEtype = guessMIMEtype(aFile);
		return (aMIMEtype==theMIMEtype || (theMIMEtype!=null && theMIMEtype.equalsIgnoreCase(aMIMEtype)));
	}

	/**
	 * Compares aMIMEmatch with the MIME type returned for guessMIMEtype(aFile).
	 *
	 * @param aMIMEmatch - a MIME type in the form "category/type", "category/*", "&#42/&#42", or NULL
	 * @param aFile - a file in which to determine the MIME type to compare against aMIMEmatch
	 * @return Returns TRUE if aMIMEmatch is NULL, "&#42/&#42", or aFile MIME type matches "category/type" or "category/*".
	 *
	 * @see com.blackmoonit.androidbits.filesystem.MIMEtypeMap#guessMIMEtype(java.io.File) guessMIMEtype(File)
	 * @see com.blackmoonit.androidbits.filesystem.MIMEtypeMap#isType(String, java.io.File) isType(String,File)
	 */
	public boolean matchType(String aMIMEmatch, File aFile) {
		if (aMIMEmatch!=null && !aMIMEmatch.equals("*/*")) {
			int idxMatchSeparator = aMIMEmatch.lastIndexOf("/");
			String theMatchCategory = aMIMEmatch.substring(0,idxMatchSeparator);
			String theMatchInstance = aMIMEmatch.substring(idxMatchSeparator+1);
			return matchType(theMatchCategory,theMatchInstance,aFile);
		} else {
			return true;
		}
	}

	public boolean matchType(String aMIMEmatchCategory, String aMIMEmatchInstance, File aFile) {
		if (aMIMEmatchCategory!=null && !aMIMEmatchCategory.equals("*")) {
			String theFileMIMEtype = guessMIMEtype(aFile);
			if (theFileMIMEtype==null)
				return false;
			int idxFileSeparator = theFileMIMEtype.lastIndexOf("/");
			String theFileCategory = theFileMIMEtype.substring(0,idxFileSeparator);
			String theFileInstance = theFileMIMEtype.substring(idxFileSeparator+1);
			if (theFileCategory==null || theFileInstance==null)
				return false;
			if (theFileCategory.equals(aMIMEmatchCategory)) {
				if (aMIMEmatchInstance.equals("*") || theFileInstance.equals(aMIMEmatchInstance)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Given a particular MIME type, return the most common extension used for it.
	 *
	 * @param aMIMEtype - a string in the form "category/type"
	 * @param defaultExtension - default string to return in case aMIMEtype is null or not found
	 * @return Returns the default extension defined in the MIME type map for the given MIME type.
	 * Returns null if aMIMEtype is null or if aMIMEtype is not found in the map.
	 */
	public String guessExtensionFromMIMEtype(String aMIMEtype, String defaultExtension) {
		if (aMIMEtype!=null) {
			String theMIMEtoFind = aMIMEtype.toLowerCase(Locale.getDefault());
			int idxMIMEtype = -1;
			for (int i=0; i<mMIMEtypes.length; i++) {
				if (mMIMEtypes[i].equals(theMIMEtoFind)) {
					idxMIMEtype = i;
					break;
				}
			}
			if (idxMIMEtype>=0 && idxMIMEtype<mMIMEtypes.length) {
				return mMIMEtypeDefExt[idxMIMEtype];
				/*
				MIMEtypeMap mExtensions = this;
				Iterator<Entry<String, Integer>> theExtensions = mExtensions.entrySet().iterator();
				while (theExtensions.hasNext()) {
					Entry<String, Integer> currExtensionEntry = theExtensions.next();
					if (currExtensionEntry.getValue()==idxMIMEtype) {
						return currExtensionEntry.getKey();
					}
				}
			 	*/
			}
		}
		return defaultExtension;
	}

	/**
	 * Determine the overall MIME type of the list. Supports Uri, File, and String.
	 *
	 * @param anIterator - an Iterator of one of the supported types: Uri, File, or String
	 * @return Returns a specific MIME type if they are all of the same type.<br>
	 * Returns a general MIME category in the form "category/*" if the list is not all of the same type
	 * but is all of the same category.<br>
	 * Returns "multipart/mixed" if neither category nor type are consistent throughout the list.<br>
	 * Returns null if the list is empty or is of an unsupported Iterator type.
	 */
	public String getOverallMIMEtype(Iterator<?> anIterator) {
		//iterate through the list and get overall mimeType
		String theOverallMIMEtype = null;
		String theMIMEtype = null;
		String theOverallMIMEcat = null;
		String theMIMEcat = null;
		while (anIterator.hasNext()) {
			Object theNextItem = anIterator.next();
			File theFile;
			if (theNextItem instanceof Uri) {
				theFile = new File(((Uri)theNextItem).getPath());
			} else if (theNextItem instanceof File) {
				theFile = (File)theNextItem;
			} else if (theNextItem instanceof String) {
				theFile = new File((String)theNextItem);
			} else {
				break; //list of unknown type, cannot process it
			}
			if (!theFile.isDirectory()) {
				//non-existing file objects return false for both isFile and isDir, so use !isDir
				if (!theFile.isHidden())
					theMIMEtype = getMIMEtype(theFile);
				else
					theMIMEtype = theOverallMIMEtype;
			} else {
				File[] af = theFile.listFiles();
				if (af!=null)
					theMIMEtype = getOverallMIMEtype(Arrays.asList(af).iterator());
				else
					theMIMEtype = null;
				//if folder is empty or only contains empty folders, do not count towards overall MIME
				if (theMIMEtype==null)
					theMIMEtype = theOverallMIMEtype;
			}
			if (theOverallMIMEtype!=null) {
				if (!theOverallMIMEtype.equals(theMIMEtype)) {
					theOverallMIMEcat = BitsFileUtils.getMIMEcategory(theOverallMIMEtype);
					theMIMEcat = BitsFileUtils.getMIMEcategory(theMIMEtype);
					if (!theOverallMIMEcat.equals(theMIMEcat)) {
						theOverallMIMEtype = BitsFileUtils.MIMETYPE_MIXED;
						break;  //no need to keep looking at the various types
					} else {
						theOverallMIMEtype = theOverallMIMEcat;
					}
				} else {
					//nothing to do
				}
			} else {
				theOverallMIMEtype = theMIMEtype;
			}
		}
		return theOverallMIMEtype;
	}

	private String setDefExt(int i, String anExt) {
		mMIMEtypeDefExt[i] = anExt;
		return anExt;
	}

	/**
	 * Create the MIME type mappings.
	 *
	 * @return Returns self in order to allow chaining calls.
	 */
	public MIMEtypeMap createMaps() {
		MIMEtypeMap mExtensions = this;
		mExtensions.clear();
		int i = 0;
		//Application types
		mMIMEtypes[i] = "application/octet-stream";
		mExtensions.put(setDefExt(i,".bin"),i);
		mExtensions.put(".dms",i);
		mExtensions.put(".lha",i);	mExtensions.put(".lzh",i);
		mExtensions.put(".class",i);mExtensions.put(".so",i);
		mExtensions.put(".iso",i);	mExtensions.put(".dmg",i);
		mExtensions.put(".dist",i);	mExtensions.put(".distz",i);
		mExtensions.put(".pkg",i);	mExtensions.put(".bpk",i);
		mExtensions.put(".dump",i);	mExtensions.put(".elc",i);

		mMIMEtypes[++i] = "application/msword";
		mExtensions.put(setDefExt(i,".doc"),i);
		mExtensions.put(".dot",i);

		mMIMEtypes[++i] = "application/pdf";
		mExtensions.put(setDefExt(i,".pdf"),i);

		mMIMEtypes[++i] = "application/x-mswrite";
		mExtensions.put(setDefExt(i,".wri"),i);

		mMIMEtypes[++i] = "application/rtf";
		mExtensions.put(setDefExt(i,".rtf"),i);

		mMIMEtypes[++i] = "application/winhlp";
		mExtensions.put(setDefExt(i,".hlp"),i);

		mMIMEtypes[++i] = "application/x-ms-wmd";
		mExtensions.put(setDefExt(i,".wmd"),i);

		mMIMEtypes[++i] = "application/x-ms-wmz";
		mExtensions.put(setDefExt(i,".wmz"),i);

		mMIMEtypes[++i] = "application/x-msaccess";
		mExtensions.put(setDefExt(i,".mdb"),i);

		mMIMEtypes[++i] = "application/x-msbinder";                              //10
		mExtensions.put(setDefExt(i,".obd"),i);

		mMIMEtypes[++i] = "application/x-mscardfile";
		mExtensions.put(setDefExt(i,".crd"),i);

		mMIMEtypes[++i] = "application/x-msclip";
		mExtensions.put(setDefExt(i,".clp"),i);

		mMIMEtypes[++i] = "application/x-msdownload";
		mExtensions.put(setDefExt(i,".exe"),i);
		mExtensions.put(".dll",i); mExtensions.put(".com",i);
		mExtensions.put(".bat",i); mExtensions.put(".msi",i);

		mMIMEtypes[++i] = "application/x-msmediaview";
		mExtensions.put(setDefExt(i,".mvb"),i);
		mExtensions.put(".m13",i); mExtensions.put(".m14",i);

		mMIMEtypes[++i] = "application/x-msmetafile";
		mExtensions.put(setDefExt(i,".wmf"),i);

		mMIMEtypes[++i] = "application/x-msmoney";
		mExtensions.put(setDefExt(i,".mny"),i);

		mMIMEtypes[++i] = "application/x-mspublisher";
		mExtensions.put(setDefExt(i,".pub"),i);

		mMIMEtypes[++i] = "application/x-msschedule";
		mExtensions.put(setDefExt(i,".scd"),i);

		mMIMEtypes[++i] = "application/x-msterminal";
		mExtensions.put(setDefExt(i,".trm"),i);

		mMIMEtypes[++i] = "application/vnd.ms-works";							//20
		mExtensions.put(setDefExt(i,".wps"),i);
		mExtensions.put(".wks",i);
		mExtensions.put(".wcm",i);	mExtensions.put(".wdb",i);

		mMIMEtypes[++i] = "application/vnd.ms-artgalry";
		mExtensions.put(setDefExt(i,".cil"),i);

		mMIMEtypes[++i] = "application/vnd.ms-asf";
		mExtensions.put(setDefExt(i,".asf"),i);

		mMIMEtypes[++i] = "application/vnd.ms-cab-compressed";
		mExtensions.put(setDefExt(i,".cab"),i);

		mMIMEtypes[++i] = "application/vnd.ms-excel";
		mExtensions.put(setDefExt(i,".xls"),i);
		mExtensions.put(".xlm",i);
		mExtensions.put(".xla",i);	mExtensions.put(".xlc",i);
		mExtensions.put(".xlt",i);	mExtensions.put(".xlw",i);

		mMIMEtypes[++i] = "application/vnd.ms-fontobject";
		mExtensions.put(setDefExt(i,".eot"),i);

		mMIMEtypes[++i] = "application/vnd.ms-htmlhelp";
		mExtensions.put(setDefExt(i,".chm"),i);

		mMIMEtypes[++i] = "application/vnd.ms-ims";
		mExtensions.put(setDefExt(i,".ims"),i);

		mMIMEtypes[++i] = "application/vnd.ms-lrm";
		mExtensions.put(setDefExt(i,".lrm"),i);

		mMIMEtypes[++i] = "application/vnd.ms-powerpoint";
		mExtensions.put(".ppt",i);	mExtensions.put(setDefExt(i,".pps"),i);
		mExtensions.put(".pot",i);

		mMIMEtypes[++i] = "application/vnd.ms-project";							//30
		mExtensions.put(setDefExt(i,".mpp"),i);	mExtensions.put(".mpt",i);

		mMIMEtypes[++i] = "application/vnd.wordperfect";
		mExtensions.put(setDefExt(i,".wpd"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.chart";
		mExtensions.put(setDefExt(i,".odc"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.chart-template";
		mExtensions.put(setDefExt(i,".otc"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.formula";
		mExtensions.put(setDefExt(i,".odf"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.formula-template";
		mExtensions.put(setDefExt(i,".otf"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.graphics";
		mExtensions.put(setDefExt(i,".odg"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.graphics-template";
		mExtensions.put(setDefExt(i,".otg"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.image";
		mExtensions.put(setDefExt(i,".odi"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.image-template";
		mExtensions.put(setDefExt(i,".oti"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.presentation";	//40
		mExtensions.put(setDefExt(i,".odp"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.presentation-template";
		mExtensions.put(setDefExt(i,".otp"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.spreadsheet";
		mExtensions.put(setDefExt(i,".ods"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.spreadsheet-template";
		mExtensions.put(setDefExt(i,".ots"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.text";
		mExtensions.put(setDefExt(i,".odt"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.text-master";
		mExtensions.put(setDefExt(i,".otm"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.text-template";
		mExtensions.put(setDefExt(i,".ott"),i);

		mMIMEtypes[++i] = "application/vnd.oasis.opendocument.text-web";
		mExtensions.put(setDefExt(i,".oth"),i);

		mMIMEtypes[++i] = "application/vnd.android.package-archive";
		mExtensions.put(setDefExt(i,".apk"),i);
		/*
		 * TOTAL IN SECTION = 48
		 */

		//Archive types
		mMIMEtypes[++i] = "application/zip";
		mExtensions.put(setDefExt(i,".zip"),i);

		mMIMEtypes[++i] = "application/java-archive";
		mExtensions.put(setDefExt(i,".jar"),i);

		mMIMEtypes[++i] = "application/x-rar-compressed";
		mExtensions.put(setDefExt(i,".rar"),i);

		mMIMEtypes[++i] = "application/gzip";
		mExtensions.put(setDefExt(i,".gz"),i);

		mMIMEtypes[++i] = "application/x-tar";
		mExtensions.put(setDefExt(i,".tar"),i);

		mMIMEtypes[++i] = "application/x-bittorrent";
		mExtensions.put(setDefExt(i,".torrent"),i);

		mMIMEtypes[++i] = "application/epub+zip";
		mExtensions.put(setDefExt(i,".epub"),i);
		/*
		 * TOTAL IN SECTION = 7
		 */

		//Audio types
		mMIMEtypes[++i] = "audio/mpeg";
		mExtensions.put(setDefExt(i,".mp3"),i);	mExtensions.put(".mp2",i);
		mExtensions.put(".mpga",i);	mExtensions.put(".mp2a",i);
		mExtensions.put(".m2a",i);	mExtensions.put(".m3a",i);

		mMIMEtypes[++i] = "audio/x-wav";
		mExtensions.put(setDefExt(i,".wav"),i);

		mMIMEtypes[++i] = "audio/x-ogg";
		mExtensions.put(setDefExt(i,".ogg"),i);

		mMIMEtypes[++i] = "audio/midi";
		mExtensions.put(setDefExt(i,".mid"),i);	mExtensions.put(".midi",i);

		mMIMEtypes[++i] = "audio/x-ms-wma";
		mExtensions.put(setDefExt(i,".wma"),i);

		mMIMEtypes[++i] = "audio/x-pn-realaudio";
		mExtensions.put(".ram",i);	mExtensions.put(setDefExt(i,".ra"),i);

		mMIMEtypes[++i] = "audio/x-pn-realaudio-plugin";
		mExtensions.put(setDefExt(i,".rmp"),i);

		mMIMEtypes[++i] = "audio/x-aiff";
		mExtensions.put(setDefExt(i,".aif"),i);	mExtensions.put(".aiff",i);
		mExtensions.put(".aifc",i);

		//mMIMEtypes[++i] = "audio/x-mpegurl";  MOVED TO TEXT
		//mExtensions.put(setDefExt(i,".m3u"),i);

		mMIMEtypes[++i] = "audio/mp4";
		mExtensions.put(".mp4a",i); mExtensions.put(setDefExt(i,".m4a"),i);
		mExtensions.put(".m4p",i); //drm'd m4a file
		mExtensions.put(".m4b",i); //ebook m4a file
		mExtensions.put(".m4r",i); //iPhone ringtone m4a file

		mMIMEtypes[++i] = "audio/basic";									//10
		mExtensions.put(setDefExt(i,".au"),i);	mExtensions.put(".snd",i);

		mMIMEtypes[++i] = "application/vnd.rn-realmedia";
		mExtensions.put(setDefExt(i,".rm"),i);

		mMIMEtypes[++i] = "audio/3gpp";
		mExtensions.put(setDefExt(i,".3gpp"),i);

		mMIMEtypes[++i] = "audio/amr";
		mExtensions.put(setDefExt(i,".amr"),i);

		mMIMEtypes[++i] = "audio/amr-wb";
		mExtensions.put(setDefExt(i,".awb"),i);
		/*
		 * TOTAL IN SECTION = 14
		 */

		//Video types
		mMIMEtypes[++i] = "video/3gpp";
		mExtensions.put(setDefExt(i,".3gp"),i);

		mMIMEtypes[++i] = "video/mpeg";
		mExtensions.put(setDefExt(i,".mpg"),i);	mExtensions.put(".mpeg",i);
		mExtensions.put(".mpe",i);	mExtensions.put(".m1v",i);
		mExtensions.put(".m2v",i);

		mMIMEtypes[++i] = "video/mp4";
		mExtensions.put(setDefExt(i,".mp4"),i);	mExtensions.put(".mp4v",i);
		mExtensions.put(".mpg4",i);		mExtensions.put(".m4v",i);

		mMIMEtypes[++i] = "video/quicktime";
		mExtensions.put(setDefExt(i,".mov"),i);	mExtensions.put(".qt",i);

		mMIMEtypes[++i] = "video/x-msvideo";
		mExtensions.put(setDefExt(i,".avi"),i);

		mMIMEtypes[++i] = "video/x-ms-asf";
		mExtensions.put(setDefExt(i,".asf"),i);	mExtensions.put(".asx",i);

		mMIMEtypes[++i] = "video/x-ms-wmv";
		mExtensions.put(setDefExt(i,".wmv"),i);

		mMIMEtypes[++i] = "video/vnd.mpegurl";
		mExtensions.put(".mxu",i);	mExtensions.put(setDefExt(i,".m4u"),i);

		mMIMEtypes[++i] = "video/x-fli";
		mExtensions.put(setDefExt(i,".fli"),i);
		/*
		 * TOTAL IN SECTION = 9
		 */

		//Picture types
		mMIMEtypes[++i] = "image/bmp";
		mExtensions.put(setDefExt(i,".bmp"),i);

		mMIMEtypes[++i] = "image/cgm";
		mExtensions.put(setDefExt(i,".cgm"),i);

		mMIMEtypes[++i] = "image/gif";
		mExtensions.put(setDefExt(i,".gif"),i);

		mMIMEtypes[++i] = "image/jpeg";
		mExtensions.put(setDefExt(i,".jpg"),i);	mExtensions.put(".jpeg",i);
		mExtensions.put(".jpe",i);

		mMIMEtypes[++i] = "image/png";
		mExtensions.put(setDefExt(i,".png"),i);

		mMIMEtypes[++i] = "image/tiff";
		mExtensions.put(".tif",i);	mExtensions.put(setDefExt(i,".tiff"),i);

		mMIMEtypes[++i] = "image/x-cmu-raster";
		mExtensions.put(setDefExt(i,".ras"),i);

		mMIMEtypes[++i] = "image/x-icon";
		mExtensions.put(setDefExt(i,".ico"),i);

		mMIMEtypes[++i] = "image/x-pcx";
		mExtensions.put(setDefExt(i,".pcx"),i);

		mMIMEtypes[++i] = "image/vnd.wap.wbmp";						//10
		mExtensions.put(setDefExt(i,".wbmp"),i);

		mMIMEtypes[++i] = "image/x-pict";
		mExtensions.put(setDefExt(i,".pict"),i);	mExtensions.put(".pct",i);
		mExtensions.put(".pic",i);

		mMIMEtypes[++i] = "image/x-targa";
		mExtensions.put(setDefExt(i,".tga"),i);		mExtensions.put(".targa",i);

		mMIMEtypes[++i] = "image/x-macpaint";
		mExtensions.put(setDefExt(i,".mac"),i);		mExtensions.put(".pntg",i);
		mExtensions.put(".pnt",i);

		mMIMEtypes[++i] = "image/x-quicktime";
		mExtensions.put(setDefExt(i,".qti"),i);		mExtensions.put(".ptif",i);

		mMIMEtypes[++i] = "image/x-photoshop";
		mExtensions.put(setDefExt(i,".psd"),i);

		mMIMEtypes[++i] = "image/x-xcf"; //GIMP image, layered like psd
		mExtensions.put(setDefExt(i,".xcf"),i);

		mMIMEtypes[++i] = "image/mpo"; //multi-picture format (stereo image)
		mExtensions.put(setDefExt(i,".mpo"),i);

		mMIMEtypes[++i] = "image/jps"; //multi-picture format (left/right stereo image in jpg)
		mExtensions.put(setDefExt(i,".jps"),i);

		mMIMEtypes[++i] = "image/pns"; //multi-picture format (left/right stereo image in png)
		mExtensions.put(setDefExt(i,".pns"),i);

		mMIMEtypes[++i] = "image/x-sgi";							//20
		mExtensions.put(setDefExt(i,".sgi"),i);		mExtensions.put(".rgb",i);

		/*
		 * TOTAL IN SECTION = 20
		 */

		//Web related
		mMIMEtypes[++i] = "application/xhtml+xml";
		mExtensions.put(setDefExt(i,".xhtml"),i);mExtensions.put(".xht",i);

		mMIMEtypes[++i] = "application/xml";
		mExtensions.put(setDefExt(i,".xml"),i);	mExtensions.put(".xsl",i);

		mMIMEtypes[++i] = "application/xml-dtd";
		mExtensions.put(setDefExt(i,".dtd"),i);

		mMIMEtypes[++i] = "application/rsd+xml";
		mExtensions.put(setDefExt(i,".rsd"),i);

		mMIMEtypes[++i] = "application/rss+xml";
		mExtensions.put(setDefExt(i,".rss"),i);

		mMIMEtypes[++i] = "application/x-shockwave-flash";
		mExtensions.put(setDefExt(i,".swf"),i);

		mMIMEtypes[++i] = "text/uri-list";
		mExtensions.put(setDefExt(i,".uri"),i);	mExtensions.put(".uris",i);
		mExtensions.put(".urls",i);

		mMIMEtypes[++i] = "text/html";
		mExtensions.put(setDefExt(i,".html"),i);	mExtensions.put(".htm",i);

		mMIMEtypes[++i] = "text/css";
		mExtensions.put(setDefExt(i,".css"),i);

		mMIMEtypes[++i] = "message/rfc822";								//10
		mExtensions.put(setDefExt(i,".eml"),i);	mExtensions.put(".mime",i);
		/*
		 * TOTAL IN SECTION = 10
		 */

		//Text types
		mMIMEtypes[++i] = "text/plain";
		mExtensions.put(setDefExt(i,".txt"),i);	mExtensions.put(".text",i);
		mExtensions.put(".conf",i);	mExtensions.put(".def",i);
		mExtensions.put(".list",i);	mExtensions.put(".log",i);
		mExtensions.put(".in",i);

		mMIMEtypes[++i] = "text/csv";
		mExtensions.put(setDefExt(i,".csv"),i);

		mMIMEtypes[++i] = "text/tab-separated-values";
		mExtensions.put(setDefExt(i,".tsv"),i);

		mMIMEtypes[++i] = "text/x-asm";
		mExtensions.put(setDefExt(i,".asm"),i); 	mExtensions.put(".s",i);

		mMIMEtypes[++i] = "text/x-c";
		mExtensions.put(".c",i);	mExtensions.put(".cc",i);
		mExtensions.put(".cxx",i);	mExtensions.put(setDefExt(i,".cpp"),i);
		mExtensions.put(".h",i);	mExtensions.put(".hh",i);
		mExtensions.put(".dic",i);

		mMIMEtypes[++i] = "text/x-fortran";
		mExtensions.put(".f",i);	mExtensions.put(setDefExt(i,".for"),i);
		mExtensions.put(".f77",i);	mExtensions.put(".f90",i);

		mMIMEtypes[++i] = "text/x-pascal";
		mExtensions.put(setDefExt(i,".pas"),i);	mExtensions.put(".p",i);

		mMIMEtypes[++i] = "text/x-java-source";
		mExtensions.put(setDefExt(i,".java"),i);

		mMIMEtypes[++i] = "application/javascript";
		mExtensions.put(setDefExt(i,".js"),i);

		mMIMEtypes[++i] = "text/php";								//10
		mExtensions.put(setDefExt(i,".php"),i);

		mMIMEtypes[++i] = "text/x-setext";
		mExtensions.put(setDefExt(i,".etx"),i);

		mMIMEtypes[++i] = "text/x-uuencode";
		mExtensions.put(setDefExt(i,".uu"),i);

		mMIMEtypes[++i] = "text/x-vcalendar";
		mExtensions.put(setDefExt(i,".vcs"),i);

		mMIMEtypes[++i] = "text/x-vcard";
		mExtensions.put(setDefExt(i,".vcf"),i);

		mMIMEtypes[++i] = "text/calendar";
		mExtensions.put(setDefExt(i,".ics"),i);	mExtensions.put(".ifb",i);

		mMIMEtypes[++i] = "text/x-mpegurl";
		mExtensions.put(setDefExt(i,".m3u"),i);
		/*
		 * TOTAL IN SECTION = 16
		 */

		/*
		 * TOTALS = AppType=48 + Archive=7 + Audio=14 + Video=9 + Pics=10 + Web=10 + Text=16
		 * >>>>> DO NOT FORGET TO UPDATE NUM_MIMETYPES WHEN ADDING NEW TYPES <<<<<<<
		 */
		return this;
	}

}
