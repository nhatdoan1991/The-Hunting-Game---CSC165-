package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import com.saechaol.game.a1.MyGame;
import net.java.games.input.Event;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Quaternion;
import ray.rml.Quaternionf;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class YawCameraLeftAction extends AbstractInputAction {

	private MyGame game;
	
	public YawCameraLeftAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		System.out.println("Yawing camera left");
		Angle rotationSpeed;
		if (game.invertYaw) {
			rotationSpeed = Degreef.createFrom(game.getEngine().getElapsedTimeMillis() / 10.0f);
		} else {
			rotationSpeed = Degreef.createFrom(-game.getEngine().getElapsedTimeMillis() / 10.0f);
		}
		if (game.camera.getMode() == 'n') {
			game.dolphinNode.yaw(rotationSpeed);
		} else {
			Quaternion quaternionRotation = Quaternionf.createFrom(rotationSpeed, game.camera.getUp());
			game.camera.setFd( (Vector3f) quaternionRotation.rotate(game.camera.getFd()));
			game.camera.setRt( (Vector3f) quaternionRotation.rotate(game.camera.getRt()));
		}
	}

}
