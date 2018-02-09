package redstonedude.programs.projectboaty.raft;

import redstonedude.programs.projectboaty.control.ControlHandler;
import redstonedude.programs.projectboaty.graphics.TextureHandler;
import redstonedude.programs.projectboaty.physics.PhysicsHandler;
import redstonedude.programs.projectboaty.raft.TileThruster.ControlType;

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
			if (thruster.controlType == ControlType.Left && ControlHandler.control_left ||  thruster.controlType == ControlType.Right && ControlHandler.control_right) {
				int index = PhysicsHandler.c%3;
				return "TileThruster_" + index;
			}
			return "TileThruster_0";
		} else {
			return "TileWood";
		}
	}
	
}
