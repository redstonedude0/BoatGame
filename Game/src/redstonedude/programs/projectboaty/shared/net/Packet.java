package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public final String packetID;
	
	public Packet(String id) {
		packetID = id;
	}
	
	
}
