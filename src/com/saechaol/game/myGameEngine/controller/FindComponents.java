package com.saechaol.game.myGameEngine.controller;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;
import net.java.games.input.Component;

/**
 * This class is intended to help find and locate connected controllers and lists their components, as well as any other utility functionality
 * @author Lucas
 *
 */

public class FindComponents {

	/**
	 * Obtains the set of the controllers from the controller environment, and then lists them
	 */
	public void listControllers() {
		System.out.println("JInput Version: " + Version.getVersion());
		ControllerEnvironment controllerEnvironment = ControllerEnvironment.getDefaultEnvironment();
		
		// get the set of controllers from the controller environment
		Controller[] controllers = controllerEnvironment.getControllers();
		
		// print the details and sub-controllers from each controller found in the environment
		for (int i = 0; i < controllers.length; i++) {
			System.out.println("\nController #" + i);
			listComponents(controllers[i]);
		}
	}
	
	/**
	 * Lists available components from the detected controller
	 * @param controller
	 */
	private void listComponents(Controller controller) {
		// print the controller's name and type
		System.out.println("Name: \"" + controller.getName() + "\". Type ID: " + controller.getType());
		
		// get the components of the controller
		Component[] components = controller.getComponents();
		for (Component component : components) {
			System.out.println(" Component name: " + component.getName() + " | ID: " + component.getIdentifier());
			
			// find sub-controllers, if they exist, and recursively list their details
			Controller[] subControllers = controller.getControllers();
			for (int i = 0; i < subControllers.length; i++) {
				System.out.println(" " + controller.getName() + " subController #" + i);
				listComponents(subControllers[i]);
			}
		}
	}
	
	
	public static void main(String[] args) {
		FindComponents componentFinder = new FindComponents();
		componentFinder.listControllers();
	}
}
