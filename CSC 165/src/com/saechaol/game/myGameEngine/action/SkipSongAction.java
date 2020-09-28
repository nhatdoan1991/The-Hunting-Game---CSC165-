package com.saechaol.game.myGameEngine.action;

/**
 * An event listener that will play and pause the current song
 * 
 * @author Lucas
 */

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;
import ray.audio.AudioManager;
import ray.audio.IAudioManager;
import ray.audio.SoundType;

public class SkipSongAction extends AbstractInputAction {

	private MyGame game;
	
	public SkipSongAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		game.playAudio();
	}
	
}
