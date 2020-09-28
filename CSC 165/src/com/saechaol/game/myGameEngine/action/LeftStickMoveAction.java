package com.saechaol.game.myGameEngine.action;

import ray.input.action.AbstractInputAction;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;

/**
 * An action handler that polls the gamepad's left stick axes for information
 * @author Lucas
 *
 */

public class LeftStickMoveAction extends AbstractInputAction {

	private Controller controller;
	
	public LeftStickMoveAction(Controller c) {
		controller = c;
	}

	@Override
	public void performAction(float time, Event e) {
		Component leftStickX = controller.getComponent(net.java.games.input.Component.Identifier.Axis.X);
		Component leftStickY = controller.getComponent(net.java.games.input.Component.Identifier.Axis.Y);
		System.out.println("Left stick moved. \nLeft stick X: " + leftStickX.getPollData());
		System.out.println("Left stick Y: " + leftStickY.getPollData() + "\n");
	}
	
	
	
}
