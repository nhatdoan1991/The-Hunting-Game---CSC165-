package com.dsgames.game.myGameEngine.action;

import com.dsgames.game.hunt.HuntingGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarMoveBackwardAction extends AbstractInputAction {

	private HuntingGame game;
	private String player;
	
	public AvatarMoveBackwardAction(HuntingGame g, String p) {
		game = g;
		player = p;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
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
