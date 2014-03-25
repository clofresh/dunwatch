package com.elderthings.dunwatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
    private ArrayList<Array<Sprite>> pigSprites;
    private ArrayList<Array<AtlasRegion>> pigRegions;
    private Integer pigDir;
    private Integer pigLocation;
    private HashMap<Integer, Integer> pigToTentacle;

    public static final int TOP_RIGHT = 94;
    public static final int BOTTOM_RIGHT = 95;
    public static final int TOP_LEFT = 92;
    public static final int BOTTOM_LEFT = 93;

    public static final int NUM_TENTACLES = 8;
    public static final int NUM_LOCATIONS = 10;
    public static final int NUM_TENTACLE_STATES = 3;
    public static final int CHANGES_PER_RENDER = 2;
    public static final int PIG_LEFT = 0;
    public static final int PIG_RIGHT = 1;

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

        // Init the pig
        pigSprites = new ArrayList<Array<Sprite>>();
        pigRegions = new ArrayList<Array<AtlasRegion>>();
        pigSprites.add(atlas.createSprites("cthulhu/pig_bw/pb"));
        pigRegions.add(atlas.findRegions("cthulhu/pig_bw/pb"));
        pigSprites.add(atlas.createSprites("cthulhu/pig_fw/pf"));
        pigRegions.add(atlas.findRegions("cthulhu/pig_fw/pf"));
        pigDir = PIG_RIGHT;
        pigLocation = 0;
        pigToTentacle = new HashMap<Integer, Integer>();
        pigToTentacle.put(0, 0);
        pigToTentacle.put(1, 1);
        pigToTentacle.put(2, null);
        pigToTentacle.put(3, 2);
        pigToTentacle.put(4, 3);
        pigToTentacle.put(5, 4);
        pigToTentacle.put(6, 5);
        pigToTentacle.put(7, null);
        pigToTentacle.put(8, 6);
        pigToTentacle.put(9, 7);

        Gdx.input.setInputProcessor(this);
        Gdx.graphics.requestRendering();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(750);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            updateTentacles();
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

    private void updatePig(int direction) {
        boolean changed = false;
        if (direction == PIG_LEFT) {
            if (pigLocation > 0) {
                pigLocation--;
                changed = true;
            }
            pigDir = direction;
        } else if (direction == PIG_RIGHT) {
            if (pigLocation < NUM_LOCATIONS - 1) {
                pigLocation++;
                changed = true;
            }
            pigDir = direction;
        }
        if (changed) {
            checkCollisions();
            Gdx.graphics.requestRendering();
        }
    }

    private void updateTentacles() {
        Random generator = new Random();
        int choice, val, newVal;
        int changed = 0;
        int j;
        for (int i = 0; i < CHANGES_PER_RENDER; i++) {
            j = generator.nextInt(NUM_TENTACLES);
            val = tentacleStates.get(j);
            if (val == 0) {
                choice = generator.nextInt(2);
            } else if (val == 2) {
                choice = generator.nextInt(2) - 1;
            } else {
                choice = generator.nextInt(3) - 1;
            }
            newVal = val + choice;
            if (newVal != val) {
                tentacleStates.set(j, newVal % NUM_TENTACLE_STATES);
                changed++;
            }
        }
        if (changed > 0) {
            checkCollisions();
            Gdx.graphics.requestRendering();
        }
    }

    private void checkCollisions() {
        Integer tentaclePos = pigToTentacle.get(pigLocation);
        if (tentaclePos != null) {
            Integer tentacleState = tentacleStates.get(tentaclePos);
            if (tentacleState == NUM_TENTACLE_STATES - 1) {
                System.out
                        .println(String
                                .format("Pig hit by tentacle at pigLocation %d, tentacleLocation %d",
                                        pigLocation, tentaclePos));
            }
        }
    }

    @Override
    public void render() {
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

        // draw the pig
        currentFrame = pigSprites.get(pigDir).get(pigLocation);
        currentRegion = pigRegions.get(pigDir).get(pigLocation);
        batch.draw(currentFrame, currentRegion.offsetX, currentRegion.offsetY);

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
        case BOTTOM_RIGHT:
        case Keys.RIGHT:
            updatePig(PIG_RIGHT);
            handled = true;
            break;
        case TOP_LEFT:
        case TOP_RIGHT:
        case Keys.LEFT:
            updatePig(PIG_LEFT);
            handled = true;
            break;
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
