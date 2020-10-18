package com.saechaol.game.myGameEngine.action.test;

import java.io.IOException;

import com.saechaol.game.a2.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class IncrementScoreAction extends AbstractInputAction {

	private MyGame game;
	
	public IncrementScoreAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event evt) {
		try {
			game.incrementScore("dolphinEntityTwoNode");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
