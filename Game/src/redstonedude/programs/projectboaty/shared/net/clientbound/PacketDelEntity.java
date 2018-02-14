package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Tile;

public class PacketDelEntity extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String uuid;
	
	public PacketDelEntity() {
		super("PacketDelEntity");
	}

}
