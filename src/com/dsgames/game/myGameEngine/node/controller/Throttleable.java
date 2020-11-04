package com.dsgames.game.myGameEngine.node.controller;

/**
 * Allows node controllers implementing this interface to have its speed mutable
 * @author Lucas
 *
 */
interface Throttleable {

	void setSpeed(float speed);
	
	float getSpeed();
	
}
