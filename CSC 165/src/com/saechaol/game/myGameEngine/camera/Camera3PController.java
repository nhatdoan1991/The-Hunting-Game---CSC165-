package com.saechaol.game.myGameEngine.camera;

import java.util.ArrayList;

import net.java.games.input.Controller;
import net.java.games.input.Event;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Quaternion;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Camera3PController {

	private Camera camera;
	private SceneNode cameraNode;
	private SceneNode cameraTarget;
	private float cameraAzimuth, cameraElevation, radius;
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
			}
		}
		
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
					null, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(controllerName, 
					net.java.games.input.Component.Identifier.Button._5, 
					null, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}

	}
	
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
			cameraAzimuth = cameraAzimuth % 360;
	//		if (cameraAzimuth < 30.0f) { cameraAzimuth = 30.0f; }
	//		if (cameraAzimuth > 30.0f) { cameraAzimuth = 330.0f; }
			updateCameraPosition();
		}
		
	}
	
	private class YawLeftAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraAzimuth += 1.2f;
			cameraAzimuth = cameraAzimuth % 360;
			if (cameraAzimuth < 30.0f) { cameraAzimuth = 30.0f; }
			if (cameraAzimuth > 330.0f) { cameraAzimuth = 330.0f; }
			updateCameraPosition();
		}
		
	}
	
	private class YawRightAction extends AbstractInputAction {

		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraAzimuth -= 1.2f;
			cameraAzimuth = cameraAzimuth % 360;
			if (cameraAzimuth < 30.0f) { cameraAzimuth = 30.0f; }
			if (cameraAzimuth > 330.0f) { cameraAzimuth = 330.0f; }
			updateCameraPosition();
		}
		
	}
	
	private class ElevateUpAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraElevation -= 1.2f;
			if (cameraElevation < -40.0f) { cameraElevation = -40.0f; }
			if (cameraElevation > 40.0f) { cameraElevation = 40.0f; }
			updateCameraPosition();
		}
	}
	
	private class ElevateDownAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			cameraElevation += 1.2f;
			if (cameraElevation < -40.0f) { cameraElevation = -40.0f; }
			if (cameraElevation > 40.0f) { cameraElevation = 40.0f; }
			updateCameraPosition();
		}
	}
	
	
	
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
			if (cameraElevation < -40.0f) { cameraElevation = -40.0f; }
			if (cameraElevation > 40.0f) { cameraElevation = 40.0f; }
			updateCameraPosition();
		}
	}
	
	private class ZoomInAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			radius -= 0.02f;
			if (radius < 1.0f) { radius = 1.0f; }
			if (radius > 4.0f) { radius = 4.0f; }
			updateCameraPosition();
		}
	}
	
	private class ZoomOutAction extends AbstractInputAction {
		
		@Override
		public void performAction(float time, net.java.games.input.Event e) {
			radius += 0.02f;
			if (radius < 1.0f) { radius = 1.0f; }
			if (radius > 4.0f) { radius = 4.0f; }
			updateCameraPosition();
		}
	}
	
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


