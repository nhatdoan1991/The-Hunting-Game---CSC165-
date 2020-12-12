package com.dsgames.game.myGameEngine.camera;

import java.util.ArrayList;

import com.dsgames.game.myGameEngine.controller.InputType;

import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.Component.Identifier.Axis;
import ray.input.InputManager;
import ray.input.InputManager.INPUT_ACTION_TYPE;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Camera3PController {
	
	
	private static final float MAX_ORBIT_SPEED = 2.0f;
	private static final float MIN_ORBIT_SPEED = 0.5f;

	private final SceneNode cameraNode; // the node the camera is attached to
	private final SceneNode cameraTarget; // the target the camera looks at
	private final InputManager inputManager; // the game input manager for this camera
	private final InputType inputType; // this controller type
	
	private float orbitSpeed = 1.0f;
	private float cameraAzimuth; // rotation of camera around Y axis
	private float cameraElevation; // elevation of camera above target
	private float maxAzimuth = 230.0f, minAzimuth = 130.0f;
	private float radius; // distance between camera and target
	private Vector3 worldUpVec; // the world's up vector
	
	private static final float MAX_ZOOM_OUT = 3.5f;
	private static final float MAX_ZOOM_IN = 2.5f;
	
	public Camera3PController(SceneNode cameraN, SceneNode target, InputType inputType, InputManager inputManager) {
		this.cameraNode = cameraN;
		this.cameraTarget = target;
		this.inputManager = inputManager;
		this.inputType = inputType;
		
		this.cameraAzimuth = 180.0f; // start from BEHIND and ABOVE the target
		this.cameraElevation = 20.0f; // elevation is in degrees
		this.radius = 3.0f;
		this.worldUpVec = Vector3f.createFrom(0.0f, 1.0f, 0.0f); // Y is UP
		setupInput(this.inputManager, this.inputType);
		updateCameraPosition();
	}
	
	public void updateCameraPosition() {
		final double theta = Math.toRadians(this.cameraAzimuth); // rot around the target
		final double phi = Math.toRadians(this.cameraElevation); // altitude angle
		final double x = this.radius * Math.cos(phi) * Math.sin(theta);
		final double y = this.radius * Math.sin(phi);
		final double z = this.radius * Math.cos(phi) * Math.cos(theta);
		this.cameraNode.setLocalPosition(Vector3f.createFrom((float)x, (float)y, (float)z).add(this.cameraTarget.getWorldPosition()));
		this.cameraNode.lookAt(this.cameraTarget, this.worldUpVec);
	}
	
	public Node getCameraTarget() {
		return this.cameraTarget;
	}
	
	private void setupInput(final InputManager inputManager, final InputType inputType) {
		final Action orbitAroundAction = new OrbitAroundAction();
		Action orbitRadiusAction = new OrbitRadiusAction();
		Action orbitElevationAction = new OrbitElevationAction();
		Action avatarTurnLeftAction = new AvatarTurnLeftAction();
		Action avatarTurnRightAction = new AvatarTurnRightAction();
		Action increaseOrbitSpeedAction = new IncreaseOrbitSpeedAction();
		Action decreaseOrbitSpeedAction = new DecreaseOrbitSpeedAction();
		
		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.Q, 
						avatarTurnLeftAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.E, 
						avatarTurnRightAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.RBRACKET, 
						increaseOrbitSpeedAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.LBRACKET, 
						decreaseOrbitSpeedAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			}
		}
		
		if (inputType == InputType.MOUSE) {
			for (Controller controller : inputManager.getControllers()) {
				if (controller.getType() == Controller.Type.MOUSE) {
					inputManager.associateAction(controller, Axis.X, orbitAroundAction, INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					inputManager.associateAction(controller, Axis.Y, orbitElevationAction, INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					inputManager.associateAction(controller, Axis.Z, orbitRadiusAction, INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				}
			}
		}
		else if (inputType == InputType.PAD) {
			for (Controller controller : inputManager.getControllers()) {
				if (controller.getType() == Controller.Type.GAMEPAD) {
					inputManager.associateAction(controller, Axis.RX, orbitAroundAction, INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					inputManager.associateAction(controller, Axis.RY, orbitElevationAction, INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
					inputManager.associateAction(controller, Axis.Z, orbitRadiusAction, INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				}
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
	
	private class IncreaseOrbitSpeedAction extends AbstractInputAction {

		@Override
		public void performAction(float time, Event evt) {
			orbitSpeed += 0.5f;
			System.out.println("Increasing orbit speed: " + orbitSpeed);
			if (orbitSpeed > MAX_ORBIT_SPEED) {
				System.out.println("Orbit speed at maximum!");
				orbitSpeed = MAX_ORBIT_SPEED;
			} else if (orbitSpeed < MIN_ORBIT_SPEED) {
				System.out.println("Orbit speed at minimum!");
				orbitSpeed = MIN_ORBIT_SPEED;
			}
			
		}
		
	}
	
	private class DecreaseOrbitSpeedAction extends AbstractInputAction {

		@Override
		public void performAction(float time, Event evt) {
			System.out.println("Decreasing orbit speed: " + orbitSpeed);
			orbitSpeed -= 0.5f;
			if (orbitSpeed > MAX_ORBIT_SPEED) {
				System.out.println("Orbit speed at maximum!");
				orbitSpeed = MAX_ORBIT_SPEED;
			} else if (orbitSpeed < MIN_ORBIT_SPEED) {
				System.out.println("Orbit speed at minimum!");
				orbitSpeed = MIN_ORBIT_SPEED;
			}
			
		}
		
	}
	
	/**
	 * Moves the camera around the target (changes camera azimuth).
	 *
	 */
	private class OrbitAroundAction extends AbstractInputAction {
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			float rotation;
			if (e.getValue() < -0.2) {
				rotation = orbitSpeed;
			} else if (e.getValue() > 0.2) {
				rotation = -orbitSpeed;
			} else 
				rotation = 0.0f;
			cameraAzimuth += rotation;
			if (cameraAzimuth < minAzimuth) { cameraAzimuth = minAzimuth; }
			if (cameraAzimuth > maxAzimuth) { cameraAzimuth = maxAzimuth; }
			updateCameraPosition();
		}
	}
	
	/**
	 * Moves the camera closer or further from the target (changes camera radius) - aka, zoom
	 * 
	 * Note, XBox controller Z axis zooms way too quickly, so there's a special case
	 * to make it slower below
	 *
	 */
	private class OrbitRadiusAction extends AbstractInputAction {
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			float rotation;
			if (e.getValue() < -0.2) {
				rotation = -0.02f;
			} else if (e.getValue() > 0.2) {
				rotation = 0.02f;
			} else 
				rotation = 0.0f;
			radius += rotation;
			if (radius < 1.0f) { radius = 1.0f; }
			if (radius > 4.0f) { radius = 4.0f; }
			updateCameraPosition();
		}
	}
	
	/**
	 * Moves the camera elevation
	 *
	 */
	private class OrbitElevationAction extends AbstractInputAction {
		public void performAction(float time, net.java.games.input.Event e) {
			float rotation;
			if (e.getValue() > -0.2) {
				rotation = orbitSpeed;
			} else if (e.getValue() < 0.2) {
				rotation = -orbitSpeed;
			} else 
				rotation = 0.0f;
			cameraElevation += rotation;
			if (cameraElevation < 0.0f) { cameraElevation = 0.0f; }
			if (cameraElevation > 40.0f) { cameraElevation = 40.0f; }
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
			cameraTarget.getParent().yaw(rotationSpeed);
			cameraAzimuth += 1.2f;
			maxAzimuth += 1.2;
			minAzimuth += 1.2;
			/*
			   Matrix3f = 	[  0.79652 |   0.00000 |  -0.60459]
    						[  0.00000 |   1.00000 |   0.00000]
    						[  0.60459 |   0.00000 |   0.79652]
    						
    		   Matrix3f = 	[  0.86602 |   0.00000 |  -0.50000]
    						[  0.00000 |   1.00000 |   0.00000]
    						[  0.50000 |   0.00000 |   0.86602]
			 */
			
			if (cameraAzimuth < minAzimuth) { cameraAzimuth = minAzimuth; }
			if (cameraAzimuth > maxAzimuth) { cameraAzimuth = maxAzimuth; }
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
			cameraTarget.getParent().yaw(rotationSpeed);
			cameraAzimuth -= 1.2f;
			maxAzimuth -= 1.2;
			minAzimuth -= 1.2;
			if (cameraAzimuth < minAzimuth) { cameraAzimuth = minAzimuth; }
			if (cameraAzimuth > maxAzimuth) { cameraAzimuth = maxAzimuth; }
			updateCameraPosition();
		}
		
	}
	
}
