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
import ray.audio.AudioManager;
import ray.audio.AudioManagerFactory;
import ray.audio.AudioResource;
import ray.audio.AudioResourceType;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.audio.SoundType;
import ray.input.*;
import ray.input.action.*;

public class MyGame extends VariableFrameRateGame {
	
	private InputManager inputManager;
	private static final Random RAND = new Random();
	private static final String SPACE_SKYBOX = "SpaceSkyBox";
	private Sound[] music = new Sound[3];
	private Sound[] sfx = new Sound[2];
	public Camera camera;
	public SceneNode cameraNode, dolphinNode, dolphinCamera, originNode;
	private Controller controller;
	private Action skipSongAction, toggleCameraAction, rollCameraLeftAction, rollCameraRightAction, invertYawAction, rightStickXAction, rightStickYAction, leftStickXAction, leftStickYAction, moveCameraUpAction, moveCameraDownAction, moveCameraBackwardAction, moveCameraLeftAction, moveCameraRightAction, moveCameraForwardAction, pitchCameraUpAction, pitchCameraDownAction, yawCameraLeftAction, yawCameraRightAction, rideDolphinToggleAction, exitGameAction, pauseGameAction;
	GL4RenderSystem renderSystem; // Initialized to minimize variable allocation in update()
	float elapsedTime = 0.0f;
	String elapsedTimeString, livesString, displayString, positionString, dolphinString;
	int elapsedTimeSeconds, lives = 3, score = 0, currentSong = 0;
	public Camera3PController orbitCameraController;
	private int totalPlanetCount = 0;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	private TextureManager textureManager;
	private Texture[] planetTextures;
	private Texture moonTexture, starTexture;
	private ZBufferState zState;
	public boolean toggleRide = false, invertYaw = true, alive = true, thirdPerson = false;
	private boolean pauseSong = false;
	public HashMap<SceneNode, Boolean> activePlanets = new HashMap<SceneNode, Boolean>();
	public IAudioManager audioManager;
	ArrayList<SceneNode> planetNodes = new ArrayList<SceneNode>();
	ArrayList<SceneNode> cubeMoonNodes = new ArrayList<SceneNode>();
	ArrayList<OrbitController> galaxyOrbitController = new ArrayList<OrbitController>();
	ArrayList<OrbitController> planetOrbitController = new ArrayList<OrbitController>();
	private OrbitController dolphinOrbitController;
	ArrayList<RotationController> planetRotationControllers = new ArrayList<RotationController>();
	ArrayList<RotationController> cubeMoonRotationControllers = new ArrayList<RotationController>();
	
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
		System.out.println("Press 'P' or 'X' to PLAY NEXT SONG");
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
		if (textureManager == null) {
			textureManager = this.getEngine().getTextureManager();
		}
		
		// initialize zState
		zState = (ZBufferState) renderSystem.createRenderState(RenderState.Type.ZBUFFER);
		zState.setEnabled(true);
		
		planetTextures = new Texture[6];
		planetTextures[0] = textureManager.getAssetByPath("earth-day.jpeg");
		planetTextures[1] = textureManager.getAssetByPath("blue.jpeg");
		planetTextures[2] = textureManager.getAssetByPath("hexagons.jpeg");
		planetTextures[3] = textureManager.getAssetByPath("earth-night.jpeg");
		planetTextures[4] = textureManager.getAssetByPath("red.jpeg");
		planetTextures[5] = textureManager.getAssetByPath("chain-fence.jpeg");
		moonTexture = textureManager.getAssetByPath("moon.jpeg");
		starTexture = textureManager.getAssetByPath("star.png");
		
		// initialize the dolphin entity
		Entity dolphinEntity = sceneManager.createEntity("dolphinEntity", "dolphinHighPoly.obj");
		dolphinEntity.setPrimitive(Primitive.TRIANGLES);
		
		// initialize world axes
		ManualAxisLineObject.renderWorldAxes(engine, sceneManager);
		
		// origin node for orbit controller
		originNode = sceneManager.getRootSceneNode().createChildSceneNode("originNode");
		originNode.setLocalPosition(0.0f, 0.0f, 0.0f);
		
		// initialize planets
		for (int i = 0; i < 6; i++) { 
			activePlanets.put(instantiateNewPlanet(engine, sceneManager), true);
		}
		
		// initialize the dolphin node and add it to the scene graph
		dolphinNode = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntity.getName() + "Node");
		dolphinNode.moveBackward(2.0f);
		dolphinNode.attachObject(dolphinEntity);
		
		dolphinOrbitController = new OrbitController(dolphinNode, 1.0f, 0.5f, 0.0f, false);
		sceneManager.addController(dolphinOrbitController);
		orbitCameraController = new Camera3PController(camera, cameraNode, dolphinNode, inputManager.getFirstGamepadName(), inputManager);
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
		
		initializeSkybox(engine, sceneManager);
		
		// manually assign textures
		Texture dolphinTexture = textureManager.getAssetByPath("Dolphin_HighPolyUV.png");
		TextureState dolphinTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		dolphinTextureState.setTexture(dolphinTexture);
		dolphinEntity.setRenderState(dolphinTextureState);
		initializeAudio(sceneManager);
	}
	
	protected void initializeAudio(SceneManager sceneManager) {
		Configuration configuration = sceneManager.getConfiguration();
		String sfxPath = configuration.valueOf("assets.sounds.path.sfx");
		String musicPath = configuration.valueOf("assets.sounds.path.music");
		AudioResource gymnopedieOne, gymnopedieTwo, gymnopedieThree, scoreSfx, destroySfx;
		audioManager = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");
		
		if (!audioManager.initialize()) {
			System.out.println("The Audio Manager failed to initialize :(");
			return;
		}
		
		gymnopedieOne = audioManager.createAudioResource(musicPath + "gymnopedie_one.wav", AudioResourceType.AUDIO_STREAM);
		gymnopedieTwo = audioManager.createAudioResource(musicPath + "gymnopedie_two.wav", AudioResourceType.AUDIO_STREAM);
		gymnopedieThree = audioManager.createAudioResource(musicPath + "gymnopedie_three.wav", AudioResourceType.AUDIO_STREAM);
		scoreSfx = audioManager.createAudioResource(sfxPath + "score.wav", AudioResourceType.AUDIO_SAMPLE);
		destroySfx = audioManager.createAudioResource(sfxPath + "destroyed.wav", AudioResourceType.AUDIO_SAMPLE);
	
		music[0] = new Sound(gymnopedieOne, SoundType.SOUND_MUSIC, 100, false);
		music[1] = new Sound(gymnopedieTwo, SoundType.SOUND_MUSIC, 100, false);
		music[2] = new Sound(gymnopedieThree, SoundType.SOUND_MUSIC, 100, false);
		sfx[0] = new Sound(scoreSfx, SoundType.SOUND_EFFECT, 25, false);
		sfx[1] = new Sound(destroySfx, SoundType.SOUND_EFFECT, 25, false);
		
		for (Sound m : music) {
			m.initialize(audioManager);
		}
		
		for (Sound s : sfx) {
			s.initialize(audioManager);
		}
		
		music[currentSong].play();
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
		skipSongAction = new SkipSongAction(this);
		
		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				// Bind exit action to escape, and gamepad 6 (select)
				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.ESCAPE, 
						exitGameAction, 
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				inputManager.associateAction(keyboards, 
						net.java.games.input.Component.Identifier.Key.P, 
						skipSongAction, 
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
			System.out.println("No gamepad detected!");
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
					net.java.games.input.Component.Identifier.Button._2, 
					skipSongAction, 
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
			replacePlanet();
		}
		if (thirdPerson) {
			synchronize3PDolphinCameraPosition(orbitCameraController.updateCameraPosition());
		} else {
			synchronizePlayerDolphinPosition();
		}
	}
	
	public void playAudio() {
		music[currentSong].stop();
		currentSong++;
		currentSong %= 3;
		music[currentSong].play();
	}
	
	private void initializeSkybox(Engine engine, SceneManager sceneManager) throws IOException {
		// initialize skybox
		SkyBox worldSkybox = sceneManager.createSkyBox(SPACE_SKYBOX);
		Configuration configuration = engine.getConfiguration();
		
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
		textureManager.setBaseDirectoryPath(configuration.valueOf("assets.textures.path"));
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
					try {
						incrementScore();
					} catch (IOException e) {
						e.printStackTrace();
					}
					activePlanets.put(k, false);
				}
			}
		});
	}
	
	private void replacePlanet() {
		HashMap<SceneNode, Boolean> currentlyActive = new HashMap<SceneNode, Boolean>();
		activePlanets.forEach((k, v) -> {
			if (!v) {
				int planetIndex = planetNodes.indexOf(k);
				System.out.println(k);
				System.out.println(cubeMoonNodes.get(planetIndex));
				// remove planet and moon from collection
				planetNodes.remove(k);
				SceneNode kMoon = cubeMoonNodes.remove(planetIndex); 
				
				// remove node controllers
				galaxyOrbitController.remove(planetIndex);
				planetOrbitController.remove(planetIndex);
				planetRotationControllers.remove(planetIndex);
				cubeMoonRotationControllers.remove(planetIndex);
				this.getEngine().getSceneManager().destroySceneNode(k);
				this.getEngine().getSceneManager().destroySceneNode(kMoon);
				try {
					currentlyActive.put(instantiateNewPlanet(this.getEngine(), this.getEngine().getSceneManager()), true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				currentlyActive.put(k, v);
			}
			
		});
		activePlanets = currentlyActive;
	}
	
	private SceneNode instantiateNewPlanet(Engine engine, SceneManager sceneManager) throws IOException {
		Entity planetEntity = sceneManager.createEntity("planetEntity" + totalPlanetCount, "earth.obj");
		ManualObject cubeEntity = ManualCubeObject.makeCubeObject(engine, sceneManager, Integer.toString(totalPlanetCount));
		Texture planetTexture;
		RenderSystem renderSystem = sceneManager.getRenderSystem();
		
		// initial planet instantiation
		switch(totalPlanetCount) {
			case 0:
			case 1:
			case 2: 
			case 3:
			case 4:
			case 5:
				planetTexture = planetTextures[totalPlanetCount];
				break;
			default:
				planetTexture = planetTextures[RAND.nextInt(6)];
		}
		
		float[] coordinates = randomFloatArray(79.0f);
		
		// initialize randomized parameters for the galaxy orbit controller
		float[] galaxyOrbitParameters = { 
				RAND.nextFloat() * 0.1f, 
				(RAND.nextFloat() * 50.0f) + 15.0f,
				(RAND.nextFloat() * 50.0f) + 15.0f,
		};
		
		// initialize randomized parameters for the planet-moon orbit controller
		float[] planetOrbitParameters = { 
				RAND.nextFloat() * 5.0f, 
				(RAND.nextFloat() * 20.0f) + 5.0f,
		};
		
		if (RAND.nextBoolean()) {
			galaxyOrbitParameters[2] *= -1.0f;
		}
		
		planetEntity.setPrimitive(Primitive.TRIANGLES);
		cubeEntity.setPrimitive(Primitive.TRIANGLES);
		
		planetEntity.setRenderState(zState);
		cubeEntity.setRenderState(zState);
		
		SceneNode planetNode = sceneManager.getRootSceneNode().createChildSceneNode(planetEntity.getName() + "Node");
		SceneNode cubeNode = sceneManager.getRootSceneNode().createChildSceneNode(cubeEntity.getName() + "Node");
		
		TextureState planetTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		planetTextureState.setTexture(planetTexture);
		planetEntity.setRenderState(planetTextureState);
		
		TextureState cubeTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		cubeTextureState.setTexture(moonTexture);
		cubeEntity.setRenderState(cubeTextureState);
		
		// initialize planet and moon positions
		planetNode.setLocalPosition(coordinates[0], coordinates[1], coordinates[2]);
		cubeNode.setLocalPosition(planetNode.getLocalPosition());
		
		// attach entities to nodes
		planetNode.attachObject(planetEntity);
		cubeNode.attachObject(cubeEntity);
		
		galaxyOrbitController.add(new OrbitController(originNode, galaxyOrbitParameters[0], galaxyOrbitParameters[1], galaxyOrbitParameters[2], RAND.nextBoolean()));
		planetOrbitController.add(new OrbitController(planetNode, planetOrbitParameters[0], planetOrbitParameters[1], 0.0f, RAND.nextBoolean()));
	
		// add rotation controllers if node is NOT always facing target
		if (!galaxyOrbitController.get(galaxyOrbitController.size() - 1).isAlwaysFacingTarget()) {
			RotationController planetRotation = randomRotation();
			planetRotationControllers.add(planetRotation);
			planetRotation.addNode(planetNode);
			sceneManager.addController(planetRotation);
		} else {
			planetRotationControllers.add(null); // maintain size integrity
		}
		
		if (!planetOrbitController.get(planetOrbitController.size() - 1).isAlwaysFacingTarget()) {
			RotationController moonRotation = randomRotation();
			cubeMoonRotationControllers.add(moonRotation);
			moonRotation.addNode(cubeNode);
			sceneManager.addController(moonRotation);
		} else {
			cubeMoonRotationControllers.add(null); // maintain size integrity
		}
		
		planetNodes.add(planetNode);
		cubeMoonNodes.add(cubeNode);
		
		galaxyOrbitController.get(galaxyOrbitController.size() - 1).addNode(planetNode);
		planetOrbitController.get(planetOrbitController.size() - 1).addNode(cubeNode);
		
		sceneManager.addController(galaxyOrbitController.get(galaxyOrbitController.size() - 1));
		sceneManager.addController(planetOrbitController.get(planetOrbitController.size() - 1));
		
		totalPlanetCount++;
		
		return planetNode;
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
	 * @throws IOException 
	 */
	private void incrementScore() throws IOException {
		System.out.println("Score incremented!");
		
		Entity starEntity = this.getEngine().getSceneManager().createEntity("starEntity" + score, "star.obj");
		starEntity.setPrimitive(Primitive.TRIANGLES);
		starEntity.setRenderState(zState);
		
		TextureState starTextureState = (TextureState) this.getEngine().getSceneManager().getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		starTextureState.setTexture(starTexture);
		starEntity.setRenderState(starTextureState);
		
		SceneNode starNode = this.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(starEntity.getName() + "Node");
		starNode.attachObject(starEntity);
		starNode.scale(0.05f, 0.05f, 0.05f);
		dolphinOrbitController.addNode(starNode);
		dolphinOrbitController.setDistanceFromTarget(dolphinOrbitController.getDistanceFromTarget() + 0.05f);
		sfx[0].play();
		score++;
	}
	
	private void decrementLives() {
		sfx[1].play();
		if (lives > 0) {
			System.out.println("Damage taken! ");
			lives--;
		} else if (lives == 0) {
			System.out.print("You are dead. Game over :(");
			audioManager.shutdown();
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
		for (int i = 0; i < randomFloat.length; i++) {
			if (RAND.nextBoolean()) {
				randomFloat[i] += RAND.nextFloat();
			} else {
				randomFloat[i] -= RAND.nextFloat();
			}
		}
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
