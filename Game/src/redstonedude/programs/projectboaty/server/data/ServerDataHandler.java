package redstonedude.programs.projectboaty.server.data;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.server.physics.ServerUserData;
import redstonedude.programs.projectboaty.server.world.ServerWorldHandler;
import redstonedude.programs.projectboaty.shared.src.Logger;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;

public class ServerDataHandler {

	public static ArrayList<ServerUserData> savedUsers = new ArrayList<ServerUserData>();
	
	public static void saveData() {
		Path currentRelativePath = Paths.get("");
		File f = new File(currentRelativePath.toAbsolutePath().toString() + "/saves/save.dat");
		try {
			f.createNewFile();
			try (FileOutputStream fos = new FileOutputStream(f); ObjectOutputStream oos = new ObjectOutputStream(fos);) {
				for (ServerUserData sud: savedUsers) {
					oos.writeObject(sud);
				}
				oos.writeObject(new Long(WorldHandler.key));
				oos.flush();
			} catch (Exception e) {
				Logger.log("Failed save: " + e.getMessage());
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadData() {
		Path currentRelativePath = Paths.get("");
		File f = new File(currentRelativePath.toAbsolutePath().toString() + "/saves/save.dat");
		if (f.exists()) {
			try (FileInputStream fis = new FileInputStream(f); ObjectInputStream ois = new ObjectInputStream(fis);) {
				Object inputObject;
				while ((inputObject = ois.readObject()) != null) {
					if (inputObject instanceof ServerUserData) {
						Logger.log("loaded user data");
						ServerUserData sud = (ServerUserData) inputObject;
						savedUsers.add(sud);
					} else if (inputObject instanceof Long) {
						//its the worldgen key
						ServerWorldHandler.key = (Long) inputObject;
						Logger.log("loaded key");
					} else {
						Logger.log("unknown class error in loading");
					}
				}
				
			} catch (Exception e) {
				if (e instanceof EOFException) {
					Logger.log("Reached end of load file");
				} else {
					Logger.log("Failed load: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			Logger.log("No save, generating new world");
		}

	}

}