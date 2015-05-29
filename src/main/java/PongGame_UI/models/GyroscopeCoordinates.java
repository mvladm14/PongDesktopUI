package PongGame_UI.models;

import java.util.Objects;

import com.fluentinterface.ReflectionBuilder;
import com.fluentinterface.builder.Builder;

public class GyroscopeCoordinates {

	private double x;
	private double y;
	private double z;
	
	public static GryoscopeCoordinatesBuilder create() {
		return ReflectionBuilder.implementationFor(GryoscopeCoordinatesBuilder.class).create();
	}

	public interface GryoscopeCoordinatesBuilder extends Builder<GyroscopeCoordinates> {
		public GryoscopeCoordinatesBuilder withX(double x);
		public GryoscopeCoordinatesBuilder withY(double y);
		public GryoscopeCoordinatesBuilder withZ(double z);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setZ(double z) {
		this.z = z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getX(), getY(), getZ());
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof GyroscopeCoordinates)
				&& Objects.equals(getX(), ((GyroscopeCoordinates) obj).getX())
				&& Objects.equals(getY(), ((GyroscopeCoordinates) obj).getY())
				&& Objects.equals(getZ(), ((GyroscopeCoordinates) obj).getZ());
	}
}
