package redstonedude.programs.projectboaty.server.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import redstonedude.programs.projectboaty.server.physics.ServerUserData;
import redstonedude.programs.projectboaty.server.world.ServerWorldHandler;
import redstonedude.programs.projectboaty.shared.src.Logger;

public class ServerDataHandler {

	public static ArrayList<ServerUserData> savedUsers = new ArrayList<ServerUserData>();
	
	public static void saveData() {
		Path currentRelativePath = Paths.get("");
		File f = new File(currentRelativePath.toAbsolutePath().toString() + "/saves/save.dat");
		try {
			f.createNewFile();
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
						ServerUserData sud = (ServerUserData) inputObject;
						savedUsers.add(sud);
					} else if (inputObject instanceof Long) {
						//its the worldgen key
						ServerWorldHandler.key = (Long) inputObject;
					} else {
						Logger.log("unknown class error in loading");
					}
				}
				
			} catch (Exception e) {
				Logger.log("Failed load: " + e.getMessage());
			}
		} else {
			Logger.log("No save, generating new world");
		}

	}

}
