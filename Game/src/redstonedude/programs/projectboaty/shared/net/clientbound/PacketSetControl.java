package redstonedude.programs.projectboaty.shared.net.clientbound;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import redstonedude.programs.projectboaty.shared.net.Packet;

public class PacketSetControl extends Packet implements Serializable {

	private static final long serialVersionUID = 1L;

	public String uuid;

	public double requiredForwardTranslation = 0;
	public double requiredClockwiseRotation = 0;
	public double requiredRightwardTranslation = 0;

	public PacketSetControl() {
		super("PacketSetControl");
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("uuid", aInputStream.readUTF(),PacketSetControl.class);
		setFieldViaReflection("requiredForwardTranslation", aInputStream.readDouble(),PacketSetControl.class);
		setFieldViaReflection("requiredClockwiseRotation", aInputStream.readDouble(),PacketSetControl.class);
		setFieldViaReflection("requiredRightwardTranslation", aInputStream.readDouble(),PacketSetControl.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(uuid);
		aOutputStream.writeDouble(requiredForwardTranslation);
		aOutputStream.writeDouble(requiredClockwiseRotation);
		aOutputStream.writeDouble(requiredRightwardTranslation);
	}
	
}
