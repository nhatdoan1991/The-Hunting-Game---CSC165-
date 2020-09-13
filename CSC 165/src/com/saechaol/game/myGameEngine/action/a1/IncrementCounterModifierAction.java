package com.saechaol.game.myGameEngine.action.a1;

import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

/**
 * A counter modifier action that works with IncrementCounter.java to test the input device
 * 
 * @author Lucas
 */
public class IncrementCounterModifierAction extends AbstractInputAction {

	private MyGame game;
	private int incrementAmount = 1;
	
	public IncrementCounterModifierAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		incrementAmount++;
		System.out.println("Modifier action fired. Modifier = " + (incrementAmount));
		if (incrementAmount > 5) {
			System.out.println("Increment Modifier exceeds 5, setting back to 1");
			incrementAmount = 1;
		}
	}
	
	protected int getIncrementAmount() {
		return incrementAmount;
	}
	
}
