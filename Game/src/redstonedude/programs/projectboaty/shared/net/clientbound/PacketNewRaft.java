package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.raft.Raft;

public class PacketNewRaft  extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String uuid;
	public Raft raft;
	
	public PacketNewRaft(String u, Raft r) {
		super();
		packetID = "PacketNewRaft";
		uuid = u;
		raft = r;
	}

}
