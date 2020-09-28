package com.saechaol.game.myGameEngine.object.manual;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.rendersystem.Renderable.DataSource;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.SceneManager;
import ray.rage.util.BufferUtil;

/**
 * This class returns a manual cube object, whose vertices, texture map, and norm are defined in makeCubeObject()
 * @author Lucas
 *
 */

public class ManualCubeObject {
	
	/**
	 * Creates a manual line object representing the three axes of a Cartesian coordinate plane
	 * @param engine
	 * @param sceneManager
	 * @return
	 * @throws IOException
	 */
	public static ManualObject makeCubeObject(Engine engine, SceneManager sceneManager, String objectNum) throws IOException {
		ManualObject cubeObject = sceneManager.createManualObject("CubeObject" + objectNum);
		ManualObjectSection cubeSection = cubeObject.createManualSection("CubeSection" + objectNum);
		cubeObject.setGpuShaderProgram(sceneManager.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
		float[] cubeVertices = {
			-1.0f, -1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			-1.0f, 1.0f, 1.0f, 
			1.0f, 1.0f, 1.0f,
			-1.0f, -1.0f, 1.0f,
			1.0f, -1.0f, 1.0f,		// front
			
			1.0f, -1.0f, 1.0f,
			1.0f, -1.0f, -1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, -1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, -1.0f, -1.0f,		// right
			
			1.0f, -1.0f, -1.0f,
			-1.0f, 1.0f, -1.0f,
			1.0f, 1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, 1.0f, -1.0f,
			1.0f, -1.0f, -1.0f,		// back
			
			-1.0f, 1.0f, 1.0f,
			-1.0f, 1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, 1.0f, 1.0f,
			-1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, 1.0f,		// left


			-1.0f, -1.0f, 1.0f,
			-1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, 1.0f,
			1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, 1.0f, 		// bottom
			
			-1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, -1.0f,
			-1.0f, 1.0f, 1.0f,
			1.0f, 1.0f, -1.0f,
			-1.0f, 1.0f, -1.0f, 		// bottom
		};
		
		float[] cubeTextureCoordinates = {
			0.0f, 0.0f, 
			1.0f, 1.0f, 
			0.0f, 1.0f,
			1.0f, 1.0f, 
			0.0f, 0.0f, 
			1.0f, 0.0f,		// front
			
			0.0f, 0.0f,
			1.0f, 0.0f,
			0.0f, 1.0f,
			1.0f, 1.0f,
			0.0f, 1.0f,
			1.0f, 0.0f,		// right
			
			0.0f, 0.0f, 
			1.0f, 1.0f, 
			0.0f, 1.0f, 
			1.0f, 0.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,		// back
			
			1.0f, 1.0f,
			0.0f, 1.0f, 
			0.0f, 0.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f,		// left

			1.0f, 1.0f,
			0.0f, 1.0f, 
			0.0f, 0.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f,		// top
			
			1.0f, 1.0f,
			0.0f, 1.0f, 
			0.0f, 0.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f,		// bottom
			
			
		};
		
		float[] cubeNormals = {
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,		// front
			
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,			
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,		// right
			
			0.0f, 1.0f, -1.0f,
			0.0f, 1.0f, -1.0f, 
			0.0f, 1.0f, -1.0f,
			0.0f, 1.0f, -1.0f,
			0.0f, 1.0f, -1.0f, 
			0.0f, 1.0f, -1.0f,		// back
		
			-1.0f, 1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,		// left
			
			0.0f, -1.0f, -1.0f,
			0.0f, -1.0f, -1.0f,
			0.0f, -1.0f, -1.0f,
			0.0f, -1.0f, -1.0f,
			0.0f, -1.0f, -1.0f,
			0.0f, -1.0f, -1.0f,		// bottom
			
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f,
			0.0f, 1.0f, 1.0f		// top
		};
		
		int[] cubeIndices = {
			0, 1, 2, 3, 4, 5,
			6, 7, 8, 9, 10, 11,
			12, 13, 14, 15, 16, 17,
			18, 19, 20, 21, 22, 23,
			24, 25, 26, 27, 28, 29,
			30, 31, 32, 33, 34, 35
		};
		
		FloatBuffer cubeVertexBuffer = BufferUtil.directFloatBuffer(cubeVertices);
		FloatBuffer cubeTextureCoordinateBuffer = BufferUtil.directFloatBuffer(cubeTextureCoordinates);
		FloatBuffer cubeNormalsBuffer = BufferUtil.directFloatBuffer(cubeNormals);
		IntBuffer cubeIndicesBuffer = BufferUtil.directIntBuffer(cubeIndices);
		
		cubeSection.setVertexBuffer(cubeVertexBuffer);
		cubeSection.setTextureCoordsBuffer(cubeTextureCoordinateBuffer);
		cubeSection.setNormalsBuffer(cubeNormalsBuffer);
		cubeSection.setIndexBuffer(cubeIndicesBuffer);
		
		Texture darkEarthTexture = engine.getTextureManager().getAssetByPath("earth-night.jpeg");
		TextureState cubeTextureState = (TextureState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		cubeTextureState.setTexture(darkEarthTexture);
		
		FrontFaceState cubeFrontFaceState = (FrontFaceState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
		cubeFrontFaceState.setVertexWinding(FrontFaceState.VertexWinding.COUNTER_CLOCKWISE);
		
		cubeObject.setDataSource(DataSource.INDEX_BUFFER);
		cubeObject.setRenderState(cubeTextureState);
		cubeObject.setRenderState(cubeFrontFaceState);
		
		return cubeObject;
	}
	
}
