package com.coinmain.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.GrayFilter;

public class CoinMan extends ApplicationAdapter {
	SpriteBatch batch;
	Texture background;
	Texture[] man;
	Texture coin;
	Texture bomb;
	Texture dizzyman;
	Rectangle manRectangle;
	int currentScore;
	int highScore=0;

	BitmapFont font;
	BitmapFont highscoreFont;

	int manState;
	int pause=0;
	int manY=0;
	int manX=0;
	int coinCount;
	int bombCount;
	int score;
	int gameState=0;

	Random random;

	float gravity=0.2f;
	float velocity=0;
	float heightCoin;
	float heightBomb=0;

	Preferences prefs;

	ArrayList<Integer> coinXs=new ArrayList<>();
	ArrayList<Integer> coinYs=new ArrayList<>();

	ArrayList<Rectangle> coinRectangles=new ArrayList<>();

    ArrayList<Integer> bombXs=new ArrayList<>();
    ArrayList<Integer> bombYs=new ArrayList<>();

	ArrayList<Rectangle> bombRectangles=new ArrayList<>();

	@Override
	public void create () {
		batch = new SpriteBatch();
		background=new Texture("bg.png");
		man=new Texture[4];
		man[0]=new Texture("frame-1.png");
		man[1]=new Texture("frame-2.png");
		man[2]=new Texture("frame-3.png");
		man[3]=new Texture("frame-4.png");
		dizzyman=new Texture("dizzy-1.png");
		manY=man[manState].getHeight();
		manX=Gdx.graphics.getWidth()/2-man[manState].getWidth()/2;
		coin=new Texture("coin.png");
		bomb=new Texture("bomb.png");

		prefs=Gdx.app.getPreferences("game preferences");
		random=new Random();

		font=new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(10);

		highscoreFont=new BitmapFont();
		highscoreFont.setColor(Color.WHITE);
		highscoreFont.getData().setScale(10);
	}

	public void makeCoin(){
		heightCoin= random.nextFloat() * Gdx.graphics.getHeight();
		if(heightCoin>=(Gdx.graphics.getHeight()-coin.getHeight())){
			heightCoin= random.nextFloat() * Gdx.graphics.getHeight();
		}
		coinYs.add((int)heightCoin);
		coinXs.add(Gdx.graphics.getWidth());
	}

	public void makeBomb(){
        heightBomb= random.nextFloat() * Gdx.graphics.getHeight();
        if(heightBomb==heightCoin){
			heightBomb= random.nextFloat() * Gdx.graphics.getHeight();
		}
		if(heightBomb>=(Gdx.graphics.getHeight()-bomb.getHeight())){
			heightBomb= random.nextFloat() * Gdx.graphics.getHeight();
		}
        bombYs.add((int)heightBomb);
        bombXs.add(Gdx.graphics.getWidth());
    }

	@Override
	public void render () {
		batch.begin();
		batch.draw(background,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		if(gameState==1){
			//Game is Live

			if(bombCount<500){
				bombCount++;
			}else {
				bombCount=0;
				makeBomb();
			}

			bombRectangles.clear();
			for (int i=0;i<bombXs.size();i++){
				batch.draw(bomb,bombXs.get(i),bombYs.get(i));
				bombXs.set(i,bombXs.get(i)-7);
				bombRectangles.add(new Rectangle(bombXs.get(i),bombYs.get(i),bomb.getWidth(),bomb.getHeight()));
			}

			if(coinCount<100){
				coinCount++;
			}else {
				coinCount=0;
				makeCoin();
			}

			coinRectangles.clear();
			for(int i=0;i<coinXs.size();i++){
				batch.draw(coin,coinXs.get(i),coinYs.get(i));
				coinXs.set(i,coinXs.get(i)-4);
				coinRectangles.add(new Rectangle(coinXs.get(i),coinYs.get(i),coin.getWidth(),coin.getHeight()));
			}


			if(Gdx.input.justTouched()){
				velocity=-10;
			}
			if(pause<8){
				pause++;
			}else {
				pause=0;
				if (manState < 3) {
					manState++;
				} else {
					manState = 0;
				}
			}

			velocity += gravity;
			manY-=velocity;

			if(manY<=0){
				manY=0;
			}

		}else if(gameState==0){
			//Waiting to start
			if(Gdx.input.justTouched()){
				gameState=1;
			}
		}else if(gameState==2){
			//Game Over
            currentScore=score;
            if(currentScore>highScore){
            	highScore=currentScore;
                prefs.putInteger("highscore",highScore);
                prefs.flush();

			}
            Gdx.app.log("Highscore",String.valueOf(highScore));
			if(Gdx.input.justTouched()){
				gameState=1;
				manY=man[manState].getHeight();
				score=0;
				velocity=0;
				coinXs.clear();
				coinYs.clear();
				coinRectangles.clear();
				coinCount=0;
				bombXs.clear();
				bombYs.clear();
				bombRectangles.clear();
				bombCount=0;
			}
		}

		if(gameState==2){
            batch.draw(dizzyman,manX,manY);
        }else {
            batch.draw(man[manState], manX, manY);
        }
		manRectangle=new Rectangle(manX,manY,man[manState].getWidth(),man[manState].getHeight());

		for(int i=0;i<coinRectangles.size();i++){
			if(Intersector.overlaps(manRectangle,coinRectangles.get(i))){
				score++;

				coinRectangles.remove(i);
				coinXs.remove(i);
				coinYs.remove(i);
				break;
			}
		}


        for(int i=0;i<bombRectangles.size();i++){
            if(Intersector.overlaps(manRectangle,bombRectangles.get(i))){
                Gdx.app.log("Bomb!","BOOM!!!!");

                gameState=2;
            }
        }

        font.draw(batch,String.valueOf(score),100,200);
		highscoreFont.draw(batch,String.valueOf(highScore),Gdx.graphics.getWidth()-150,200);

		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
}
