package com.saechaol.game.myGameEngine.action;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

/**
 * An action handler that polls the right stick for input information
 * @author Lucas
 *
 */

public class RightStickMoveAction extends AbstractInputAction {

	private Controller controller;
	
	public RightStickMoveAction(Controller c) {
		controller = c;
	}

	@Override
	public void performAction(float time, Event e) {
		Component rightStickX = controller.getComponent(net.java.games.input.Component.Identifier.Axis.RX);
		Component rightStickY = controller.getComponent(net.java.games.input.Component.Identifier.Axis.RY);
		System.out.println("Right stick moved. \nRight stick X: " + rightStickX.getPollData());
		System.out.println("Right stick Y: " + rightStickY.getPollData() + "\n");
	}
	
	
	
}
