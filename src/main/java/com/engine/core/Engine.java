package com.engine.core;

import com.engine.input.Cursor;
import com.engine.input.Keyboard;
import com.engine.input.MouseButtons;
import com.engine.input.MouseWheel;
import com.engine.items.World;
import com.graphics.RendererEngine;
import com.utils.StopWatch;

import javax.swing.*;
import java.util.prefs.Preferences;

import static com.engine.core.Options.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Engine {

    public static final Preferences prefs = Preferences.userNodeForPackage(Engine.class);

    private static final int TARGET_UPDATES_PER_SECOND = 300;

    private final Window window;
    private final RendererEngine renderer;
    private final World world;
    private final StopWatch loopTimer;
    private final StopWatch fpsTimer;

    private final Thread loopThread;

    private int frameCount;
    private boolean running = false;

    public Engine() {
        this.window = new Window("3D Engine", 700, 600);
        this.renderer = new RendererEngine();
        this.world = new World();
        this.loopTimer = new StopWatch();
        this.fpsTimer = new StopWatch();

        this.loopThread = new Thread(() -> {
            try {
                loadOptions();
                window.init();
                System.err.println("WINDOW INITIALISED");
                renderer.init();
                System.err.println("RENDERER INITIALISED");
                world.init();
                System.err.println("GAME INITIALISED");

                System.err.printf("SUCCESSFUL BOOT OpenGL: %s Card: %s\n/////////////////////////////////////////////////////////////////////////////////////\n\n", glGetString(GL_VERSION), glGetString(GL_RENDERER));
                gameLoop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                world.dispose();
                renderer.dispose();
                window.dispose();
                saveOptions();
                System.err.println("\n/////////////////////////////////////////////////////////////////////////////////////\nENGINE TERMINATED");
            }
        });

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start() {
        running = true;
        loopThread.start();
    }

    private void gameLoop() {
        float elapsedTime;
        float cumulativeTime = 0f;
        final float SECONDS_PER_UPDATE = 1f / (float)TARGET_UPDATES_PER_SECOND;
        final float SECONDS_PER_FRAME = 1f / window.getRefreshRate();
        loopTimer.start();
        fpsTimer.start();

        while (running && window.isOpen()) {
            elapsedTime = loopTimer.getElapsedTime();
            cumulativeTime += elapsedTime;
            loopTimer.start();

            if(window.isFocused()) {
                handleInput();
            }

            while (cumulativeTime >= SECONDS_PER_UPDATE) {
                update(SECONDS_PER_UPDATE);
                cumulativeTime -= SECONDS_PER_UPDATE;
            }

            if(!window.isMinimized()) {
                render();
            }

            window.update();

            if (isVSyncEnabled()) {
                sync(SECONDS_PER_FRAME);
            }

            loopTimer.stop();
        }
    }

    private void sync(float SECONDS_PER_FRAME) {
        double expectedEndTime = loopTimer.getLoopStartTime() + SECONDS_PER_FRAME;
        while (loopTimer.getTime() < expectedEndTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput() {
        if(Keyboard.isKeyTapped(GLFW_KEY_M)) {
            toggleFullScreenMode();
        }

//        if(Keyboard.isKeyTapped(GLFW_KEY_F1)){
//            renderer.takeScreenshot();
//        }

        if(!isWindowFullScreen()){
            if(Cursor.inCameraMode()){
                if (Keyboard.isKeyTapped(GLFW_KEY_ESCAPE)) {
                    Cursor.setCameraMode(false);
                }
            }else {
                if(MouseButtons.isButtonTapped(GLFW_MOUSE_BUTTON_1)){
                    Cursor.setCameraMode(true);
                }
                if (Keyboard.isKeyTapped(GLFW_KEY_ESCAPE)) {
                    stop();
                }
            }
        }

        if(Cursor.onScreen()){
            world.handleInput();
        }

        Keyboard.update();
        MouseButtons.update();
        Cursor.update();
        MouseWheel.update();
    }

    private void update(float interval) {
        world.updateLogic(interval);
    }

    private void render() {
        if (fpsTimer.getElapsedTime() > 1) {
            fpsTimer.restart();
            window.appendToTitle(String.format("FPS: %d", frameCount));
            frameCount = 0;
        }
        frameCount++;

        try {
            world.render(window, renderer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}