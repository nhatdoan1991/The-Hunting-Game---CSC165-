package com.saechaol.game.a2;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.dsgames.game.myGameEngine.action.AvatarLeftStickXAction;
import com.dsgames.game.myGameEngine.action.AvatarLeftStickYAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveBackwardAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveForwardAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveLeftAction;
import com.dsgames.game.myGameEngine.action.AvatarMoveRightAction;
import com.dsgames.game.myGameEngine.action.ExitGameAction;
import com.dsgames.game.myGameEngine.action.SkipSongAction;
import com.dsgames.game.myGameEngine.node.controller.StretchController;
import com.dsgames.game.myGameEngine.node.controller.VerticalOrbitController;
import com.dsgames.game.myGameEngine.action.AvatarChargeAction;
import com.dsgames.game.myGameEngine.action.AvatarJumpAction;
import com.saechaol.game.myGameEngine.camera.Camera3PController;
import com.saechaol.game.myGameEngine.display.DisplaySettingsDialog;
import com.saechaol.game.myGameEngine.object.manual.ManualAxisLineObject;
import com.saechaol.game.myGameEngine.object.manual.ManualCubeObject;
import com.saechaol.game.myGameEngine.object.manual.ManualFloorObject;

import net.java.games.input.Controller;
import ray.audio.AudioManagerFactory;
import ray.audio.AudioResource;
import ray.audio.AudioResourceType;
import ray.audio.IAudioManager;
import ray.audio.Sound;
import ray.audio.SoundType;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsEngineFactory;
import ray.physics.PhysicsObject;
import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.Viewport;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.rendersystem.states.ZBufferState;
import ray.rage.scene.Camera;
import ray.rage.scene.Light;
import ray.rage.scene.ManualObject;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.controllers.OrbitController;
import ray.rage.scene.controllers.RotationController;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.scene.Tessellation;
import ray.rage.util.Configuration;
import ray.rml.Degreef;
import ray.rml.Matrix4;
import ray.rml.Matrix4f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class MyGame extends VariableFrameRateGame {

	public boolean running = true, jumpP1 = false, jumpP2 = false;
	public float cooldownP1 = 0, cooldownP2 = 0;
	public float velocityP1 = 0.0f, velocityP2 = 0.0f;
	public HashMap<SceneNode, Boolean> playerCharge = new HashMap<SceneNode, Boolean>();
	public IAudioManager audioManager;
	public int chargeTimeP1 = 1000, chargeTimeP2 = 1000;
	public SceneNode dolphinNodeOne, dolphinNodeTwo, originNode, tessellationNode;
	public PhysicsEngine physicsEngine;
	public PhysicsObject ballOnePhysicsObject, ballTwoPhysicsObject, groundPlane, dolphinOnePhysicsObject,
			dolphinTwoPhysicsObject;

	private Action moveLeftActionP1, moveRightActionP1, moveForwardActionP1, moveBackwardActionP1, leftStickXActionP2,
			leftStickYActionP2, exitGameAction, skipSongAction, avatarJumpActionP1, avatarJumpActionP2,
			avatarChargeActionP1, avatarChargeActionP2;
	private Camera3PController orbitCameraOne, orbitCameraTwo;
	private DecimalFormat formatFloat = new DecimalFormat("#.##");
	private float orbitingAxis = 0.0f;
	private InputManager inputManager;
	private int playerOneInvulnerable = 0, playerTwoInvulnerable = 0, starUID = 0, totalPlanetCount = 0;
	private OrbitController playerOrbitController;
	private SceneNode groundNode;
	private Sound[] music = new Sound[3];
	private Sound[] sfx = new Sound[3];
	private StretchController playerStretchController;
	private Tessellation tessellationEntity;
	private TextureManager textureManager;
	private Texture moonTexture, starTexture;
	private Texture[] planetTextures;
	private VerticalOrbitController playerOrbitControllerVertical;
	private ZBufferState zState;

	private static final int INVULNERABLE_SECONDS = 3;
	private static final int TERMINAL_VELOCITY = 1000;
	private static final Random RAND = new Random();
	private static final String BUILD_STATE = "release"; // test for debugging, release for submission
	private final static String GROUND_NODE = "GroundNode";
	private static final String SKYBOX = "OceanSkybox";

	ArrayList<SceneNode> cubeMoonNodes = new ArrayList<SceneNode>();
	ArrayList<SceneNode> planetNodes = new ArrayList<SceneNode>();
	ArrayList<OrbitController> galaxyOrbitController = new ArrayList<OrbitController>();
	ArrayList<OrbitController> planetOrbitController = new ArrayList<OrbitController>();
	ArrayList<RotationController> cubeMoonRotationControllers = new ArrayList<RotationController>();
	ArrayList<RotationController> planetRotationControllers = new ArrayList<RotationController>();
	float elapsedTime = 0.0f;
	GL4RenderSystem renderSystem;
	HashMap<SceneNode, Boolean> activePlanets = new HashMap<SceneNode, Boolean>();
	int elapsedTimeSeconds, playerOneLives = 2, playerTwoLives = 2, playerOneScore = 0, playerTwoScore = 0,
			currentSong = 0;
	String elapsedTimeString, displayString, playerOneLivesString, playerTwoLivesString, playerOneScoreString,
			playerTwoScoreString;

	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'Up/Down/Left/Right' or control the right stick to ROTATE CAMERA");
		System.out.println("Press 'Q/E' or the left and right bumpers to YAW DOLPHIN");
		System.out.println("Press 'P' or 'Y' to PLAY NEXT SONG");
		System.out.println("Press 'Space' or 'A' to JUMP");
		System.out.println("Press 'ESC' or 'Menu' to EXIT");
		System.out.println("----------------------------------------------------");
		formatFloat.setRoundingMode(RoundingMode.DOWN);
	}

	/**
	 * Implements a dialogue to allow the user to pick their preferred viewport size
	 * settings
	 * 
	 * @param renderSystem
	 * @param graphicsEnvironment
	 */
	@Override
	protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
		if (BUILD_STATE.equalsIgnoreCase("release")) {
			DisplaySettingsDialog displaySettingsDialogue = new DisplaySettingsDialog(
					graphicsEnvironment.getDefaultScreenDevice());
			displaySettingsDialogue.showIt();
			renderSystem
					.createRenderWindow(displaySettingsDialogue.getSelectedDisplayMode(),
							displaySettingsDialogue.isFullScreenModeSelected())
					.setTitle("Competitive Planet Chaser | Saechao Lucas A2");
		} else if (BUILD_STATE.equalsIgnoreCase("test")) {
			int displayHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDisplayMode().getHeight();
			int displayWidth = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDisplayMode().getWidth();
			if (displayHeight > 1920 && displayWidth > 1080)
				renderSystem.createRenderWindow(new DisplayMode(1920, 1080, 24, 60), false);
			else
				renderSystem.createRenderWindow(new DisplayMode(1280, 720, 24, 60), false);
		}
	}

	/**
	 * Initializes two viewports and instantiates camera projection matrices for each.
	 */
	@Override
	protected void setupCameras(SceneManager sceneManager, RenderWindow renderWindow) {
		SceneNode rootNode = sceneManager.getRootSceneNode();

		Camera cameraOne = sceneManager.createCamera("cameraOne", Projection.PERSPECTIVE);
		renderWindow.getViewport(1).setCamera(cameraOne);

		SceneNode cameraOneNode = rootNode.createChildSceneNode(cameraOne.getName() + "Node");

		cameraOneNode.attachObject(cameraOne);
		cameraOne.setMode('n');
		cameraOne.getFrustum().setFarClipDistance(1000.0f);

		Camera cameraTwo = sceneManager.createCamera("cameraTwo", Projection.PERSPECTIVE);
		renderWindow.getViewport(0).setCamera(cameraTwo);

		SceneNode cameraTwoNode = rootNode.createChildSceneNode(cameraTwo.getName() + "Node");

		cameraTwoNode.attachObject(cameraTwo);
		cameraTwo.setMode('n');
		cameraTwo.getFrustum().setFarClipDistance(1000.0f);
	}

	/**
	 * Initializes the game state, and calls related "setup" functions
	 */
	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		if (inputManager == null)
			inputManager = new GenericInputManager();

		if (textureManager == null)
			textureManager = engine.getTextureManager();

		if (renderSystem == null)
			renderSystem = (GL4RenderSystem) engine.getRenderSystem();

		setupSkybox(engine, sceneManager);

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

		// initialize world axes
		ManualAxisLineObject.renderWorldAxes(engine, sceneManager);

		// origin node for orbit controller
		originNode = sceneManager.getRootSceneNode().createChildSceneNode("originNode");
		originNode.setLocalPosition(0.0f, 5.0f, 0.0f);

		// initialize planets
		for (int i = 0; i < 6; i++) {
			activePlanets.put(instantiateNewPlanet(engine, sceneManager), true);
		}

		Entity dolphinEntityOne = sceneManager.createEntity("dolphinEntityOne", "dolphin.obj");
		Entity dolphinEntityTwo = sceneManager.createEntity("dolphinEntityTwo", "dolphin.obj");

		dolphinEntityOne.setPrimitive(Primitive.TRIANGLES);
		dolphinEntityTwo.setPrimitive(Primitive.TRIANGLES);

		dolphinNodeOne = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityOne.getName() + "Node");
		dolphinNodeTwo = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityTwo.getName() + "Node");

		dolphinNodeOne.attachObject(dolphinEntityOne);
		dolphinNodeTwo.attachObject(dolphinEntityTwo);

		playerCharge.put(dolphinNodeOne, false);
		playerCharge.put(dolphinNodeTwo, false);

		playerStretchController = new StretchController();
		sceneManager.addController(playerStretchController);

		playerOrbitController = new OrbitController(dolphinNodeOne, 1.0f, 0.5f, 0.0f, false);
		sceneManager.addController(playerOrbitController);

		playerOrbitControllerVertical = new VerticalOrbitController(dolphinNodeTwo, 1.0f, 0.5f, 0.0f, false);
		sceneManager.addController(playerOrbitControllerVertical);

		sceneManager.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));

		Light keyLight = sceneManager.createLight("keyLightOne", Light.Type.POINT);
		keyLight.setAmbient(new Color(0.5f, 0.5f, 0.5f));
		keyLight.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		keyLight.setSpecular(new Color(0.5f, 0.5f, 0.5f));
		keyLight.setRange(500.0f);

		SceneNode keyLightNode = sceneManager.getRootSceneNode().createChildSceneNode(keyLight.getName() + "Node");
		keyLightNode.moveUp(500.0f);
		keyLightNode.attachObject(keyLight);

		Light pointLightFlashOne = sceneManager.createLight("pointLightFlashOne", Light.Type.SPOT);
		pointLightFlashOne.setAmbient(new Color(0.25f, 0.25f, 0.25f));
		pointLightFlashOne.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightFlashOne.setSpecular(new Color(0.5f, 0.5f, 0.5f));
		pointLightFlashOne.setConeCutoffAngle(Degreef.createFrom(20.0f));
		pointLightFlashOne.setConstantAttenuation(0.3f);
		pointLightFlashOne.setLinearAttenuation(0.06f);
		pointLightFlashOne.setQuadraticAttenuation(0.001f);
		pointLightFlashOne.setFalloffExponent(40.0f);
		pointLightFlashOne.setRange(30.0f);
		SceneNode flashNodeOne = dolphinNodeOne.createChildSceneNode(pointLightFlashOne.getName() + "Node");
		flashNodeOne.attachObject(pointLightFlashOne);

		Light pointLightFlashTwo = sceneManager.createLight("pointLightFlashTwo", Light.Type.SPOT);
		pointLightFlashTwo.setAmbient(new Color(0.25f, 0.25f, 0.25f));
		pointLightFlashTwo.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightFlashTwo.setSpecular(new Color(0.5f, 0.5f, 0.5f));
		pointLightFlashTwo.setConeCutoffAngle(Degreef.createFrom(20.0f));
		pointLightFlashTwo.setConstantAttenuation(0.3f);
		pointLightFlashTwo.setLinearAttenuation(0.06f);
		pointLightFlashTwo.setQuadraticAttenuation(0.001f);
		pointLightFlashTwo.setFalloffExponent(40.0f);
		pointLightFlashTwo.setRange(20.0f);
		SceneNode flashNodeTwo = dolphinNodeTwo.createChildSceneNode(pointLightFlashTwo.getName() + "Node");
		flashNodeTwo.attachObject(pointLightFlashTwo);

		dolphinNodeOne.moveLeft(3.0f);
		dolphinNodeOne.scale(0.04f, 0.04f, 0.04f);

		dolphinNodeTwo.moveRight(3.0f);
		dolphinNodeTwo.scale(0.04f, 0.04f, 0.04f);

		Texture dolphinOneTexture = textureManager.getAssetByPath("leggedDolphinRed.png");
		Texture dolphinTwoTexture = textureManager.getAssetByPath("leggedDolphinBlue.png");

		TextureState dolphinOneTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState dolphinTwoTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);

		dolphinOneTextureState.setTexture(dolphinOneTexture);
		dolphinTwoTextureState.setTexture(dolphinTwoTexture);

		dolphinEntityOne.setRenderState(dolphinOneTextureState);
		dolphinEntityTwo.setRenderState(dolphinTwoTextureState);

		setupInputs(sceneManager);

		ManualObject groundEntity = ManualFloorObject.manualFloorObject(engine, sceneManager);
		groundNode = sceneManager.getRootSceneNode().createChildSceneNode(GROUND_NODE);
		groundNode.attachObject(groundEntity);
		groundNode.setLocalPosition(0.0f, -0.8f, 0.0f);

		setupPhysics();
		setupPhysicsWorld();
		setupOrbitCameras(engine, sceneManager);
		setupTessellation(sceneManager);
		setupAudio(sceneManager);
	}

	/**
	 * Imports and generates a height map from a noise map image, and scales it to the size of the ground plane. 
	 * A normal map is then applied to the original noise map to give the terrain surface some semblance of texture.
	 * @param sceneManager
	 */
	protected void setupTessellation(SceneManager sceneManager) {
		tessellationEntity = sceneManager.createTessellation("tessellationEntity", 8);
		tessellationEntity.setSubdivisions(32.0f);

		tessellationNode = sceneManager.getRootSceneNode().createChildSceneNode(tessellationEntity.getName() + "Node");
		tessellationNode.attachObject(tessellationEntity);

		tessellationNode.translate(0.0f, -10.f, 0.0f);
		tessellationNode.scale(1000.0f, 1000.0f, 1000.0f);
		tessellationEntity.setHeightMap(this.getEngine(), "noisemap.jpg");
		tessellationEntity.setNormalMap(this.getEngine(), "noisemapnormal.png");
		tessellationEntity.setTexture(this.getEngine(), "grass.jpg");
		tessellationEntity.setQuality(8);
	}

	/**
	 * Initializes and loads audio resources from the asset folder, and sets music[]
	 * and sfx[] to their respective resources, and plays them.
	 * 
	 * @param sceneManager
	 */
	protected void setupAudio(SceneManager sceneManager) {
		Configuration configuration = sceneManager.getConfiguration();
		String sfxPath = configuration.valueOf("assets.sounds.path.a2.sfx");
		String musicPath = configuration.valueOf("assets.sounds.path.a2.music");
		AudioResource clairDeLune, arabesqueNoOne, reverie, scoreSfx, destroySfx, lifeUpSfx;
		audioManager = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");

		if (!audioManager.initialize()) {
			System.out.println("The Audio Manager failed to initialize :(");
			return;
		}

		clairDeLune = audioManager.createAudioResource(musicPath + "clairdelune.wav", AudioResourceType.AUDIO_STREAM);
		arabesqueNoOne = audioManager.createAudioResource(musicPath + "arabesque_no_one.wav",
				AudioResourceType.AUDIO_STREAM);
		reverie = audioManager.createAudioResource(musicPath + "reverie.wav", AudioResourceType.AUDIO_STREAM);
		scoreSfx = audioManager.createAudioResource(sfxPath + "score.wav", AudioResourceType.AUDIO_SAMPLE);
		destroySfx = audioManager.createAudioResource(sfxPath + "destroyed.wav", AudioResourceType.AUDIO_SAMPLE);
		lifeUpSfx = audioManager.createAudioResource(sfxPath + "lifeup.wav", AudioResourceType.AUDIO_SAMPLE);

		music[0] = new Sound(clairDeLune, SoundType.SOUND_MUSIC, 100, false);
		music[1] = new Sound(arabesqueNoOne, SoundType.SOUND_MUSIC, 100, false);
		music[2] = new Sound(reverie, SoundType.SOUND_MUSIC, 100, false);
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
	 * Initializes the JBullet physics engine and sets gravity to -9.8 units along the world Y component.
	 */
	protected void setupPhysics() {
		String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = { 0.0f, -9.8f, 0.0f };

		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);
	}

	/**
	 * Initializes the player physics objects and adds them to the physics world, as well as the ground plane
	 */
	protected void setupPhysicsWorld() {
		float mass = 1.0f;
		float up[] = { 0.0f, 1.0f, 0.0f };
		double[] transform;

		transform = toDoubleArray(dolphinNodeOne.getLocalTransform().toFloatArray());
		dolphinOnePhysicsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, transform, 0.3f, 1.0f);

		dolphinOnePhysicsObject.setBounciness(0.0f);
		dolphinOnePhysicsObject.setFriction(0.0f);
		dolphinOnePhysicsObject.setDamping(0.99f, 0.99f);
		dolphinOnePhysicsObject.setSleepThresholds(0.0f, 0.0f);
		dolphinNodeOne.setPhysicsObject(dolphinOnePhysicsObject);

		transform = toDoubleArray(dolphinNodeTwo.getLocalTransform().toFloatArray());
		dolphinTwoPhysicsObject = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), mass, transform, 0.3f, 1.0f);

		dolphinTwoPhysicsObject.setBounciness(0.0f);
		dolphinTwoPhysicsObject.setFriction(0.0f);
		dolphinTwoPhysicsObject.setDamping(0.99f, 0.99f);
		dolphinTwoPhysicsObject.setSleepThresholds(0.0f, 0.0f);
		dolphinNodeTwo.setPhysicsObject(dolphinTwoPhysicsObject);

		transform = toDoubleArray(groundNode.getLocalTransform().toFloatArray());
		groundPlane = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), transform, up, 0.0f);

		groundPlane.setBounciness(0.5f);
		groundNode.scale(500.0f, 1.0f, 500.0f);
		groundNode.setLocalPosition(0.0f, -0.8f, 0.0f);
		double[] planeTransform = groundPlane.getTransform();
		planeTransform[12] = groundNode.getLocalPosition().x();
		planeTransform[13] = groundNode.getLocalPosition().y();
		planeTransform[14] = groundNode.getLocalPosition().z();
		groundPlane.setTransform(planeTransform);
		groundNode.setPhysicsObject(groundPlane);

	}

	@Override
	protected void setupWindowViewports(RenderWindow renderWindow) {
		renderWindow.addKeyListener(this);
		renderWindow.addMouseListener(this);
		renderWindow.addMouseMotionListener(this);
		renderWindow.addMouseWheelListener(this);

		Viewport playerOneViewport = renderWindow.getViewport(0);
		playerOneViewport.setDimensions(0.0f, 0.0f, 1.0f, 0.5f);
		playerOneViewport.setClearColor(new Color(0.5f, 1.0f, 0.5f));

		Viewport playerTwoViewport = renderWindow.createViewport(0.5f, 0.0f, 1.0f, 0.5f);
		playerTwoViewport.setClearColor(new Color(0.5f, 1.0f, 0.5f));
	}

	protected void setupSkybox(Engine engine, SceneManager sceneManager) throws IOException {
		SkyBox worldSkybox = sceneManager.createSkyBox(SKYBOX);
		Configuration configuration = engine.getConfiguration();

		// initialize skybox textures
		textureManager.setBaseDirectoryPath(configuration.valueOf("assets.skyboxes.path.a2"));
		Texture skyboxFrontTexture = textureManager.getAssetByPath("oceanFront.jpg");
		Texture skyboxBackTexture = textureManager.getAssetByPath("oceanBack.jpg");
		Texture skyboxLeftTexture = textureManager.getAssetByPath("oceanLeft.jpg");
		Texture skyboxRightTexture = textureManager.getAssetByPath("oceanRight.jpg");
		Texture skyboxTopTexture = textureManager.getAssetByPath("oceanTop.jpg");
		Texture skyboxBottomTexture = textureManager.getAssetByPath("oceanBottom.jpg");

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
	 * Initializes 3P Camera controls for both players as well as their controls.
	 * @param engine
	 * @param sceneManager
	 */
	protected void setupOrbitCameras(Engine engine, SceneManager sceneManager) {
		SceneNode cameraOneNode = sceneManager.getSceneNode("cameraOneNode");
		SceneNode cameraTwoNode = sceneManager.getSceneNode("cameraTwoNode");

		Camera cameraOne = sceneManager.getCamera("cameraOne");
		Camera cameraTwo = sceneManager.getCamera("cameraTwo");

		String keyboardName = inputManager.getKeyboardName();
		String gamepadName = inputManager.getFirstGamepadName();

		orbitCameraOne = new Camera3PController(cameraOne, cameraOneNode, dolphinNodeOne, keyboardName, inputManager);
		orbitCameraTwo = new Camera3PController(cameraTwo, cameraTwoNode, dolphinNodeTwo, gamepadName, inputManager);
	}

	protected void setupInputs(SceneManager sceneManager) {
		String gamepadName = inputManager.getFirstGamepadName();

		/*moveLeftActionP1 = new AvatarMoveLeftAction(this, dolphinNodeOne.getName());
		moveRightActionP1 = new AvatarMoveRightAction(this, dolphinNodeOne.getName());
		moveForwardActionP1 = new AvatarMoveForwardAction(this, dolphinNodeOne.getName());
		moveBackwardActionP1 = new AvatarMoveBackwardAction(this, dolphinNodeOne.getName());
		leftStickXActionP2 = new AvatarLeftStickXAction(this, dolphinNodeTwo.getName());
		leftStickYActionP2 = new AvatarLeftStickYAction(this, dolphinNodeTwo.getName());
		exitGameAction = new ExitGameAction(this);
		skipSongAction = new SkipSongAction(this);
		avatarJumpActionP1 = new AvatarJumpAction(this, dolphinNodeOne.getName());
		avatarJumpActionP2 = new AvatarJumpAction(this, dolphinNodeTwo.getName());
		avatarChargeActionP1 = new AvatarChargeAction(this, dolphinNodeOne.getName());
		avatarChargeActionP2 = new AvatarChargeAction(this, dolphinNodeTwo.getName());
		*/
		/*
		 * Player One - KB
		 * - WASD 		: Move 
		 * - Arrows 	: Orbit camera 
		 * - Q/E		: Yaw dolphin left/right
		 * - SPACE 		: Jump
		 * - LSHIFT 	: Activate charge 
		 * - ESC 		: Quit 
		 * - P 			: Skip Song 
		 * - F 			: Zoom out 
		 * - R 			: Zoom in
		 */
		ArrayList<Controller> controllersArrayList = inputManager.getControllers();
		for (Controller keyboards : controllersArrayList) {
			if (keyboards.getType() == Controller.Type.KEYBOARD) {
				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.A,
						moveLeftActionP1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.D,
						moveRightActionP1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.W,
						moveForwardActionP1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.S,
						moveBackwardActionP1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.P, skipSongAction,
						InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.ESCAPE,
						exitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.SPACE,
						avatarJumpActionP1, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

				inputManager.associateAction(keyboards, net.java.games.input.Component.Identifier.Key.LSHIFT,
						avatarChargeActionP1, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			}
		}

		/*
		 * Player Two - Gamepad 
		 * - LStick 	: X/Y 		: Move 
		 * - RStick 	: RX/RY 	: Orbit camera 
		 * - LB			: 4			: Yaw dolphin left
		 * - RB			: 5			: Yaw dolphin right
		 * - A 			: 0 		: Jump 
		 * - B 			: 1 		: Activate charge 
		 * - Y 			: 3 		: Skip Song 
		 * - Menu 		: 7 		: Quit 
		 * - LT 		: Z+ 		: Zoom out 
		 * - RT 		: Z- 		: Zoom in
		 */
		if (gamepadName == null) {
			System.out.println("No gamepad detected!");
		} else {
			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Axis.X,
					leftStickXActionP2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Axis.Y,
					leftStickYActionP2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._6,
					exitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._7,
					exitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_AND_RELEASE);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._0,
					avatarJumpActionP2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._1,
					avatarChargeActionP2, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

			inputManager.associateAction(gamepadName, net.java.games.input.Component.Identifier.Button._3,
					skipSongAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		}

	}

	@Override
	protected void update(Engine engine) {
		renderSystem = (GL4RenderSystem) engine.getRenderSystem();
		elapsedTime += engine.getElapsedTimeMillis();

		if (running) {
			Matrix4 matrix;
			physicsEngine.update(elapsedTime);
			for (SceneNode sceneNode : engine.getSceneManager().getSceneNodes()) {
				if (sceneNode.getPhysicsObject() != null) {
					matrix = Matrix4f.createFrom(toFloatArray(sceneNode.getPhysicsObject().getTransform()));
					sceneNode.setLocalPosition(matrix.value(0, 3), matrix.value(1, 3), matrix.value(2, 3));
				}
			}

		}

		elapsedTimeSeconds = Math.round(elapsedTime / 1000.0f);
		elapsedTimeString = Integer.toString(elapsedTimeSeconds);
		playerOneLivesString = Integer.toString(playerOneLives);
		playerTwoLivesString = Integer.toString(playerTwoLives);
		playerOneScoreString = Integer.toString(playerOneScore);
		playerTwoScoreString = Integer.toString(playerTwoScore);

		displayString = "Player One Time: " + elapsedTimeString;
		displayString += " | Lives = " + playerOneLivesString;
		displayString += " | Score = " + playerOneScoreString;
		displayString += " | Position: (" + formatFloat.format(dolphinNodeOne.getWorldPosition().x()) + ", "
				+ formatFloat.format(dolphinNodeOne.getWorldPosition().y()) + ", "
				+ formatFloat.format(dolphinNodeOne.getWorldPosition().z()) + ")";
		if (cooldownP1 - elapsedTimeSeconds < 0) {
			displayString += " | Charge Ready!";
			playerStretchController.removeNode(dolphinNodeOne);
		} else if (cooldownP1 - elapsedTimeSeconds > 10) {
			displayString += " | Charge active!";
		} else {
			displayString += " | Charge cooldown: " + (cooldownP1 - elapsedTimeSeconds);
		}
		displayString += " | Current song: ";
		switch (currentSong % 3) {
		case 0:
			displayString += "Claude Debussy - Suite bergamasque ~ Clair de lune";
			break;
		case 1:
			displayString += "Claude Debussy - Arabesque No. 1";
			break;
		case 2:
			displayString += "Claude Debussy - Reverie";
			break;
		}

		renderSystem.setHUD(displayString, 15, (renderSystem.getRenderWindow().getViewport(1).getActualBottom()) + 15);

		displayString = "Player Two Time: " + elapsedTimeString;
		displayString += " | Lives = " + playerTwoLivesString;
		displayString += " | Score = " + playerTwoScoreString;
		displayString += " | Position: (" + formatFloat.format(dolphinNodeTwo.getWorldPosition().x()) + ", "
				+ formatFloat.format(dolphinNodeTwo.getWorldPosition().y()) + ", "
				+ formatFloat.format(dolphinNodeTwo.getWorldPosition().z()) + ")";
		if (cooldownP2 - elapsedTimeSeconds < 0) {
			displayString += " | Charge Ready!";
			playerStretchController.removeNode(dolphinNodeTwo);
		} else if (cooldownP2 - elapsedTimeSeconds > 10) {
			displayString += " | Charge active!";
		} else {
			displayString += " | Charge cooldown: " + (cooldownP2 - elapsedTimeSeconds);
		}
		displayString += " | Current song: ";
		switch (currentSong % 3) {
		case 0:
			displayString += "Claude Debussy - Suite bergamasque ~ Clair de lune";
			break;
		case 1:
			displayString += "Claude Debussy - Arabesque No. 1";
			break;
		case 2:
			displayString += "Claude Debussy - Reverie";
			break;
		}

		renderSystem.setHUD2(displayString, 15, 15);
		updateVerticalPosition();
		inputManager.update(elapsedTime);

		checkChargeTime();

		if (jumpP1) {
			velocityP1 -= 1.0f;
			if (Math.abs(velocityP2) > TERMINAL_VELOCITY) {
				velocityP1 = TERMINAL_VELOCITY * -1;
			}
			dolphinNodeOne.getPhysicsObject().applyForce(0.0f, velocityP1, 0.0f, 0.0f, 0.0f, 0.0f);
		} else if (dolphinNodeOne.getWorldPosition().y() <= 0.5f && jumpP1) {
			velocityP1 = 0.0f;
			jumpP1 = false;
		}

		if (dolphinNodeTwo.getWorldPosition().y() > 1.0f && jumpP2) {
			velocityP2 -= 1.0f;
			if (Math.abs(velocityP2) > TERMINAL_VELOCITY) {
				velocityP2 = TERMINAL_VELOCITY * -1;
			}
			dolphinNodeTwo.getPhysicsObject().applyForce(0.0f, velocityP2, 0.0f, 0.0f, 0.0f, 0.0f);
		} else if (dolphinNodeTwo.getWorldPosition().y() <= 0.5f && jumpP2) {
			velocityP2 = 0.0f;
			jumpP2 = false;
		}

		orbitCameraOne.updateCameraPosition();
		orbitCameraTwo.updateCameraPosition();

		if (elapsedTime > 500.0) {
			moonCollisionDetection(dolphinNodeOne);
			moonCollisionDetection(dolphinNodeTwo);

			planetCollisionDetection(dolphinNodeOne);
			planetCollisionDetection(dolphinNodeTwo);

			playerCollisionDetection();

			replacePlanet();
			incrementMoonOrbitAxis();
		}

	}
	
	
	/**
	 * Ensures that the player's vertical position traverses along the terrain
	 */
	public void updateVerticalPosition() {
		Vector3 avatarWorldPositionP1 = dolphinNodeOne.getWorldPosition();
		Vector3 avatarLocalPositionP1 = dolphinNodeOne.getLocalPosition();
		Vector3 terrainPositionP1 = (Vector3) Vector3f.createFrom(avatarLocalPositionP1.x(),
				tessellationEntity.getWorldHeight(avatarWorldPositionP1.x(), avatarWorldPositionP1.z()) + 0.5f,
				avatarLocalPositionP1.z());

		if (avatarLocalPositionP1.y() <= terrainPositionP1.y() + 0.5f) {
			Vector3 avatarPositionP1 = terrainPositionP1;
			dolphinNodeOne.setLocalPosition(avatarPositionP1);
			synchronizeAvatarPhysics(dolphinNodeOne);
			if (jumpP1) {
				dolphinNodeOne.getPhysicsObject().applyForce(0.0f, 1500.0f, 0.0f, 0.0f, 0.0f, 0.0f);
				jumpP1 = false;
			}
		} else if (avatarLocalPositionP1.y() > terrainPositionP1.y() + 1.0f) {
			jumpP1 = true;
		}

		Vector3 avatarWorldPositionP2 = dolphinNodeTwo.getWorldPosition();
		Vector3 avatarLocalPositionP2 = dolphinNodeTwo.getLocalPosition();
		Vector3 terrainPositionP2 = (Vector3) Vector3f.createFrom(avatarLocalPositionP2.x(),
				tessellationEntity.getWorldHeight(avatarWorldPositionP2.x(), avatarWorldPositionP2.z()) + 0.5f,
				avatarLocalPositionP2.z());

		if (avatarLocalPositionP2.y() <= terrainPositionP2.y() + 0.5f) {
			Vector3 avatarPositionP2 = terrainPositionP2;
			dolphinNodeTwo.setLocalPosition(avatarPositionP2);
			synchronizeAvatarPhysics(dolphinNodeTwo);
			if (jumpP2) {
				dolphinNodeTwo.getPhysicsObject().applyForce(0.0f, 2000.0f, 0.0f, 0.0f, 0.0f, 0.0f);
				jumpP2 = false;
			}
		} else if (avatarLocalPositionP2.y() > terrainPositionP2.y() + 1.0f) {
			jumpP2 = true;
		}

	}

	/**
	 * Invoked by SkipAudioAction. Stops the current soundtrack and plays the next
	 * one.
	 */
	public void playAudio() {
		music[currentSong].stop();
		currentSong++;
		currentSong %= music.length;
		music[currentSong].play();
	}

	/**
	 * Checks if the player can perform a charge or not
	 */
	private void checkChargeTime() {
		if (elapsedTimeSeconds > chargeTimeP1) {
			playerCharge.put(dolphinNodeOne, false);
		}
		if (elapsedTimeSeconds > chargeTimeP2) {
			playerCharge.put(dolphinNodeTwo, false);
		}
	}

	/**
	 * Adds the scene node the the stretch controller
	 * @param player
	 */
	public void addToStretchController(SceneNode player) {
		playerStretchController.addNode(player);
	}

	/**
	 * Checks player to player collision detection, as well as player charge
	 */
	private void playerCollisionDetection() {
		boolean playerOneCharge = playerCharge.get(dolphinNodeOne);
		boolean playerTwoCharge = playerCharge.get(dolphinNodeTwo);

		Vector3f playerOnePosition = (Vector3f) dolphinNodeOne.getWorldPosition();
		Vector3f playerTwoPosition = (Vector3f) dolphinNodeTwo.getWorldPosition();
		if ((Math.pow((playerOnePosition.x() - playerTwoPosition.x()), 2)
				+ Math.pow((playerOnePosition.y() - playerTwoPosition.y()), 2)
				+ Math.pow((playerOnePosition.z() - playerTwoPosition.z()), 2)) < Math.pow((0.4f), 2.0f)) {
			if (playerOneCharge && !playerTwoCharge && (playerTwoInvulnerable <= elapsedTimeSeconds)) { // P1 > P2
				dolphinNodeTwo.setLocalPosition(3.0f, 0.0f, 0.0f);
				double[] transformP2 = dolphinNodeTwo.getPhysicsObject().getTransform();
				transformP2[12] = dolphinNodeTwo.getLocalPosition().x();
				transformP2[13] = dolphinNodeTwo.getLocalPosition().y();
				transformP2[14] = dolphinNodeTwo.getLocalPosition().z();
				dolphinNodeTwo.getPhysicsObject().setTransform(transformP2);
				decrementLives(dolphinNodeTwo.getName());
				playerTwoInvulnerable = elapsedTimeSeconds + INVULNERABLE_SECONDS;

			} else if (playerTwoCharge && !playerOneCharge && (playerOneInvulnerable <= elapsedTimeSeconds)) { // P2 >
																												// P1
				dolphinNodeOne.setLocalPosition(-3.0f, 0.0f, 0.0f);
				double[] transformP1 = dolphinNodeOne.getPhysicsObject().getTransform();
				transformP1[12] = dolphinNodeOne.getLocalPosition().x();
				transformP1[13] = dolphinNodeOne.getLocalPosition().y();
				transformP1[14] = dolphinNodeOne.getLocalPosition().z();
				dolphinNodeOne.getPhysicsObject().setTransform(transformP1);
				decrementLives(dolphinNodeOne.getName());
				playerOneInvulnerable = elapsedTimeSeconds + INVULNERABLE_SECONDS;

			} else if ((playerOneCharge && playerTwoCharge) || (!playerOneCharge && !playerTwoCharge)
					&& (playerOneInvulnerable <= elapsedTimeSeconds && playerTwoInvulnerable <= elapsedTimeSeconds)) {
				dolphinNodeOne.setLocalPosition(-3.0f, 0.0f, 0.0f);
				dolphinNodeTwo.setLocalPosition(3.0f, 0.0f, 0.0f);

				double[] transformP1 = dolphinNodeOne.getPhysicsObject().getTransform();
				transformP1[12] = dolphinNodeOne.getLocalPosition().x();
				transformP1[13] = dolphinNodeOne.getLocalPosition().y();
				transformP1[14] = dolphinNodeOne.getLocalPosition().z();
				dolphinNodeOne.getPhysicsObject().setTransform(transformP1);
				playerOneLives--;
				System.out.println("Player One took damage! ");
				playerOneInvulnerable = elapsedTimeSeconds + INVULNERABLE_SECONDS;

				double[] transformP2 = dolphinNodeTwo.getPhysicsObject().getTransform();
				transformP2[12] = dolphinNodeTwo.getLocalPosition().x();
				transformP2[13] = dolphinNodeTwo.getLocalPosition().y();
				transformP2[14] = dolphinNodeTwo.getLocalPosition().z();
				dolphinNodeTwo.getPhysicsObject().setTransform(transformP2);
				decrementLives(dolphinNodeTwo.getName());
				playerTwoInvulnerable = elapsedTimeSeconds + INVULNERABLE_SECONDS;
			}
		}
	}

	/**
	 * Helper function for planetCollisionDetection and incrementScore. Replaces the
	 * planet within the activePlanets HashMap with a new planet, while mindful of
	 * concurrently modifying the activePlanets HashMap
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
					currentlyActive.put(instantiateNewPlanet(this.getEngine(), this.getEngine().getSceneManager()),
							true);
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
	 * Simulates a sinusodial axial tilt by multiplying the orbit controller's
	 * vertical distance by sin(x)
	 */
	private void incrementMoonOrbitAxis() {
		Iterator<OrbitController> orbitControllerIterator = planetOrbitController.iterator();
		orbitControllerIterator.forEachRemaining(controller -> {
			controller.setVerticalDistance((float) Math.sin(orbitingAxis) * 10.0f);
		});
		orbitingAxis += 0.05f;
		orbitingAxis %= 360;
	}

	public void synchronizeAvatarPhysics(SceneNode player) {
		if (running) {
			double[] transform = player.getPhysicsObject().getTransform();
			transform[12] = player.getLocalPosition().x();
			transform[13] = player.getLocalPosition().y();
			transform[14] = player.getLocalPosition().z();
			player.getPhysicsObject().setTransform(transform);
		} else {
			player.getPhysicsObject().setTransform(toDoubleArray(player.getWorldTransform().toFloatArray()));
		}
	}

	/**
	 * Instantiates a planet with a random texture. Called by setupScene to
	 * initialize at least one planet of every texture. Planets are instantiated in
	 * random locations some units away from the origin.
	 * 
	 * @param engine
	 * @param sceneManager
	 * @return
	 * @throws IOException
	 */
	private SceneNode instantiateNewPlanet(Engine engine, SceneManager sceneManager) throws IOException {
		Entity planetEntity = sceneManager.createEntity("planetEntity" + totalPlanetCount, "earth.obj");
		ManualObject cubeEntity = ManualCubeObject.makeCubeObject(engine, sceneManager,
				Integer.toString(totalPlanetCount));
		Texture planetTexture;
		RenderSystem renderSystem = sceneManager.getRenderSystem();

		// initial planet instantiation
		switch (totalPlanetCount) {
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
		float[] galaxyOrbitParameters = { RAND.nextFloat() * 0.1f, (RAND.nextFloat() * 65.0f) + 25.0f,
				(RAND.nextFloat() * 1.0f) };

		// initialize randomized parameters for the planet-moon orbit controller
		float[] planetOrbitParameters = { RAND.nextFloat() * 5.0f, (RAND.nextFloat() * 20.0f) + 5.0f, };

		if (RAND.nextBoolean()) {
			galaxyOrbitParameters[2] *= -1.0f;
		}

		planetEntity.setPrimitive(Primitive.TRIANGLES);
		cubeEntity.setPrimitive(Primitive.TRIANGLES);

		planetEntity.setRenderState(zState);
		cubeEntity.setRenderState(zState);

		SceneNode planetNode = originNode.createChildSceneNode(planetEntity.getName() + "Node");
		SceneNode cubeNode = originNode.createChildSceneNode(cubeEntity.getName() + "Node");

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
		galaxyOrbitController.add(new OrbitController(originNode, galaxyOrbitParameters[0], galaxyOrbitParameters[1],
				galaxyOrbitParameters[2] + 8.0f, RAND.nextBoolean()));
		planetOrbitController.add(new OrbitController(planetNode, planetOrbitParameters[0], planetOrbitParameters[1],
				0.0f, RAND.nextBoolean()));

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
	 * Uses the formula for the radius of a sphere and check's if the player's
	 * position intersects the resulting value to calculate collision detection. If
	 * true, the player is given a score, and the planet is removed from the game
	 * and replaced with a new planet.
	 */
	private void planetCollisionDetection(SceneNode player) {
		activePlanets.forEach((k, v) -> {
			if (v) {
				Vector3f playerPosition = (Vector3f) player.getWorldPosition();
				Vector3f planetPosition = (Vector3f) k.getWorldPosition();
				if ((Math.pow((playerPosition.x() - planetPosition.x()), 2)
						+ Math.pow((playerPosition.y() - planetPosition.y()), 2)
						+ Math.pow((playerPosition.z() - planetPosition.z()), 2)) < Math.pow((2.15f), 2.0f)) {
					try {
						incrementScore(player.getName());
					} catch (IOException e) {
						e.printStackTrace();
					}
					activePlanets.put(k, false);
				}
			}
		});
	}

	/**
	 * Checks to see if the player has collided with one of the moons, and
	 * decrements their lives accordingly
	 */
	private void moonCollisionDetection(SceneNode player) {
		Iterator<SceneNode> activeMoonsIterator = cubeMoonNodes.iterator();
		activeMoonsIterator.forEachRemaining(node -> {
			Vector3f dolphinPosition = (Vector3f) player.getLocalPosition();
			if ((Math.pow((dolphinPosition.x() - node.getLocalPosition().x()), 2)
					+ Math.pow((dolphinPosition.y() - node.getLocalPosition().y()), 2)
					+ Math.pow((dolphinPosition.z() - node.getLocalPosition().z()), 2)) < Math.pow((2.15f), 2.0f)) {
				if (player.getName() == "dolphinEntityOneNode")
					player.setLocalPosition(-3.0f, 0.0f, 0.0f);
				else
					player.setLocalPosition(3.0f, 0.0f, 0.0f);
				double[] transform = player.getPhysicsObject().getTransform();
				transform[12] = player.getLocalPosition().x();
				transform[13] = player.getLocalPosition().y();
				transform[14] = player.getLocalPosition().z();
				player.getPhysicsObject().setTransform(transform);
				decrementLives(player.getName());
			}
		});
	}

	/**
	 * Called by the collision detection methods. When the player has lost all their
	 * lives, the game will automatically close.
	 */
	private void decrementLives(String playerName) {
		String currentPlayer;
		sfx[1].play();
		switch (playerName) {
		case "dolphinEntityOneNode":
			currentPlayer = "Player One";
			if (playerOneLives >= 0) {
				System.out.println(currentPlayer + " took damage!");
				playerOneLives--;
			}
			break;
		case "dolphinEntityTwoNode":
			currentPlayer = "Player Two";
			if (playerTwoLives >= 0) {
				System.out.println(currentPlayer + " took damage!");
				playerTwoLives--;
			}
			break;
		}

		if (playerOneLives < 0 || playerTwoLives < 0) {
			if (playerOneLives > playerTwoLives) {
				System.out.println("Player Two has lost. ");
			} else if (playerTwoLives > playerOneLives) {
				System.out.println("Player One has lost. ");
			} else {
				System.out.println("Both players have lost.");
			}

			if (playerOneScore > playerTwoScore) {
				System.out.println("Player One took the score victory! ");
			} else if (playerOneScore == playerTwoScore) {
				System.out.println("Tied score! ");
			} else if (playerTwoScore > playerOneScore) {
				System.out.println("Player Two took the score victory! ");
			}
			audioManager.shutdown();
			this.setState(Game.State.STOPPING);
		}
	}

	/**
	 * Increments player score and gives them an orbiting star
	 * 
	 * @throws IOException
	 */
	public void incrementScore(String playerName) throws IOException {
		String currentPlayer = "";
		Entity starEntity = this.getEngine().getSceneManager().createEntity("starEntity" + starUID, "star.obj");
		starUID++;
		starEntity.setPrimitive(Primitive.TRIANGLES);
		starEntity.setRenderState(zState);

		TextureState starTextureState = (TextureState) this.getEngine().getSceneManager().getRenderSystem()
				.createRenderState(RenderState.Type.TEXTURE);
		starTextureState.setTexture(starTexture);
		starEntity.setRenderState(starTextureState);

		SceneNode starNode = this.getEngine().getSceneManager().getRootSceneNode()
				.createChildSceneNode(starEntity.getName() + "Node");
		starNode.attachObject(starEntity);
		starNode.scale(0.05f, 0.05f, 0.05f);
		switch (playerName) {
		case "dolphinEntityOneNode":
			currentPlayer = "Player One";
			playerOrbitController.addNode(starNode);
			playerOrbitController.setDistanceFromTarget(playerOrbitController.getDistanceFromTarget() + 0.05f);
			playerOneScore++;
			if (playerOneScore % 10 == 0) {
				sfx[2].play();
				playerOneLives++;
			} else
				sfx[0].play();
			break;
		case "dolphinEntityTwoNode":
			currentPlayer = "Player Two";
			playerOrbitControllerVertical.addNode(starNode);
			playerOrbitControllerVertical.setDistance(playerOrbitControllerVertical.getDistance() + 0.05f);
			playerTwoScore++;
			if (playerTwoScore % 10 == 0) {
				sfx[2].play();
				playerTwoLives++;
			} else
				sfx[0].play();
			break;
		}
		System.out.println(currentPlayer + " has scored a point!");
	}

	/**
	 * Returns a random rotation controller object
	 * 
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
	 * Returns a random float array of size 3
	 * 
	 * @param args
	 */
	private float[] randomFloatArray(float upperBound) {
		float[] randomFloat = { (RAND.nextFloat() * upperBound), Math.abs((RAND.nextFloat() * upperBound)),
				(RAND.nextFloat() * upperBound) };
		for (int i = 0; i < randomFloat.length; i++) {
			if (RAND.nextBoolean()) {
				randomFloat[i] += RAND.nextFloat();
			} else {
				randomFloat[i] -= RAND.nextFloat();
			}
		}
		return randomFloat;
	}
	
	/**
	 * Converts a double array into a float array
	 * @param arr
	 * @return
	 */
	private float[] toFloatArray(double[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		float[] outputFloat = new float[n];

		for (int i = 0; i < n; i++) {
			outputFloat[i] = (float) arr[i];
		}

		return outputFloat;
	}

	/**
	 * Converts a float array into a double array
	 * @param arr
	 * @return
	 */
	private double[] toDoubleArray(float[] arr) {
		if (arr == null)
			return null;
		int n = arr.length;
		double[] outputFloat = new double[n];

		for (int i = 0; i < n; i++) {
			outputFloat[i] = (double) arr[i];

		}
		return outputFloat;
	}

	@Override
	protected void loadConfiguration(Configuration config) throws IOException {
		config.load("assets/config/a2.properties");
	}

	public static void main(String[] args) {
		System.out.println("a2.MyGame.main() running!");
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
