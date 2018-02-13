package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketRequestNewCharacter  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public PacketRequestNewCharacter() {
		super();
		packetID = "PacketRequestNewCharacter";
	}

}
