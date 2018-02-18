package redstonedude.programs.projectboaty.server.world;

import java.util.Random;

import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;

public class ServerWorldHandler extends WorldHandler {
	
	public static void init() {
		Random rand = new Random(System.currentTimeMillis());
		if (key == 0) {
			key = rand.nextLong();
		}
		if (getWind() == null) {
			double theta = rand.nextFloat()*Math.PI*2;//generate random angle
			VectorDouble win = new VectorDouble(0,1).rotate(theta).setMagnitude(0.01);
			setWind(win);
		}
	}
	
}
