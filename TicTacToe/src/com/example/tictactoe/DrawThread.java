package com.example.tictactoe;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;


public class DrawThread extends Thread {

	boolean threadIsRunning;
	Canvas boardCanvas;
	Context context;
	SurfaceHolder boardHolder;
	DrawBoard boardView;
	
	public DrawThread(Context setContext, DrawBoard brdView, SurfaceHolder surfaceHolder){
		threadIsRunning = false;
		context = setContext;
		boardView = brdView;
		boardHolder = surfaceHolder;
	}
	
	void setThreadRunState(boolean isRunning) {
		threadIsRunning = isRunning;
	}
	
	@Override
	public void run(){
		super.run();
		while(threadIsRunning){
			boardCanvas = null;
			try{
				boardCanvas = boardHolder.lockCanvas(null);
				if(boardCanvas!=null){
					synchronized(boardHolder){
						boardView.draw(boardCanvas);
					}
				}	
			} finally{
				if(boardCanvas!=null){
					boardHolder.unlockCanvasAndPost(boardCanvas);
				}
			}
		}
	}
}
