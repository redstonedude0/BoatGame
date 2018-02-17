package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class PacketMoveRaft extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public String uuid;

	public VectorDouble pos = new VectorDouble();
	public double theta = 0;
	public VectorDouble velocity = new VectorDouble();
	public double dtheta = 0;

	public double sin = 0;
	public double cos = 1;
	public VectorDouble COMPos = new VectorDouble();

	public PacketMoveRaft() {
		super("PacketMoveRaft");
	}

}
