package com.saechaol.game.myGameEngine.action;

/**
 * An action to increment an in-game counter to test the input device
 * 
 * @author Lucas
 */

import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

public class InvertYawAction extends AbstractInputAction {

	private MyGame game;
	
	public InvertYawAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		System.out.println("Invert Yaw: " + game.invertYaw);
		game.invertYaw();
	}
	
}
