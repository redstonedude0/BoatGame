package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketConnect extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String uuid;
	public long key;
	
	public PacketConnect(String u, long k) {
		super();
		packetID = "PacketConnect";
		uuid = u;
		key = k;
	}

}
