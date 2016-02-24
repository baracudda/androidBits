package com.blackmoonit.androidbits.map;
/*
 * Copyright (C) 2014 Blackmoon Info Tech Services
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

/**
 * Utilities and Interface definitions pertaining to points and vectors in several dimensions.
 * 2D, 3D, GeoSpatial, as well as the vectors of each. All values are integers.
 *
 * @author baracudda
 */
public class SpatialInt {
	/**
	 * The average radius spherical approximation of the Earth is ~6,371,010 meters.
	 */
	static public final int RADIUS_EARTH_M = 6371010;

	static public interface IPoint2D {
		int getXi();
		int getYi();

	}

	static public interface IPoint3D extends IPoint2D {
		int getZi();
	}

	static final int MIN_LATITUDE = -90*(int)1E6;
	static final int MAX_LATITUDE =  90*(int)1E6;
	static final int MIN_LONGITUDE = -180*(int)1E6;
	static final int MAX_LONGITUDE =  180*(int)1E6;

	/**
	 * GeoPoints are planetary mapping coordinates and all functions relate to points on the
	 * surface of a sphere. Latitude indicates how far north or south of the equator one is,
	 * making it's range -90..+90. Longitude is how far east or west one is of the Prime Meridian,
	 * making it's range -180..+180. Since this is the integer form, all values are shifted by 1E6.
	 *
	 */
	static public interface IPointGeo extends IPoint3D {
		/**
		 * Corresponds to getY().
		 * @return Returns latitude in micro-degrees (1E6).
		 */
		int getLatitudeE6();
		/**
		 * Corresponds to getX().
		 * @return Returns longitude in micro-degrees (1E6).
		 */
		int getLongitudeE6();
		/**
		 * Corresponds to getZ().
		 * @return Returns altitude in meters.
		 */
		int getAltitudeE3();
	}

	static public interface IVector2D extends IPoint2D {
		/**
		 * Compass heading style.
		 * @return Returns heading in micro-degrees (1E6).
		 */
		int getHeadingE6();
		/**
		 * The vector's amplitude.
		 * @return Returns the velocity in meters per hour.
		 */
		int getVelocityE3();
	}


	static public interface IVector3D extends IVector2D, IPoint3D {
		/**
		 * Direction in 3D space requires heading and pitch, much like latitude and longitude.
		 * @return Returns the pitch in micro-degrees (1E6).
		 */
		int getPitchE6();
	}

	static public interface IVectorGeo extends IVector3D, IPointGeo {

	}

	static public class Point2D implements IPoint2D {
		protected int mX;
		protected int mY;

		public Point2D(int aX, int aY) {
			super();
			mX = aX;
			mY = aY;
		}

		public Point2D(IPoint2D aPoint) {
			this(aPoint.getXi(),aPoint.getYi());
		}

		@Override
		public int getXi() {
			return mX;
		}

		@Override
		public int getYi() {
			return mY;
		}

		@Override
		public String toString() {
			return "("+getXi()+","+getYi()+")";
		}

	}

	static public class Point3D extends Point2D implements IPoint3D {
		protected int mZ;

		public Point3D(int aX, int aY, int aZ) {
			super(aX, aY);
			mZ = aZ;
		}

		public Point3D(IPoint3D aPoint) {
			this(aPoint.getXi(),aPoint.getYi(),aPoint.getZi());
		}

		@Override
		public int getZi() {
			return mZ;
		}

		@Override
		public String toString() {
			return "("+getXi()+","+getYi()+","+getZi()+")";
		}

	}

	static public class PointGeo extends Point3D implements IPointGeo, SpatialDouble.IPointGeo {

		public PointGeo(int aLatitudeE6, int aLongitudeE6, int aAltitude) {
			super(aLongitudeE6, aLatitudeE6, aAltitude);
			if (aLongitudeE6<MIN_LONGITUDE || aLongitudeE6>MAX_LONGITUDE ||
					aLatitudeE6<MIN_LATITUDE || aLatitudeE6>MAX_LATITUDE) {
				throw new IllegalArgumentException();
			}
		}

		public PointGeo(IPointGeo aPoint) {
			this(aPoint.getLatitudeE6(),aPoint.getLongitudeE6(),aPoint.getAltitudeE3());
		}

		public PointGeo(SpatialDouble.IPointGeo aPoint) {
			this((int)(aPoint.getLatitude()*1E6),(int)(aPoint.getLongitude()*1E6),(int)(aPoint.getAltitude()*1E3));
		}

		@Override
		public int getLatitudeE6() {
			return getYi();
		}

		@Override
		public int getLongitudeE6() {
			return getXi();
		}

		@Override
		public int getAltitudeE3() {
			return getZi();
		}

		@Override
		public String toString() {
			return "("+getLatitudeE6()+","+getLongitudeE6()+","+getAltitudeE3()+")";
		}

		/**
		 * Computes the great circle distance between two IPointGeos on the Earth.
		 * @return Returns the distance in meters.
		 */
		static public int calcDistance(IPointGeo aSrcPoint, IPointGeo aDstPoint) {
			SpatialDouble.IPointGeo theSrcDbl, theDstDbl;
			if (aSrcPoint instanceof SpatialDouble.IPointGeo) {
				theSrcDbl = (SpatialDouble.IPointGeo)aSrcPoint;
			} else {
				theSrcDbl = new SpatialDouble.PointGeo(aSrcPoint.getLatitudeE6()/1E6,
						aSrcPoint.getLongitudeE6()/1E6,aSrcPoint.getAltitudeE3()/1E3);
			}
			if (aDstPoint instanceof SpatialDouble.IPointGeo) {
				theDstDbl = (SpatialDouble.IPointGeo)aDstPoint;
			} else {
				theDstDbl = new SpatialDouble.PointGeo(aDstPoint.getLatitudeE6()/1E6,
						aDstPoint.getLongitudeE6()/1E6,aDstPoint.getAltitudeE3()/1E3);
			}
			return (int)(1E3*SpatialDouble.PointGeo.calcDistance(theSrcDbl,theDstDbl));
		}

		public int distanceTo(IPointGeo aGeoPoint) {
			return calcDistance(this,aGeoPoint);
		}

		@Override
		public double getX() {
			return getXi()/1E6;
		}

		@Override
		public double getY() {
			return getYi()/1E6;
		}

		@Override
		public double getZ() {
			return getZi()/1E3;
		}

		@Override
		public double getLatitude() {
			return getLatitudeE6()/1E6;
		}

		@Override
		public double getLongitude() {
			return getLongitudeE6()/1E6;
		}

		@Override
		public double getAltitude() {
			return getAltitudeE3()/1E3;
		}

	}

	static public class Vector2D extends Point2D implements IVector2D {
		protected int mHeadingE6;
		protected int mVelocityE3;

		public Vector2D(int aX, int aY, int aHeadingE6, int aVelocityE3) {
			super(aX, aY);
			mHeadingE6 = aHeadingE6;
			mVelocityE3 = aVelocityE3;
		}

		public Vector2D(IVector2D aVector) {
			this(aVector.getXi(),aVector.getYi(),aVector.getHeadingE6(),aVector.getVelocityE3());
		}

		@Override
		public int getHeadingE6() {
			return mHeadingE6;
		}

		@Override
		public int getVelocityE3() {
			return mVelocityE3;
		}

		@Override
		public String toString() {
			return super.toString()+"@("+getHeadingE6()+")>["+getVelocityE3()+"]";
		}

	}

	static public class Vector3D extends Point3D implements IVector3D {
		protected int mHeadingE6;
		protected int mPitchE6;
		protected int mVelocityE3;

		public Vector3D(int aX, int aY, int aZ, int aHeadingE6, int aPitchE6, int aVelocityE3) {
			super(aX, aY, aZ);
			mHeadingE6 = aHeadingE6;
			mPitchE6 = aPitchE6;
			mVelocityE3 = aVelocityE3;
		}

		public Vector3D(IVector3D aVector) {
			this(aVector.getXi(),aVector.getYi(),aVector.getZi(),aVector.getHeadingE6(),
					aVector.getPitchE6(),aVector.getVelocityE3());
		}

		@Override
		public int getHeadingE6() {
			return mHeadingE6;
		}

		@Override
		public int getPitchE6() {
			return mPitchE6;
		}

		@Override
		public int getVelocityE3() {
			return mVelocityE3;
		}

		@Override
		public String toString() {
			return super.toString()+"@("+getHeadingE6()+","+getPitchE6()+")>["+getVelocityE3()+"]";
		}

	}

	static public class VectorGeo extends PointGeo implements IVectorGeo {
		protected int mHeadingE6;
		protected int mPitchE6;
		protected int mVelocityE3;

		public VectorGeo(int aLatitudeE6, int aLongitudeE6, int aAltitudeE3,
				int aHeadingE6, int aPitchE6, int aVelocityE3) {
			super(aLongitudeE6, aLatitudeE6, aAltitudeE3);
			mHeadingE6 = aHeadingE6;
			mPitchE6 = aPitchE6;
			mVelocityE3 = aVelocityE3;
		}

		public VectorGeo(IVectorGeo aVector) {
			this(aVector.getLatitudeE6(),aVector.getLongitudeE6(),aVector.getAltitudeE3(),
					aVector.getHeadingE6(),aVector.getPitchE6(),aVector.getVelocityE3());
		}

		@Override
		public int getHeadingE6() {
			return mHeadingE6;
		}

		@Override
		public int getPitchE6() {
			return mPitchE6;
		}

		@Override
		public int getVelocityE3() {
			return mVelocityE3;
		}

		@Override
		public String toString() {
			return super.toString()+"@("+getHeadingE6()+","+getPitchE6()+")>["+getVelocityE3()+"]";
		}

	}

}
