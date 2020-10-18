package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a2.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarMoveRightAction extends AbstractInputAction {

	private MyGame game;
	private String player;
	
	public AvatarMoveRightAction(MyGame g, String p) {
		game = g;
		player = p;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		switch (player) {
		case "dolphinEntityOneNode":
			if (game.playerCharge.get(game.dolphinNodeOne)) 
				game.dolphinNodeOne.moveRight(-speed * 3.0f);
			else
				game.dolphinNodeOne.moveRight(-speed * 1.5f);
			game.synchronizeAvatarPhysics(game.dolphinNodeOne);
			break;
		case "dolphinEntityTwoNode":
			if (game.playerCharge.get(game.dolphinNodeTwo))
				game.dolphinNodeTwo.moveRight(-speed * 3.0f);
			else
				game.dolphinNodeTwo.moveRight(-speed * 1.5f);
			game.synchronizeAvatarPhysics(game.dolphinNodeTwo);
			break;
		}
	}

}
