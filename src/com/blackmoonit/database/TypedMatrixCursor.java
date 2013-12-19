package com.blackmoonit.database;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.database.AbstractCursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;

/**
 * Replacement for Android's MatrixCursor to add support for Blobs.
 *
 * @author Ryan Fischbach
 */
public class TypedMatrixCursor extends AbstractCursor {
	//private static final String TAG = "BITS.lib.database.TypedMatrixCursor";
    
	public static final int COLUMN_TYPE_NULL = 0;
	public static final int COLUMN_TYPE_INTEGER = 1;
	public static final int COLUMN_TYPE_FLOAT = 2;
	public static final int COLUMN_TYPE_STRING = 3;
	public static final int COLUMN_TYPE_BLOB = 4;
	public static final int COLUMN_TYPE_LONG = 5;
	public static final int COLUMN_TYPE_DOUBLE = COLUMN_TYPE_FLOAT;

    protected final String[] mColumnNames;
    protected final int[] mColumnTypes;
    protected final int mColumnCount;

    protected Object[] mData;
    protected int mRowCount = 0;

    /**
     * Constructs a new cursor with the given initial capacity.
     *
     * @param aColumnNames - names of the columns, the ordering of which determines 
     * column ordering elsewhere in this cursor
     * @param aColumnTypes - optional column types. COLUMN_TYPE_STRING is the default type.
     * @param initialCapacity in rows
     */
    public TypedMatrixCursor(String[] aColumnNames, int[] aColumnTypes, int initialCapacity) {
        mColumnNames = aColumnNames;
        mColumnCount = aColumnNames.length;
        mColumnTypes = new int[mColumnCount];
        for (int i=0; i<mColumnCount; i++) {
        	if (aColumnTypes!=null && i<aColumnTypes.length)
        		mColumnTypes[i] = aColumnTypes[i];
        	else
        		mColumnTypes[i] = TypedMatrixCursor.COLUMN_TYPE_STRING;
        }

        if (initialCapacity < 1) {
            initialCapacity = 1;
        }

        mData = new Object[mColumnCount * initialCapacity];
    }

    /**
     * Constructs a new cursor.
     *
     * @param aColumnNames - names of the columns, the ordering of which determines 
     * column ordering elsewhere in this cursor
     * @param aColumnTypes - optional column types. COLUMN_TYPE_STRING is the default type.
     */
    public TypedMatrixCursor(String[] aColumnNames, int[] aColumnTypes) {
        this(aColumnNames, aColumnTypes, 16);
    }

    /**
     * Gets value at the given column for the current row.
     */
    protected Object get(int aColumnIdx) {
    	checkColumnIdx(aColumnIdx);
        checkPosition();
        return mData[mPos*mColumnCount+aColumnIdx];
    }
    
	/**
     * Get the type for a given column.
     * 
     * @param aColumnIdx - column index
     */
    protected int getColumnType(int aColumnIdx) {
        if (mColumnTypes==null)
        	return TypedMatrixCursor.COLUMN_TYPE_STRING;
    	checkColumnIdx(aColumnIdx);
    	return mColumnTypes[aColumnIdx];
    }

    @Override
    public void fillWindow(int aRowPosition, CursorWindow aDataWindow) {
        if (aRowPosition<0 || aRowPosition>=getCount()) {
            return;
        }
        aDataWindow.acquireReference();
        try {
            int oldRowPos = mPos;
            mPos = aRowPosition - 1;
            aDataWindow.clear();
            aDataWindow.setStartPosition(aRowPosition);
            int theNumCols = getColumnCount();
            aDataWindow.setNumColumns(theNumCols);
            while (moveToNext() && aDataWindow.allocRow()) {            
                for (int theColIdx=0; theColIdx<theNumCols; theColIdx++) {
                    boolean bPutNull = false;
                    boolean bPutFail = false;
                	switch (getColumnType(theColIdx)) {
                		case COLUMN_TYPE_STRING: 
                        case COLUMN_TYPE_INTEGER:
                            String strField = getString(theColIdx);
                            if (strField != null)
                                bPutFail = (!aDataWindow.putString(strField,mPos,theColIdx));
                            else
                            	bPutNull = true;
                            break;
                        case COLUMN_TYPE_LONG:
                            Long longField = getLong(theColIdx);
                            if (longField != null)
                                bPutFail = (!aDataWindow.putLong(longField,mPos,theColIdx));
                            else
                            	bPutNull = true;
                            break;
                		case COLUMN_TYPE_FLOAT:
                            Double floatField = getDouble(theColIdx);
                            if (floatField != null)
                                bPutFail = (!aDataWindow.putDouble(floatField,mPos,theColIdx));
                            else
                            	bPutNull = true;
                            break;
                		case COLUMN_TYPE_BLOB:
                            byte[] blobField = getBlob(theColIdx);
                            if (blobField != null)
                                bPutFail = (!aDataWindow.putBlob(blobField,mPos,theColIdx));
                            else
                            	bPutNull = true;
                            break;
                		default:
                			bPutNull = true;
                	}
                	if (bPutNull) {
                		bPutFail = (!aDataWindow.putNull(mPos,theColIdx));
                	}
                	if (bPutFail) {
                        aDataWindow.freeLastRow();
                        break;
                	}
                }
            }
            
            mPos = oldRowPos;
        } catch (IllegalStateException e) {
            // simply ignore it
        } finally {
            aDataWindow.releaseReference();
        }
    }

    /**
     * Adds a new row to the end and returns a builder for that row. 
     * Not safe for concurrent use.
     *
     * @return builder which can be used to set the column values for the new row.
     */
    public RowBuilder newRow() {
        mRowCount++;
        int theEndIdx = mRowCount*mColumnCount;
        ensureCapacity(theEndIdx);
        int theStartIdx = theEndIdx-mColumnCount;
        return new RowBuilder(theStartIdx,theEndIdx);
    }

	/**
     * Adds a new row to the end with the given column values. 
     * Not safe for concurrent use.
     *
     * @throws IllegalArgumentException if {@code aColumnValues.length != mColumnNames.length}
     * @param aColumnValues in the same order as the the column names specified at cursor 
     * construction time
     */
    public void addRow(Object[] aColumnValues) {
    	checkColumnCount(aColumnValues.length);
        int theStartIdx = mRowCount++ * mColumnCount;
        ensureCapacity(theStartIdx + mColumnCount);
        System.arraycopy(aColumnValues, 0, mData, theStartIdx, mColumnCount);
    }

    /**
     * Adds a new row to the end with the given column values. 
     * Not safe for concurrent use.
     *
     * @throws IllegalArgumentException if {@code aColumnValues.size() != mColumnNames.length}
     * @param aColumnValues in the same order as the the column names specified at cursor 
     * construction time
     */
    public void addRow(Iterable<?> aColumnValues) {
        int theStartIdx = mRowCount * mColumnCount;
        int theEndIdx = theStartIdx + mColumnCount;
        ensureCapacity(theEndIdx);

        if (aColumnValues instanceof ArrayList<?>) {
            addRow((ArrayList<?>) aColumnValues, theStartIdx);
            return;
        }

        int current = theStartIdx;
        Object[] localData = mData;
        for (Object columnValue : aColumnValues) {
            if (current == theEndIdx) {
                throw new IllegalArgumentException("aColumnValues.size() > mColumnNames.length");
            }
            localData[current++] = columnValue;
        }

        if (current != theEndIdx) {
            throw new IllegalArgumentException("aColumnValues.size() < mColumnNames.length");
        }
        mRowCount++;
    }

    /** 
     * Optimization for {@link ArrayList}.
     */
    protected void addRow(ArrayList<?> aColumnValues, int aStartIdx) {
        int theSize = aColumnValues.size();
        checkColumnCount(theSize);
        mRowCount++;
        Object[] localData = mData;
        for (int i=0; i<theSize; i++) {
            localData[aStartIdx+i] = aColumnValues.get(i);
        }
    }

    /** 
     * Ensures that this cursor has enough capacity.
     */
    protected void ensureCapacity(int aSize) {
        if (aSize > mData.length) {
            Object[] oldData = mData;
            int newSize = Math.max(mData.length*2,aSize);
            mData = new Object[newSize];
            System.arraycopy(oldData,0,mData,0,oldData.length);
        }
    }

    @Override
    protected void checkPosition() {
        if (mPos<0 || mPos>=getCount()) {
            throw new CursorIndexOutOfBoundsException(mPos, getCount());
        }
    }

    protected void checkColumnIdx(int aColumnIdx) {
        if (aColumnIdx<0 || aColumnIdx>=mColumnCount) {
            throw new CursorIndexOutOfBoundsException("Requested column: "+aColumnIdx+
            		", # of columns: "+mColumnCount);
        }
	}

    protected void checkColumnCount(int aNumColumns) {
        if (aNumColumns!=mColumnCount) {
            throw new IllegalArgumentException("columnNames.length = "+mColumnCount + 
            		", columnValues.length = "+aNumColumns);
        }
	}

    /**
     * Builds a row, starting from the left-most column and adding one column
     * value at a time. Follows the same ordering as the column names specified
     * at cursor construction time.
     */
    public class RowBuilder {

        private int mCurrIndex;
        private final int mEndIndex;

        RowBuilder(int aStartIdx, int aEndIdx) {
            mCurrIndex = aStartIdx;
            mEndIndex = aEndIdx;
        }

        /**
         * Sets the next column value in this row.
         *
         * @throws CursorIndexOutOfBoundsException if you try to add too many values
         * @return this builder to support chaining
         */
        public RowBuilder add(Object aColumnValue) {
            if (mCurrIndex == mEndIndex) {
                throw new CursorIndexOutOfBoundsException("No more columns left.");
            }
            mData[mCurrIndex++] = aColumnValue;
            return this;
        }

        /**
         * sets the next column value in this row to a blob.
         * @param aColumnValue - instanceof ByteArrayOutputStream, BitmapDrawable, Bitmap
         * @return this builder to support chaining
         */
        public RowBuilder addBlob(Object aColumnValue) {
        	if (aColumnValue!=null) {
        		ByteArrayOutputStream theBlob = null;
        		if (aColumnValue instanceof ByteArrayOutputStream)
        			theBlob = (ByteArrayOutputStream)aColumnValue;
        		else {
        			Bitmap theBitmap = null;
        			if (aColumnValue instanceof Bitmap) {
        				theBitmap = (Bitmap)aColumnValue;
        			} else if (aColumnValue instanceof BitmapDrawable) {
       					theBitmap = ((BitmapDrawable)aColumnValue).getBitmap();
        			}
        			if (theBitmap!=null) {
            			theBlob = new ByteArrayOutputStream();
                		theBitmap.compress(CompressFormat.PNG,0,theBlob);
        			}
        		}
        		if (theBlob!=null) {
        			add(theBlob.toByteArray());
        			return this;
        		}
        	}
        	add(null);
        	return this;        	
        }

    }

    /**
     * Defines how to query the data store.
     */
	public interface OnQueryDataStore {
		/**
		 * Action to take when querying or requerying the data store.
		 * 
		 * @param aCursor - the cursor to fill, it will always be emptied first.
		 */
	    public boolean onQueryDataStore(TypedMatrixCursor aCursor);
	}
	
	protected OnQueryDataStore mOnQueryDataStore = null;
	
	/**
	 * Set the action used to query the data store. Required for {@link #requery()} to work.
	 */
	public void setOnQueryDataStore(TypedMatrixCursor.OnQueryDataStore onQueryDataStore) {
		mOnQueryDataStore = onQueryDataStore;
	}

    @Override
    public boolean requery() {
        if (isClosed())
            return false;
        if (mOnQueryDataStore==null)
        	return super.requery();
        
        //reset cursor
        int oldRowPos = mPos;
        mPos = 0;
        mRowCount = 0;
        //refill cursor
        boolean theResult = mOnQueryDataStore.onQueryDataStore(this);
        if (theResult) {
        	//try to restore old row pos
        	moveToPosition(oldRowPos);
        }
        return theResult;
    }

    
    //=========================== AbstractCursor implementation ===========================

    @Override
    public int getCount() {
        return mRowCount;
    }

    @Override
    public String[] getColumnNames() {
        return mColumnNames;
    }

    @Override
    public String getString(int aColumnIdx) {
        Object value = get(aColumnIdx);
        if (value == null) return null;
        return value.toString();
    }

    @Override
    public short getShort(int aColumnIdx) {
        Object value = get(aColumnIdx);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).shortValue();
        return Short.parseShort(value.toString());
    }

    @Override
    public int getInt(int aColumnIdx) {
        Object value = get(aColumnIdx);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    @Override
    public long getLong(int aColumnIdx) {
        Object value = get(aColumnIdx);
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    @Override
    public float getFloat(int aColumnIdx) {
        Object value = get(aColumnIdx);
        if (value == null) return 0.0f;
        if (value instanceof Number) return ((Number) value).floatValue();
        return Float.parseFloat(value.toString());
    }

    @Override
    public double getDouble(int aColumnIdx) {
        Object value = get(aColumnIdx);
        if (value == null) return 0.0d;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }

    @Override
    public boolean isNull(int aColumnIdx) {
        return get(aColumnIdx) == null;
    }
    
    @Override
    public byte[] getBlob(int aColumnIdx) {
        Object value = get(aColumnIdx);
      	return ((byte[])value);
    }
    
}
