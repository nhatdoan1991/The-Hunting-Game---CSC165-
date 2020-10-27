package com.dsgames.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import com.dsgames.game.hunt.MyGame;

/**
 * An action handler that will play and pause the current song
 * 
 * @author Lucas
 */

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
