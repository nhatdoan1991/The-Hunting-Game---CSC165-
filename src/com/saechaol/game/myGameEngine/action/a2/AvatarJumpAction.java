package com.saechaol.game.myGameEngine.action.a2;

import com.saechaol.game.a2.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class AvatarJumpAction extends AbstractInputAction {

	private MyGame game;
	
	public AvatarJumpAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		
		game.dolphinOnePhysicsObject.applyForce(0.0f, 200.0f, 0.0f, 0.0f, 0.0f, 0.0f);
		game.dolphinNodeOne.setPhysicsObject(game.dolphinOnePhysicsObject);
		game.physicsEngine.update(game.getEngine().getElapsedTimeMillis());
	}

}
