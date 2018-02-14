package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketConnect extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String uuid;
	public long key;
	
	public PacketConnect(String u, long k) {
		super("PacketConnect");
		uuid = u;
		key = k;
	}

}
