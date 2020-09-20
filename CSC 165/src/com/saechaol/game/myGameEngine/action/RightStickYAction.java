package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

public class RightStickYAction extends AbstractInputAction {

	private MyGame game;
	private Camera camera;
	
	public RightStickYAction(MyGame g, Camera c) {
		game = g;
		camera = c;
	}
	
	@Override
	public void performAction(float time, Event e) {
		Angle rotationSpeed = Degreef.createFrom((-game.getEngine().getElapsedTimeMillis() / 10.0f) * e.getValue());

		if (e.getValue() >= 0.15f || e.getValue() <= -0.15f) {
			if (game.camera.getMode() == 'c') {
				System.out.println("Camera mode");
				System.out.println("Right stick Y moved: " + e.getValue());
				
				// UVN Vector is left handed
				Vector3f uVector = game.camera.getRt();
				Vector3f vVector = game.camera.getUp();
				Vector3f nVector = game.camera.getFd();
				
				// transform vectors
				Vector3 vTransform = vVector.rotate(rotationSpeed, uVector).normalize();
				Vector3 nTransform = nVector.rotate(rotationSpeed, uVector).normalize();
				
				game.camera.setFd( (Vector3f) nTransform);
				game.camera.setUp( (Vector3f) vTransform);
			} else {
				System.out.println("Node mode");
				System.out.println("Right stick Y moved: " + e.getValue());
				game.dolphinNode.pitch(rotationSpeed);
			}
		}
	}
}
