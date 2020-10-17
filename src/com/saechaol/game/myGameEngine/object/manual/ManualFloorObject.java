package com.saechaol.game.myGameEngine.object.manual;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import ray.rage.Engine;
import ray.rage.asset.material.Material;
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

public class ManualFloorObject {

    public static ManualObject manualFloorObject(Engine engine, SceneManager sceneManager) throws IOException {
    	ManualObject floor = sceneManager.createManualObject("floorObject");
    	ManualObjectSection floorSection = floor.createManualSection(floor.getName() + "Section");
    	floor.setGpuShaderProgram(sceneManager.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		
    	Material floorMaterial = sceneManager.getMaterialManager().getAssetByPath("default.mtl");
    	Texture floorTexture = engine.getTextureManager().getAssetByPath("oceanTexture.jpg");
    	
    	float[] floorVertices = {
    			-100.0f, 0.0f, -100.0f, 
    			-100.0f, 0.0f, 100.0f, 
    			100.0f, 0.0f, 0.0f,
    
    			100.0f, 0.0f, 100.0f, 
    			100.0f, 0.0f, -100.0f, 
    			-100.0f, 0.0f, 100.0f	// front
    	};
    	
    	float[] floorTextureCoordinates = {
    			0.0f, 0.0f, 
    			1.0f, 1.0f, 
    			0.0f, 1.0f,
    			1.0f, 1.0f, 
    			0.0f, 0.0f, 
    			1.0f, 0.0f,		// front
    	};
    	
		float[] floorNormals = {
				0.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,		// front
		};
    	
    	int[] indices = { 0, 1, 2, 3, 4, 5 };
    	
    	FloatBuffer floorVertexBuffer = BufferUtil.directFloatBuffer(floorVertices);
		FloatBuffer floorTextureCoordinateBuffer = BufferUtil.directFloatBuffer(floorTextureCoordinates);
		FloatBuffer floorNormalsBuffer = BufferUtil.directFloatBuffer(floorNormals);
    	IntBuffer floorIndicesBuffer = BufferUtil.directIntBuffer(indices);
    	
    	floorSection.setVertexBuffer(floorVertexBuffer);
    	floorSection.setTextureCoordsBuffer(floorTextureCoordinateBuffer);
    	floorSection.setNormalsBuffer(floorNormalsBuffer);
    	floorSection.setIndexBuffer(floorIndicesBuffer);
    	
    	
    	floorMaterial.setEmissive(Color.BLUE);
    	
    	
    	TextureState floorTextureState = (TextureState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	floorTextureState.setTexture(floorTexture);
    	
    	FrontFaceState floorFrontFaceState = (FrontFaceState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	
    	floor.setDataSource(DataSource.INDEX_BUFFER);
    	floor.setRenderState(floorTextureState);
    	floor.setRenderState(floorFrontFaceState);
    	
    	return floor;
    }
	
}
