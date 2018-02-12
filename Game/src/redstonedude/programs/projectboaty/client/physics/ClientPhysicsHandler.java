package redstonedude.programs.projectboaty.client.physics;

import redstonedude.programs.projectboaty.client.control.ControlHandler;
import redstonedude.programs.projectboaty.client.control.ControlHandler.Mode;
import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.server.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.raft.Raft;

public class ClientPhysicsHandler {
	// consider making this do all the physics for local boats perhaps?

	public static VectorDouble cameraPosition = new VectorDouble(0, 0);
	public static int c = 0;

	public static void physicsUpdate() {
		if (ControlHandler.mode == Mode.Playing) {
			c++;
			for (UserData ud : ClientPacketHandler.userData) {
				physicsUpdate(ud);
			}
			// move camera accordingly
			UserData currentUser = ClientPacketHandler.getCurrentUserData();
			if (currentUser != null && currentUser.raft != null) {
				VectorDouble posDiff = currentUser.raft.getCOMPos().getAbsolute(currentUser.raft.getUnitX(), currentUser.raft.getUnitY()).add(currentUser.raft.getPos()).subtract(ClientPhysicsHandler.cameraPosition);
				posDiff = posDiff.divide(10);// do it slower
				ClientPhysicsHandler.cameraPosition = ClientPhysicsHandler.cameraPosition.add(posDiff);
			}
		}
	}

	public static void physicsUpdate(UserData sud) {
		Raft raft = sud.raft;
		if (raft != null) {
			raft.setPos(raft.getPos().add(raft.getVelocity()));
			raft.theta += raft.dtheta;
		}
	}

}
