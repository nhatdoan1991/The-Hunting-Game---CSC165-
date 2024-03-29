package com.dsgames.game.myGameEngine.action;

import com.saechaol.game.a1.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rml.Vector3f;

/**
 * An action handler that moves the camera left
 * @author Lucas
 *
 */

public class MoveCameraLeftAction extends AbstractInputAction {

	private MyGame game;
	
	public MoveCameraLeftAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * 0.003f;
		if (game.camera.getMode() == 'c') {
		//	System.out.println("Camera mode left");
			Vector3f vel = game.camera.getRt();
			Vector3f position = game.camera.getPo();
			Vector3f pointOne = (Vector3f) Vector3f.createFrom((-speed * vel.x()), (-speed * vel.y()), (-speed * vel.z()));
			Vector3f pointTwo = (Vector3f) position.add(pointOne);
			game.camera.setPo( (Vector3f) Vector3f.createFrom(pointTwo.x(), pointTwo.y(), pointTwo.z()));
		
		} else {
		//	System.out.println("Node mode left");
			game.dolphinNode.moveLeft(speed * -1.5f);
		}
	}

}
