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

public class ManualLineObject {
	
	public static ManualObject makeLine(Engine engine, SceneManager sceneManager) throws IOException {
		ManualObject line = sceneManager.createManualObject("Line");
		
		ManualObjectSection lineSection = line.createManualSection("LineSection");
		line.setGpuShaderProgram(sceneManager.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
		float[] vertices = {
			-1.0f, -1.0f, 1.0f, 
			1.0f, -1.0f, 1.0f,
			0.0f, 1.0f, 0.0f,	// front
			
			1.0f, -1.0f, 1.0f,
			1.0f, -1.0f, -1.0f,
			0.0f, 1.0f, 0.0f,	// right
			
			1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,
			0.0f, 1.0f, 0.0f,	// back
			
			-1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, 1.0f,
			0.0f, 1.0f, 0.0f,	// left
			
			-1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, 1.0f,
			-1.0f, -1.0f, 1.0f,// LF
			
			1.0f, -1.0f, 1.0f,
			-1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f	// RR
		};
		
		float[] textureCoordinates = {
			0.0f, 0.0f, 
			1.0f, 0.0f, 
			0.5f, 1.0f,	// front	
			
			0.0f, 0.0f, 
			1.0f, 0.0f, 
			0.5f, 1.0f,	// right
			
			0.0f, 0.0f, 
			1.0f, 0.0f, 
			0.5f, 1.0f,	// back
			
			0.0f, 0.0f, 
			1.0f, 0.0f, 
			0.5f, 1.0f,	// left
			
			0.0f, 0.0f, 
			1.0f, 1.0f, 
			0.0f, 1.0f, // LF
			
			1.0f, 1.0f, 
			0.0f, 0.0f, 
			1.0f, 0.0f	// RR
		};
		
		float[] normals = {
			0.0f, 1.0f, 1.0f, 
			0.0f, 1.0f, 1.0f, 
			0.0f, 1.0f, 1.0f,	// front
			
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,	// right
			
			0.0f, 1.0f, -1.0f, 
			0.0f, 1.0f, -1.0f,
			0.0f, 1.0f, -1,0f,	// back
			
			-1.0f, 1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,
			-1.0f, 1.0f, 0.0f,	// left
			
			0.0f, -1.0f, 0.0f, 
			0.0f, -1.0f, 0.0f,
			0.0f, -1.0f, 0.0f,	// LF
			
			0.0f, -1.0f, 0.0f,
			0.0f, -1.0f, 0.0f,
			0.0f, -1.0f, 0.0f	// RR
		};
		
		int[] indices = {
				0, 1, 2, 3, 4, 5,
				6, 7, 8, 9, 10, 11,
				12, 13, 14, 15, 16, 17
		};
		
		FloatBuffer vertexBuffer = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer textureBuffer = BufferUtil.directFloatBuffer(textureCoordinates);
		FloatBuffer normalBuffer = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuffer = BufferUtil.directIntBuffer(indices);
		
		lineSection.setVertexBuffer(vertexBuffer);
		lineSection.setTextureCoordsBuffer(textureBuffer);
		lineSection.setNormalsBuffer(normalBuffer);
		lineSection.setIndexBuffer(indexBuffer);
		
		Texture pyramidTexture = engine.getTextureManager().getAssetByPath("blue.jpeg");
		TextureState pyramidTextureState = (TextureState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
		pyramidTextureState.setTexture(pyramidTexture);
		
		FrontFaceState pyramidFrontFaceState = (FrontFaceState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
		
		line.setDataSource(DataSource.INDEX_BUFFER);
		line.setRenderState(pyramidTextureState);
		line.setRenderState(pyramidFrontFaceState);
		
		return line;
	}

}
