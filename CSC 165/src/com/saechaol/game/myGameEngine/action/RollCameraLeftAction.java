package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a1.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Quaternion;
import ray.rml.Quaternionf;
import ray.rml.Vector3f;

/**
 * An action handler that rolls the camera left. Rotation is calculated using Quaternions.
 * @author Lucas
 *
 */

public class RollCameraLeftAction extends AbstractInputAction {

	private MyGame game;
	
	public RollCameraLeftAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
	//	System.out.println("Rolling camera left");
		Angle rotationSpeed;
		if (game.invertYaw) {
			rotationSpeed = Degreef.createFrom(-game.getEngine().getElapsedTimeMillis() / 10.0f);
		} else {
			rotationSpeed = Degreef.createFrom(game.getEngine().getElapsedTimeMillis() / 10.0f);
		}
		if (game.camera.getMode() == 'n') {
			game.dolphinNode.roll(rotationSpeed);
		} else {
			Quaternion quaternionRotation = Quaternionf.createFrom(rotationSpeed, game.camera.getFd());
			game.camera.setUp( (Vector3f) quaternionRotation.rotate(game.camera.getUp()));
			game.camera.setRt( (Vector3f) quaternionRotation.rotate(game.camera.getRt()));
		}
	}

}
