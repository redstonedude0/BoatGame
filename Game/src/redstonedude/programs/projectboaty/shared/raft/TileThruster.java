package redstonedude.programs.projectboaty.shared.raft;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;

public class TileThruster extends Tile {

	private static final long serialVersionUID = 1L;
	public double thrustAngle = 0;
	public double thrustStrength = 0;
	public double maxThrustStrength = 0.1;
	
	public TileThruster() {
		mass = 50;
	}
	
	//vector pointing in direction of thrust
	//magnitude matches strength of thrust, relative to raft
	public VectorDouble getRelativeThrustVector() {
		//take y unit vector and rotate it by thrustAngle, then apply magnitude
		VectorDouble v = new VectorDouble(0,1);
		v = v.rotate(thrustAngle);
		v = v.multiply(thrustStrength); 
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
	
	/**
	 * set thrust strength based on control
	 * @param parent
	 */
	public void setThrustStrength(Raft parent) {
		//w alone will set up thrusts to thrust forward
		//d alone will set up thrusts to clockwise rotation
		//w and d will set up thrusts to forward and clockwise
		//e alone will set up thrusts for rightward translation
		double requiredForwardTranslation = 0;//0 unrequired, 1 forward, -1 backward
		double requiredClockwiseRotation = 0;
		double requiredRightwardTranslation = 0;
		
		if (ControlHandler.control_forward) {
			requiredForwardTranslation++;
		}
		if (ControlHandler.control_backward) {
			requiredForwardTranslation--;
		}
		if (ControlHandler.control_right_rotate) {
			requiredClockwiseRotation++;
		}
		if (ControlHandler.control_left_rotate) {
			requiredClockwiseRotation--;
		}
		if (ControlHandler.control_right_translate) {
			requiredRightwardTranslation++;
		}
		if (ControlHandler.control_left_translate) {
			requiredRightwardTranslation--;
		}
		
		//get vectors for this thruster if it were at max
		thrustStrength = maxThrustStrength;
		VectorDouble thrust = getRelativeThrustVector();
		double forwardTranslation = thrust.y;
		double rightwardTranslation = thrust.x;
		//multiply to see if it is useful in that direction
		forwardTranslation *= requiredForwardTranslation;//0 if not caring, positive if helpful
		rightwardTranslation *= requiredRightwardTranslation;//0 if not caring, positive if helpful
		VectorDouble dpos = getPos().add(new VectorDouble(0.5, 0.5)).subtract(parent.getCOMPos());//this is relative dpos, calculate absolute
		double clockwiseMoments = 0;
		clockwiseMoments += thrust.x*-dpos.y;
		clockwiseMoments += thrust.y*-dpos.x;
		clockwiseMoments *= requiredClockwiseRotation;//0 if not caring, positive if helpful
		
		if (forwardTranslation == 0 && rightwardTranslation == 0 && clockwiseMoments == 0) {
			thrustStrength = 0;
			return; //no control input
		}
		if (forwardTranslation >= 0 && rightwardTranslation >= 0 && clockwiseMoments >= 0) {
			thrustStrength = maxThrustStrength;
			return;
		}
		if (forwardTranslation <= 0 && rightwardTranslation <= 0 && clockwiseMoments <= 0) {
			thrustStrength = -maxThrustStrength;
			return;
		}
		thrustStrength = 0;
		
		
	}
	
}
