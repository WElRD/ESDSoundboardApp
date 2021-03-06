package vveird.TabletopSoundboard.streamdeck.items;

import java.io.File;

import de.rcblum.stream.deck.event.KeyEvent;
import de.rcblum.stream.deck.items.AbstractStreamItem;
import de.rcblum.stream.deck.util.IconHelper;
import vveird.TabletopSoundboard.AudioApp;
import vveird.TabletopSoundboard.config.Sound;
import vveird.TabletopSoundboard.config.Sound.Type;

/**
 * 
 * Stream Deck Item to display a sound on the ESD.
 * 
 * @author vveird
 * 
 *
 */
public class SoundItem extends AbstractStreamItem {
	
	private Sound sound = null;

	public SoundItem(Sound sound) {
		super(IconHelper.convertImage(sound.getCover()));
		this.sound = sound;
		this.setTextPosition(TEXT_POS_CENTER);
		this.setText(sound.getName());
	}

	@Override
	public void onKeyEvent(KeyEvent event) {
		if (event.getType() == KeyEvent.Type.RELEASED_CLICKED) {
			System.out.println("Trigger: " + sound.getName());
			System.out.println("Trigger: " + sound.getCurrentFile());
			if (this.sound.getType() == null || this.sound.getType() == Type.AMBIENCE)
				AudioApp.playAmbience(this.sound);
			else
				AudioApp.playEffect(this.sound);
		}
	}

	public Sound getSound() {
		return sound;
	}
}
