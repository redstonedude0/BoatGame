package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.Entity;

public class PacketNewEntity  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Entity entity;
	
	public PacketNewEntity(Entity e) {
		super();
		entity = e;
		packetID = "PacketNewEntity";
	}

}
