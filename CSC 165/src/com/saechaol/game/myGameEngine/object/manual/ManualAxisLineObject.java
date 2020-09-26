package com.saechaol.game.myGameEngine.object.manual;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import ray.rage.Engine;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.Texture;
import ray.rage.rendersystem.Renderable.DataSource;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.util.BufferUtil;

public class ManualAxisLineObject {
	
	/**
	 * Creates a manual line object representing the three axes of a Cartesian coordinate plane
	 * @param engine
	 * @param sceneManager
	 * @param axis
	 * @return
	 * @throws IOException
	 */
	private static ManualObject makeAxisLine(Engine engine, SceneManager sceneManager, String axis) throws IOException {
		
		if (axis.length() > 1) {
			throw new IOException("Axis must be a single char");
		}
		char axisUpper = axis.toUpperCase().charAt(0);
		
		ManualObject line = sceneManager.createManualObject(axisUpper + "Line");
		ManualObjectSection lineSection = line.createManualSection(axisUpper + "LineSection");
		line.setGpuShaderProgram(sceneManager.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
		Material lineMaterial = sceneManager.getMaterialManager().getAssetByPath("default.mtl");
		Texture lineTexture;
		float[] lineVertices = new float[6];
		
		float[] lineTextureCoordinates = {
			0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f
		};
		
		float[] lineNormals = {
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f
		};
		
		int[] indices = { 0, 1 };
		
		switch (axisUpper) {
		
		case 'X': // X axis
			lineVertices[0] = -100.0f;
			lineVertices[3] = 100.0f;
			lineMaterial.setEmissive(Color.RED);
			lineTexture = engine.getTextureManager().getAssetByPath("x_axis.png");
			break;
			
		case 'Y': // Y axis
			lineVertices[1] = -100.0f;
			lineVertices[4] = 100.0f;
			lineMaterial.setEmissive(Color.GREEN);
			lineTexture = engine.getTextureManager().getAssetByPath("y_axis.png");
			break;
		
		default: // Z axis
			lineVertices[2] = -100.0f;
			lineVertices[5] = 100.0f;
			lineMaterial.setEmissive(Color.BLUE);
			lineTexture = engine.getTextureManager().getAssetByPath("z_axis.png");
			
		}
		
		FloatBuffer lineVertexBuffer = BufferUtil.directFloatBuffer(lineVertices);
		FloatBuffer lineTextureBuffer = BufferUtil.directFloatBuffer(lineTextureCoordinates);
		FloatBuffer lineNormalsBuffer = BufferUtil.directFloatBuffer(lineNormals);
		IntBuffer lineIndicesBuffer = BufferUtil.directIntBuffer(indices);
		
		lineSection.setVertexBuffer(lineVertexBuffer);
		lineSection.setTextureCoordsBuffer(lineTextureBuffer);
		lineSection.setNormalsBuffer(lineNormalsBuffer);
		lineSection.setIndexBuffer(lineIndicesBuffer);
		
		TextureState lineTextureState = (TextureState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		lineTextureState.setTexture(lineTexture);
		
		FrontFaceState lineFrontFaceState = (FrontFaceState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
		
		line.setRenderState(lineTextureState);
		line.setRenderState(lineFrontFaceState);
		
		return line;
	}
	
	/**
	 * Renders the in game world axis
	 * @param engine
	 * @param sceneManager
	 * @throws IOException
	 */
	public static void renderWorldAxes(Engine engine, SceneManager sceneManager) throws IOException {	
		SceneNode xAxisNode, yAxisNode, zAxisNode;
		
		ManualObject xAxis = makeAxisLine(engine, sceneManager, "X");
		ManualObject yAxis = makeAxisLine(engine, sceneManager, "Y");
		ManualObject zAxis = makeAxisLine(engine, sceneManager, "Z");
		
		xAxis.setPrimitive(Primitive.LINES);
		yAxis.setPrimitive(Primitive.LINES);
		zAxis.setPrimitive(Primitive.LINES);
		
		xAxisNode = sceneManager.getRootSceneNode().createChildSceneNode("XAxisLine");
		yAxisNode = sceneManager.getRootSceneNode().createChildSceneNode("YAxisLine");
		zAxisNode = sceneManager.getRootSceneNode().createChildSceneNode("ZAxisLine");
		
		xAxisNode.attachObject(xAxis);
		yAxisNode.attachObject(yAxis);
		zAxisNode.attachObject(zAxis);
		
		xAxisNode.scale(1.0f, 1.0f, 1.0f);
		yAxisNode.scale(1.0f, 1.0f, 1.0f);
		zAxisNode.scale(1.0f, 1.0f, 1.0f);
		
		xAxisNode.setLocalPosition(0.0f, 0.0f, 0.0f);
		yAxisNode.setLocalPosition(0.0f, 0.0f, 0.0f);
		zAxisNode.setLocalPosition(0.0f, 0.0f, 0.0f);
	}

}
