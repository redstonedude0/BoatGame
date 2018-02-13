package redstonedude.programs.projectboaty.shared.net.serverbound;

import java.io.Serializable;

import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketRequestMoveRaft extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public VectorDouble pos = new VectorDouble();
	public double theta = 0;
	public VectorDouble velocity = new VectorDouble();
	public double dtheta = 0;
	
	public double sin = 0;
	public double cos = 1;
	public VectorDouble COMPos = new VectorDouble();
	
	public PacketRequestMoveRaft() {
		super();
		packetID = "PacketRequestMoveRaft";
	}

}
