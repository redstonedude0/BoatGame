package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

public class PacketRequestRaft  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public int raftID;
	
	public PacketRequestRaft(int id) {
		super();
		packetID = "PacketRequestRaft";
		raftID = id;
	}

}
