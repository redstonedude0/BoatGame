package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketNewUser  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String uuid;
	
	public PacketNewUser(String u) {
		super();
		uuid = u;
		packetID = "PacketNewUser";
	}

}
