package com.dsgames.game.myGameEngine.action;

import com.saechaol.game.a1.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Quaternion;
import ray.rml.Quaternionf;
import ray.rml.Vector3f;

/**
 * An action handler that pitches the camera up or down. 
 * Rotation is calculated using Quaternions.
 * @author Lucas
 *
 */

public class RightStickYAction extends AbstractInputAction {

	private MyGame game;
	
	public RightStickYAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		Angle rotationSpeed = Degreef.createFrom((-game.getEngine().getElapsedTimeMillis() / 10.0f) * e.getValue());

		if (e.getValue() >= 0.15f || e.getValue() <= -0.15f) {
			if (game.camera.getMode() == 'c') {
			//	System.out.println("Camera mode");
			//	System.out.println("Right stick Y moved: " + e.getValue());
				Quaternion quaternionRotation = Quaternionf.createFrom(rotationSpeed, game.camera.getRt());
				game.camera.setFd( (Vector3f) quaternionRotation.rotate(game.camera.getFd()));
				game.camera.setUp( (Vector3f) quaternionRotation.rotate(game.camera.getUp()));
			} else {
			//	System.out.println("Node mode");
			//	System.out.println("Right stick Y moved: " + e.getValue());
				rotationSpeed = rotationSpeed.mult(-1.0f);
				game.dolphinNode.pitch(rotationSpeed);
			}
		}
	}
}
