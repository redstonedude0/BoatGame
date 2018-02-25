package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.physics.Location;

public class PacketRequestMoveCharacter extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public Location loc;
	public String uuid = "";
	
	public PacketRequestMoveCharacter() {
		super("PacketRequestMoveCharacter");
	}

}
