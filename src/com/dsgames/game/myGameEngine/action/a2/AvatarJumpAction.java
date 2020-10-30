package com.dsgames.game.myGameEngine.action.a2;

import com.dsgames.game.hunt.HuntingGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarJumpAction extends AbstractInputAction {

	private HuntingGame game;
	private String player;
	
	public AvatarJumpAction(HuntingGame g, String p) {
		game = g;
		player = p;
	}
	
	@Override
	public void performAction(float time, Event e) {
		switch (player) {
		case "dolphinEntityOneNode":
			game.jumpP1 = true;
			game.dolphinOnePhysicsObject.applyForce(0.0f, 400.0f, 0.0f, 0.0f, 0.0f, 0.0f);
			game.velocityP1 = 0.0f;
			break;
		}
	}

}
