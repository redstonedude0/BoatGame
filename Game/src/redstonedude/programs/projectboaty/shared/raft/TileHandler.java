package redstonedude.programs.projectboaty.shared.raft;

import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.server.physics.PhysicsHandler;

public class TileHandler {
	
	public static void init() {
		TextureHandler.loadTexture("TileWood", 0, 0, 32, 32);
		TextureHandler.loadTexture("TileThruster_0", 32, 0, 32, 32);
		TextureHandler.loadTexture("TileThruster_1", 64, 0, 32, 32);
		TextureHandler.loadTexture("TileThruster_2", 96, 0, 32, 32);
		TextureHandler.loadTexture("Water_0", 0, 32, 32, 32);
		TextureHandler.loadTexture("Water_1", 32, 32, 32, 32);
		TextureHandler.loadTexture("Water_2", 64, 32, 32, 32);
		TextureHandler.loadTexture("Water_3", 96, 32, 32, 32);
	}
	
	public static String getTextureName(Tile t) {
		if (t instanceof TileThruster) {
			TileThruster thruster = (TileThruster) t;
			if (thruster.thrustStrength != 0) {
				int index = PhysicsHandler.c%3;
				return "TileThruster_" + index;
			}
			return "TileThruster_0";
		} else {
			return "TileWood";
		}
	}
	
}