package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

public class RightStickXAction extends AbstractInputAction {

	private MyGame game;
	private Camera camera;
	
	public RightStickXAction(MyGame g, Camera c) {
		game = g;
		camera = c;
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
				System.out.println("Camera mode");
				System.out.println("Right stick X moved: " + e.getValue());
				
				// UVN Vector is left handed
				Vector3f uVector = game.camera.getRt();
				Vector3f vVector = game.camera.getUp();
				Vector3f nVector = game.camera.getFd();
				
				// transform vectors
				Vector3 uTransform = uVector.rotate(rotationSpeed, vVector).normalize();
				Vector3 nTransform = nVector.rotate(rotationSpeed, vVector).normalize();
				
				game.camera.setFd( (Vector3f) nTransform);
				game.camera.setRt( (Vector3f) uTransform);
			} else {
				System.out.println("Node mode");
				System.out.println("Right stick X moved: " + e.getValue());
				game.dolphinNode.yaw(rotationSpeed);
			}
		}
	}
}
