package com.saechaol.game.a1;

import java.awt.geom.AffineTransform;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.saechaol.game.myGameEngine.action.ExitGameAction;
import com.saechaol.game.myGameEngine.action.InvertYawAction;
import com.saechaol.game.myGameEngine.action.LeftStickXAction;
import com.saechaol.game.myGameEngine.action.LeftStickYAction;
import com.saechaol.game.myGameEngine.action.MoveCameraBackwardAction;
import com.saechaol.game.myGameEngine.action.MoveCameraDownAction;
import com.saechaol.game.myGameEngine.action.MoveCameraForwardAction;
import com.saechaol.game.myGameEngine.action.MoveCameraLeftAction;
import com.saechaol.game.myGameEngine.action.MoveCameraRightAction;
import com.saechaol.game.myGameEngine.action.MoveCameraUpAction;
import com.saechaol.game.myGameEngine.action.PitchCameraDownAction;
import com.saechaol.game.myGameEngine.action.PitchCameraUpAction;
import com.saechaol.game.myGameEngine.action.RightStickXAction;
import com.saechaol.game.myGameEngine.action.RightStickYAction;
import com.saechaol.game.myGameEngine.action.RollCameraLeftAction;
import com.saechaol.game.myGameEngine.action.RollCameraRightAction;
import com.saechaol.game.myGameEngine.action.SkipSongAction;
import com.saechaol.game.myGameEngine.action.ToggleCameraAction;
import com.saechaol.game.myGameEngine.action.YawCameraLeftAction;
import com.saechaol.game.myGameEngine.action.YawCameraRightAction;
import com.saechaol.game.myGameEngine.action.a1.RideDolphinToggleAction;
import com.saechaol.game.myGameEngine.camera.Camera3PController;
import com.saechaol.game.myGameEngine.display.DisplaySettingsDialog;
import com.saechaol.game.myGameEngine.object.manual.ManualAxisLineObject;
import com.saechaol.game.myGameEngine.object.manual.ManualCubeObject;

import net.java.games.input.Controller;

import ray.rage.util.Configuration;
import ray.rml.Vector3;
import ray.rml.Vector3f;
import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.ZBufferState;
import ray.rage.scene.Camera;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.ManualObject;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.scene.controllers.OrbitController;
import ray.rage.scene.controllers.RotationController;
import ray.audio.AudioManagerFactory;
import ray.audio.AudioResource;
import ray.audio.AudioResourceType;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.audio.SoundType;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;

/**
 * A RAGE game in which you crash a dolphin through planets in space.
 * 
 * Controls:
 * 	1st Person POV Mode
 * 	-	WASD / Left Stick			:	move forward, back, left, right
 * 	-	LShift / Left stick button	:	ascend
 * 	-	C / Right stick button		: 	descend
 * 	-	Arrow Keys/Right Stick		:	pitch/yaw camera
 * 	-	QE/Shoulder buttons			:	roll camera
 * 	-	V / Y button				:	invert yaw controls
 * 	-	Space / A button			:	mount/dismount dolphin
 * 	-	P / X button				: 	play next song
 * 	-	TAB / Menu/Start button		:	toggle 3P camera on/off	
 * 	-	ESC / View/Select button	:	request shutdown
 * 
 * 	3rd Person POV Mode
 * 	-	Arrow Keys / Right stick	:	orbit camera
 * 	-	R / Right trigger			:	zoom in
 * 	-	F / Left trigger			: 	zoom out
 * 	-	Space / A button			:	dismount and switch to 1P camera
 * 	-	TAB / Menu/Start button		:	toggle 3P camera on/off
 * 
 * @author Lucas Saechao
 */

public class MyGame extends VariableFrameRateGame {
	
	private Action skipSongAction, toggleCameraAction, rollCameraLeftAction, rollCameraRightAction, invertYawAction, 
		rightStickXAction, rightStickYAction, leftStickXAction, leftStickYAction, moveCameraUpAction, moveCameraDownAction, 
		moveCameraBackwardAction, moveCameraLeftAction, moveCameraRightAction, moveCameraForwardAction, pitchCameraUpAction, 
		pitchCameraDownAction, yawCameraLeftAction, yawCameraRightAction, rideDolphinToggleAction, exitGameAction;
	private boolean pauseSong = false;
	private Controller controller;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	private float orbitingAxis = 0.0f;
	private InputManager inputManager;
	private int totalPlanetCount = 0;
	private OrbitController dolphinOrbitController;
	private Sound[] music = new Sound[3];
	private Sound[] sfx = new Sound[3];
	private static final Random RAND = new Random();
	private static final String SPACE_SKYBOX = "SpaceSkyBox";
	private TextureManager textureManager;
	private Texture[] planetTextures;
	private Texture moonTexture, starTexture;
	private ZBufferState zState;
	
	public boolean toggleRide = false, invertYaw = true, alive = true, thirdPerson = false;
	public Camera camera;
	public Camera3PController orbitCameraController;
	public IAudioManager audioManager;
	public SceneNode cameraNode, dolphinNode, dolphinCamera, originNode;
	
	ArrayList<SceneNode> planetNodes = new ArrayList<SceneNode>();
	ArrayList<SceneNode> cubeMoonNodes = new ArrayList<SceneNode>();
	ArrayList<OrbitController> galaxyOrbitController = new ArrayList<OrbitController>();
	ArrayList<OrbitController> planetOrbitController = new ArrayList<OrbitController>();
	ArrayList<RotationController> planetRotationControllers = new ArrayList<RotationController>();
	ArrayList<RotationController> cubeMoonRotationControllers = new ArrayList<RotationController>();
	float elapsedTime = 0.0f;
	GL4RenderSystem renderSystem; // Initialized to minimize variable allocation in update()
	HashMap<SceneNode, Boolean> activePlanets = new HashMap<SceneNode, Boolean>();
	int elapsedTimeSeconds, lives = 3, score = 0, currentSong = 0;
	String elapsedTimeString, livesString, displayString, positionString, dolphinString;
	
	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ROTATE CAMERA");
		System.out.println("Press 'Q/E' or the left and right bumpers to ROLL CAMERA");
		System.out.println("Press 'V' or 'Y' to INVERT YAW");
		System.out.println("Press 'LSHIFT' or the left stick button to ASCEND");
		System.out.println("Press 'C' or the right stick button to DESCEND");
		System.out.println("Press 'Space' or 'A' to RIDE/HOP OFF DOLPHIN");
		System.out.println("Press 'P' or 'X' to PLAY NEXT SONG");
		System.out.println("Press 'TAB' or 'Start' to TOGGLE 3P CAMERA");
		System.out.println("Press 'ESC' or 'Select' to EXIT");
		System.out.println("----------------------------------------------------");
		formatFloat.setRoundingMode(RoundingMode.DOWN);
	}

	/**
	 * Implements a dialogue to allow the user to pick their preferred viewport size settings
	 * 
	 * @param renderSystem
	 * @param graphicsEnvironment
	 */
	@Override
	protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
		DisplaySettingsDialog displaySettingsDialogue = new DisplaySettingsDialog(graphicsEnvironment.getDefaultScreenDevice());
		displaySettingsDialogue.showIt();
		renderSystem.createRenderWindow(displaySettingsDialogue.getSelectedDisplayMode(), displaySettingsDialogue.isFullScreenModeSelected()).setTitle("Planet Chaser | Saechao Lucas A1");
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
		
		// initialize the camera's position and UVN vectors
		camera.setRt( (Vector3f) Vector3f.createFrom(-1.0f, 0.0f, 0.0f));
		camera.setUp( (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 1.0f));
		camera.setPo( (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f));
		
		// initialize the camera node
		cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
		cameraNode.attachObject(camera);
	}

	/**
	 * Initializes the entire game's scene in the following order:
	 * 	-	textures
	 * 	-	entities / scene nodes / controllers
	 * 	-	lights
	 * 	-	skybox
	 * 	-	input
	 * 	-	audio
	 * 
	 * @param engine
	 * @param sceneManager
	 */
	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		// initialize input manager
		if (inputManager == null)
			inputManager = new GenericInputManager();

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
		setupInputs();
		initializeAudio(sceneManager);
	}
	
	/**
	 * Initializes and loads audio resources from the asset folder, 
	 * and sets music[] and sfx[] to their respective resources, and plays them.
	 * 
	 * @param sceneManager
	 */
	protected void initializeAudio(SceneManager sceneManager) {
		Configuration configuration = sceneManager.getConfiguration();
		String sfxPath = configuration.valueOf("assets.sounds.path.sfx");
		String musicPath = configuration.valueOf("assets.sounds.path.music");
		AudioResource gymnopedieOne, gymnopedieTwo, gymnopedieThree, scoreSfx, destroySfx, lifeUpSfx;
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
		lifeUpSfx = audioManager.createAudioResource(sfxPath + "lifeup.wav", AudioResourceType.AUDIO_SAMPLE);
	
		music[0] = new Sound(gymnopedieOne, SoundType.SOUND_MUSIC, 100, false);
		music[1] = new Sound(gymnopedieTwo, SoundType.SOUND_MUSIC, 100, false);
		music[2] = new Sound(gymnopedieThree, SoundType.SOUND_MUSIC, 100, false);
		sfx[0] = new Sound(scoreSfx, SoundType.SOUND_EFFECT, 25, false);
		sfx[1] = new Sound(destroySfx, SoundType.SOUND_EFFECT, 25, false);
		sfx[2] = new Sound(lifeUpSfx, SoundType.SOUND_EFFECT, 25, false);
		
		for (Sound m : music) {
			m.initialize(audioManager);
		}
		
		for (Sound s : sfx) {
			s.initialize(audioManager);
		}
		
		music[currentSong].play();
	}
	
	/**
	 * Initializes input devices for the game
	 */
	protected void setupInputs() {
		String gamepadName = inputManager.getFirstGamepadName();
		controller = inputManager.getControllerByName(gamepadName);
		
		// Build action objects for listening to user input
		exitGameAction = new ExitGameAction(this);
		moveCameraForwardAction = new MoveCameraForwardAction(this);
		moveCameraBackwardAction = new MoveCameraBackwardAction(this);
		moveCameraLeftAction = new MoveCameraLeftAction(this);
		moveCameraRightAction = new MoveCameraRightAction(this);
		moveCameraUpAction = new MoveCameraUpAction(this);
		moveCameraDownAction = new MoveCameraDownAction(this);
		leftStickXAction = new LeftStickXAction(this);
		leftStickYAction = new LeftStickYAction(this);
		rideDolphinToggleAction = new RideDolphinToggleAction(this);
		yawCameraLeftAction = new YawCameraLeftAction(this);
		yawCameraRightAction = new YawCameraRightAction(this);
		pitchCameraUpAction = new PitchCameraUpAction(this);
		pitchCameraDownAction = new PitchCameraDownAction(this);
		rollCameraLeftAction = new RollCameraLeftAction(this);
		rollCameraRightAction = new RollCameraRightAction(this);
		rightStickXAction = new RightStickXAction(this);
		rightStickYAction = new RightStickYAction(this);
		invertYawAction = new InvertYawAction(this);
		toggleCameraAction = new ToggleCameraAction(this, camera, inputManager);
		skipSongAction = new SkipSongAction(this);
		
		// binds actions to ALL keyboards
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
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.F,
						null,
						InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				
				inputManager.associateAction(keyboards,
						net.java.games.input.Component.Identifier.Key.R,
						null,
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
			
			inputManager.associateAction(gamepadName, 
					net.java.games.input.Component.Identifier.Axis.Z, 
					null, 
					InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			
		}
	}

	/**
	 * Updates and redraws the viewport, as well as handle other recurring 
	 * calculations such as collision detection, position synchronization,
	 * and music.
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
		displayString += " | Current song: ";
		switch (currentSong % 3) {
		case 0:
			displayString += "Erik Satie - Gymnopedie No. 1";
			break;
		case 1:
			displayString += "Erik Satie - Gymnopedie No. 2";
			break;
		case 2:
			displayString += "Erik Satie - Gymnopedie No. 3";
			break;
		}
		
		renderSystem.setHUD(displayString, 15, 15);
		inputManager.update(elapsedTime);
		checkPlayerDistanceToDolphin(10.0f);
		if (elapsedTime > 500.0) {
			moonCollisionDetection();
			planetCollisionDetection();
			replacePlanet();
			incrementMoonOrbitAxis();
		}
		if (thirdPerson) {
			synchronize3PDolphinCameraPosition(orbitCameraController.updateCameraPosition());
		} else {
			synchronizePlayerDolphinPosition();
		}
	}
	
	/**
	 * Invoked by SkipAudioAction. Stops the current soundtrack and plays the next one.
	 */
	public void playAudio() {
		music[currentSong].stop();
		currentSong++;
		currentSong %= music.length;
		music[currentSong].play();
	}
	
	/**
	 * Initializes the skybox for the game during setupScene, and textures it
	 * 
	 * @param engine
	 * @param sceneManager
	 * @throws IOException
	 */
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
	
	/**
	 * Ensures that the 3P camera is always near the dolphin
	 * @param position
	 */
	private void synchronize3PDolphinCameraPosition(Vector3 position) {
		camera.setPo((Vector3f) position);
	}
	
	/**
	 * Ensures that the player's camera is always near the dolphin, facing the same direction
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
	 * Places the player back on the dolphin if they stray a certain radius too far
	 * @param radius
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
	 * Uses the formula for the radius of a sphere and check's if the player's position intersects the
	 * resulting value to calculate collision detection. If true, the player is given a score, and the planet is
	 * removed from the game and replaced with a new planet.
	 */
	private void planetCollisionDetection() {
		activePlanets.forEach((k, v) -> {
			if (v) {
				Vector3f playerPosition = (Vector3f) camera.getPo();
				Vector3f planetPosition = (Vector3f) k.getLocalPosition();
				if (toggleRide && (Math.pow((playerPosition.x() - planetPosition.x()), 2) + Math.pow((playerPosition.y() - planetPosition.y()), 2) + Math.pow((playerPosition.z() - planetPosition.z()), 2)) < Math.pow((2.15f), 2.0f)) {
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
	
	/**
	 * Helper function for planetCollisionDetection and incrementScore. Replaces the planet
	 * within the activePlanets HashMap with a new planet, while mindful of concurrently modifying
	 * the activePlanets HashMap
	 */
	private void replacePlanet() {
		HashMap<SceneNode, Boolean> currentlyActive = new HashMap<SceneNode, Boolean>();
		activePlanets.forEach((k, v) -> {
			if (!v) {
				int planetIndex = planetNodes.indexOf(k);
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
	
	/**
	 * Instantiates a planet with a random texture. Called by setupScene to initialize at least one planet
	 * of every texture. Planets are instantiated in random locations some units away from the origin.
	 * 
	 * @param engine
	 * @param sceneManager
	 * @return
	 * @throws IOException
	 */
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
	 * Checks to see if the player has collided with one of the moons, and decrements their lives accordingly
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
	 * Simulates a sinusodial axial tilt by multiplying the orbit controller's vertical distance by sin(x)
	 */
	private void incrementMoonOrbitAxis() {
		Iterator<OrbitController> orbitControllerIterator = planetOrbitController.iterator();
		orbitControllerIterator.forEachRemaining(controller -> {
			controller.setVerticalDistance((float) Math.sin(orbitingAxis) * 10.0f);
		});
		orbitingAxis += 0.05f;
		orbitingAxis %= 360;
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
	
	/**
	 * Toggles the third person camera
	 */
	public void toggleCamera() {
		if (thirdPerson)
			thirdPerson = false;
		else
			thirdPerson = true;
	}
	

	/**
	 * Increments player score and gives them an orbiting star
	 * @throws IOException
	 */
	private void incrementScore() throws IOException {
		System.out.println("Score!");
		
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
		score++;
		if (score % 10 == 0) {
			sfx[2].play();
			lives++;
		} else
			sfx[0].play();
	}
	
	/**
	 * Called by the collision detection methods. 
	 * When the player has lost all their lives, the game will automatically close.
	 */
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
	
	/**
	 * Rebinds inputs to the original controls. Typically invoked by ToggleCameraAction.
	 */
	public void reinitializeInputs() {
		setupInputs();
		dolphinNode.attachChild(dolphinCamera);
		dolphinCamera.attachObject(camera);
		toggleCamera();
	}
	
	/**
	 * Returns a random float array of size 3
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
