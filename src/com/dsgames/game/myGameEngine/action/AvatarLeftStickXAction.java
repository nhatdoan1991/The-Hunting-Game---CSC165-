package com.dsgames.game.myGameEngine.action;

import com.dsgames.game.hunt.HuntingGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarLeftStickXAction extends AbstractInputAction {

	private HuntingGame game;
	private String player;
	
	public AvatarLeftStickXAction(HuntingGame g, String p) {
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
						game.dolphinNodeOne.moveRight(-speed * 3.0f);
					else
						game.dolphinNodeOne.moveRight(-speed * 1.5f);
					game.synchronizeAvatarPhysics(game.dolphinNodeOne);
					break;
				case "dolphinEntityTwoNode":
				}
			} else if (speed < 0) {
				switch (player) {
				case "dolphinEntityOneNode":
					if (game.playerCharge.get(game.dolphinNodeOne)) 
						game.dolphinNodeOne.moveLeft(speed * 3.0f);
					else
						game.dolphinNodeOne.moveLeft(speed * 1.5f);
					game.synchronizeAvatarPhysics(game.dolphinNodeOne);
					break;
				case "dolphinEntityTwoNode":
				}
			}
		}
	}
	
}
