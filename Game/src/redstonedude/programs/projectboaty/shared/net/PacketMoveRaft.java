package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketMoveRaft extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public PacketMoveRaft() {
		super();
		packetID = "PacketMoveRaft";
	}

}
