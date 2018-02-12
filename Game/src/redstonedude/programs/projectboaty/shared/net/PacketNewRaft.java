package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

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
