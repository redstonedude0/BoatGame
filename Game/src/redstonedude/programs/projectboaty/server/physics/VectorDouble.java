package redstonedude.programs.projectboaty.server.physics;

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
		VectorDouble ans = new VectorDouble(this);
		ans.x -= v.x;
		ans.y -= v.y;
		return ans;
	}
	
	public VectorDouble multiply(double d) {
		VectorDouble ans = new VectorDouble(this);
		ans.x *= d;
		ans.y *= d;
		return ans;
	}
	
	public VectorDouble divide(double d) {
		VectorDouble ans = new VectorDouble(this);
		ans.x /= d;
		ans.y /= d;
		return ans;
	}
	
	public double getSquaredLength() {
		return x*x + y*y;
	}
	
	public VectorDouble rotate(double theta) {
		VectorDouble ans = new VectorDouble();
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		//[cos -sin] [yx (0) ] = [cosyx-sinyy]
		//[sin  cos] [yy (1)]   [sinyx+cosyy]
		ans.x = cos*x-sin*y;
		ans.y = sin*x+cos*y;
		return ans;
	}
	
	/**
	 * DOESNT SET MAGNITUDE, MUST STILL USE EQUALS OPERATOR
	 * @param mag
	 * @return
	 */
	public VectorDouble setMagnitude(double mag) {
		VectorDouble ans = new VectorDouble(this);
		double currentMag = Math.sqrt(getSquaredLength());
		ans.x /= currentMag;
		ans.y /= currentMag;
		ans.x *= mag;
		ans.y *= mag;
		return ans;
	}
	
	public VectorDouble getAbsolute(VectorDouble unitx, VectorDouble unity) {
		VectorDouble ans = new VectorDouble();
		ans.x = x*unitx.x+y*unity.x;
		ans.y = x*unitx.y+y*unity.y;
		return ans;
	}
	
	
	
}
