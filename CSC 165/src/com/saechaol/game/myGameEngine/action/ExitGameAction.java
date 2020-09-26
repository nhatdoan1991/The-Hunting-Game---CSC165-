package com.saechaol.game.myGameEngine.action;

/**
 * A general event listener that will execute game shutdown
 * 
 * @author Lucas
 */
import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;
import com.saechaol.game.a1.MyGame;

public class ExitGameAction extends AbstractInputAction {

	private MyGame game;
	
	public ExitGameAction(MyGame g) {
		game = g;
	}
	
	@Override
	public void performAction(float time, Event e) {
		System.out.println("Shutdown requested");
		game.setState(Game.State.STOPPING);
	}
	
}
