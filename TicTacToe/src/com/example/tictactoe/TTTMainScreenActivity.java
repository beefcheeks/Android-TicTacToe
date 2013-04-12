package com.example.tictactoe;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.app.Activity;
import android.content.Intent;


public class TTTMainScreenActivity extends Activity implements Constants {
	private DrawBoard drawBoard;
	private Game game;
	private ComputerPlayer computer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent buttonIntent = getIntent();
		String gameType = buttonIntent.getExtras().getString(Constants.BUTTON_KEY);
		int numRows = Integer.parseInt(buttonIntent.getExtras().getString(Constants.ROW_KEY));
		int numColumns = Integer.parseInt(buttonIntent.getExtras().getString(Constants.COLUMN_KEY));
		int numNeededToWin = Integer.parseInt(buttonIntent.getExtras().getString(Constants.WIN_KEY));
		resetBoard(numRows, numColumns, numNeededToWin, gameType);
		drawBoard = new DrawBoard(this);
		drawBoard.startNewGame(game);
		
		
		setContentView(drawBoard);
		
		drawBoard.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
					if(game.hasEnded()){
						resetBoard(game.getRows(), game.getColumns(), game.getNumToWin(), game.getGameType());
						drawBoard.startNewGame(game);
						drawBoard.postInvalidate();
					} else {
						addPlayerMove((int) event.getX(), (int) event.getY());
						if(game.getGameType().equals(Constants.SINGLE_PLAYER) && game.player1Moved() && !game.hasEnded()) {
							computer.makeBestMove();
							drawBoard.postInvalidate();
						}
					}					
					return true;
				}
				return false;
			}
			
		});		
	}
	
	private void addPlayerMove(int x, int y){
		int displayWidth = this.getResources().getDisplayMetrics().widthPixels;
		for(int i=0; i<game.getRows(); i++){
			if(y<=((i+1)*displayWidth/game.getRows()) && y>(i*displayWidth/game.getRows())){
				for(int j=0; j<game.getColumns(); j++){
					if(x<=((j+1)*displayWidth/game.getColumns()) && x >(j*displayWidth/game.getColumns()) && game.isEmpty(i, j)){
						game.addMove(i, j);
						game.efficientCheckForWin();
						drawBoard.postInvalidate();	
						return;
					}
				}
			}
		}
	}		

	private void resetBoard(int rows, int columns, int numToWin, String typeOfGame){
		game = new Game (rows, columns, numToWin, typeOfGame);
		if(typeOfGame.equals(Constants.SINGLE_PLAYER)) computer = new ComputerPlayer(game, 'O');	
	}
	
	/*@Override
	public void onBackPressed(){
		drawBoard.boardThread.setThreadRunState(false);
		//drawBoard.cleanUpThreads();
		boolean tryAgain = true;
		//while(tryAgain){
			try{
				drawBoard.boardThread.join();
			}catch(InterruptedException e){}
		//}
		finish();
	}*/
}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tttmain_screen, menu);
		return true;
	}*/

