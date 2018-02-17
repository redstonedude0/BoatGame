package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class PacketRequestMoveCharacter extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public VectorDouble pos = new VectorDouble();
	public boolean absolutePos;
	public String raftPosID;
	public String uuid = "";
	
	public PacketRequestMoveCharacter() {
		super("PacketRequestMoveCharacter");
	}

}
