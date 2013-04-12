package com.example.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.graphics.Point;


public class ComputerPlayer {
	
	private Game game;
	private Random random;
	private char computer;
	private char player;
	private boolean computerTurn;

	public  ComputerPlayer(Game newGame, char computerChar){
		game = newGame;
		random = new Random();
		computer = computerChar;
		if(computer=='X'){
			player = 'O';
		} else {
			player = 'X';
		}
	}
	
	public void nextMove(){
		computerTurn = true;
		if(computerTurn) computerWin();
		if(computerTurn) preventPlayerWin();
		if(computerTurn) checkCenter();
		if(computerTurn) adjacentMoveComputer();
		if(computerTurn) adjacentMovePlayer();
		
	}
	
	private void computerWin(){
		addToNextCellInLine(computer);
	}
	
	private void preventPlayerWin(){
		addToNextCellInLine(player);
	}
	
	private void adjacentMoveComputer(){
		adjacentMove(computer);
	}
	
	private void adjacentMovePlayer(){
		adjacentMove(player);
	}
	
	private void adjacentMove(char move){
		for(int i=0; i<game.getRows(); i++){
			for(int j=0; j<game.getColumns(); j++){
				if(game.getMove(i,j)==move){
					List<Point> adjCells = adjacentCells(i,j);
						for(int k=0; k<adjCells.size(); k++){
							int nextRow = adjCells.get(k).x;
							int nextColumn = adjCells.get(k).y;
							if(game.isEmpty(nextRow, nextColumn) && isDiagonal(i,j,nextRow,nextColumn)){
								game.addMove(nextRow, nextColumn);
								computerTurn=false;
								return;
							}
						}
						for(int l=0; l<adjCells.size(); l++){
							if(game.isEmpty(adjCells.get(l).x, adjCells.get(l).y)){
								game.addMove(adjCells.get(l).x, adjCells.get(l).y);
								computerTurn=false;
								return;
							}
						}
					}
				}
			}
		}
	
	private void addToNextCellInLine(char move){
			for(int i=0; i<game.getRows(); i++){
				for(int j=0; j<game.getColumns(); j++){
					if(game.getMove(i, j)== move){
						List<Point> borderCells = adjacentCells(i,j);
						int nextCellRow;
						int nextCellColumn;
						for(int k=0; k<borderCells.size(); k++){
							nextCellRow = borderCells.get(k).x;
							nextCellColumn = borderCells.get(k).y;
							if(game.getMove(nextCellRow, nextCellColumn) == move){
								List<Point> nextMoves = nextCellsInLine(i, j, nextCellRow, nextCellColumn);
								for(int m=0; m<nextMoves.size(); m++){
									if (game.isEmpty(nextMoves.get(m).x,nextMoves.get(m).y)) {
										game.addMove(nextMoves.get(m).x, nextMoves.get(m).y);
										if(move==computer) game.recordComputerWin();
										computerTurn=false;
										return;
									}
								}
							}
							if( game.isEmpty(nextCellRow, nextCellColumn)){
								List<Point> nextCells = nextCellsInLine(i,j, nextCellRow, nextCellColumn);
								for(int n=0; n<nextCells.size(); n++){
									if(game.getMove(nextCells.get(n).x, nextCells.get(n).y) == move){
										game.addMove(nextCellRow, nextCellColumn);
										if(move==computer) game.recordComputerWin();
										computerTurn=false;
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	
	public void makeBestMove(){
		//Some really big negative number
		int highScore = -100000000;
		int bestRow = 0;
		int bestColumn = 0;
		for(int i=0; i<game.getRows(); i++){
			for(int j=0; j<game.getColumns(); j++){
				if(game.isEmpty(i, j)){
					int score = moveScore(game.getBoard(), i, j, game.getLastMoveChar(), 1);
					if(score > highScore) {
						highScore = score;
						bestRow = i;
						bestColumn = j;
					}
				}
			}
		}
		game.addMove(bestRow, bestColumn);
		game.efficientCheckForWin();
	}
	
	private int moveScore(char board[][], int row, int column, char lastMoveChar, int numMoves){
		char newBoard[][] = new char[board.length][board[0].length];
		cloneDoubleCharArray(board, newBoard);
		char nextChar;
		if(lastMoveChar == 'X'){
			nextChar = 'O';
		}else{
			nextChar = 'X';
		}
		newBoard[row][column] = nextChar;
		game.efficientCheckForWin(nextChar, newBoard, new Point(row, column));
		int lowScore = 0;
		if(game.hasEnded() || (numMoves+game.getNumMoves())==board.length*board[0].length){
			if(game.playerWon()) {
				game.resetBooleans();
				if(numMoves==2) return -1000000;
				return -1;
			}
			if(game.otherPlayerWon()){
				game.resetBooleans();
				if(numMoves==1) return 1000000;
				return 1;
			}
			game.resetBooleans();
			return 0;
		} else {
			for(int i=0; i<newBoard.length; i++){
				for(int j=0; j<newBoard[i].length; j++){
					if(game.isEmpty(i, j, newBoard)){
						numMoves++;
						int tempScore= moveScore(newBoard, i, j, nextChar, numMoves);
						if(lowScore>tempScore) lowScore=tempScore;
						numMoves--;
					}
				}
			}
		}
		return lowScore;
	}
	
	private void cloneDoubleCharArray(char oldBoard[][], char newBoard[][]){
		for (int i=0; i<oldBoard.length; i++){
			for(int j=0; j<oldBoard[i].length; j++){
				newBoard[i][j] = oldBoard[i][j];
			}			
		}
	}
	
	private int maxNumInLine(int row1, int column1, int row2, int column2, char move, int maxNum){
		List<Point> nextCells = nextCellsInLine(row1, column1, row2, column2);
		for(int i=0; i<nextCells.size(); i++){
			if(isAdjacent(row1, column1, nextCells.get(i).x, nextCells.get(i).y)){
				return maxNumInLine(row1, column1, nextCells.get(i).x, nextCells.get(i).y, move, maxNum++);
			}
			if(isAdjacent(row2, column2, nextCells.get(i).x, nextCells.get(i).y));
				return maxNumInLine(row2, column2, nextCells.get(i).x, nextCells.get(i).y, move, maxNum++);
		}
		return maxNum;
	}
	
	private List <List<Point>> possibleLines(int rowIndex, int columnIndex){
		List <List<Point>> lines = new ArrayList<List<Point>>();
		List<Point> adjCells = adjacentCells(rowIndex, columnIndex);
		for(int i=0; i<adjCells.size(); i++){
			int adjRow = adjCells.get(i).x;
			int adjColumn = adjCells.get(i).y;
	
			List<Point> line = new ArrayList<Point>();
			line.add(new Point(rowIndex,columnIndex));
			line.add(new Point(adjRow, adjColumn));
	
			
			int slopeX = adjRow-rowIndex;
			int slopeY = adjColumn-columnIndex;
			Point nextCell = new Point(rowIndex-slopeX, columnIndex-slopeY);
			
			if(adjCells.contains(nextCell)){
				line.add(nextCell);
				adjCells.remove(nextCell);
			}
			lines.add(line);
		}
		return lines;
	}
	
	
	private boolean isAdjacent(int row1, int column1, int row2, int column2){
		List<Point> adjCells = adjacentCells(row1, column1);
		if(adjCells.contains(new Point(row2, column2))) return true;
		return false;
	}
	
	private boolean isDiagonal (int row1, int column1, int row2, int column2){
		if(row1!=row2 && column1!=column2){
			return true;
		}
		return false;
	}
	
	private List <Point> nextCellsInLine (int row1, int column1, int row2, int column2){
		List<Point> nextCells = new ArrayList<Point>();
		if(row1==row2){
			if(column1<column2){
				if(column1!=0) nextCells.add(new Point(row1,column1-1));
				if(column2!=game.getColumns()-1) nextCells.add(new Point(row1,column2+1));
			}
			if(column1>column2){
				if(column2!=0) nextCells.add(new Point(row1,column2-1));
				if(column1!=game.getColumns()-1) nextCells.add(new Point(row1,column1+1));
			}
		}
			
		if(row1<row2){
			if(column1==column2){
				if(row1!=0) nextCells.add(new Point(row1-1,column1));
				if(row2!=game.getRows()-1) nextCells.add(new Point(row2+1,column1));
			}
			
			if(column1<column2){
				if(column1!=0 && row1!=0) nextCells.add(new Point(row1-1,column1-1));
				if(column2!=game.getColumns()-1 && row2!=game.getRows()-1) nextCells.add(new Point(row2+1,column2+1));
			}
				
			if(column1>column2){
				if(column2!=0 && row2!=game.getRows()-1) nextCells.add(new Point(row2+1,column2-1));
				if(column1!=game.getColumns()-1 && row1!=0) nextCells.add(new Point(row1-1,column1+1));
			}
		}
		
		if(row1>row2){
			if(column1==column2){
				if(row2!=0) nextCells.add(new Point(row2-1,column1));
				if(row1!=game.getRows()-1) nextCells.add(new Point(row1+1,column1));
			}
			
			if(column1<column2){
				if(column1!=0 && row1!=game.getRows()-1) nextCells.add(new Point(row1+1,column1-1));
				if(column2!=game.getColumns()-1 && row2!=0) nextCells.add(new Point(row2-1,column2+1));
			}
				
			if(column1>column2){
				if(column2!=0 && row2!=0) nextCells.add(new Point(row2-1,column2-1));
				if(column1!=game.getColumns()-1 && row1!=game.getRows()-1) nextCells.add(new Point(row1+1,column1+1));
			}
		}
		return nextCells;
	}
	
	private List<Point> adjacentCells(int rowIndex, int columnIndex){
		List<Point> borderingCells = new ArrayList<Point>();
		if(rowIndex!=0){
			borderingCells.add(new Point(rowIndex-1, columnIndex));
			if(columnIndex!=0){
				borderingCells.add(new Point(rowIndex-1, columnIndex-1));
			}
			if(columnIndex!=game.getColumns()-1){
				borderingCells.add(new Point(rowIndex-1, columnIndex+1));
			}
		}
		if(rowIndex!=game.getRows()-1){
			borderingCells.add(new Point(rowIndex+1, columnIndex));
			if(columnIndex!=0){
				borderingCells.add(new Point(rowIndex+1, columnIndex-1));
			}
			if(columnIndex!=game.getColumns()-1){
				borderingCells.add(new Point(rowIndex+1, columnIndex+1));
			}
		}
		if(columnIndex!=0){
			borderingCells.add(new Point(rowIndex, columnIndex-1));
		}
		if(columnIndex!=game.getColumns()-1){
			borderingCells.add(new Point(rowIndex, columnIndex+1));
		}
		return borderingCells;
	}
	
	private void checkCenter(){
		List<Point> availableCenters = availableCenterCells();
		if(availableCenters.size()!=0){
			int randomIndex = random.nextInt(availableCenters.size());
			Point centerPoint = availableCenters.get(randomIndex);
			game.addMove(centerPoint.x, centerPoint.y);
			computerTurn=false;
		}
		
	}
	
	private List<Point> availableCenterCells(){
		int numCenterRows = findNumCenters(game.getRows());
		int numCenterColumns = findNumCenters(game.getColumns());
		List<Point> centers = new ArrayList<Point>();
		for(int i=0; i<numCenterRows; i++){
			for(int j=0; j<numCenterColumns; j++){
				int centerRowIndex = findCenterIndex(game.getRows()-i);
				int centerColumnIndex = findCenterIndex(game.getColumns()-j);
				if(game.isEmpty(centerRowIndex, centerColumnIndex)){
					centers.add(new Point(centerRowIndex, centerColumnIndex));
				}
			}
		}
		return centers;
	}
	
	private int findNumCenters(int numDivisions){
		if(numDivisions % 2 == 0) {
			return 2;
		} else {
			return 1;
		}
	}
	
	private int findCenterIndex(int numDivisions){
		if(numDivisions % 2 ==0){
			return (numDivisions-1)/2;
		}else{
			return numDivisions/2;
		}
	}
	
	
}
