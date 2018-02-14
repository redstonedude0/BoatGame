package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.task.Task;

public class PacketRequestCharacterState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String characterUUID;
	public Task currentTask;
	public boolean carryingBarrel;
	
	public PacketRequestCharacterState() {
		super();
		packetID = "PacketRequestCharacterState";
	}

}