package com.elderthings.dunwatch;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class Dunwatch implements ApplicationListener, InputProcessor {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sprite body;
    private AtlasRegion bodyRegion;
    private ArrayList<Array<Sprite>> tentacles;
    private ArrayList<Array<AtlasRegion>> tentacleRegions;
    private ArrayList<Integer> tentacleStates;
    private int index;

    public static final int TOP_RIGHT = 94;
    public static final int BOTTOM_RIGHT = 95;
    public static final int TOP_LEFT = 92;
    public static final int BOTTOM_LEFT = 93;

    public static final int NUM_TENTACLES = 8;

    @Override
    public void create() {
        String name;

        Gdx.graphics.setContinuousRendering(false);

        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        batch = new SpriteBatch();

        TextureAtlas atlas = new TextureAtlas(
                Gdx.files.internal("packed/dunwatch.atlas"));

        // Init the body
        name = "cthulhu/cthulhu_body";
        body = atlas.createSprite(name);
        bodyRegion = atlas.findRegion(name);

        // Init the tentacles
        tentacles = new ArrayList<Array<Sprite>>();
        tentacleRegions = new ArrayList<Array<AtlasRegion>>();
        tentacleStates = new ArrayList<Integer>();
        for (int i = 0; i < NUM_TENTACLES; i++) {
            name = "cthulhu/t_" + String.valueOf(i + 1) + "/t";
            tentacles.add(atlas.createSprites(name));
            tentacleRegions.add(atlas.findRegions(name));
            tentacleStates.add(0);
        }
        Gdx.input.setInputProcessor(this);
        Gdx.graphics.requestRendering();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            index++;
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());
        draw();
    }

    private void update(float dt) {
        for (int i = 0; i < NUM_TENTACLES; i++) {
            tentacleStates.set(i, index % 3);
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float x, y;
        Sprite currentFrame;
        AtlasRegion currentRegion;
        int state;
        for (int i = 0; i < NUM_TENTACLES; i++) {
            state = tentacleStates.get(i);
            currentFrame = tentacles.get(i).get(state);
            currentRegion = tentacleRegions.get(i).get(state);
            x = currentRegion.offsetX;
            y = currentRegion.offsetY;
            batch.draw(currentFrame, x, y);
        }
        batch.draw(body, bodyRegion.offsetX, bodyRegion.offsetY);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    // Input
    @Override
    public boolean keyDown(int keycode) {
        boolean handled = false;
        switch (keycode) {
        case BOTTOM_LEFT:
        case Keys.DOWN:
            handled = true;
            break;
        case BOTTOM_RIGHT:
        case Keys.RIGHT:
            handled = true;
            index += 1;
            break;
        case TOP_LEFT:
        case Keys.LEFT:
            handled = true;
            index -= 1;
            break;
        case TOP_RIGHT:
        case Keys.UP:
            handled = true;
            break;
        }
        if (handled) {
            Gdx.graphics.requestRendering();
        }
        return handled;
    }

    @Override
    public boolean keyUp(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        // TODO Auto-generated method stub
        return false;
    }

}
