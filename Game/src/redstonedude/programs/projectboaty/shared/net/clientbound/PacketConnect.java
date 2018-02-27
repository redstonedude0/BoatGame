package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class PacketConnect extends Packet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public String uuid;
	public long key;
	public VectorDouble wind;
	
	public PacketConnect(String u, long k, VectorDouble w) {
		super("PacketConnect");
		uuid = u;
		key = k;
		wind = w;
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketConnect.class);
		setFieldViaReflection("key", aInputStream.readLong(),PacketConnect.class);
		setFieldViaReflection("wind", aInputStream.readObject(),PacketConnect.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
		aOutputStream.writeLong(key);
		aOutputStream.writeObject(wind);
	}

}
