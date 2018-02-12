package redstonedude.programs.projectboaty.server.world;

import java.util.Random;

import redstonedude.programs.projectboaty.shared.world.WorldHandler;

public class ServerWorldHandler extends WorldHandler {
	
	public static void init() {
		if (key == 0) {
			Random rand = new Random(System.currentTimeMillis());
			key = rand.nextLong();
		}
	}
	
}
