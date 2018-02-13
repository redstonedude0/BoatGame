package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.net.PacketRequestCharacterState;
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
		System.out.println("  PRCSU:" + prcs.currentTask.assignedEntityID);
		System.out.println("  PRCST:" + prcs.currentTask.taskTypeID);
		System.out.println("  PRCSC:" + prcs.currentTask.completed);
		ClientPacketHandler.sendPacket(prcs);
		//System.out.println("  UUIDS:" + uuid + ":" + currentTask.assignedEntityID);
	}
	
}
