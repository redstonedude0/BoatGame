package redstonedude.programs.projectboaty.raft;

import redstonedude.programs.projectboaty.physics.VectorDouble;

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
	public VectorDouble getRelativeThrustVector() {
		//take y unit vector and rotate it by thrustAngle
		//[cos -sin] [yx (0) ] = [-sinyy]
		//[sin  cos] [yy (1)]   [cosyy]
		double cos = Math.cos(thrustAngle);
		double sin = Math.sin(thrustAngle);
		VectorDouble v = new VectorDouble();
		v.x = -sin*thrustStrength;
		v.y = cos*thrustStrength;
		//System.out.println(v.get(0) + ":" + v.get(1));
		return v;
	}
	
	public VectorDouble getAbsoluteThrustVector(Raft parent) {
		VectorDouble unity = parent.getUnitY();
		VectorDouble unitx = parent.getUnitX();
		VectorDouble v = new VectorDouble();
		VectorDouble relativeThrust = getRelativeThrustVector();
		v.x = relativeThrust.x*unitx.x+relativeThrust.y*unity.x;
		v.y = relativeThrust.x*unitx.y+relativeThrust.y*unity.y;
		return v;
	}
	
	//vector pointing against direction of thrust, absolute
	public VectorDouble getDrawnThrustVector(Raft parent) {
		VectorDouble unity = parent.getUnitY();
		//take y unit vector and rotate it by thrustAngle
		//[cos -sin] [yx] = [cosyx-sinyy]
		//[sin  cos] [yy]   [sinyx+cosyy]
		double cos = Math.cos(thrustAngle);
		double sin = Math.sin(thrustAngle);
		VectorDouble v = new VectorDouble();
		v.x = (-cos*unity.x+sin*unity.y)*thrustStrength*10;//magnify to make visible
		v.y = (-sin*unity.x-cos*unity.y)*thrustStrength*10;
		return v;
	}
}
