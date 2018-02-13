package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketUserData  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public PacketUserData() {
		super();
		packetID = "PacketUserData";
	}

}
