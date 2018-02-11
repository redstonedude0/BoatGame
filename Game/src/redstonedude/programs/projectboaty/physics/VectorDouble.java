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
	
	//public VectorDouble multiply
}
