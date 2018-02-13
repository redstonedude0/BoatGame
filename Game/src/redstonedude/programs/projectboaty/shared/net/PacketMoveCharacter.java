package redstonedude.programs.projectboaty.shared.net;

import java.io.Serializable;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;

public class PacketMoveCharacter extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public VectorDouble pos = new VectorDouble();
	public boolean absolutePos;
	public String raftPosID;
	public String uuid = "";

	public PacketMoveCharacter() {
		super();
		packetID = "PacketMoveCharacter";
	}

}
