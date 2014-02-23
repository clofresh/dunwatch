package com.elderthings.dunwatch.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.elderthings.dunwatch.Dunwatch;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "dunwatch";
        cfg.useGL20 = false;
        cfg.width = 800;
        cfg.height = 600;

        new LwjglApplication(new Dunwatch(), cfg);
    }
}
