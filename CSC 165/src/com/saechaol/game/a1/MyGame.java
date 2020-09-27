package com.saechaol.game.a1;

/**
 * A RAGE game in which you crash a dolphin through planets in space.
 * 
 * @author Lucas Saechao
 */

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.saechaol.game.myGameEngine.action.*;
import com.saechaol.game.myGameEngine.action.a1.*;
import com.saechaol.game.myGameEngine.camera.Camera3PController;
import com.saechaol.game.myGameEngine.object.manual.*;

import net.java.games.input.Controller;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rage.util.Configuration;
import ray.rml.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.input.*;
import ray.input.action.*;

public class MyGame extends VariableFrameRateGame {
	
	private InputManager inputManager;
	private static final Random RAND = new Random();
	private static final String SPACE_SKYBOX = "SpaceSkyBox";
	public Camera camera;
	public Camera3PController orbitCameraController;
	public SceneNode cameraNode, dolphinNode, dolphinCamera, originNode;
	private Controller controller;
	private Action toggleCameraAction, rollCameraLeftAction, rollCameraRightAction, invertYawAction, rightStickXAction, rightStickYAction, leftStickXAction, leftStickYAction, moveCameraUpAction, moveCameraDownAction, moveCameraBackwardAction, moveCameraLeftAction, moveCameraRightAction, moveCameraForwardAction, pitchCameraUpAction, pitchCameraDownAction, yawCameraLeftAction, yawCameraRightAction, rideDolphinToggleAction, exitGameAction, pauseGameAction;
	GL4RenderSystem renderSystem; // Initialized to minimize variable allocation in update()
	float elapsedTime = 0.0f;
	String elapsedTimeString, livesString, displayString, positionString, dolphinString;
	int elapsedTimeSeconds, lives = 3, score = 0;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	public boolean toggleRide = false, invertYaw = true, alive = true, thirdPerson = false;
	public HashMap<SceneNode, Boolean> activePlanets = new HashMap<SceneNode, Boolean>();
	ArrayList<SceneNode> planetNodes = new ArrayList<SceneNode>();
	ArrayList<SceneNode> cubeMoonNodes = new ArrayList<SceneNode>();
	ArrayList<OrbitController> galaxyOrbitController = new ArrayList<OrbitController>();
	ArrayList<OrbitController> planetOrbitController = new ArrayList<OrbitController>();
	
	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ROTATE CAMERA");
		System.out.println("Press 'Q/E' or the left and right bumpers to ROLL CAMERA");
		System.out.println("Press 'V' or 'Y' to INVERT YAW");
		System.out.println("Press 'LSHIFT' or the left stick to ASCEND");
		System.out.println("Press 'C' or the right stick to DESCEND");
		System.out.println("Press 'Space' or 'A' to RIDE/HOP OFF DOLPHIN");
		System.out.println("Press 'ESC' or 'Select' to EXIT");
		System.out.println("Press 'TAB' or 'Start' to TOGGLE 3P CAMERA");
		System.out.println("----------------------------------------------------");
		formatFloat.setRoundingMode(RoundingMode.DOWN);
	}

	/**
	 * Implements VariableFrameRateEngine.setupWindow()
	 * Initializes a window size 1920x1080 for displays larger than 1080p,
	 * and a 1280x720 window for displays smaller
	 * 
	 * @param renderSystem
	 * @param graphicsEnvironment
	 */
	@Override
	protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
		int displayHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		int displayWidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		if (displayHeight > 1920 && displayWidth > 1080)
			renderSystem.createRenderWindow(new DisplayMode(1920, 1080, 24, 60), false);
		else
			renderSystem.createRenderWindow(new DisplayMode(1280, 720, 24, 60), false);
	}
	
	/**
	 * Initializes a perspective camera and adds it to the scene graph
	 * 
	 * @param sceneManager
	 * @param renderWindow
	 */
	@Override
	protected void setupCameras(SceneManager sceneManager, RenderWindow renderWindow) {
		// initialize the camera and add it to the viewport
		SceneNode rootNode = sceneManager.getRootSceneNode();
		camera = sceneManager.createCamera("mainCamera", Projection.PERSPECTIVE);
		renderWindow.getViewport(0).setCamera(camera);
		camera.setMode('n');
		// initialize the camera frustum and set its position to the origin
		camera.setRt( (Vector3f) Vector3f.createFrom(-1.0f, 0.0f, 0.0f));
		camera.setUp( (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 1.0f));
		camera.setPo( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f));
		
		// initialize the cameraNode, add it to the scene graph and then attach the camera to it
		cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
		cameraNode.attachObject(camera);
	}

	/**
	 * Initializes the game's scene with a single dolphin entity, an ambient and point light,
	 * and rotates it about its Y axis
	 * 
	 * @param engine
	 * @param sceneManager
	 */
	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		// initialize input manager
		setupInputs();
		RenderSystem renderSystem = sceneManager.getRenderSystem();
		// initialize the dolphin entity
		Entity dolphinEntity = sceneManager.createEntity("dolphinEntity", "dolphinHighPoly.obj");
		dolphinEntity.setPrimitive(Primitive.TRIANGLES);
		
		// initialize world axes
		ManualAxisLineObject.renderWorldAxes(engine, sceneManager);
		
		// initialize planets
		Entity[] planetEntities = new Entity[6];
		ManualObject[] cubeMoonEntities = new ManualObject[planetEntities.length];
		ZBufferState zState = (ZBufferState) renderSystem.createRenderState(RenderState.Type.ZBUFFER);
		zState.setEnabled(true);
		for (int i = 0; i < planetEntities.length; i++) { 
			planetEntities[i] = sceneManager.createEntity("planetEntity" + i, "earth.obj");
			cubeMoonEntities[i] = ManualCubeObject.makeCubeObject(engine, sceneManager, Integer.toString(i));
			planetEntities[i].setPrimitive(Primitive.TRIANGLES);
			cubeMoonEntities[i].setPrimitive(Primitive.TRIANGLES);
			planetEntities[i].setRenderState(zState);
			cubeMoonEntities[i].setRenderState(zState);
			SceneNode planetNode = sceneManager.getRootSceneNode().createChildSceneNode(planetEntities[i].getName() + "Node");
			SceneNode cubeMoonNode = sceneManager.getRootSceneNode().createChildSceneNode(cubeMoonEntities[i].getName() + "Node");
			planetNodes.add(planetNode);
			cubeMoonNodes.add(cubeMoonNode);
		}
		
		// randomized coordinates from <-40, -40, -40> to <40, 40, 40>
		float[][] randomPlanetCoordinates = new float[planetEntities.length][3];
		for(float[] coordinates : randomPlanetCoordinates) {
			coordinates = randomFloatArray(79.9f);
		}
		
		for (int i = 0; i < randomPlanetCoordinates.length; i++) {
			for (int j = 0; j < randomPlanetCoordinates[i].length; j++) {
				float dec = RAND.nextFloat();
				// negate this value
				if (RAND.nextBoolean()) {
					randomPlanetCoordinates[i][j] *= -1.0f;
				}
				
				// add or subtract the decimal value
				if (RAND.nextBoolean()) {
					randomPlanetCoordinates[i][j] += dec;
				} else {
					randomPlanetCoordinates[i][j] -= dec;
				}
			}
		}
		
		// set planet nodes and initialize orbit controllers
		originNode = sceneManager.getRootSceneNode().createChildSceneNode("originNode");
		originNode.setLocalPosition(0.0f, 0.0f, 0.0f);
		
		for (int i = 0; i < planetEntities.length; i++) {
			float[] orbitParameters = { 
				RAND.nextFloat() * 0.1f, 
				(RAND.nextFloat() * 50.0f) + 15.0f,
				(RAND.nextFloat() * 50.0f) + 15.0f,
			};
			if (RAND.nextBoolean()) {
				orbitParameters[2] *= -1.0f;
			}
			galaxyOrbitController.add(new OrbitController(originNode, orbitParameters[0], orbitParameters[1], orbitParameters[2], RAND.nextBoolean()));
		}
		
		for (int i = 0; i < planetNodes.size(); i++) {
			planetNodes.get(i).setLocalPosition(randomPlanetCoordinates[i][0], randomPlanetCoordinates[i][1], randomPlanetCoordinates[i][2]);
			cubeMoonNodes.get(i).setLocalPosition(randomPlanetCoordinates[i][0], randomPlanetCoordinates[i][1], randomPlanetCoordinates[i][2]);
			planetNodes.get(i).attachObject(planetEntities[i]);
			cubeMoonNodes.get(i).attachObject(cubeMoonEntities[i]);
			float[] orbitParameters = { 
					RAND.nextFloat() * 5.0f, 
					(RAND.nextFloat() * 20.0f) + 5.0f,
				};
			planetOrbitController.add(new OrbitController(planetNodes.get(i), orbitParameters[0], orbitParameters[1], 0.0f, RAND.nextBoolean()));
			galaxyOrbitController.get(i).addNode(planetNodes.get(i));
			planetOrbitController.get(i).addNode(cubeMoonNodes.get(i));
			activePlanets.put(planetNodes.get(i), true);
		}
		
		// add orbit controllers to scene
		for (int i = 0; i < planetNodes.size(); i++) {
			sceneManager.addController(galaxyOrbitController.get(i));
			sceneManager.addController(planetOrbitController.get(i));
		}
		
		// initialize a rotation controller for the orbits, moons and planets
		for (SceneNode planet : planetNodes) {
			RotationController planetRotation = randomRotation();
			planetRotation.addNode(planet);
			sceneManager.addController(planetRotation);
		}
		
		for (SceneNode cubeMoon : cubeMoonNodes) {
			RotationController moonRotation = randomRotation();
			moonRotation.addNode(cubeMoon);
			sceneManager.addController(moonRotation);
		}
		
		// initialize the dolphin node and add it to the scene graph
		dolphinNode = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntity.getName() + "Node");
		dolphinNode.moveBackward(2.0f);
		dolphinNode.attachObject(dolphinEntity);
		
		dolphinCamera = dolphinNode.createChildSceneNode("dolphinCamera");
		dolphinCamera.moveBackward(0.3f);
		dolphinCamera.moveUp(0.3f);
		dolphinCamera.moveRight(0.01f);
		dolphinCamera.attachObject(camera);
		
		// initialize the ambient light
		sceneManager.getAmbientLight().setIntensity(new Color(0.01f, 0.01f, 0.01f));
		
		// initialize a point light
		Light pointLightOne = sceneManager.createLight("pointLightOne", Light.Type.POINT);
		pointLightOne.setAmbient(new Color(0.05f, 0.05f, 0.05f));
		pointLightOne.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightOne.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		pointLightOne.setRange(5.0f);
		
		// initialize a node for pointLightOne and add it to the scene graph
		SceneNode pointLightNode = sceneManager.getRootSceneNode().createChildSceneNode(pointLightOne.getName() + "Node");
		pointLightNode.attachObject(pointLightOne);
		pointLightNode.setLocalPosition(Vector3f.createFrom(0.0f, 0.0f, 0.0f));
		
		// initialize a flashlight
		Light pointLightFlash = sceneManager.createLight("pointLightFlash", Light.Type.POINT);
		pointLightFlash.setAmbient(new Color(1.0f, 1.0f, 1.0f));
		pointLightFlash.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightFlash.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		pointLightFlash.setRange(20.0f);
		
		// attach the flashlight to the dolphin
		cameraNode.attachObject(pointLightFlash);
		
		Configuration configuration = engine.getConfiguration();
		
		// manually assign textures
		TextureManager textureManager = engine.getTextureManager();
		Texture dolphinTexture = textureManager.getAssetByPath("Dolphin_HighPolyUV.png");
		Texture moonTexture = textureManager.getAssetByPath("moon.jpeg");
		
		Texture[] planetTextures = {
			textureManager.getAssetByPath("earth-day.jpeg"),
			textureManager.getAssetByPath("blue.jpeg"),
			textureManager.getAssetByPath("hexagons.jpeg"),
			textureManager.getAssetByPath("earth-night.jpeg"),
			textureManager.getAssetByPath("red.jpeg"),
			textureManager.getAssetByPath("chain-fence.jpeg")
			
		};
		
		// initialize skybox
		SkyBox worldSkybox = sceneManager.createSkyBox(SPACE_SKYBOX);
		
		// initialize skybox textures
		textureManager.setBaseDirectoryPath(configuration.valueOf("assets.skyboxes.path.a1"));
		Texture skyboxFrontTexture = textureManager.getAssetByPath("spaceSkyboxFront.jpg");
		Texture skyboxBackTexture = textureManager.getAssetByPath("spaceSkyboxBack.jpg");
		Texture skyboxLeftTexture = textureManager.getAssetByPath("spaceSkyboxLeft.jpg");
		Texture skyboxRightTexture = textureManager.getAssetByPath("spaceSkyboxRight.jpg");
		Texture skyboxTopTexture = textureManager.getAssetByPath("spaceSkyboxTop.jpg");
		Texture skyboxBottomTexture = textureManager.getAssetByPath("spaceSkyboxBottom.jpg");
		
		// transform skybox textures
		AffineTransform skyboxAffineTransform = new AffineTransform();
		skyboxAffineTransform.translate(0.0, skyboxFrontTexture.getImage().getHeight());
		skyboxAffineTransform.scale(1.0, 1.0);
		skyboxFrontTexture.transform(skyboxAffineTransform);
		skyboxBackTexture.transform(skyboxAffineTransform);
		skyboxLeftTexture.transform(skyboxAffineTransform);
		skyboxRightTexture.transform(skyboxAffineTransform);
		skyboxTopTexture.transform(skyboxAffineTransform);
		skyboxBottomTexture.transform(skyboxAffineTransform);
		
		// set skybox textures
		worldSkybox.setTexture(skyboxFrontTexture, SkyBox.Face.FRONT);
		worldSkybox.setTexture(skyboxBackTexture, SkyBox.Face.BACK);
		worldSkybox.setTexture(skyboxLeftTexture, SkyBox.Face.LEFT);
		worldSkybox.setTexture(skyboxRightTexture, SkyBox.Face.RIGHT);
		worldSkybox.setTexture(skyboxTopTexture, SkyBox.Face.TOP);
		worldSkybox.setTexture(skyboxBottomTexture, SkyBox.Face.BOTTOM);
		
		// assign skybox to sceneManager
		sceneManager.setActiveSkyBox(worldSkybox);
		
		// initialize texture states
		TextureState dolphinTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState moonTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState[] planetTextureStates = new TextureState[planetTextures.length];

		dolphinTextureState.setTexture(dolphinTexture);
		dolphinEntity.setRenderState(dolphinTextureState);
		
		moonTextureState.setTexture(moonTexture);
		for (ManualObject cubeMoonEntity : cubeMoonEntities) {
			cubeMoonEntity.setRenderState(moonTextureState);
		}
		
		for (int i = 0; i < planetTextures.length; i++) {
			planetTextureStates[i] = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
			planetTextureStates[i].setTexture(planetTextures[i]);
		}
		
		for (int i = 0; i < planetEntities.length; i++) {
			planetEntities[i].setRenderState(planetTextureStates[i]);
		}
		
	}
	
	protected void setupOrbitCamera(Engine engine, SceneManager sceneManager) {
		String kb = inputManager.getFirstGamepadName();
		orbitCameraController = new Camera3PController(camera, cameraNode, dolphinNode, kb, inputManager);
	}
	
	/**
	 * Initializes controller inputs
	 */
	protected void setupInputs() {
		if (inputManager == null)
			inputManager = new GenericInputManager();

		String gamepadName = inputManager.getFirstGamepadName();
		controller = inputManager.getControllerByName(gamepadName);
		
		// Build action objects for listening to user input
		exitGameAction = new ExitGameAction(this);
		moveCameraForwardAction = new MoveCameraForwardAction(this, camera);
		moveCameraBackwardAction = new MoveCameraBackwardAction(this, camera);
		moveCameraLeftAction = new MoveCameraLeftAction(this, camera);
		moveCameraRightAction = new MoveCameraRightAction(this, camera);
		moveCameraUpAction = new MoveCameraUpAction(this, camera);
		moveCameraDownAction = new MoveCameraDownAction(this, camera);
		leftStickXAction = new LeftStickXAction(this, camera);
		leftStickYAction = new LeftStickYAction(this, camera);
		rideDolphinToggleAction = new RideDolphinToggleAction(this);
		yawCameraLeftAction = new YawCameraLeftAction(this);
		yawCameraRightAction = new YawCameraRightAction(this);
		pitchCameraUpAction = new PitchCameraUpAction(this);
		pitchCameraDownAction = new PitchCameraDownAction(this);
		rollCameraLeftAction = new RollCameraLeftAction(this);
		rollCameraRightAction = new RollCameraRightAction(this);
		rightStickXAction = new RightStickXAction(this, camera);
		rightStickYAction = new RightStickYAction(this, camera);
		invertYawAction = new InvertYawAction(this);
		toggleCameraAction = new ToggleCameraAction(this, camera, inputManager);
		
		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				// Bind exit action to escape, and gamepad 6 (select)
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.ESCAPE, 
						exitGameAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.TAB, 
						toggleCameraAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				
				// Bind movement actions
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.W, 
						moveCameraForwardAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	

				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.S,
						moveCameraBackwardAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.A,
						moveCameraLeftAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.D, 
						moveCameraRightAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.Q, 
						rollCameraLeftAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.E, 
						rollCameraRightAction, 
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.LSHIFT,
						moveCameraUpAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.C, 
						moveCameraDownAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.V, 
						invertYawAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				
				// Bind dolphin toggle
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.SPACE,
						rideDolphinToggleAction,
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
				
				// Bind rotational movement to arrow keys
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.LEFT,
						yawCameraLeftAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.RIGHT,
						yawCameraRightAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.UP,
						pitchCameraUpAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.DOWN,
						pitchCameraDownAction,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			}
		}
		
		if (isGamepadNull(gamepadName)) {
			System.out.println("No gamepad attached!");
		} else {
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._6, 
					exitGameAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._7, 
					toggleCameraAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._3, 
					invertYawAction, 
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			// Left and Right shoulder buttons
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._4, 
					rollCameraLeftAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._5, 
					rollCameraRightAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			// Left and Right stick button
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._8, 
					moveCameraUpAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Button._9, 
					moveCameraDownAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName,
					net.java.games.input.Component.Identifier.Button._0,
					rideDolphinToggleAction,
					InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.Y, 
					leftStickYAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.X, 
					leftStickXAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.RX, 
					rightStickXAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.RY, 
					rightStickYAction, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
		}
	}

	/**
	 * Updates and redraws the viewport with running elapsed time and keyboard count
	 * 
	 * @param engine
	 */
	@Override
	protected void update(Engine engine) {
		renderSystem = (GL4RenderSystem) engine.getRenderSystem();
		elapsedTime += engine.getElapsedTimeMillis();
		elapsedTimeSeconds = Math.round(elapsedTime / 1000.0f);
		elapsedTimeString = Integer.toString(elapsedTimeSeconds);
		livesString = Integer.toString(lives);
		displayString = "Time = " + elapsedTimeString;
		displayString += " | Lives = " + livesString;
		displayString += " | Score = " + score;
		displayString += " | Camera position: (" + formatFloat.format(camera.getPo().x()) + ", " + formatFloat.format(camera.getPo().y()) + ", " + formatFloat.format(camera.getPo().z()) + ")";
		displayString += " | Dolphin position: (" + formatFloat.format(dolphinNode.getWorldPosition().x()) + ", " + formatFloat.format(dolphinNode.getWorldPosition().y()) + ", " + formatFloat.format(dolphinNode.getWorldPosition().z()) + ")";
		renderSystem.setHUD(displayString, 15, 15);
		inputManager.update(elapsedTime);
		checkPlayerDistanceToDolphin(10.0f);
		if (elapsedTime > 500.0) {
			moonCollisionDetection();
			planetCollisionDetection();
		}
		if (thirdPerson) {
			synchronize3PDolphinCameraPosition(orbitCameraController.updateCameraPosition());
		} else {
			synchronizePlayerDolphinPosition();
		}
	}
	
	/**
	 * Returns a random rotation controller object
	 * @return
	 */
	private RotationController randomRotation() {
		int axisSwitch = RAND.nextInt(3);
		RotationController rotationController;
		switch (axisSwitch) {
			case 1:
				rotationController = new RotationController(Vector3f.createUnitVectorX(), RAND.nextFloat());
				break;
			case 2:
				rotationController = new RotationController(Vector3f.createUnitVectorY(), RAND.nextFloat());
				break;
			default:
				rotationController = new RotationController(Vector3f.createUnitVectorZ(), RAND.nextFloat());
		}
		return rotationController;
	}
	
	private void synchronize3PDolphinCameraPosition(Vector3 position) {
		camera.setPo((Vector3f) position);
	}
	
	/**
	 * Ensures that the player's position is the same as the dolphin's when riding the dolphin
	 */
	private void synchronizePlayerDolphinPosition() {
		if (!toggleRide) {
			camera.setPo((Vector3f) Vector3f.createFrom(dolphinNode.getLocalPosition().x(), dolphinNode.getLocalPosition().y(), dolphinNode.getLocalPosition().z()));
			camera.setRt((Vector3f) dolphinNode.getLocalRightAxis().mult(-1.0f));
			camera.setUp((Vector3f) dolphinNode.getLocalUpAxis());
			camera.setFd((Vector3f) dolphinNode.getLocalForwardAxis());
		}
		cameraNode.setLocalPosition(camera.getPo());
	}
	
	/**
	 * Places the player back on the dolphin if they stray 10 units too far in any direction
	 * The boundary is spherical rather than a bounded X box
	 */
	private void checkPlayerDistanceToDolphin(float radius) {
		Vector3f playerPosition = (Vector3f) camera.getPo();
		Vector3f dolphinPosition = (Vector3f) dolphinNode.getLocalPosition();
		
		if ((Math.pow((playerPosition.x() - dolphinPosition.x()), 2) + Math.pow((playerPosition.y() - dolphinPosition.y()), 2) + Math.pow((playerPosition.z() - dolphinPosition.z()), 2)) > Math.pow(radius, 2.0f)) {
			System.out.println("You're too far! Position: (" + playerPosition.x() + ", " + playerPosition.y() + ", " + playerPosition.z() + ")");
			((RideDolphinToggleAction) rideDolphinToggleAction).manualAction();
			decrementLives();
		}
	}
	
	/**
	 * Detects planet collision detection
	 */
	private void planetCollisionDetection() {
		activePlanets.forEach((k, v) -> {
			if (v) {
				Vector3f playerPosition = (Vector3f) camera.getPo();
				Vector3f planetPosition = (Vector3f) k.getLocalPosition();
				if (toggleRide && (Math.pow((playerPosition.x() - planetPosition.x()), 2) + Math.pow((playerPosition.y() - planetPosition.y()), 2) + Math.pow((playerPosition.z() - planetPosition.z()), 2)) < Math.pow((2.15f), 2.0f)) {
					System.out.println("Score!");
					incrementScore();
					activePlanets.put(k, false);
				}
			}
		});
	}
	
	/**
	 * 
	 */
	private void moonCollisionDetection() {
		Iterator<SceneNode> activeMoonsIterator = cubeMoonNodes.iterator();
		activeMoonsIterator.forEachRemaining(node -> {
			if (toggleRide) {
				Vector3f playerPosition = (Vector3f) camera.getPo();
				if ((Math.pow((playerPosition.x() - node.getLocalPosition().x()), 2) + Math.pow((playerPosition.y() - node.getLocalPosition().y()), 2) + Math.pow((playerPosition.z() - node.getLocalPosition().z()), 2)) < Math.pow((2.15f), 2.0f)) {
					((RideDolphinToggleAction) rideDolphinToggleAction).manualAction();
					decrementLives();
				}
			} else {
				Vector3f dolphinPosition = (Vector3f) dolphinNode.getLocalPosition();
				if ((Math.pow((dolphinPosition.x() - node.getLocalPosition().x()), 2) + Math.pow((dolphinPosition.y() - node.getLocalPosition().y()), 2) + Math.pow((dolphinPosition.z() - node.getLocalPosition().z()), 2)) < Math.pow((2.15f), 2.0f)) {
					dolphinNode.setLocalPosition(0.0f, 0.0f, 0.0f);
					decrementLives();
				}
			}
		});
	}
	
	/**
	 * Inverts Yaw rotation for those who like inverted controls
	 */
	public void invertYaw() {
		if (invertYaw)
			invertYaw = false;
		else
			invertYaw = true;
	}
	
	public void toggleCamera() {
		if (thirdPerson)
			thirdPerson = false;
		else
			thirdPerson = true;
	}
	
	/**
	 * Increments score
	 */
	private void incrementScore() {
		System.out.println("Score incremented!");
		score++;
	}
	
	private void decrementLives() {
		if (lives > 0) {
			System.out.println("Damage taken! ");
			lives--;
		} else if (lives == 0) {
			System.out.print("You are dead. Game over :(");
			this.setState(Game.State.STOPPING);
		}
	}
	
	/**
	 * Checks if there is a functioning gamepad attached to the system
	 * @param gamepadName
	 * @return
	 */
	public boolean isGamepadNull(String gamepadName) {
		if (gamepadName == null) {
			return true;
		} else 
			return false;
	}
	
	public void reinitializeInputs() {
		setupInputs();
		dolphinNode.attachChild(dolphinCamera);
		dolphinCamera.attachObject(camera);
		toggleCamera();
	}
	
	/**
	 * Returns a random float array
	 * @param args
	 */
	private float[] randomFloatArray(float upperBound) {
		float[] randomFloat = {
				(RAND.nextFloat() * upperBound), (RAND.nextFloat() * upperBound), (RAND.nextFloat() * upperBound)
		};
		return randomFloat;
	}
	
	public static void main(String[] args) {
		System.out.println("MyGame.main() running!");
		Game game = new MyGame();
		try {
			game.startup();
			game.run();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			game.shutdown();
			game.exit();
		}
	}

}
