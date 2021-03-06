package engine.core;

import componentSystem.World;
import engine.input.Cursor;
import engine.input.Keyboard;
import engine.input.MouseButtons;
import engine.input.MouseWheel;
import utils.StopWatch;

import javax.swing.*;

import static engine.core.Options.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Engine
{
    private static final int TARGET_UPDATES_PER_SECOND = 300;

    private final Window window;
    private final RendererEngine renderer;
    private final World world;
    private final StopWatch loopTimer;
    private final StopWatch fpsTimer;

    private final Thread loopThread;

    private int frameCount;
    private boolean running = false;

    public Engine()
    {
        this.window = new Window("3D Engine", 700, 600);
        this.renderer = new RendererEngine();
        this.world = new World();
        this.loopTimer = new StopWatch();
        this.fpsTimer = new StopWatch();

        this.loopThread = new Thread(() ->
        {
            try
            {
                loadOptions();
                window.init();
                renderer.init(window);
                world.init();

                System.out.printf("Successful Boot, OpenGL: %s Card: %s\n\n", glGetString(GL_VERSION), glGetString(GL_RENDERER));
                gameLoop();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                world.dispose();
                renderer.dispose();
                window.dispose();
                saveOptions();
            }
        });

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        running = true;
        loopThread.start();
    }

    private void gameLoop()
    {
        float elapsedTime;
        float cumulativeTime = 0f;
        final float secondsPerUpdate = 1f / (float) TARGET_UPDATES_PER_SECOND;
        final float secondsPerFrame = 1f / window.getRefreshRate();
        loopTimer.start();
        fpsTimer.start();

        while (running && window.isOpen())
        {
            elapsedTime = loopTimer.getElapsedTime();
            cumulativeTime += elapsedTime;
            loopTimer.start();

            if (window.isFocused())
            {
                handleInput();
            }

            while (cumulativeTime >= secondsPerUpdate)
            {
                update(secondsPerUpdate);
                cumulativeTime -= secondsPerUpdate;
            }

            if (!window.isMinimized())
            {
                render();
            }

            window.pollEvents();

            if (isVSyncEnabled())
            {
                sync(secondsPerFrame);
            }

            loopTimer.stop();
        }
    }

    private void sync(float secondsPerFrame)
    {
        double expectedEndTime = loopTimer.getStartTime() + secondsPerFrame;
        while (loopTimer.getTime() < expectedEndTime)
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void handleInput()
    {
        if (Keyboard.isKeyTapped(GLFW_KEY_F1))
        {
            renderer.takeScreenshot();
        }

        if (Keyboard.isKeyTapped(GLFW_KEY_F2) && !Cursor.isVisible())
        {
            window.toggleFullScreen();
        }

        if (Keyboard.isKeyTapped(GLFW_KEY_1))
        {
            Options.toggleCullFaces();
        }

        if (Keyboard.isKeyTapped(GLFW_KEY_2))
        {
            Options.toggleWireframeMode();
        }

        if (Keyboard.isKeyTapped(GLFW_KEY_3))
        {
            Options.toggleVSync();
        }

        if (!window.isFullScreen())
        {
            if (Cursor.isVisible())
            {
                if (MouseButtons.isButtonTapped(GLFW_MOUSE_BUTTON_1))
                {
                    Cursor.setVisibility(false);
                }
                if (Keyboard.isKeyTapped(GLFW_KEY_ESCAPE))
                {
                    stop();
                }
            }
            else
            {
                if (Keyboard.isKeyTapped(GLFW_KEY_ESCAPE))
                {
                    Cursor.setVisibility(true);
                }
            }
        }

        if (Cursor.onScreen())
        {
            world.handleInput();
        }

        Keyboard.update();
        MouseButtons.update();
        Cursor.update();
        MouseWheel.update();
    }

    private void update(float interval)
    {
        world.updateLogic(interval);
    }

    private void render()
    {
        if (fpsTimer.getElapsedTime() > 1)
        {
            fpsTimer.restart();
            window.appendToTitle(String.format("FPS: %d", frameCount));
            frameCount = 0;
        }
        frameCount++;

        window.preRender();
        world.preRender();

        try
        {
            renderer.render(window, world);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        running = false;
    }
}