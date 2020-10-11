package com.saechaol.game.a2;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import com.saechaol.game.myGameEngine.camera.Camera3PController;
import com.saechaol.game.myGameEngine.display.DisplaySettingsDialog;

import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderSystemFactory;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.Viewport;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Camera;
import ray.rage.scene.Light;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneManagerFactory;
import ray.rage.scene.SceneNode;
import ray.rage.scene.SkyBox;
import ray.rage.util.Configuration;

public class MyGame extends VariableFrameRateGame {

	private Camera3PController orbitCameraOne, orbitCameraTwo;
	GL4RenderSystem renderSystem;
	float elapsedTime = 0.0f;
	String elapsedTimeString, displayString;
	int elapsedTimeSeconds;
	private static final String SPACE_SKYBOX = "SpaceSkyBox";
	private TextureManager textureManager;
	private InputManager inputManager;

	public MyGame() {
		super();
		System.out.println("Press 'W/A/S/D' or control the left stick to MOVE");
		System.out.println("Press 'ESC' or 'Select' to EXIT");
		System.out.println("----------------------------------------------------");
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
		DisplaySettingsDialog displaySettingsDialogue = new DisplaySettingsDialog(graphicsEnvironment.getDefaultScreenDevice());
		displaySettingsDialogue.showIt();
		renderSystem.createRenderWindow(displaySettingsDialogue.getSelectedDisplayMode(),displaySettingsDialogue.isFullScreenModeSelected()).setTitle("Competitive Planet Chaser | Saechao Lucas A2");
	}

	@Override
	protected void setupCameras(SceneManager sceneManager, RenderWindow renderWindow) {
		SceneNode rootNode = sceneManager.getRootSceneNode();

		Camera cameraOne = sceneManager.createCamera("cameraOne", Projection.PERSPECTIVE);
		renderWindow.getViewport(0).setCamera(cameraOne);

		SceneNode cameraOneNode = rootNode.createChildSceneNode(cameraOne.getName() + "Node");
		
		cameraOneNode.attachObject(cameraOne);
		cameraOne.setMode('n');
		cameraOne.getFrustum().setFarClipDistance(1000.0f);
		
		Camera cameraTwo = sceneManager.createCamera("cameraTwo", Projection.PERSPECTIVE); 
		renderWindow.getViewport(1).setCamera(cameraTwo);
		
		SceneNode cameraTwoNode = rootNode.createChildSceneNode(cameraTwo.getName() + "Node");
		
		cameraTwoNode.attachObject(cameraTwo);
		cameraTwo.setMode('n');
		cameraTwo.getFrustum().setFarClipDistance(1000.0f);
	}

	@Override
	protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {
		if (inputManager == null)
			inputManager = new GenericInputManager();
		
		if (textureManager == null)
			textureManager = engine.getTextureManager();
		
		if (renderSystem == null)
			renderSystem = (GL4RenderSystem) engine.getRenderSystem();
		
		setupSkybox(engine, sceneManager);
		
		Entity dolphinEntityOne = sceneManager.createEntity("dolphinEntityOne", "dolphinHighPoly.obj");
		Entity dolphinEntityTwo = sceneManager.createEntity("dolphinEntityTwo", "dolphinHighPoly.obj");
		
		dolphinEntityOne.setPrimitive(Primitive.TRIANGLES);
		dolphinEntityTwo.setPrimitive(Primitive.TRIANGLES);
		
		SceneNode dolphinNodeOne = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityOne.getName() + "Node");
		SceneNode dolphinNodeTwo = sceneManager.getRootSceneNode().createChildSceneNode(dolphinEntityTwo.getName() + "Node");
		
		dolphinNodeOne.attachObject(dolphinEntityOne);
		dolphinNodeTwo.attachObject(dolphinEntityTwo);
		
		sceneManager.getAmbientLight().setIntensity(new Color(0.1f, 0.1f, 0.1f));
		
		Light pointLightFlash = sceneManager.createLight("pointLightFlash", Light.Type.POINT);
		pointLightFlash.setAmbient(new Color(1.0f, 1.0f, 1.0f));
		pointLightFlash.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
		pointLightFlash.setSpecular(new Color(1.0f, 1.0f, 1.0f));
		pointLightFlash.setRange(20.0f);
	
		SceneNode pointLightFlashNode = sceneManager.getRootSceneNode().createChildSceneNode(pointLightFlash.getName() + "Node");
		pointLightFlashNode.attachObject(pointLightFlash);
		
		dolphinNodeOne.moveLeft(0.5f);
		dolphinNodeTwo.moveRight(0.5f);
		
		// manually assign textures
		Texture dolphinOneTexture = textureManager.getAssetByPath("red.jpeg");
		Texture dolphinTwoTexture = textureManager.getAssetByPath("blue.jpeg");
		
		TextureState dolphinOneTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		TextureState dolphinTwoTextureState = (TextureState) renderSystem.createRenderState(RenderState.Type.TEXTURE);
		
		dolphinOneTextureState.setTexture(dolphinOneTexture);
		dolphinTwoTextureState.setTexture(dolphinTwoTexture);
		
		dolphinEntityOne.setRenderState(dolphinOneTextureState);
		dolphinEntityTwo.setRenderState(dolphinTwoTextureState);
		
		setupOrbitCameras(engine, sceneManager);
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
		SkyBox worldSkybox = sceneManager.createSkyBox(SPACE_SKYBOX);
		Configuration configuration = engine.getConfiguration();
		
		// initialize skybox textures
		textureManager.setBaseDirectoryPath(configuration.valueOf("assets.skyboxes.path.a2"));
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

	protected void setupOrbitCameras(Engine engine, SceneManager sceneManager) {
		SceneNode dolphinNodeOne = sceneManager.getSceneNode("dolphinEntityOneNode");
		SceneNode cameraOneNode = sceneManager.getSceneNode("cameraOneNode");
		
		SceneNode dolphinNodeTwo = sceneManager.getSceneNode("dolphinEntityTwoNode");
		SceneNode cameraTwoNode = sceneManager.getSceneNode("cameraTwoNode");
		
		Camera cameraOne = sceneManager.getCamera("cameraOne");
		Camera cameraTwo = sceneManager.getCamera("cameraTwo");
		
		String keyboardName = inputManager.getKeyboardName();
		String gamepadName = inputManager.getFirstGamepadName();
		
		orbitCameraOne = new Camera3PController(cameraOne, cameraOneNode, dolphinNodeOne, gamepadName, inputManager);
		orbitCameraTwo = new Camera3PController(cameraTwo, cameraTwoNode, dolphinNodeTwo, keyboardName, inputManager);
	}
	
	protected void setupInputs(SceneManager sceneManager) {
		/*
		String keyboardName = inputManager.getKeyboardName();
		String gamepadName = inputManager.getFirstGamepadName();
		String mouseName = inputManager.getMouseName();
		
		SceneNode dolphinNodeOne = sceneManager.getSceneNode("dolphinEntityNodeOne");
		SceneNode dolphinNodeTwo = sceneManager.getSceneNode("dolphinEntityNodeTwo");
		*/
		
	}

	protected void setupAudio() {

	}

	@Override
	protected void update(Engine engine) {
		renderSystem = (GL4RenderSystem) engine.getRenderSystem();
		elapsedTime += engine.getElapsedTimeMillis();
		elapsedTimeSeconds = Math.round(elapsedTime / 1000.0f);
		elapsedTimeString = Integer.toString(elapsedTimeSeconds);
		
		displayString = "Player One Time: " + elapsedTimeString;
		renderSystem.setHUD(displayString, 15, (renderSystem.getRenderWindow().getViewport(1).getActualBottom()) + 15);
		
		displayString = "Player Two Time: " + elapsedTimeString;
		renderSystem.setHUD2(displayString, 15, 15);
		
		orbitCameraOne.updateCameraPosition();
		orbitCameraTwo.updateCameraPosition();
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
