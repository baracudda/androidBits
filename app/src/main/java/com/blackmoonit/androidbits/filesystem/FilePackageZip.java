package com.blackmoonit.androidbits.filesystem;

import android.net.Uri;

import com.blackmoonit.androidbits.utils.BitsArrays;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Zip archive descendant of {@link com.blackmoonit.androidbits.filesystem.FilePackage}
 *
 * @author Ryan Fischbach
 */
public class FilePackageZip extends FilePackage {
	protected byte[] mBuffer = null;
	public static final String CRC_TYPE = "adler32";
	public Long crcValue = 0L;

	public interface OnEachFileEntry {
		public boolean process(final ZipOutputStream zos, File aFile, ZipEntry anEntry);
	}

	public interface OnEachEntry {
	    public boolean process(final ZipInputStream zis, ZipEntry anEntry);
	}

	public interface OnException {
		public void caught(Exception e);
	}

	public FilePackageZip(File aPackageFile) {
		super(aPackageFile);
		mBuffer = new byte[BitsFileUtils.computeFileBufferSize(mPackageFile)];
	}

	private void packFiles(Iterator<?> anIterator, ZipOutputStream aOutStream,
			OnEachFileEntry onEachFile) throws IOException {
		boolean bCancelled = false;
		while (anIterator.hasNext() && !bCancelled) {
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
			if (!theFile.equals(mPackageFile)) {
				String theZipEntryName = BitsFileUtils.getRelativePath(theFile,mBasePath)+
						((theFile.isDirectory())?"/":"");
				ZipEntry theEntry = new ZipEntry(theZipEntryName);
				theEntry.setTime(theFile.lastModified());
				if (onEachFile!=null) {
					bCancelled = !onEachFile.process(aOutStream,theFile,theEntry);
				}
				if (!bCancelled) {
					aOutStream.putNextEntry(theEntry);
					if (theFile.isFile()) {
						theEntry.setSize(theFile.length());
						BufferedInputStream inStream = new BufferedInputStream(
								new FileInputStream(theFile),mBuffer.length);
						BitsFileUtils.copyStreamData(inStream,aOutStream,mBuffer,theFile.length(),
								mMsgHandler,mProgressID);
						inStream.close();
					} else {
						File[] af = theFile.listFiles();
						if (af!=null)
							packFiles(Arrays.asList(af).iterator(),aOutStream,onEachFile);
					}
				}
			}
			Thread.yield();
		}
	}

	@Override
	public void pack(Iterator<?> anIterator) {
		pack(anIterator,null,null);
	}

	public void pack(Iterator<?> anIterator, OnEachFileEntry onEachFile, OnException onException) {
		try {
			if (!mPackageFile.exists())
				mPackageFile.createNewFile();
			FileOutputStream rawOutStream = new FileOutputStream(mPackageFile);
			CheckedOutputStream crcOutStream = new CheckedOutputStream(rawOutStream, new Adler32());
			ZipOutputStream zipOutStream = new ZipOutputStream(new BufferedOutputStream(crcOutStream));
			zipOutStream.setMethod(ZipOutputStream.DEFLATED);
			packFiles(anIterator,zipOutStream,onEachFile);
			zipOutStream.flush();
			zipOutStream.close();
			crcValue = crcOutStream.getChecksum().getValue();
		} catch (Exception e) {
			if (onException!=null)
				onException.caught(e);
			else
				e.printStackTrace();
		}
	}

	@Override
	public void unpack(final String aDestPath) {
		foreach(new OnEachEntry() {

			@Override
			public boolean process(ZipInputStream zis, ZipEntry anEntry) {
				try {
					unpackEntry(zis,aDestPath,anEntry);
					Thread.yield();
				} catch (IOException e) {
					return false;
				}
				return true;
			}

		}, new OnException() {

			@Override
			public void caught(Exception e) {
				e.printStackTrace();
			}

		});
	}

	public void unpackFileEntry(ZipInputStream zis, File aDestFile, ZipEntry anEntry) throws IOException {
		if (anEntry.isDirectory()) {
			aDestFile.mkdirs();
		} else {
			File aParent = aDestFile.getParentFile();
			if (aParent!=null && !aParent.exists()) {
				aParent.mkdirs();
			}
			BufferedOutputStream outStream = new BufferedOutputStream(
					new FileOutputStream(aDestFile),mBuffer.length);
			BitsFileUtils.copyStreamData(zis,outStream,mBuffer,anEntry.getSize(),
					mMsgHandler,mProgressID);
			outStream.flush();
			outStream.close();
		}
		aDestFile.setLastModified(anEntry.getTime());
	}

	public void unpackEntry(ZipInputStream zis, String aDestPath, ZipEntry anEntry) throws IOException {
		try {
			File dstFile = new File(aDestPath,anEntry.getName());
			unpackFileEntry(zis,dstFile,anEntry);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	public void foreach(OnEachEntry onEachEntry, OnException onException) {
		if (onEachEntry!=null && mPackageFile.exists()) {
			boolean bCancelled = false;
			FileInputStream rawInStream;
			try {
				rawInStream = new FileInputStream(mPackageFile);
				CheckedInputStream crcInStream = new CheckedInputStream(rawInStream, new Adler32());
				BufferedInputStream buffInStream = new BufferedInputStream(crcInStream,mBuffer.length);
				ZipInputStream zipInStream = new ZipInputStream(buffInStream);
				try {
					ZipEntry theEntry = zipInStream.getNextEntry();
					while (theEntry!=null) {
						if (!onEachEntry.process(zipInStream,theEntry)) {
							bCancelled = true;
							break;
						}
						Thread.yield();
						if (!bCancelled) {
							theEntry = zipInStream.getNextEntry();
						} else {
							break;
						}
					}
					crcValue = crcInStream.getChecksum().getValue();
				} catch (IllegalArgumentException iae) {
					throw new ZipException("Zip format not recognized");
				}
				zipInStream.close();
			} catch (FileNotFoundException fnfe) {
				if (onException!=null)
					onException.caught(fnfe);
			} catch (ZipException ze) {
				if (onException!=null)
					onException.caught(ze);
			} catch (IOException ioe) {
				if (onException!=null)
					onException.caught(ioe);
			}
		}
	}

	private String getZipCommentFromBuffer(int numRead) {
		String theResult = null;
		byte[] magicCentralDirectoryEnd = {0x50, 0x4b, 0x05, 0x06};
		int buffLen = Math.min(mBuffer.length,numRead);
		int idxCentralDirEnd = BitsArrays.lastIndexOf(mBuffer,magicCentralDirectoryEnd,0,buffLen);
		if (idxCentralDirEnd>=0) {
			int commentLen = mBuffer[idxCentralDirEnd+20]+mBuffer[idxCentralDirEnd+21]*256;
			if (idxCentralDirEnd+22+commentLen<=buffLen) {
				theResult = new String(mBuffer,idxCentralDirEnd+22,commentLen);
			}
		}
		return theResult;
	}

	public String extractZipComment() {
		String theResult = null;
		if (mPackageFile.exists()) {
			try {
				/* The whole Zip end of central directory record MUST fit in the buffer.
				 * Otherwise, the comment will not be recognized correctly
				 * The record is 22 bytes + comment length in size
				 */
				FileInputStream inStream = new FileInputStream(mPackageFile);
				inStream.skip(mPackageFile.length()-mBuffer.length);
				theResult = getZipCommentFromBuffer(inStream.read(mBuffer));
				inStream.close();
			} catch (Exception e) {
				//on exception, then return null, which was already set before try block
			}
		}
		return theResult;
	}

	public boolean isCRCsame(String aCrcValue) {
		//if you call this function, make sure string conforms to Long.decode() "0x…" for hex, etc.
		Long crcZipShouldBe = Long.decode(aCrcValue);
		return (crcValue==crcZipShouldBe);
	}

}
