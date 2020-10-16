package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a2.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class StartPhysicsAction extends AbstractInputAction {

	private MyGame game;
	
	public StartPhysicsAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		game.running = true;
	}

	
	
}
