package redstonedude.programs.projectboaty.client.audio;

import java.io.File;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MusicHandler {

	public static void musicTester() {
		final JFXPanel fxp = new JFXPanel();
		String bip = System.getProperty("user.dir") + "/resources/audio/music/bip.mp3";
		Media hit = new Media(new File(bip).toURI().toString());
		MediaPlayer mediaPlayer = new MediaPlayer(hit);
		mediaPlayer.play();

	}
}
