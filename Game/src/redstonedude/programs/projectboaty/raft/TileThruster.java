package redstonedude.programs.projectboaty.raft;

import java.util.Vector;

public class TileThruster extends Tile {

	public double thrustAngle = 0;
	public double thrustStrength = 0;
	public static enum ControlType {//
		Right, Left, Uncontrolled
	}
	public ControlType controlType = ControlType.Uncontrolled;
	public double maxThrustStrength = 0.1;
	public double mass = 50;
	
	//vector pointing in direction of thrust
	//magnitude matches strength of thrust, relative to raft
	public Vector<Double> getRelativeThrustVector() {
		//take y unit vector and rotate it by thrustAngle
		//[cos -sin] [yx (0) ] = [-sinyy]
		//[sin  cos] [yy (1)]   [cosyy]
		double cos = Math.cos(thrustAngle);
		double sin = Math.sin(thrustAngle);
		Vector<Double> v = new Vector<Double>();
		v.add(-sin*thrustStrength);
		v.add(cos*thrustStrength);
		//System.out.println(v.get(0) + ":" + v.get(1));
		return v;
	}
	
	public Vector<Double> getAbsoluteThrustVector(Raft parent) {
		Vector<Double> unity = parent.getUnitY();
		Vector<Double> unitx = parent.getUnitX();
		Vector<Double> v = new Vector<Double>();
		Vector<Double> relativeThrust = getRelativeThrustVector();
		v.add(relativeThrust.get(0)*unitx.get(0)+relativeThrust.get(1)*unity.get(0));
		v.add(relativeThrust.get(0)*unitx.get(1)+relativeThrust.get(1)*unity.get(1));
		return v;
	}
	
	//vector pointing against direction of thrust, absolute
	public Vector<Double> getDrawnThrustVector(Raft parent) {
		Vector<Double> unity = parent.getUnitY();
		//take y unit vector and rotate it by thrustAngle
		//[cos -sin] [yx] = [cosyx-sinyy]
		//[sin  cos] [yy]   [sinyx+cosyy]
		double cos = Math.cos(thrustAngle);
		double sin = Math.sin(thrustAngle);
		Vector<Double> v = new Vector<Double>();
		v.add((-cos*unity.get(0)+sin*unity.get(1))*thrustStrength*10);
		v.add((-sin*unity.get(0)-cos*unity.get(1))*thrustStrength*10);
		return v;
	}
}
