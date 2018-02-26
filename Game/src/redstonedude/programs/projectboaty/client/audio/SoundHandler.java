package redstonedude.programs.projectboaty.client.audio;

import java.io.File;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

import sun.misc.IOUtils;

public abstract class SoundHandler {
	
	public SoundHandler() {
		registerHandler(this);
	}
	
	static byte[] sound_plank_1;
	public static void init() {
		try {
			sound_plank_1 = IOUtils.toByteArray(AudioSystem.getAudioInputStream(new File(System.getProperty("user.dir") + "/resources/audio/sounds/plank_1.wav")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract void soundEnd();
	public Clip clip;
	
	public void play(String sound) {
		try {
			Random rand = new Random();
			int id = rand.nextInt(3)+1;
			clip = AudioSystem.getClip();
			//System.out.println("meme "+id);
			clip.open(AudioSystem.getAudioInputStream(new File(System.getProperty("user.dir") + "/resources/audio/sounds/"+ sound + "_" + id + ".wav")));
			AudioSystem.getAudioInputStream((File) null);
			//clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();
			clip.addLineListener(new LineListener() {
				@Override
				public void update(LineEvent e) {
					if (e.getType() == Type.STOP) {
						soundEnd();
						//play(sound);
					}
				}
				
			});
			
			//play(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stopSound() {
		if (clip != null) {
			clip.stop();
		}
	}
	
	public static ConcurrentLinkedQueue<SoundHandler> soundHandlers = new ConcurrentLinkedQueue<SoundHandler>();
	
	public static void registerHandler(SoundHandler sh) {
		soundHandlers.add(sh);
	}
	
	public static void unregisterHandler(SoundHandler sh) {
		soundHandlers.remove(sh);
	}
	
	
	
	
	
	
	
}
