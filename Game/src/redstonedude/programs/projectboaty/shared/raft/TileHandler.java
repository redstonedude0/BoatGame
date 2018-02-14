package redstonedude.programs.projectboaty.shared.raft;

import redstonedude.programs.projectboaty.client.graphics.TextureHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.entity.EntityCharacter;

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
		TextureHandler.loadTexture("Character_Normal", 0, 64, 32, 32);//probably need a more standard name than 'Dude', PCE? Character? Seaman?
		TextureHandler.loadTexture("Character_Barrel", 32, 64, 32, 32);
		TextureHandler.loadTexture("Island", 128, 0, 32, 32);
		TextureHandler.loadTexture("Barrel", 160, 0, 32, 32);
		TextureHandler.loadTexture("TileConstruction", 192, 0, 32, 32);
		TextureHandler.loadTexture("TileDamage_25", 64, 64, 32, 32);
		TextureHandler.loadTexture("TileDamage_50", 96, 64, 32, 32);
		TextureHandler.loadTexture("TileDamage_75", 128, 64, 32, 32);
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
	
	public static String getTextureName(String entityName, Entity e) {
		if (entityName.equals("EntityCharacter")) {
			EntityCharacter ec = (EntityCharacter) e;
			if (ec.carryingBarrel) {
				return "Character_Barrel";
			} else {
				return "Character_Normal";
			}
		} else if (entityName.equals("EntityBarrel")) {
			return "Barrel";
		}
		return "Barrel"; //if something breaks atleast return a texture
	}
	
}
