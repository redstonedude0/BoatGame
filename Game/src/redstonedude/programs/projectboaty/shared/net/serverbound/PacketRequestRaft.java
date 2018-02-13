package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketRequestRaft  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public int raftID;
	
	public PacketRequestRaft(int id) {
		super();
		packetID = "PacketRequestRaft";
		raftID = id;
	}

}
