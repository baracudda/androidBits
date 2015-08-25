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
 * 2D, 3D, GeoSpatial, as well as the vectors of each. All values are Doubles.
 *
 * @author Ryan Fischbach
 */
public class SpatialDouble {
	/**
	 * The average radius spherical approximation of the Earth is ~6371.01 kilometers.
	 */
	static public final double RADIUS_EARTH_KM = 6371.01;

	static public interface IPoint2D {
		double getX();
		double getY();

	}

	static public interface IPoint3D extends IPoint2D {
		double getZ();
	}

	static final double MIN_LATITUDE = -90d;
	static final double MAX_LATITUDE =  90d;
	static final double MIN_LONGITUDE = -180d;
	static final double MAX_LONGITUDE =  180d;
	static final double MIN_LATITUDE_RAD = Math.toRadians(-90);
	static final double MAX_LATITUDE_RAD = Math.toRadians( 90);
	static final double MIN_LONGITUDE_RAD = Math.toRadians(-180);
	static final double MAX_LONGITUDE_RAD = Math.toRadians( 180);

	/**
	 * GeoPoints are planetary mapping coordinates and all functions relate to points on the
	 * surface of a sphere. Latitude indicates how far north or south of the equator one is,
	 * making it's range -90..+90. Longitude is how far east or west one is of the Prime Meridian,
	 * making it's range -180..+180.
	 *
	 */
	static public interface IPointGeo extends IPoint3D {
		/**
		 * Corresponds to getY().
		 * @return Returns latitude.
		 */
		double getLatitude();
		/**
		 * Corresponds to getX().
		 * @return Returns longitude.
		 */
		double getLongitude();
		/**
		 * Corresponds to getZ().
		 * @return Returns altitude in kilometers.
		 */
		double getAltitude();
	}

	static public interface IVector2D extends IPoint2D {
		/**
		 * Compass heading style.
		 * @return Returns heading in degrees.
		 */
		double getHeading();
		/**
		 * The vector's amplitude.
		 * @return Returns the velocity in kilometers per hour.
		 */
		double getVelocity();
	}


	static public interface IVector3D extends IVector2D, IPoint3D {
		/**
		 * Direction in 3D space requires heading and pitch, much like latitude and longitude.
		 * @return Returns the pitch.
		 */
		double getPitch();
	}

	static public interface IVectorGeo extends IVector3D, IPointGeo {

	}

	static public class Point2D implements IPoint2D {
		protected double mX;
		protected double mY;

		public Point2D(double aX, double aY) {
			super();
			mX = aX;
			mY = aY;
		}

		public Point2D(IPoint2D aPoint) {
			this(aPoint.getX(),aPoint.getY());
		}

		@Override
		public double getX() {
			return mX;
		}

		@Override
		public double getY() {
			return mY;
		}

		@Override
		public String toString() {
			return "("+getX()+","+getY()+")";
		}

	}

	static public class Point3D extends Point2D implements IPoint3D {
		protected double mZ;

		public Point3D(double aX, double aY, double aZ) {
			super(aX, aY);
			mZ = aZ;
		}

		public Point3D(IPoint3D aPoint) {
			this(aPoint.getX(),aPoint.getY(),aPoint.getZ());
		}

		@Override
		public double getZ() {
			return mZ;
		}

		@Override
		public String toString() {
			return "("+getX()+","+getY()+","+getZ()+")";
		}

	}

	static public class PointGeo extends Point3D implements IPointGeo {

		public PointGeo(double aLatitude, double aLongitude, double aAltitude) {
			super(aLongitude, aLatitude, aAltitude);
			if (aLongitude<MIN_LONGITUDE || aLongitude>MAX_LONGITUDE ||
					aLatitude<MIN_LATITUDE || aLatitude>MAX_LATITUDE) {
				throw new IllegalArgumentException();
			}
		}

		public PointGeo(IPointGeo aPoint) {
			this(aPoint.getLatitude(),aPoint.getLongitude(),aPoint.getAltitude());
		}

		@Override
		public double getLatitude() {
			return getY();
		}

		@Override
		public double getLongitude() {
			return getX();
		}

		@Override
		public double getAltitude() {
			return getZ();
		}

		@Override
		public String toString() {
			return "("+getLatitude()+","+getLongitude()+","+getAltitude()+")";
		}

		/**
		 * Computes the great circle distance between two IPointGeos.
		 * @param aSphereRadius - the radius of the sphere (planet) in kilometers.
		 * @return Returns the spherical surface distance in meters.
		 */
		static public double calcDistance(IPointGeo aFromPoint, IPointGeo aToPoint, double aSphereRadius) {
			if (aSphereRadius<0) {
				throw new IllegalArgumentException();
			}
			PointGeo theSrcPoint = deg2rad(aFromPoint);
			PointGeo theDstPoint = deg2rad(aToPoint);
			return Math.acos(
					Math.sin(theSrcPoint.getLatitude()) * Math.sin(theDstPoint.getLatitude()) +
					Math.cos(theSrcPoint.getLatitude()) * Math.cos(theDstPoint.getLatitude()) *
					Math.cos(theSrcPoint.getLongitude() - theDstPoint.getLongitude()) ) * aSphereRadius;
		}

		/**
		 * Computes the great circle distance between two IPointGeos on the Earth.
		 * @return Returns the distance in meters.
		 */
		static public double calcDistance(IPointGeo aFromPoint, IPointGeo aToPoint) {
			return calcDistance(aFromPoint,aToPoint,RADIUS_EARTH_KM);
		}

		public double distanceTo(IPointGeo aGeoPoint) {
			return calcDistance(this,aGeoPoint);
		}

		/**
		 * <p>Computes the bounding coordinates of all points on the surface
		 * of a sphere that have a great circle distance to the IPointGeo that
		 * is less or equal to the aDistance parameter.</p>
		 * <p>For more information about the formulae used in this method visit
		 * <a href="http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates">
		 * http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates</a>.</p>
		 * @param aPoint - center point.
		 * @param aDistance - the distance from the center point in kilometers.
		 * @param aSphereRadius - the radius of the sphere (planet) in kilometers.
		 * @return an array of two PointGeo objects such that:<ul>
		 * <li>The latitude of any point within the specified distance is
		 * &gt;= the latitude of the first array element and &lt;= the
		 * latitude of the second array element.</li>
		 * <li>If the longitude of the first array element is &lt;= the
		 * longitude of the second element, then the longitude of any point
		 * within the specified distance is &gt;= the longitude of the first
		 * array element and &lt;= the longitude of the second array element.</li>
		 * <li>If the longitude of the first array element is &gt; the
		 * longitude of the second element (this is the case if the 180th
		 * meridian is within the distance), then the longitude of any point
		 * within the specified distance is &gt;= to the longitude of the
		 * first array element <strong>OR</strong> &lt;= the longitude of the
		 * second array element.</li>
		 * </ul>
		 * SELECT * FROM my_table WHERE (result[0].lat<=lat AND lat<=result[1].lat) AND
		 *     (result[0].lng<=lng ((result[0].lng<=result[1].lng) ? AND : OR) lng<=result[1].lng)
		 */
		static public PointGeo[] calcBoundingCoordinates(IPointGeo aPoint, double aDistance, double aSphereRadius) {
			if (aSphereRadius<=0 || aDistance<0) {
				throw new IllegalArgumentException();
			}
			PointGeo theRadPoint = deg2rad(aPoint);
			// angular distance in radians on a great circle
			double theRadDist = aDistance/aSphereRadius;
			double theMinLat = theRadPoint.getLatitude() -theRadDist;
			double theMaxLat = theRadPoint.getLatitude() +theRadDist;
			double theMinLng, theMaxLng;
			if (theMinLat>MIN_LATITUDE_RAD && theMaxLat<MAX_LATITUDE_RAD) {
				double theDeltaLng = Math.asin(Math.sin(theRadDist) / Math.cos(theRadPoint.getLatitude()));
				theMinLng = theRadPoint.getLongitude() - theDeltaLng;
				if (theMinLng < MIN_LONGITUDE_RAD) {
					theMinLng += 2d * Math.PI;
				}
				theMaxLng = theRadPoint.getLongitude() + theDeltaLng;
				if (theMaxLng > MAX_LONGITUDE_RAD) {
					theMaxLng -= 2d * Math.PI;
				}
			} else {
				//a pole is within the distance
				theMinLat = Math.max(theMinLat, MIN_LATITUDE_RAD);
				theMaxLat = Math.min(theMaxLat, MAX_LATITUDE_RAD);
				theMinLng = MIN_LONGITUDE_RAD;
				theMaxLng = MAX_LONGITUDE_RAD;
			}

			return new PointGeo[] {
					new PointGeo(Math.toDegrees(theMinLat),Math.toDegrees(theMinLng),aPoint.getAltitude()),
					new PointGeo(Math.toDegrees(theMaxLat),Math.toDegrees(theMaxLng),aPoint.getAltitude()),
				};
		}

		static public PointGeo[] calcBoundingCoordinates(IPointGeo aPoint, double aDistance) {
			return calcBoundingCoordinates(aPoint, aDistance, RADIUS_EARTH_KM);
		}

		public PointGeo[] getBoundingCoordinates(double aDistance) {
			return calcBoundingCoordinates(this, aDistance);
		}

	}

	static public class Vector2D extends Point2D implements IVector2D {
		protected double mHeading;
		protected double mVelocity;

		public Vector2D(double aX, double aY, double aHeading, double aVelocity) {
			super(aX, aY);
			mHeading = aHeading;
			mVelocity = aVelocity;
		}

		public Vector2D(IVector2D aVector) {
			this(aVector.getX(),aVector.getY(),aVector.getHeading(),aVector.getVelocity());
		}

		@Override
		public double getHeading() {
			return mHeading;
		}

		@Override
		public double getVelocity() {
			return mVelocity;
		}

		@Override
		public String toString() {
			return super.toString()+"@("+getHeading()+")>["+getVelocity()+"]";
		}

	}

	static public class Vector3D extends Point3D implements IVector3D {
		protected double mHeading;
		protected double mPitch;
		protected double mVelocity;

		public Vector3D(double aX, double aY, double aZ, double aHeading, double aPitch, double aVelocity) {
			super(aX, aY, aZ);
			mHeading = aHeading;
			mPitch = aPitch;
			mVelocity = aVelocity;
		}

		public Vector3D(IVector3D aVector) {
			this(aVector.getX(),aVector.getY(),aVector.getZ(),aVector.getHeading(),
					aVector.getPitch(),aVector.getVelocity());
		}

		@Override
		public double getHeading() {
			return mHeading;
		}

		@Override
		public double getPitch() {
			return mPitch;
		}

		@Override
		public double getVelocity() {
			return mVelocity;
		}

		@Override
		public String toString() {
			return super.toString()+"@("+getHeading()+","+getPitch()+")>["+getVelocity()+"]";
		}

	}

	static public class VectorGeo extends PointGeo implements IVectorGeo {
		protected double mHeading;
		protected double mPitch;
		protected double mVelocity;

		public VectorGeo(double aLatitude, double aLongitude, double aAltitude,
				double aHeading, double aPitch, double aVelocity) {
			super(aLongitude, aLatitude, aAltitude);
			mHeading = aHeading;
			mPitch = aPitch;
			mVelocity = aVelocity;
		}

		public VectorGeo(IVectorGeo aVector) {
			this(aVector.getLatitude(),aVector.getLongitude(),aVector.getAltitude(),
					aVector.getHeading(),aVector.getPitch(),aVector.getVelocity());
		}

		@Override
		public double getHeading() {
			return mHeading;
		}

		@Override
		public double getPitch() {
			return mPitch;
		}

		@Override
		public double getVelocity() {
			return mVelocity;
		}

		@Override
		public String toString() {
			return super.toString()+"@("+getHeading()+","+getPitch()+")>["+getVelocity()+"]";
		}

	}

	/**
	 * Converts a point from degrees to radians.
	 * @param aPoint - a point defined in degrees.
	 * @return Returns the point converted to radians.
	 */
	static public PointGeo deg2rad(IPointGeo aPoint) {
		return new PointGeo(Math.toRadians(aPoint.getLatitude()),
				Math.toRadians(aPoint.getLongitude()),aPoint.getAltitude());
	}

	/**
	 * Converts a point from radians to degrees.
	 * @param aPoint - a point defined in radians.
	 * @return Returns the point converted to degrees.
	 */
	static public PointGeo rad2deg(IPointGeo aPoint) {
		return new PointGeo(Math.toDegrees(aPoint.getLatitude()),
				Math.toDegrees(aPoint.getLongitude()),aPoint.getAltitude());
	}

}
