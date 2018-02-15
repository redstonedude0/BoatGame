package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.entity.Entity;
import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketEntityState extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Entity entity;
	
	public PacketEntityState(Entity t) {
		super("PacketEntityState");
		entity = t;
	}

}
