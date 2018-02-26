package redstonedude.programs.projectboaty.client.audio;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;

import redstonedude.programs.projectboaty.client.net.ClientPacketHandler;
import redstonedude.programs.projectboaty.client.physics.ClientPhysicsHandler;
import redstonedude.programs.projectboaty.shared.entity.WrappedEntity;
import redstonedude.programs.projectboaty.shared.event.EventCharacterMountDismount;
import redstonedude.programs.projectboaty.shared.event.EventHandler;
import redstonedude.programs.projectboaty.shared.event.EventListener;
import redstonedude.programs.projectboaty.shared.net.UserData;
import redstonedude.programs.projectboaty.shared.physics.VectorDouble;

public class SoundHandlerCharacter extends SoundHandler implements EventListener {
	
	public WrappedEntity character;
	
	public SoundHandlerCharacter(WrappedEntity we) {
		super();
		character = we;
	}
	
	@Override
	public void soundEnd() {
		System.out.println("sound end");
		playSound();
	}
	
	@EventHandler
	public static void onMountDismount(EventCharacterMountDismount ecmd) {
		for (SoundHandler sh: SoundHandler.soundHandlers) {
			if (sh instanceof SoundHandlerCharacter) {
				SoundHandlerCharacter shc = (SoundHandlerCharacter) sh;
				if (shc.character != null && shc.character.entity != null && shc.character.entity.uuid.equals(ecmd.entityCharacter.uuid)) {
					shc.stopSound();
					return;
				} else if (shc.character == null) {
					//unregister this one
					SoundHandler.unregisterHandler(shc);
				}
			}
		}
		//not returned - no sound playing
		
	}
	
	
	
	public void playSound() {
		VectorDouble absPos = character.entity.getLoc().getPos();
		if (!character.entity.isAbsolute()) {
			UserData ud = ClientPacketHandler.getUserData(character.entity.getLoc().raftUUID);
			if (ud != null && ud.raft != null) {
				absPos = absPos.getAbsolute(ud.raft.getUnitX(),ud.raft.getUnitY()).add(ud.raft.getPos());
			}
		}
		VectorDouble displacement = absPos.subtract(ClientPhysicsHandler.cameraPosition);
		double squaredistance = displacement.getSquaredLength();
		double distance = Math.sqrt(squaredistance);
		//float volume = (float) ((100-squaredistance)/100);
		float volume = (float) (200-200*Math.pow(Math.E, Math.abs(0.05*distance))/(Math.pow(Math.E, Math.abs(0.05*distance)) + 1));
		volume /= 100;
		if (volume < 0) {
			volume = 0;
		}
		if (character.entity.isAbsolute()) {
			//play water
			play("splash");
		} else {
			//play plank
			play("plank");
		}
		FloatControl gainControl = (FloatControl) clip.getControl(Type.MASTER_GAIN);
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range*volume) + gainControl.getMinimum();
		gainControl.setValue(gain);
	}
	
}
