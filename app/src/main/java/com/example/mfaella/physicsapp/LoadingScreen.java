package com.example.mfaella.physicsapp;

import android.graphics.Color;
import android.util.Log;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Graphics.PixmapFormat;

public class LoadingScreen extends com.badlogic.androidgames.framework.LoadingScreen {
    public LoadingScreen(Game game) {
        super(game);
    }

    @Override
    public void onProgress(int progress) {
        int x = (game.getGraphics().getWidth()-Assets.loading.getWidth())/2;
        int y = (game.getGraphics().getHeight()-Assets.loading.getHeight())/2;
        //x: 13, y: 254, width: 290, height: 19
        game.getGraphics().drawPixmap(Assets.loading, x,y);
        int val = 475;
        float vals = (float)val*(((float)progress)/100.0f);
        game.getGraphics().drawRect((int)vals+11+x,y+12,val-(int)vals,31, Color.BLACK);
        game.display();
    }

    @Override
    public void update(float deltaTime) {
        Assets.backgroundTile = game.getGraphics().newTile("bgtile.png", PixmapFormat.ARGB8888);
        Assets.loading = game.getGraphics().newPixmap("loading.png", PixmapFormat.ARGB8888);
        game.getGraphics().drawTile(Assets.backgroundTile, 0,0, game.getGraphics().getWidth(), game.getGraphics().getHeight());
        setProgress(70);
        Assets.click = game.getAudio().newSound("urto1.wav");
        setProgress(100);
        // long to = Calendar.getInstance().getTime().getTime()+50;
        // while(Calendar.getInstance().getTime().getTime() < to);
        // Assets.click = game.getAudio().newSound("click.ogg");
        // Settings.load(game.getFileIO());
        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void present(float deltaTime) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

}