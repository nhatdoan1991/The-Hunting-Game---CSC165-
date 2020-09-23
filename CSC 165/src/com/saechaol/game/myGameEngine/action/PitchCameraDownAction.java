package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import com.saechaol.game.a1.MyGame;
import net.java.games.input.Event;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class PitchCameraDownAction extends AbstractInputAction {

	private MyGame game;
	
	public PitchCameraDownAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		System.out.println("Pitch camera Down");
		Angle rotationSpeed = Degreef.createFrom(-game.getEngine().getElapsedTimeMillis() / 10.0f);
		if (game.camera.getMode() == 'n') {
			rotationSpeed = rotationSpeed.mult(-1.0f);
			game.dolphinNode.pitch(rotationSpeed);
		} else {
			// UVN Vector is left handed
			Vector3f uVector = game.camera.getRt();
			Vector3f vVector = game.camera.getUp();
			Vector3f nVector = game.camera.getFd();
			
			// transform vectors
			Vector3 vTransform = vVector.rotate(rotationSpeed, uVector).normalize();
			Vector3 nTransform = nVector.rotate(rotationSpeed, uVector).normalize();
			
			game.camera.setFd( (Vector3f) nTransform);
			game.camera.setUp( (Vector3f) vTransform);
		}
	}

}
