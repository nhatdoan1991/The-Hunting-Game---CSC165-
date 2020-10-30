package com.dsgames.game.myGameEngine.action;

import com.dsgames.game.hunt.HuntingGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;

public class AvatarLeftStickYAction extends AbstractInputAction {
	private HuntingGame game;
	private String player;
	
	public AvatarLeftStickYAction(HuntingGame g, String p) {
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
					if (game.playerCharge.get(game.dolphinNodeOne)) 
						game.dolphinNodeOne.moveForward(-speed * 3.0f);
					else
						game.dolphinNodeOne.moveForward(-speed * 1.5f);
					game.synchronizeAvatarPhysics(game.dolphinNodeOne);
					break;
				}
			} else if (speed < 0) {
				switch (player) {
				case "dolphinEntityOneNode":
					if (game.playerCharge.get(game.dolphinNodeOne)) 
						game.dolphinNodeOne.moveBackward(speed * 3.0f);
					else
						game.dolphinNodeOne.moveBackward(speed * 1.5f);
					game.synchronizeAvatarPhysics(game.dolphinNodeOne);
					break;
				}
			}
			
		}
	}
	
}
