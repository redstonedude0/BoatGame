package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.net.Packet;

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
