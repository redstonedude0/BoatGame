package redstonedude.programs.projectboaty.shared.physics;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;

public class PhysicsHandler {

	public static double getLengthOfACollidedAngularLineWithBoundaryLines(Point2D angularLineOrigin, double angle, double length, Collection<Line2D> boundaryLines) {
		// Calculate start and end X and Y positions of the line
		double baseX = angularLineOrigin.getX();
		double baseY = angularLineOrigin.getY();
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		double endX = baseX + sin * length;
		double endY = baseY + cos * length;
		for (Line2D l2d : boundaryLines) {
			double scalar = getCollisionScalarAbsolute(baseX, baseY, endX, endY, l2d.getP1().getX(), l2d.getP1().getY(), l2d.getP2().getX(), l2d.getP2().getY());
			length *= scalar;
			endX = baseX + sin * length;
			endY = baseY + cos * length;
		}
		// return the final length
		return length;
	}

	public static double getCollisionScalarAbsolute(double sstartX, double sstartY, double sendX, double sendY, double fstartX, double fstartY, double fendX, double fendY) {
		double ax = sendX - sstartX;
		double ay = sendY - sstartY;
		double bx = fendX - fstartX;
		double by = fendY - fstartY;
		return getCollisionScalarRelative(sstartX, sstartY, ax, ay, fstartX, fstartY, bx, by);
	}
	
	public static double getCollisionScalarRelative(double sstartX, double sstartY, double sdX, double sdY, double fstartX, double fstartY, double fdX, double fdY) {
        //Calculate the cross product of the 2 vectors, this gets a scalar representing how parallel the lines are
        double scalar = crossVectors(sdX, sdY, fdX, fdY);
        //If the scalar is 0 (the lines are parallel)
        if (scalar == 0) {
            //Return 1 as the line can remain the same length
            return 1;
        }
        //Get the values of u and t
        //t is how much the line S needs to be scaled to collide with line F (meet at a common endpoint)
        //u is how much the line F needs to be scaled to collide with line S (meet at a common endpoint)
        double t = crossVectors(fstartX - sstartX, fstartY - sstartY, fdX, fdY) / scalar;
        double u = crossVectors(fstartX - sstartX, fstartY - sstartY, sdX, sdY) / scalar;

        //If line F would normally pass through the common endpoint
        if (0 <= u && u <= 1) {
            //If line S needs to be scaled down to meet line F 
            if (0 <= t && t <= 1) {
                //Return the value by which it needs to be scaled
                return t;
            //Line S needs to be extended, or flipped to meet line F
            } else {
                //Return 1 since it will not normally meet
                return 1;
            }
        } else {
            //Return 1, line S passes past the end of line F
            return 1;
        }

    }
	
	public static double crossVectors(double ax, double ay, double bx, double by) {
        //Return the cross product of the vectors.
        return ax * by - ay * bx;
    }

}
