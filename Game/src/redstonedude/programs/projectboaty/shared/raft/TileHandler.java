package redstonedude.programs.projectboaty.shared.raft;

import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;

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
		TextureHandler.loadTexture("Water_4", 128, 32, 32, 32);
		TextureHandler.loadTexture("Water_5", 160, 32, 32, 32);
		TextureHandler.loadTexture("Water_6", 192, 32, 32, 32);
		TextureHandler.loadTexture("Water_7", 224, 32, 32, 32);
		TextureHandler.loadTexture("Dude_0", 0, 64, 32, 32);
	}
	
	public static String getTextureName(Tile t) {
		if (t instanceof TileThruster) {
			TileThruster thruster = (TileThruster) t;
			if (thruster.thrustStrength != 0) {
				int index = ClientPhysicsHandler.c%3;
				return "TileThruster_" + index;
			}
			return "TileThruster_0";
		} else {
			return "TileWood";
		}
	}
	
}
