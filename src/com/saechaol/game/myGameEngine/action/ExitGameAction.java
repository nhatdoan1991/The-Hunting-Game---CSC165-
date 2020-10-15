package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;

/**
 * An action handler that requests game shutdown
 * @author Lucas
 *
 */

public class ExitGameAction extends AbstractInputAction {

	private BaseGame game;
	
	public ExitGameAction(BaseGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		System.out.println("Shutdown requested");
		game.setState(Game.State.STOPPING);
	}
	
}
