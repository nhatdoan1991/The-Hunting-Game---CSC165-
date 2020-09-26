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
		} else { // U -> Rt; V -> Up; N -> Fd
			Quaternion quaternionRotation = Quaternionf.createFrom(rotationSpeed, game.camera.getRt());
			game.camera.setFd( (Vector3f) quaternionRotation.rotate(game.camera.getFd()));
			game.camera.setUp( (Vector3f) quaternionRotation.rotate(game.camera.getUp()));
		}
	}

}
