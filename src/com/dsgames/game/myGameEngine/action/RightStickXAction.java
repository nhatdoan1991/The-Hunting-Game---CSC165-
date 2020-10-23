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
 * An action handler that yaws the camera left/right with the gamepad. 
 * Rotation is calculated using Quaternions.
 * @author Lucas
 *
 */

public class RightStickXAction extends AbstractInputAction {

	private MyGame game;
	
	public RightStickXAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		Angle rotationSpeed;
		if (game.invertYaw) {
			rotationSpeed = Degreef.createFrom(-1.0f * (game.getEngine().getElapsedTimeMillis() / 10.0f) * e.getValue());
		} else {
			rotationSpeed = Degreef.createFrom((game.getEngine().getElapsedTimeMillis() / 10.0f) * e.getValue());
		}
		if (e.getValue() >= 0.15f || e.getValue() <= -0.15f) {
			if (game.camera.getMode() == 'c') {
		//		System.out.println("Camera mode");
		//		System.out.println("Right stick X moved: " + e.getValue());
				Quaternion quaternionRotation = Quaternionf.createFrom(rotationSpeed, game.camera.getUp());
				game.camera.setFd( (Vector3f) quaternionRotation.rotate(game.camera.getFd()));
				game.camera.setRt( (Vector3f) quaternionRotation.rotate(game.camera.getRt()));
			} else {
		//		System.out.println("Node mode");
		//		System.out.println("Right stick X moved: " + e.getValue());
				game.dolphinNode.yaw(rotationSpeed);
			}
		}
	}
}
