package com.saechaol.game.myGameEngine.object.manual;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import ray.rage.Engine;
import ray.rage.asset.material.Material;
import ray.rage.asset.texture.Texture;
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
    	Texture floorTexture = engine.getTextureManager().getAssetByPath("water.png");
    	
    	float [] floorVertices = new float[] {
    			-100.0f, 0.0f, -100.0f, 
    			-100.0f, 0.0f, 100.0f, 
    			100.0f, 0.0f, 0.0f,
    			
    			100.0f, 0.0f, 100.0f, 
    			100.0f, 0.0f, -100.0f, 
    			-100.0f, 0.0f, 100.0f
    	};
    	
    	int[] indices = new int[] { 0, 1, 2, 3, 4, 5 };
    	
    	FloatBuffer floorVertexBuffer = BufferUtil.directFloatBuffer(floorVertices);
    	IntBuffer floorIndexBuffer = BufferUtil.directIntBuffer(indices);
    	
    	floorSection.setVertexBuffer(floorVertexBuffer);
    	floorSection.setIndexBuffer(floorIndexBuffer);
    	
    	
    	floorMaterial.setEmissive(Color.BLUE);
    	
    	
    	TextureState floorTextureState = (TextureState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	floorTextureState.setTexture(floorTexture);
    	
    	FrontFaceState floorFrontFaceState = (FrontFaceState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	
    	floor.setRenderState(floorTextureState);
    	floor.setRenderState(floorFrontFaceState);
    	
    	return floor;
    }
	
}
