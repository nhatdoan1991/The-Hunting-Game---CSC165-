package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import com.saechaol.game.a1.MyGame;
import net.java.games.input.Event;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class YawCameraRightAction extends AbstractInputAction {

	private MyGame game;
	
	public YawCameraRightAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		System.out.println("Yawing camera right");
		Angle rotationSpeed;
		if (game.invertYaw) {
			rotationSpeed = Degreef.createFrom(-game.getEngine().getElapsedTimeMillis() / 10.0f);
		} else {
			rotationSpeed = Degreef.createFrom(game.getEngine().getElapsedTimeMillis() / 10.0f);
		}
		if (game.camera.getMode() == 'n') {
			game.dolphinNode.yaw(rotationSpeed);
		} else {
			// UVN Vector is left handed
			Vector3f uVector = game.camera.getRt();
			Vector3f vVector = game.camera.getUp();
			Vector3f nVector = game.camera.getFd();
			
			// transform vectors
			Vector3 uTransform = uVector.rotate(rotationSpeed, vVector).normalize();
			Vector3 nTransform = nVector.rotate(rotationSpeed, vVector).normalize();
			
			game.camera.setFd( (Vector3f) nTransform);
			game.camera.setRt( (Vector3f) uTransform);
		}
	}

}
