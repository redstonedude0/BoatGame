package redstonedude.programs.projectboaty.client.audio;

import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicHandler {
	
	public static void musicTester() {
	String bip = "resources/audio/music/bip.mp3";
	Media hit = new Media(new File(bip).toURI().toString());
	MediaPlayer mediaPlayer = new MediaPlayer(hit);
	mediaPlayer.play();
	
	}

}
