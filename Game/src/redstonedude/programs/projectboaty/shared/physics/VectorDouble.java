package redstonedude.programs.projectboaty.shared.physics;

import java.io.Serializable;

public class VectorDouble implements Serializable {

	private static final long serialVersionUID = 1L;

	public double x = 0;
	public double y = 0;

	public VectorDouble() {
	}

	public VectorDouble(double x1, double y1) {
		x = x1;
		y = y1;
	}

	public VectorDouble(VectorDouble v) {
		this(v.x, v.y);
	}

	public VectorDouble add(VectorDouble v) {
		VectorDouble ans = new VectorDouble(this);
		ans.x += v.x;
		ans.y += v.y;
		return ans;
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
		if (d == 0) {
			return new VectorDouble(0,0);
		}
		ans.x /= d;
		ans.y /= d;
		return ans;
	}

	public double getSquaredLength() {
		return x * x + y * y;
	}

	public VectorDouble rotate(double theta) {
		VectorDouble ans = new VectorDouble();
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		// [cos -sin] [yx (0) ] = [cosyx-sinyy]
		// [sin cos] [yy (1)] [sinyx+cosyy]
		ans.x = cos * x - sin * y;
		ans.y = sin * x + cos * y;
		return ans;
	}

	/**
	 * DOESNT SET MAGNITUDE, MUST STILL USE EQUALS OPERATOR
	 * 
	 * @param mag
	 * @return
	 */
	public VectorDouble setMagnitude(double mag) {
		//handle 0 and error case
		if (mag == 0) {
			return new VectorDouble(0,0);
		}
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
		ans.x = x * unitx.x + y * unity.x;
		ans.y = x * unitx.y + y * unity.y;
		return ans;
	}

	public VectorDouble getRelative(VectorDouble unitx, VectorDouble unity) {
		VectorDouble ans = new VectorDouble();
		// [Xx Yx][Rx] = [Ax]
		// [Xy Yy][Ry] = [Ay]

		// 1/det[YY -Yx][Ax] = [Rx]
		// [-Xy Xx][Ay] = [Ry]
		double a = unitx.x;
		double b = unity.x;
		double c = unitx.y;
		double d = unity.y;
		double det = a * d - b * c;
		ans.x = x * d - y * b;
		ans.y = -x * c + y * a;
		ans = ans.divide(det);
		return ans;
	}

	@Override
	public boolean equals(Object obj) {
		VectorDouble vd = (VectorDouble) obj;
		if (x == vd.x && y == vd.y) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return x + "," + y;
	}
}
