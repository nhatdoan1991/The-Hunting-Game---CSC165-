package com.dsgames.game.myGameEngine.action;

import com.dsgames.game.hunt.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarJumpAction extends AbstractInputAction {

	private MyGame game;
	private String player;
	
	public AvatarJumpAction(MyGame g, String p) {
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
