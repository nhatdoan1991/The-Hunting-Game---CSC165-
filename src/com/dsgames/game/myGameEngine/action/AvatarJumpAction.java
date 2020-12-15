package com.dsgames.game.myGameEngine.action;

import com.dsgames.game.hunt.HuntingGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rml.Vector3f;

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
		case "playerNode":
			if(game.jumpP1 == false)
			{
				game.jumpP1 = true;
				game.setLastJumpTime(game.getGameTime());
				game.dolphinOnePhysicsObject.applyForce(0.0f, 800.0f, 0.0f, 0.0f, 0.0f, 0.0f);
				game.velocityP1 = 0.0f;
				game.playJumpSound();
				game.playPlayerJumpAnimation();
				break;
			}
	
		}
	}

}
