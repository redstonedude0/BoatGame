package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketNewEntity  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public Entity entity;
	
	public PacketNewEntity(Entity e) {
		super("PacketNewEntity");
		entity = e;
	}

}
