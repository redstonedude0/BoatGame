package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class PacketMoveCharacter extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public String uuid = "";
	public Location loc;

	public PacketMoveCharacter() {
		super("PacketMoveCharacter");
	}

}
