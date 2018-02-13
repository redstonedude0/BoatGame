package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketDelUser  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String uuid;
	
	public PacketDelUser(String u) {
		super();
		uuid = u;
		packetID = "PacketDelUser";
	}

}
