package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.task.Task;

public class PacketCharacterState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String characterUUID;
	public Task currentTask;
	public boolean carryingBarrel;
	
	public PacketCharacterState() {
		super();
		packetID = "PacketCharacterState";
	}

}
