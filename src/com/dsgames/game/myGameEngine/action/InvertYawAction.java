package com.dsgames.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

/**
 * An action handler that inverts the yaw controls when invoked
 * @author Lucas
 *
 */

public class InvertYawAction extends AbstractInputAction {

	private MyGame game;
	
	public InvertYawAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
	//	System.out.println("Invert Yaw: " + game.invertYaw);
		game.invertYaw();
	}
	
}
