package com.saechaol.game.myGameEngine.action;

import com.saechaol.game.a1.MyGame;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rml.Vector3f;

/**
 * An action handler that moves the camera forward and backward from the gamepad
 * @author Lucas
 *
 */

public class LeftStickYAction extends AbstractInputAction {

	private MyGame game;
	
	public LeftStickYAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		float speed = game.getEngine().getElapsedTimeMillis() * -0.003f;
		speed *= e.getValue();
		if (e.getValue() >= 0.15f || e.getValue() <= -0.15f) {
			if (game.camera.getMode() == 'c') {
		//		System.out.println("Camera mode");
				Vector3f vel = game.camera.getFd();
				Vector3f position = game.camera.getPo();
		//		System.out.println("Left stick Y moved: " + e.getValue());
				Vector3f pointOne = (Vector3f) Vector3f.createFrom((speed * vel.x()), (speed * vel.y()), (speed * vel.z()));
				Vector3f pointTwo = (Vector3f) position.add(pointOne);
				game.camera.setPo( (Vector3f) Vector3f.createFrom(pointTwo.x(), pointTwo.y(), pointTwo.z()));
			} else {
		//		System.out.println("Node mode");
				if (speed > 0) {
		//			System.out.println("Left stick Y moved: " + e.getValue());
					game.dolphinNode.moveForward(speed * 1.5f);
				} else if (speed < 0) {
		//			System.out.println("Left stick Y moved: " + e.getValue());
					game.dolphinNode.moveBackward(-speed * 1.5f);
				}
			}
		}
	}

}
