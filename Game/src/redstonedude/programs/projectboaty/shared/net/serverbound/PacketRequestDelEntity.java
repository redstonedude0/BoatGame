package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketRequestDelEntity extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public String uuid;

	public PacketRequestDelEntity(String u) {
		super("PacketRequestDelEntity");
		uuid = u;
	}

}
