package redstonedude.programs.projectboaty.shared.world;

import java.util.Random;

import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class WorldHandler {
	
	public static long key = 0;
	private static VectorDouble wind;
	
	public static enum TerrainType {
		Water(0.2, 0), Land(5, 50);
		
		public double frictionCoefficient;
		public double damageCoefficient; //1m will do this much damage
		
		TerrainType(double f, double d) {
			frictionCoefficient = f;
			damageCoefficient = d;
		}
		
	};
	
	public static VectorDouble getWind() {
		if (wind == null) {
			return null;
		}
		return new VectorDouble(wind);
	}
	
	public static void setWind(VectorDouble w) {
		wind = w;
	}
	
	public static TerrainType getTerrainType(double x, double y) {
		if (getTerrainHeight(x, y) >= 0.8) {
			return TerrainType.Land;
		} else {
			return TerrainType.Water;
		}
	}
	
	public static double getTerrainHeight(double x, double y) {
		x = Math.floor(x);
		y = Math.floor(y);
		//generates in 16x16 chunks.
		//do in x direction
		double xHeight = get1DHeightAtBlock(x, 0);
		double yHeight = get1DHeightAtBlock(y, 5000);
		return (xHeight+yHeight)/2;
	}
	
	public static double get1DHeightAtBlock(double x, double salt) {
		double aLocation = Math.floor(x/16);
		double percentage = x-aLocation*16;
		percentage /= 16;
		aLocation += salt;
		double beforeA = getHeightAtChunk(aLocation-1);
		double atA = getHeightAtChunk(aLocation);
		double atB = getHeightAtChunk(aLocation+1);
		double afterB = getHeightAtChunk(aLocation+2);
		//System.out.println(beforeA + ":" + atA + ":" + atB + ":" + afterB);
		return interpolateNumber(beforeA, atA, atB, afterB, percentage);
	}
	
	
	private static double getHeightAtChunk(double value) {
		Random rand = new Random(key+(long) value*1000);
		return rand.nextDouble();
	}
	
	public static double interpolateNumber(double beforeA, double a, double b, double afterB,double x/*A to b, 0 to 1*/) {
		double P = (afterB - b) - (beforeA - a);
		double Q = (beforeA - a) - P;
		double R = b - beforeA;
		double S = a;

		return P*x*x*x + Q*x*x + R*x + S;
	}
	
}
