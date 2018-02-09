package redstonedude.programs.projectboaty.physics;

import java.util.ArrayList;

public class PendulumPhysicsHandler {
	
	public static int c = 0;
	
	public static double theta = Math.PI*5/6;//Math.PI/2;
	public static double omega = 0;
	public static int x = 200;
	public static int y = 200;
	public static int mount_x = 400;
	public static int mount_y = 100;
	public static double length = 200;
	public static double mass = 1;
	public static double g = 9.81;
	public static ArrayList<Double> measuredPeriods = new ArrayList<Double>();
	public static double currentPeriod;
	
	public static void physicsUpdate() {
		c++;
		//gravity acts at angle of theta = pi (acting clockwise)
		//a = Fr/mrr
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		double force = mass*g*sin;
		double force_r = length/2;
		
		double a = force*force_r/(mass*length*length);
		omega += a;
		theta += omega;
		
		cos = Math.cos(theta);
		sin = Math.sin(theta);
		int newx = (int) (mount_x+sin*length);
		y = (int) (mount_y-cos*length);
		
		currentPeriod++;
		if (newx < mount_x && x > mount_x) {
			//going from right to left across mount, 1 period
			measuredPeriods.add(currentPeriod);
			currentPeriod = 0;
		}
		x = newx;
		
	}
}
