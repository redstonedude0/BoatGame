package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketConnect extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String uuid;
	public long key;
	public VectorDouble wind;
	
	public PacketConnect(String u, long k, VectorDouble w) {
		super("PacketConnect");
		uuid = u;
		key = k;
		wind = w;
	}

}
