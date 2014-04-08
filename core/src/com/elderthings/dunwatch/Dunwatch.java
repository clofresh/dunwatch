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
    private Random generator;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    // Tentacle state
    private Sprite body;
    private AtlasRegion bodyRegion;
    private ArrayList<Array<Sprite>> tentacles;
    private ArrayList<Array<AtlasRegion>> tentacleRegions;
    private ArrayList<Integer> tentacleStates;
    private double tentacleTimer;
    private Integer tentacleLevel;

    // Pig state
    private ArrayList<Array<Sprite>> pigSprites;
    private ArrayList<Array<AtlasRegion>> pigRegions;
    private Integer pigDir;
    private Integer pigLocation;
    private float pigMoveTimer;
    private float pigHitTimer;
    private Integer newDirection = -1;
    private HashMap<Integer, Integer> pigToTentacle;
    private Integer pigLife;

    // Fruit state
    private Integer fruitLocation;
    private float fruitTimer;
    private Array<Sprite> fruitSprites;
    private Array<AtlasRegion> fruitRegions;
    private Integer pigFruits;
    private Integer tentacleFruits;

    // Nook button constants
    public static final int TOP_RIGHT = 94;
    public static final int BOTTOM_RIGHT = 95;
    public static final int TOP_LEFT = 92;
    public static final int BOTTOM_LEFT = 93;

    // Tentacle constants
    public static final int NUM_TENTACLES = 8;
    public static final int NUM_LOCATIONS = 10;
    public static final int NUM_TENTACLE_STATES = 3;
    public static final int CHANGES_PER_RENDER = 2;
    public static final double MAX_LEVELS_TIL_ZERO = 10;
    public static final double TENTACLE_START_DELAY = 0.5;

    // Pig constants
    public static final int PIG_LEFT = 0;
    public static final int PIG_RIGHT = 1;
    public static final float PIG_MOVE_DELAY = 0.125f;
    public static final float PIG_HIT_DELAY = 0.5f;
    public static final int MAX_PIG_LIFE = 3;

    // Fruit constants
    public static final float FRUIT_SPAWN_DELAY = 1f;
    public static final float FRUIT_DISAPPEAR_DELAY = 5.0f;
    public static final int WINNING_THRESHOLD = 3;

    @Override
    public void create() {
        String name;

        generator = new Random();
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
            tentacleStates.add(null);
        }

        // Init the pig
        pigSprites = new ArrayList<Array<Sprite>>();
        pigRegions = new ArrayList<Array<AtlasRegion>>();
        pigSprites.add(atlas.createSprites("cthulhu/pig_bw/pb"));
        pigRegions.add(atlas.findRegions("cthulhu/pig_bw/pb"));
        pigSprites.add(atlas.createSprites("cthulhu/pig_fw/pf"));
        pigRegions.add(atlas.findRegions("cthulhu/pig_fw/pf"));
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

        // Init the fruit
        fruitSprites = atlas.createSprites("cthulhu/pear/fp");
        fruitRegions = atlas.findRegions("cthulhu/pear/fp");

        Gdx.input.setInputProcessor(this);
        reset();
    }

    private void reset() {
        // Init the tentacles
        for (int i = 0; i < NUM_TENTACLES; i++) {
            tentacleStates.set(i, 0);
        }
        tentacleTimer = 0.0f;
        tentacleLevel = 0;

        // Init the pig
        pigDir = PIG_RIGHT;
        pigLocation = 0;
        pigMoveTimer = 0.0f;
        pigHitTimer = 0.0f;
        pigLife = MAX_PIG_LIFE;

        // Init the fruit
        pigFruits = 0;
        tentacleFruits = 0;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    private void movePig(Integer direction) {
        newDirection = direction;
    }

    private double computeTentacleDelay() {
        // The delay value is an inverted parabola whose peak is at y =
        // TENTACLE_START_DELAY and intersects the x axis at
        // x = MAX_LEVELS_TIL_ZERO, meaning that that point, there is no delay
        return -TENTACLE_START_DELAY / MAX_LEVELS_TIL_ZERO
                / MAX_LEVELS_TIL_ZERO * tentacleLevel * tentacleLevel
                + TENTACLE_START_DELAY;
    }

    private void updatePig(float deltaTime) {
        pigMoveTimer += deltaTime;
        if (pigMoveTimer > PIG_MOVE_DELAY) {
            if (newDirection == PIG_LEFT) {
                if (pigLocation > 0) {
                    pigLocation--;
                }
                pigDir = newDirection;
                newDirection = -1;
            } else if (newDirection == PIG_RIGHT) {
                if (pigLocation < NUM_LOCATIONS - 1) {
                    pigLocation++;
                }
                pigDir = newDirection;
                newDirection = -1;
            }
            pigMoveTimer = 0.0f;
        }
    }

    private void updateTentacles(float deltaTime) {
        tentacleTimer += deltaTime;
        if (tentacleTimer > computeTentacleDelay()) {
            int choice, val, newVal;
            int j;
            for (int i = 0; i < CHANGES_PER_RENDER; i++) {
                j = generator.nextInt(NUM_TENTACLES);
                val = tentacleStates.get(j);
                if (val == 0) {
                    choice = generator.nextInt(NUM_TENTACLE_STATES - 1);
                } else if (val == 2) {
                    choice = generator.nextInt(NUM_TENTACLE_STATES - 1) - 1;
                } else {
                    choice = generator.nextInt(NUM_TENTACLE_STATES) - 1;
                }
                newVal = val + choice;
                if (newVal != val) {
                    tentacleStates.set(j, newVal % NUM_TENTACLE_STATES);
                }
            }
            tentacleTimer = 0.0f;
        }
    }

    private void updateFruit(float deltaTime) {
        fruitTimer += deltaTime;
        if (fruitLocation == null && fruitTimer > FRUIT_SPAWN_DELAY) {
            fruitLocation = getFreeFruitLocation();
            fruitTimer = 0;
        } else if (fruitLocation != null && fruitTimer > FRUIT_DISAPPEAR_DELAY) {
            fruitLocation = null;
            fruitTimer = 0;
        }
    }

    private Integer getFreeFruitLocation() {
        return pigToTentacle.get(generator.nextInt(NUM_LOCATIONS));
    }

    private void checkCollisions(float deltaTime) {
        pigHitTimer += deltaTime;
        Integer pigPos = pigToTentacle.get(pigLocation);
        Integer tentacleState;
        if (pigPos != null) {
            tentacleState = tentacleStates.get(pigPos);
            if (tentacleState == NUM_TENTACLE_STATES - 1
                    && pigHitTimer > PIG_HIT_DELAY) {
                pigLife--;
                System.out.println(String.format(
                        "Pig hit by tentacle at %d. %d life left.", pigPos,
                        pigLife));
                pigHitTimer = 0.0f;
            }
        }
        if (fruitLocation != null) {
            tentacleState = tentacleStates.get(fruitLocation);
            if (fruitLocation == pigPos) {
                pigFruits++;
                System.out.println(String.format(
                        "Pig got fruit at %d. %d to %d.", fruitLocation,
                        pigFruits, tentacleFruits));
                fruitLocation = null;
            } else if (tentacleState == NUM_TENTACLE_STATES - 1) {
                tentacleFruits++;
                System.out.println(String.format(
                        "Tentacle got fruit at %d. %d to %d.", fruitLocation,
                        tentacleFruits, pigFruits));
                fruitLocation = null;
            }
        }

    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        updatePig(deltaTime);
        updateTentacles(deltaTime);
        updateFruit(deltaTime);
        checkCollisions(deltaTime);

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

        // draw the fruit
        if (fruitLocation != null) {
            currentFrame = fruitSprites.get(fruitLocation);
            currentRegion = fruitRegions.get(fruitLocation);
            batch.draw(currentFrame, currentRegion.offsetX,
                    currentRegion.offsetY);
        }

        batch.end();

        // Check end conditions
        if (pigLife <= 0) {
            System.out.println(String.format("Dead! Pig: %d, Tentacles: %d",
                    pigFruits, tentacleFruits));
            reset();
        } else if (tentacleFruits >= WINNING_THRESHOLD) {
            tentacleLevel++;
            System.out
                    .println(String
                            .format("Tentacles win, %d to %d. Raising level to %d (tentacle delay: %f)",
                                    tentacleFruits, pigFruits, tentacleLevel,
                                    computeTentacleDelay()));
            tentacleFruits = 0;
            pigFruits = 0;
        } else if (pigFruits >= WINNING_THRESHOLD) {
            tentacleLevel++;
            System.out
                    .println(String
                            .format("Pig wins, %d to %d. Raising level to %d (tentacle delay: %f)",
                                    pigFruits, tentacleFruits, tentacleLevel,
                                    computeTentacleDelay()));
            pigLife = MAX_PIG_LIFE;
            tentacleFruits = 0;
            pigFruits = 0;
        }
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
            movePig(PIG_RIGHT);
            handled = true;
            break;
        case TOP_LEFT:
        case TOP_RIGHT:
        case Keys.LEFT:
            movePig(PIG_LEFT);
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
