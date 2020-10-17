package com.saechaol.game.myGameEngine.camera;

import java.util.ArrayList;

import net.java.games.input.Controller;
import net.java.games.input.Event;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Quaternion;
import ray.rml.Quaternionf;
import ray.rml.Vector3;
import ray.rml.Vector3f;

/**
 * Camera3PController handles and initializes the orbiting camera view, as well as 
 * reinitializes the controls for both the keyboard and the gamepad
 * @author Lucas
 *
 */

public class Camera3PController {

	private Camera camera;
	private SceneNode cameraNode;
	private SceneNode cameraTarget;
	private float cameraAzimuth, cameraElevation, radius, cameraAzimuthMin = 130.0f, cameraAzimuthMax = 230.0f;
	private Vector3 worldUpVector;
	
	public Camera3PController(Camera c, SceneNode cNode, SceneNode cTarget, String controllerName, InputManager inputManager) {
		camera = c;
		cameraNode = cNode;
		cameraTarget = cTarget;
		cameraAzimuth = 180.0f;
		cameraElevation = 20.0f;
		radius = 2.0f;
		worldUpVector = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
		setupInput(inputManager, controllerName);
		updateCameraPosition();
	}
	
	/**
	 * Updates the camera's position as it rotates around the player
	 * @return a Vector representing the camera's current position
	 */
	public Vector3 updateCameraPosition() {
		double theta = Math.toRadians(cameraAzimuth);
		double phi = Math.toRadians(cameraElevation);
		double x = radius * Math.cos(phi) * Math.sin(theta);
		double y = radius * Math.sin(phi);
		double z = radius * Math.cos(phi) * Math.cos(theta);
		cameraNode.setLocalRotation(cameraTarget.getWorldRotation());
		cameraNode.setLocalPosition(Vector3f.createFrom( (float) x, (float) y, (float) z).add(cameraTarget.getWorldPosition()));
		cameraNode.lookAt(cameraTarget, cameraTarget.getWorldUpAxis());
		return cameraNode.getLocalPosition();
	}
	
	/**
	 * Reinitializes the controls specifically for 3P camera mode.
	 * @param inputManager
	 * @param controllerName
	 */
	private void setupInput(InputManager inputManager, String controllerName) {
		Action orbitAction = new OrbitAroundAction();
		Action elevateAction = new OrbitElevationAction();
		Action orbitRadiusAction = new OrbitRadiusAction();
		Action yawLeftAction = new YawLeftAction();
		Action yawRightAction = new YawRightAction();
		Action elevateUpAction = new ElevateUpAction();
		Action elevateDownAction = new ElevateDownAction();
		Action zoomInAction = new ZoomInAction();
		Action zoomOutAction = new ZoomOutAction();
		Action avatarTurnLeftAction = new AvatarTurnLeftAction();
		Action avatarTurnRightAction = new AvatarTurnRightAction();
		
		if (cameraTarget.getName().equalsIgnoreCase("dolphinEntityOneNode")) {
			ArrayList<Controller> controllersArrayList = inputManager.getControllers();
			for (Controller keyboards : controllersArrayList) {
				if (keyboards.getType() == Controller.Type.KEYBOARD) {
					inputManager.associateAction(keyboards,
							net.java.games.input.Component.Identifier.Key.LEFT,
							yawLeftAction,
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
					inputManager.associateAction(keyboards,
							net.java.games.input.Component.Identifier.Key.RIGHT,
							yawRightAction,
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
					inputManager.associateAction(keyboards,
							net.java.games.input.Component.Identifier.Key.UP,
							elevateUpAction,
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
					inputManager.associateAction(keyboards,
							net.java.games.input.Component.Identifier.Key.DOWN,
							elevateDownAction,
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
					inputManager.associateAction(keyboards,
							net.java.games.input.Component.Identifier.Key.F,
							zoomOutAction,
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
					inputManager.associateAction(keyboards,
							net.java.games.input.Component.Identifier.Key.R,
							zoomInAction,
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
					inputManager.associateAction(keyboards, 
							net.java.games.input.Component.Identifier.Key.Q, 
							avatarTurnLeftAction, 
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
					inputManager.associateAction(keyboards, 
							net.java.games.input.Component.Identifier.Key.E, 
							avatarTurnRightAction, 
							InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					
				}
			}
		} else if (cameraTarget.getName().equalsIgnoreCase("dolphinEntityTwoNode")) {
			if(controllerName == null) {
			} else {
				inputManager.associateAction(controllerName, 
						net.java.games.input.Component.Identifier.Axis.RX, 
						orbitAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(controllerName, 
						net.java.games.input.Component.Identifier.Axis.RY, 
						elevateAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(controllerName, 
						net.java.games.input.Component.Identifier.Axis.Z, 
						orbitRadiusAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(controllerName, 
						net.java.games.input.Component.Identifier.Button._4, 
						avatarTurnLeftAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(controllerName, 
						net.java.games.input.Component.Identifier.Button._5, 
						avatarTurnRightAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			}
		}
	}
	
	private double[] toDoubleArray(float[] arr) {
		if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++) {
			ret[i] = (double) arr[i];
		}
		return ret;
	}
	
	/**
	 * An action handler that controls how the player orbits left and right
	 * @author Lucas
	 *
	 */
	private class OrbitAroundAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			float rotation;
			if (e.getValue() < -0.2) {
				rotation = 1.2f;
			} else if (e.getValue() > 0.2) {
				rotation = -1.2f;
			} else 
				rotation = 0.0f;
			cameraAzimuth += rotation;
			if (cameraAzimuth < cameraAzimuthMin) { cameraAzimuth = cameraAzimuthMin; }
			if (cameraAzimuth > cameraAzimuthMax) { cameraAzimuth = cameraAzimuthMax; }
			updateCameraPosition();
		}
		
	}
	
	/**
	 * An action handler that controls how the player orbits left 
	 * @author Lucas
	 *
	 */
	private class YawLeftAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraAzimuth += 1.2f;
			if (cameraAzimuth < cameraAzimuthMin) { cameraAzimuth = cameraAzimuthMin; }
			if (cameraAzimuth > cameraAzimuthMax) { cameraAzimuth = cameraAzimuthMax; }
			updateCameraPosition();
		}
		
	}
	
	/**
	 * An action handler that controls how the player orbits right
	 * @author Lucas
	 *
	 */
	private class YawRightAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraAzimuth -= 1.2f;
			if (cameraAzimuth < cameraAzimuthMin) { cameraAzimuth = cameraAzimuthMin; }
			if (cameraAzimuth > cameraAzimuthMax) { cameraAzimuth = cameraAzimuthMax; }
			updateCameraPosition();
		}
		
	}
	
	/**
	 * An action handler that rotates the player avatar left, and maintains camera location 
	 * @author Lucas
	 *
	 */
	private class AvatarTurnLeftAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			Angle rotationSpeed = Degreef.createFrom(1.2f);
			cameraTarget.yaw(rotationSpeed);
			cameraAzimuth += 1.2f;
			cameraAzimuthMax += 1.2;
			cameraAzimuthMin += 1.2;
			/*
			   Matrix3f = 	[  0.79652 |   0.00000 |  -0.60459]
    						[  0.00000 |   1.00000 |   0.00000]
    						[  0.60459 |   0.00000 |   0.79652]
    						
    		   Matrix3f = 	[  0.86602 |   0.00000 |  -0.50000]
    						[  0.00000 |   1.00000 |   0.00000]
    						[  0.50000 |   0.00000 |   0.86602]
			 */
			
			if (cameraAzimuth < cameraAzimuthMin) { cameraAzimuth = cameraAzimuthMin; }
			if (cameraAzimuth > cameraAzimuthMax) { cameraAzimuth = cameraAzimuthMax; }
			updateCameraPosition();
		}
		
	}
	
	/**
	 * An action handler that rotates the player avatar right, and maintains camera location
	 * @author Lucas
	 *
	 */
	private class AvatarTurnRightAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			Angle rotationSpeed = Degreef.createFrom(-1.2f);
			cameraTarget.yaw(rotationSpeed);
			cameraAzimuth -= 1.2f;
			cameraAzimuthMax -= 1.2;
			cameraAzimuthMin -= 1.2;
			if (cameraAzimuth < cameraAzimuthMin) { cameraAzimuth = cameraAzimuthMin; }
			if (cameraAzimuth > cameraAzimuthMax) { cameraAzimuth = cameraAzimuthMax; }
			updateCameraPosition();
		}
		
	}
	
	/**
	 * An action handler that controls 3P camera upwards elevation
	 * @author Lucas
	 *
	 */
	private class ElevateUpAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraElevation -= 1.2f;
			if (cameraElevation < 0.0f) { cameraElevation = 0.0f; }
			if (cameraElevation > 40.0f) { cameraElevation = 40.0f; }
			updateCameraPosition();
		}
	}
	
	/**
	 * An action handler that controls 3P camera downwards elevation
	 * @author Lucas
	 *
	 */
	private class ElevateDownAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraElevation += 1.2f;
			if (cameraElevation < 0.0f) { cameraElevation = 0.0f; }
			if (cameraElevation > 40.0f) { cameraElevation = 40.0f; }
			updateCameraPosition();
		}
	}
	
	/**
	 * An action handler that controls 3P camera elevation
	 * @author Lucas
	 *
	 */
	private class OrbitElevationAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			float rotation;
			if (e.getValue() < -0.2) {
				rotation = 1.2f;
			} else if (e.getValue() > 0.2) {
				rotation = -1.2f;
			} else 
				rotation = 0.0f;
			cameraElevation += rotation;
			if (cameraElevation < 0.0f) { cameraElevation = 0.0f; }
			if (cameraElevation > 40.0f) { cameraElevation = 40.0f; }
			updateCameraPosition();
		}
	}
	
	/**
	 * An action handler that controls 3P camera inwards zoom
	 * @author Lucas
	 *
	 */
	private class ZoomInAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			radius -= 0.02f;
			if (radius < 1.0f) { radius = 1.0f; }
			if (radius > 4.0f) { radius = 4.0f; }
			updateCameraPosition();
		}
	}
	
	/**
	 * An action handler that controls 3P camera outwards zoom
	 * @author Lucas
	 *
	 */
	private class ZoomOutAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			radius += 0.02f;
			if (radius < 1.0f) { radius = 1.0f; }
			if (radius > 4.0f) { radius = 4.0f; }
			updateCameraPosition();
		}
	}
	
	/**
	 * An action handler that controls 3P camera zoom
	 * @author Lucas
	 *
	 */
	private class OrbitRadiusAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			float rotation;
			if (e.getValue() < -0.2) {
				rotation = 0.02f;
			} else if (e.getValue() > 0.2) {
				rotation = -0.02f;
			} else 
				rotation = 0.0f;
			radius += rotation;
			if (radius < 1.0f) { radius = 1.0f; }
			if (radius > 4.0f) { radius = 4.0f; }
			updateCameraPosition();
		}
	}
	
}


