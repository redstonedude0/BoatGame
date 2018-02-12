package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String packetID = "";
	public Object data;
	
	public Packet(String id, Object d) {
		packetID = id;
		data = d;
	}
	
}
