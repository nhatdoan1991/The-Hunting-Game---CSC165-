package com.dsgames.game.myGameEngine.action;

import com.saechaol.game.a1.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;

/**
 * An action handler that moves the camera forwards
 * @author Lucas
 *
 */

public class MoveCameraForwardAction extends AbstractInputAction {

	private MyGame game;
	private SceneNode dolphin;
	
	public MoveCameraForwardAction(MyGame g) {
		game = g;
	}
	
	public MoveCameraForwardAction(MyGame g, SceneNode d) {
		game = g;
		dolphin = d;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		if (game.camera.getMode() == 'c') {
		//	System.out.println("Camera mode forward");
			Vector3f vel = game.camera.getFd();
			Vector3f position = game.camera.getPo();
			Vector3f pointOne = (Vector3f) Vector3f.createFrom((speed * vel.x()), (speed * vel.y()), (speed * vel.z()));
			Vector3f pointTwo = (Vector3f) position.add(pointOne);
			game.camera.setPo( (Vector3f) Vector3f.createFrom(pointTwo.x(), pointTwo.y(), pointTwo.z()));
		
		} else if (dolphin != null) {
			dolphin.moveForward(speed * 1.5f);
		} else {
		//	System.out.println("Node mode forward");
			game.dolphinNode.moveForward(speed * 1.5f);
		}
	}
	
	

}
