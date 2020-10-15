package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a2.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;

public class AvatarLeftStickYAction extends AbstractInputAction {
	private MyGame game;
	private String player;
	
	public AvatarLeftStickYAction(MyGame g, String p) {
		game = g;
		player = p;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		speed *= e.getValue();
		if (e.getValue() >= 0.15f || e.getValue() <= -0.15f) {
			if (speed > 0) {
				switch (player) {
				case "dolphinEntityOneNode":
					game.dolphinNodeOne.moveForward(-speed * 1.5f);
					break;
				case "dolphinEntityTwoNode":
					game.dolphinNodeTwo.moveForward(-speed * 1.5f);
					break;
				}
			} else if (speed < 0) {
				switch (player) {
				case "dolphinEntityOneNode":
					game.dolphinNodeOne.moveBackward(speed * 1.5f);
					break;
				case "dolphinEntityTwoNode":
					game.dolphinNodeTwo.moveBackward(speed * 1.5f);
					break;
				}
			}
			
		}
	}
	
}
