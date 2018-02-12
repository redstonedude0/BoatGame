package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketDebug extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public Object data;
	
	public PacketDebug(Object d) {
		super();
		data = d;
		packetID = "PacketDebug";
	}
	
}
