package com.saechaol.game.myGameEngine.action.a2;

import com.saechaol.game.a2.MyGame;

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
		case "dolphinEntityTwoNode":
			game.jumpP2 = true;
			game.dolphinTwoPhysicsObject.applyForce(0.0f, 400.0f, 0.0f, 0.0f, 0.0f, 0.0f);
			game.velocityP2 = 0.0f;
			break;
		}
	}

}
