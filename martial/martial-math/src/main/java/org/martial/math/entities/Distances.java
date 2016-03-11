package org.martial.math.entities;

public class Distances {
	private PointWritable point;
	private double distance;

	public Distances(PointWritable point, double distance) {
		this.point = point;
		this.distance = distance;
	}

	public PointWritable getPoint() {
		return point;
	}

	public void setPoint(PointWritable point) {
		this.point = point;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

}
