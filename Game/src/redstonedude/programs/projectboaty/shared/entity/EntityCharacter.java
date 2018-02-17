package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestCharacterState;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.task.Task;

public class EntityCharacter extends Entity implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public String ownerUUID = "";
	public Task currentTask = null;
	public boolean carryingBarrel = false;
	
	public EntityCharacter() {
		super();
		entityTypeID = "EntityCharacter";
	}
	
	public void sendState() {
		PacketRequestCharacterState prcs = new PacketRequestCharacterState();
		prcs.carryingBarrel = carryingBarrel;
		prcs.characterUUID = uuid;
		prcs.currentTask = currentTask;
		ClientPacketHandler.sendPacket(prcs);
	}
	
	public void moveToward(Location loc) {
		
	}
	
}
