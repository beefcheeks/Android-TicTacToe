package com.example.tictactoe;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Canvas;
import android.graphics.Paint.Style;


public class DrawBoard extends SurfaceView implements SurfaceHolder.Callback {
	
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
	//DrawThread boardThread;
	BoardThread boardThread;
	SurfaceHolder boardHolder;
	Context contxt;
	
	public DrawBoard(Context context){
		super(context);
		contxt = context;
		boardHolder = getHolder();
		boardHolder.addCallback(this);
	}
	
	public void startNewGame(Game newGame){
		game = newGame;
		setDisplayConstants(contxt);
	}
	
	class BoardThread implements Runnable {
		boolean threadIsRunning;
		
		@Override
		public void run() {
			Canvas boardCanvas;
			threadIsRunning = true;
			while(threadIsRunning){
				boardCanvas = null;
				try{
					boardCanvas = boardHolder.lockCanvas(null);
					if(boardCanvas!=null){
						synchronized(boardHolder){
						  draw(boardCanvas);
						}
					}	
				} finally{
					if(boardCanvas!=null){
						boardHolder.unlockCanvasAndPost(boardCanvas);
					}
				}
			}
		}
		
		void setRunState(boolean isRunning) {
			threadIsRunning = isRunning;
		}
		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)	{
		boardThread.setRunState(false);
	}
	 
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height)	{}

	@Override
	public void surfaceCreated(SurfaceHolder holder){
		setWillNotDraw(false);
		boardThread = new BoardThread();
	}

	//Draws a grid based on the number of horizontal and vertical lines
	//@Override
	public void onDraw(Canvas canvas){
		drawGrid(canvas);
		if(game.boardHasMoves()) drawMoves(canvas);
		if(game.hasEnded()) endGame(canvas);
		
	}
	
	/**Determines pixels**/
	private void setDisplayConstants(Context context){
		numHorizontalLines = game.getRows()-1;
		numVerticalLines = game.getColumns()-1;
		displayWidth = context.getResources().getDisplayMetrics().widthPixels;
		displayHeight=displayWidth;
		lineSpacingVertical = displayWidth/(numVerticalLines+1);
		lineSpacingHorizontal = displayHeight/(numHorizontalLines+1);
		//displayHeight=displayMetrics.heightPixels - SYSTEM_BAR_OFFSET;
	}
	
	private void drawGrid(Canvas canvas){
		canvas.drawColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(STROKE_WIDTH);
		paint.setColor(Color.BLACK);
		for(int i=0;i<(numHorizontalLines+1);i++){
			int Lineheight = (i+1)*lineSpacingHorizontal;
			canvas.drawLine(0,Lineheight,displayWidth,Lineheight, paint);
		}
		
		for(int i=0;i<numVerticalLines;i++){
			int Linewidth = (i+1)*displayWidth/(numVerticalLines+1);
			canvas.drawLine(Linewidth, 0, Linewidth, displayHeight, paint);
		}
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
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(STROKE_WIDTH*2);
		paint.setColor(Color.BLACK);
		canvas.drawLine(column*lineSpacingVertical+PADDING, row*lineSpacingHorizontal+PADDING, (column+1)*lineSpacingVertical-PADDING, (row+1)*lineSpacingHorizontal-PADDING, paint);
		canvas.drawLine(column*lineSpacingVertical+PADDING, (row+1)*lineSpacingHorizontal-PADDING, (column+1)*lineSpacingVertical-PADDING, row*lineSpacingHorizontal+PADDING, paint);
	}
	
	private void drawO(Canvas canvas, int row, int column){
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(STROKE_WIDTH*2);
		paint.setColor(Color.RED);
		RectF oval = new RectF(column*lineSpacingVertical+PADDING, row*lineSpacingHorizontal+PADDING, (column+1)*lineSpacingVertical-PADDING, (row+1)*lineSpacingHorizontal-PADDING);
		canvas.drawOval(oval, paint);
		//canvas.drawCircle(row*lineSpacingVertical+lineSpacingVertical/2, column*lineSpacingHorizontal+lineSpacingHorizontal/2, 4*lineSpacingVertical/10, paint);
	}
}

