package redstonedude.programs.projectboaty.shared.entity;

import java.io.Serializable;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.shared.event.EventCharacterMountDismount;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.net.serverbound.PacketRequestCharacterState;
import redstonedude.programs.projectboaty.shared.physics.Location;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;
import redstonedude.programs.projectboaty.shared.raft.Tile;
import redstonedude.programs.projectboaty.shared.task.Task;
import redstonedude.programs.projectboaty.shared.world.WorldHandler;
import redstonedude.programs.projectboaty.shared.world.WorldHandler.TerrainType;

public class EntityCharacter extends Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	public String ownerUUID = "";
	public Task currentTask = null;
	public EntityResource carrying;
	public double walkSpeed = 0.1; // unchanged currently but can be changed
	public double swimSpeed = 0.08;

	public EntityCharacter() {
		super();
		entityTypeID = "EntityCharacter";
	}

	public void sendState() {
		PacketRequestCharacterState prcs = new PacketRequestCharacterState();
		prcs.carrying = carrying;
		prcs.characterUUID = uuid;
		prcs.currentTask = currentTask;
		ClientPacketHandler.sendPacket(prcs);
	}
	
	@Override
	public void setLoc(Location loc) {
		boolean absBefore = isAbsolute();
		String raftUUIDBefore = getLoc().raftUUID;
		super.setLoc(loc);
		if (absBefore != isAbsolute() || !raftUUIDBefore.equals(getLoc().raftUUID)) {
			//change of absolutism or change or raft - mounted or dismounted
			EventCharacterMountDismount ecmd = new EventCharacterMountDismount(this);
			ecmd.fire();
		}
	}

	/**
	 * Move toward a target at maximum speed
	 * 
	 * @param target
	 * @return true if target reached, false if not
	 */
	public boolean moveToward(Location target, double speedModifier) {
		
		// System.out.println("Iteration: #####################");
		// System.out.println(" Initial loc: " + loc.getPos().x + ",");
		// System.out.println(" Target loc: " + target.getPos().x + ",");

		// first of all we need to remain in our current reference frame until we encounter an edge, so cast target to our frame
		// then move directly toward target at our maximum speed (use set magnitude), then update our current frame - if we have left the
		// raft or if we have walked onto another raft then set it as such. For now don't account for raft-jumping, simply into and out of
		// water.
		/**
		 * TODO - account for raft jumping in Fleet update so characters don't get wet.
		 */

		// Transform the target location on the characters reference frame
		VectorDouble transformedTargetCOM = target.getPos().add(new VectorDouble(0.5, 0.5));
		if (target.isAbsolute) {
			if (!isAbsolute()) {// target is absolute, player on raft, transform
				UserData locUD = ClientPacketHandler.getUserData(getLoc().raftUUID);
				transformedTargetCOM = transformedTargetCOM.subtract(locUD.raft.getPos()).getRelative(locUD.raft.getUnitX(), locUD.raft.getUnitY());
			} // both absolute, don't transform
		} else {
			if (isAbsolute()) {// target is on raft, player is absolute, transform
				UserData targetUD = ClientPacketHandler.getUserData(target.raftUUID);
				transformedTargetCOM = transformedTargetCOM.getAbsolute(targetUD.raft.getUnitX(), targetUD.raft.getUnitY()).add(targetUD.raft.getPos());
			} else {
				// both relative, same raft?
				if (!target.raftUUID.equals(getLoc().raftUUID)) {// different rafts
					// Map target to absolute, then map to this raft
					UserData locUD = ClientPacketHandler.getUserData(getLoc().raftUUID);
					UserData targetUD = ClientPacketHandler.getUserData(target.raftUUID);
					transformedTargetCOM = transformedTargetCOM.getAbsolute(targetUD.raft.getUnitX(), targetUD.raft.getUnitY()).add(targetUD.raft.getPos());
					transformedTargetCOM = transformedTargetCOM.subtract(locUD.raft.getPos()).getRelative(locUD.raft.getUnitX(), locUD.raft.getUnitY());
				} // same raft, no transform needed
			}
		}
		// If the maximum speed would exceed the distance then move the target to the raw location and return
		VectorDouble bodyCOM = getLoc().getPos().add(new VectorDouble(0.5, 0.5));
		VectorDouble change = transformedTargetCOM.subtract(bodyCOM);
		double speed = walkSpeed;
		if (isAbsolute()) {// offboard, see if swimming
			TerrainType tt = WorldHandler.getTerrainType(Math.floor(getLoc().getPos().x), Math.floor(getLoc().getPos().y));
			if (tt == TerrainType.Water) {
				speed = swimSpeed;
			}
		}
		speed *= speedModifier;//multiply by modifier
		if (change.getSquaredLength() <= Math.pow(speed, 2)) {
			setLoc(new Location(target));// Move to target. (redundancy clone)
			// System.out.println("Curr loc3: " + loc.getPos().x + ",");
			return true;
		}
		// Move the character directly towards the transformed location at maximum speed
		change = change.setMagnitude(speed);
		// System.out.println(" Change : " + change.x + ",");
		Location loc2 = getLoc();
		loc2.setPos(loc2.getPos().add(change));
		setLoc(loc2);
		// System.out.println(" New loc: " + loc.getPos().x + ",");
		// Test if the characters reference frame has changed and update accordingly
		bodyCOM = getLoc().getPos().add(new VectorDouble(0.5, 0.5));
		if (isAbsolute()) {
			// currently offboard, see if boarded a raft
			for (UserData udPotential : ClientPacketHandler.userData) {
				if (udPotential != null && udPotential.raft != null) {
					// transform and see if it fits
					VectorDouble transformedCOM = bodyCOM.subtract(udPotential.raft.getPos()).getRelative(udPotential.raft.getUnitX(), udPotential.raft.getUnitY());
					Tile t = udPotential.raft.getTileAt((int) Math.floor(transformedCOM.x), (int) Math.floor(transformedCOM.y));
					if (t != null) { // mount about COM
						Location loc3 = new Location();
						loc3.setPos(transformedCOM.subtract(new VectorDouble(0.5, 0.5)));
						loc3.isAbsolute = false;
						loc3.raftUUID = udPotential.uuid;
						setLoc(loc3);
						break;// mounted 1 raft, don't mount another.
					}
				}
			}
		} else {
			// currently onboard, see if dismounted
			UserData locUD = ClientPacketHandler.getUserData(getLoc().raftUUID);
			Tile t = locUD.raft.getTileAt((int) Math.floor(bodyCOM.x), (int) Math.floor(bodyCOM.y));
			//System.out.println("aboard: " + t.tileType);
			if (t == null) { // dismount about COM
				Location loc3 = new Location();
				loc3.setPos(bodyCOM.getAbsolute(locUD.raft.getUnitX(), locUD.raft.getUnitY()).add(locUD.raft.getPos()).subtract(new VectorDouble(0.5, 0.5)));
				//System.out.println("setting absolute");
				loc3.isAbsolute = true;
				loc3.raftUUID = "";
				setLoc(loc3);
			}
		}
		// System.out.println(" Final loc: " + loc.getPos().x + ",");
		return false;
	}

}
