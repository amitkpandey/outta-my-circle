package com.example.mfaella.physicsapp;

import android.util.Log;

import com.badlogic.androidgames.framework.Button;
import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.AndroidCircularButton;
import com.badlogic.androidgames.framework.impl.AndroidRectangularButton;

public class MainMenuScreen extends Screen {
    private final Button sound = new AndroidCircularButton(200,200,150);
    private final Button start = new AndroidRectangularButton(500,500,100,100);
    private int colorStart = android.graphics.Color.BLUE;
    public MainMenuScreen(Game game) {
        super(game);
        game.getInput().getTouchEvents(); //clear
    }

    @Override
    public void update(float deltaTime) {
        Graphics g = game.getGraphics();

        game.getInput().getKeyEvents();

        for(TouchEvent event: game.getInput().getTouchEvents()) {
            if(event.type == TouchEvent.TOUCH_UP) {
                if(sound.inBounds(event)) {
                    Log.d("MainMenuScreen","sound");
                    Settings.soundEnabled = !Settings.soundEnabled;
//                    if(Settings.soundEnabled)
  //                      Assets.click.play(1);
                }
                if(start.inBounds(event)) {
                    Log.d("MainMenuScreen","start");
                    if(colorStart == android.graphics.Color.BLUE)
                        colorStart = android.graphics.Color.MAGENTA;
                    else
                        colorStart = android.graphics.Color.BLUE;
    //                if(Settings.soundEnabled)
      //                  Assets.click.play(1);
                    //Start lobby
                }
            }
        }
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();
        g.drawTile(Assets.backgroundTile, 0,0, g.getWidth(), g.getHeight());
        if(Settings.soundEnabled)
            sound.draw(g, android.graphics.Color.GREEN);
        else
            sound.draw(g, android.graphics.Color.RED);
        start.draw(g, colorStart);
    }

    @Override
    public void pause() {
       // Settings.save(game.getFileIO());
    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
