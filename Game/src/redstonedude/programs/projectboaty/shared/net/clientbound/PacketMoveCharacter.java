package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class PacketMoveCharacter extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public VectorDouble pos = new VectorDouble();
	public boolean absolutePos;
	public String raftPosID;
	public String uuid = "";

	public PacketMoveCharacter() {
		super("PacketMoveCharacter");
	}

}
