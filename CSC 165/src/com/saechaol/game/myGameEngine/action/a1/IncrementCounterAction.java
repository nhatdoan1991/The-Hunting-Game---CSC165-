package com.saechaol.game.myGameEngine.action.a1;

/**
 * An action to increment an in-game counter to test the input device
 * 
 * @author Lucas
 */

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

public class IncrementCounterAction extends AbstractInputAction {

	private MyGame game;
	private IncrementCounterModifierAction incrementCounterModifierAction;
	
	public IncrementCounterAction(MyGame g, IncrementCounterModifierAction incrementModifierAction) {
		game = g;
		incrementCounterModifierAction = incrementModifierAction;
	}
	
	@Override
	public void performAction(float time, Event e) {
		System.out.println("Counter incremented");
		int incrementAmount = incrementCounterModifierAction.getIncrementAmount();
		game.incrementCounter(incrementAmount);
	}
	
}
