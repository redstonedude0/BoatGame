package redstonedude.programs.projectboaty.physics;

public class VectorDouble {
	
	public double x = 0;
	public double y = 0;
	
	public VectorDouble() {
		
	}
	
	public VectorDouble(double x1, double y1) {
		x = x1;
		y = y1;
	}
	
	public VectorDouble(VectorDouble v) {
		this(v.x,v.y);
	}
	
	/**
	 * Add another vector to this vector
	 * @param v the vector to add to this vector
	 */
	public VectorDouble add(VectorDouble v) {
		x += v.x;
		y += v.y;
		return this;
	}
	
	public VectorDouble subtract(VectorDouble v) {
		x -= v.x;
		y -= v.y;
		return this;
	}
	
	public void multiply(double d) {
		x *= d;
		y *= d;
	}
	
	public void divide(double d) {
		x /= d;
		y /= d;
	}
	
	public double getSquaredLength() {
		return x*x + y*y;
	}
	
	public void rotate(double theta) {
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		//[cos -sin] [yx (0) ] = [cosyx-sinyy]
		//[sin  cos] [yy (1)]   [sinyx+cosyy]
		double x2 = cos*x-sin*y;
		double y2 = sin*x+cos*y;
		x = x2;
		y = y2;
	}
	
	public void setMagnitude(double mag) {
		double currentMag = Math.sqrt(getSquaredLength());
		x /= currentMag;
		y /= currentMag;
		x *= mag;
		y *= mag;
	}
	
	
	
}
