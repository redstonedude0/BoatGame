package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketNewUser  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String uuid;
	
	public PacketNewUser(String u) {
		super("PacketNewUser");
		uuid = u;
	}

}
