package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketUserData  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public PacketUserData() {
		super();
		packetID = "PacketUserData";
	}

}
