package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a2.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.game.BaseGame;
import ray.rage.scene.SceneNode;

public class AvatarMoveForwardAction extends AbstractInputAction {

	private MyGame game;
	private String player;
	
	public AvatarMoveForwardAction(MyGame g, String p) {
		game = g;
		player = p;
	}

	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		switch (player) {
		case "dolphinEntityOneNode":
			game.dolphinNodeOne.moveForward(speed * 1.5f);
			game.synchronizeAvatarPhysics(game.dolphinNodeOne);
			break;
		case "dolphinEntityTwoNode":
			game.dolphinNodeTwo.moveForward(speed * 1.5f);
			game.synchronizeAvatarPhysics(game.dolphinNodeTwo);
			break;
		}
	}

}
