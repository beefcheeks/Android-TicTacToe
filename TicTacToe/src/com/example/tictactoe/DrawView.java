package com.example.tictactoe;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.graphics.Canvas;


public class DrawView extends View {
	
	final float STROKE_WIDTH = 7;
	final int PADDING = 25;
	
	private int displayWidth;
	private int displayHeight;
	
	private int numHorizontalLines;
	private int numVerticalLines;
	private int lineSpacingVertical;
	private int lineSpacingHorizontal;
	
	private Game game;
	Paint paint = new Paint();
	
	public DrawView(Context context, Game newGame){
		super(context);
		paint.setStrokeWidth(STROKE_WIDTH);
		paint.setStyle(Paint.Style.STROKE);
		numHorizontalLines = newGame.getRows()-1;
		numVerticalLines = newGame.getColumns()-1;
		game = newGame;
		setDisplayConstants(context);
	}
	
	//Draws a grid based on the number of horizontal and vertical lines
	@Override
	public void onDraw(Canvas canvas){
		drawGrid(canvas);
		if(game.boardHasMoves()) drawMoves(canvas);
		if(game.hasEnded()) endGame(canvas);
		
	}
	
	public void newGame(Game newGame){
		game = newGame;
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.BLACK);
	}
	
	//Determine the width and height of the display in pixels
	private void setDisplayConstants(Context context){
		displayWidth = context.getResources().getDisplayMetrics().widthPixels;
		displayHeight=displayWidth;
		lineSpacingVertical = displayWidth/(numVerticalLines+1);
		lineSpacingHorizontal = displayHeight/(numHorizontalLines+1);
		//displayHeight=displayMetrics.heightPixels - SYSTEM_BAR_OFFSET;
		  }
	
	private void drawGrid(Canvas canvas){
		paint.setStrokeWidth(STROKE_WIDTH);
		for(int i=0;i<(numHorizontalLines+1);i++){
			int Lineheight = (i+1)*lineSpacingHorizontal;
			canvas.drawLine(0,Lineheight,displayWidth,Lineheight, paint);
		}
		
		for(int i=0;i<numVerticalLines;i++){
			int Linewidth = (i+1)*displayWidth/(numVerticalLines+1);
			canvas.drawLine(Linewidth, 0, Linewidth, displayHeight, paint);
		}
		paint.setStrokeWidth(STROKE_WIDTH*2);
	}
	
	private void drawMoves(Canvas canvas){
		for(int i=0; i<game.getRows();i++){
			for(int j=0; j<game.getColumns();j++){
				char ch = game.getMove(i, j);
				if(ch=='X'){
					drawX(canvas, i, j);
				} else if(ch=='O'){
					drawO(canvas, i, j);
				}
			}
		}
	}
	
	private void endGame(Canvas canvas){
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(STROKE_WIDTH/3);
		paint.setTextSize(46);
		if(game.boardFull()){
			paint.setColor(Color.BLACK);
			canvas.drawText("Tie! :O Tap screen to play again", PADDING, displayWidth+100, paint);
			
		} else if (game.otherPlayerWon()){
			paint.setColor(Color.RED);
			canvas.drawText("You Lose! :( Tap screen to play again", PADDING, displayWidth+100, paint);

			
		} else if (game.playerWon()){
			paint.setColor(Color.BLACK);
			canvas.drawText("You Win! :D Tap screen to play again", PADDING, displayWidth+100, paint);
		}
	}
	
	private void drawX(Canvas canvas, int row, int column){
		canvas.drawLine(column*lineSpacingVertical+PADDING, row*lineSpacingHorizontal+PADDING, (column+1)*lineSpacingVertical-PADDING, (row+1)*lineSpacingHorizontal-PADDING, paint);
		canvas.drawLine(column*lineSpacingVertical+PADDING, (row+1)*lineSpacingHorizontal-PADDING, (column+1)*lineSpacingVertical-PADDING, row*lineSpacingHorizontal+PADDING, paint);
	}
	
	private void drawO(Canvas canvas, int row, int column){
		paint.setColor(Color.RED);
		RectF oval = new RectF(column*lineSpacingVertical+PADDING, row*lineSpacingHorizontal+PADDING, (column+1)*lineSpacingVertical-PADDING, (row+1)*lineSpacingHorizontal-PADDING);
		canvas.drawOval(oval, paint);
		//canvas.drawCircle(row*lineSpacingVertical+lineSpacingVertical/2, column*lineSpacingHorizontal+lineSpacingHorizontal/2, 4*lineSpacingVertical/10, paint);
		paint.setColor(Color.BLACK);
	}
}

