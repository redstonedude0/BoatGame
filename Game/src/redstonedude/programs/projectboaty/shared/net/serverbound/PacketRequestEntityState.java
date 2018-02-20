package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketRequestEntityState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Entity entity;
	
	public PacketRequestEntityState(Entity e) {
		super("PacketRequestEntityState");
		entity = e;
	}

}
