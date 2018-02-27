package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketMoveRaft.class);
		setFieldViaReflection("pos", aInputStream.readObject(),PacketMoveRaft.class);
		setFieldViaReflection("theta", aInputStream.readDouble(),PacketMoveRaft.class);
		setFieldViaReflection("velocity", aInputStream.readObject(),PacketMoveRaft.class);
		setFieldViaReflection("dtheta", aInputStream.readDouble(),PacketMoveRaft.class);
		setFieldViaReflection("sin", aInputStream.readDouble(),PacketMoveRaft.class);
		setFieldViaReflection("cos", aInputStream.readDouble(),PacketMoveRaft.class);
		setFieldViaReflection("COMPos", aInputStream.readObject(),PacketMoveRaft.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
		aOutputStream.writeObject(pos);
		aOutputStream.writeDouble(theta);
		aOutputStream.writeObject(velocity);
		aOutputStream.writeDouble(dtheta);
		aOutputStream.writeDouble(sin);
		aOutputStream.writeDouble(cos);
		aOutputStream.writeObject(COMPos);
	}

}
