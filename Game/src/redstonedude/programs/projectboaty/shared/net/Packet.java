package redstonedude.programs.projectboaty.shared.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

public class Packet implements Serializable {

	private static final long serialVersionUID = 1L;
	public final String packetID;

	public Packet(String id) {
		packetID = id;
	}
	
	public void setFieldViaReflection(String fieldName, Object fieldValue, Class<? extends Packet> packetClass) {
		try {
			// packetID = aInputStream.readUTF();
			Field testField = packetClass.getDeclaredField(fieldName);
			boolean wasAccessible = testField.isAccessible();
			testField.setAccessible(true);
			testField.set(this, fieldValue);
			testField.setAccessible(wasAccessible);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		setFieldViaReflection("packetID", aInputStream.readUTF(),Packet.class);
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeUTF(packetID);
	}

}
