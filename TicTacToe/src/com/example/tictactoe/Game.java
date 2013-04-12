package com.example.tictactoe;

import android.graphics.Point;

public class Game {
	
	private char board[][];
	private int rows;
	private int columns;
	private boolean hasMoves;
	private char lastMoveChar;
 	private boolean otherPlayerWin; 
	private boolean playerWin;
	private int numMoves;
	private int numNeededToWin;
	private char player1Char;
	private Point lastMoveLocation;
	private String gameType;
	
	public Game(int numRows, int numColumns, int numToWin, String typeOfGame){
		rows = numRows;
		columns = numColumns;
		board = new char[rows][columns];
		setBlankboard();
		hasMoves = false;
		otherPlayerWin = false;
		playerWin = false;
		numMoves=0;
		numNeededToWin = numToWin;
		player1Char = 'X';
		lastMoveChar = ' ';
		lastMoveLocation = new Point(-1,-1);
		gameType = typeOfGame;
	}
	
	/**Returns the number of rows**/
	public int getRows(){
		return rows;
	}
	
	/**Returns the number of columns**/
	public int getColumns(){
		return columns;
	}
	
	/**Returns the number of moves in a row needed to win**/
	public int getNumToWin(){
		return numNeededToWin;
	}
	
	/**Returns the character at a given cell on the board**/
	public char getMove (int row, int column){
		return board[row][column];
	}
	
	/**Returns the total number of moves made so far on the board**/
	public int getNumMoves(){
		return numMoves;
	}

	/**Returns the character of the last move made on the board**/
	public char getLastMoveChar(){
		return lastMoveChar;
	}
	
	/**Returns the current game board in the form of a double char array (char[][])**/
	public char[][] getBoard(){
		return board;
	}

	/**Returns true if there are any moves on the board**/
	public boolean boardHasMoves(){
		return hasMoves;
	}
	
	/**Returns the String gameType, describing the type of game (Single Player, Two Player, etc)**/
	public String getGameType(){
		return gameType;
	}
	
	/**Returns true if all spaces on the board have been filled**/
	public boolean boardFull(){
		if(numMoves==rows*columns && !playerWin && !otherPlayerWin) return true;
		return false;
	}
	
	/**Returns true if player one has won the game**/
	public boolean playerWon(){
		if(playerWin) return true;
		return false;
	}
	
	/**Returns true if the computer player or player two has won the game**/
	public boolean otherPlayerWon(){
		if(otherPlayerWin) return true;
		return false;
	}
	
	/**Returns true if the game has ended**/
	public boolean hasEnded(){
		if(boardFull() || playerWin || otherPlayerWin) return true;
		return false;
	}
	
	/**Returns true if player 1 was last player to add a move to the board**/
	public boolean player1Moved(){
		if(lastMoveChar == player1Char){
			return true;
		} else {
			return false;
		}
	}
	
	/**Returns true if the cell specified on an alternate board is empty**/
	public boolean isEmpty(int row, int column, char altBoard[][]){
		if(altBoard[row][column] == ' '){
			return true;
		} else{
			return false;
		}
	}
	
	/**Returns true if the cell specified on the game board is empty**/
	public boolean isEmpty(int row, int column){
		return isEmpty(row, column, board);
	}
	
	/**Resets any player wins back to false**/
	public void resetBooleans(){
		playerWin = false;
		otherPlayerWin = false;
	}
	
	/**Directly records a win for the computer player**/
	public void recordComputerWin(){
		otherPlayerWin=true;
	}
	
	/**Adds a move to a given cell. The correct character for the move is determined by the game class**/
	public void addMove(int row, int column){
		if(!hasMoves){
			board[row][column] = 'X';
			hasMoves = true;
			lastMoveChar = 'X';
		}else if(hasMoves){
			if(lastMoveChar=='X'){
				board[row][column] = 'O';
				lastMoveChar='O';
			} else if(lastMoveChar=='O'){
				board[row][column] = 'X';
				lastMoveChar='X';
			}
		}
		lastMoveLocation = new Point(row, column);
		numMoves++;
	}
	
	/**Checks the game board for a win based on the character of the last move played**/
	public void checkForWin(){
		checkForWin(lastMoveChar, board);
	}
	
	/**Checks the game board for a win based on the location and character of the last move played**/
	public void efficientCheckForWin(){
		efficientCheckForWin(lastMoveChar, board, lastMoveLocation);
	}
	
	public void efficientCheckForWin(char move, char gameBoard[][], Point lastMove){
		int numInARow;
		
		//check row of last move for win
		if(columns >= numNeededToWin){
			numInARow = 0;
			for(int i=0; i<columns; i++){
				if(i==0) numInARow=0;
				if(gameBoard[lastMove.x][i] == move){
					numInARow++;
					if(numInARow == numNeededToWin){
						aPlayerWon(move);
						return;
					}
				} else {
					numInARow = 0;
				}
			}
		}
		
		//check column of last move for win
		if(rows >= numNeededToWin){
			numInARow = 0;
			for(int i=0; i<rows; i++){
				if(i==0) numInARow=0;
				if(gameBoard[i][lastMove.y] == move){
					numInARow++;
					if(numInARow == numNeededToWin){
						aPlayerWon(move);
						return;
					}
				} else {
					numInARow = 0;
				}
			}	
		}
		
		//Checks diagonals of last move for win
		if(rows >= numNeededToWin && columns >= numNeededToWin){
			int startRow;
			int startColumn;
			int maxIterations;
			int smallerDimension;
			int dimensionsDifference;
			boolean rowsLarger;
			
			//Determines the smaller dimension, difference in dimensions, and whether the number of rows is greater than the number of columns
			if(rows > columns){
				smallerDimension = columns;
				dimensionsDifference = rows-columns;
				rowsLarger = true;
			} else {
				smallerDimension = rows;
				dimensionsDifference = columns-rows;
				rowsLarger = false;
			}
			
			//Checks diagonal from L Top to R Bottom
			if(lastMoveLocation.x >= lastMoveLocation.y){
				startRow = lastMoveLocation.x - lastMoveLocation.y;
				startColumn = 0;
				if(rowsLarger){
					if(startRow<=dimensionsDifference){
						maxIterations = smallerDimension;
					}else{
						maxIterations = smallerDimension-(startRow-dimensionsDifference);
					}
				} else {
					maxIterations = smallerDimension-startRow;
				}
			} else {
				startRow = 0;
				startColumn = lastMoveLocation.y - lastMoveLocation.x;
				if(!rowsLarger){
					if(startColumn<=dimensionsDifference){
						maxIterations = smallerDimension;
					} else {
						maxIterations = smallerDimension-(startColumn-dimensionsDifference);
					}
				} else {
					maxIterations = smallerDimension-startColumn;
				}
			}
			//Checks diagonal containing the last move for a win
			numInARow=0;
			for(int i=0; i<maxIterations; i++){
				if(gameBoard[startRow+i][startColumn+i]==move){
					numInARow++;
					if(numInARow == numNeededToWin){
						aPlayerWon(move);
						return;
					}
				} else {
					numInARow = 0;
				}
			}
			
			//Checks diagonal from L Bottom to R Top
			if((rows-lastMoveLocation.x-1) <= lastMoveLocation.y){
				startRow = rows-1;
				startColumn = lastMoveLocation.y - (rows-lastMoveLocation.x-1);
				if(!rowsLarger){
					if(startColumn<=dimensionsDifference){
						maxIterations = smallerDimension;
					} else {
						maxIterations = smallerDimension-(startColumn-dimensionsDifference);
					}
				} else {
					maxIterations = smallerDimension-startColumn;
				}
			} else {
				startRow = lastMoveLocation.x+lastMoveLocation.y;
				startColumn = 0;
				if(rowsLarger){
					if((rows-startRow-1)<=dimensionsDifference){
						maxIterations = smallerDimension;
					}else{
						maxIterations = smallerDimension-(rows-startRow-1-dimensionsDifference);
					}
				} else {
					maxIterations = smallerDimension-(rows-startRow-1);
				}
			}
			
			//Check anti-diagonal containing the last move for a win
			numInARow=0;
			for(int i=0; i<maxIterations; i++){
				if(gameBoard[startRow-i][startColumn+i]==move){
					numInARow++;
					if(numInARow == numNeededToWin){
						aPlayerWon(move);
						return;
					}
				} else {
					numInARow = 0;
				}
			}
		}
	}
	
	public void checkForWin(char move, char gameBoard[][]){
		
		int numInARow;
		
		//check rows for win
		if(rows >= numNeededToWin){
			numInARow = 0;
			for(int i=0; i<rows; i++){
				for(int j=0; j<columns; j++){
					if(j==0) numInARow=0;
					if(gameBoard[i][j] == move){
						numInARow++;
						if(numInARow==numNeededToWin){
							if(move==player1Char){
								playerWin = true;
							} else {
								otherPlayerWin = true;
							}
							return;
						}
					}else{
						numInARow=0;
					}
				}
			}	
		}
		
		//check columns for win
		if(columns >= numNeededToWin){
			numInARow = 0;
			for(int i=0; i<columns; i++){
				for(int j=0; j<rows; j++){
					if(j==0) numInARow=0;
					if(gameBoard[j][i] == move){
						numInARow++;
						if(numInARow==numNeededToWin){
							if(move==player1Char){
								playerWin = true;
							} else {
								otherPlayerWin = true;
							}
							return;
						}
					}else{
						numInARow=0;
					}
				}
			}	
		}
		
		//check diagonals & anti-diagonals for win
		if(rows >= numNeededToWin && columns >=numNeededToWin){
		
			//check diagonals for win
			numInARow = 0;
			for(int i=0; i<=(rows-numNeededToWin); i++){
				for(int j=0; j<=(columns-numNeededToWin); j++){
					for(int k=0; k<numNeededToWin; k++){
						if(gameBoard[i+k][j+k] == move){
							numInARow++;
							if(numInARow==numNeededToWin){
								if(move==player1Char){
									playerWin = true;
								} else {
									otherPlayerWin = true;
								}
								return;
							}
						} else {
							numInARow=0;
							break;
						}
					}
				}
			}
		
			//check anti-diagonals for win
			numInARow = 0;
			for(int i=(rows-1); i>(numNeededToWin-2); i--){
				for(int j=0; j<=(columns-numNeededToWin); j++){
					for(int k=0; k<numNeededToWin; k++){
						if(gameBoard[i-k][j+k] == move){
							numInARow++;
							if(numInARow==numNeededToWin){
								if(move==player1Char){
									playerWin = true;
								} else {
									otherPlayerWin = true;
								}
								return;
							}
						} else {
							numInARow=0;
							break;
						}
					}
				}
			}
		}
	}
	
	/**Sets each cell value of the game board to ' ' (blank)**/
	private void setBlankboard(){
		for(int i=0; i<rows; i++){
			for(int j=0; j<columns; j++){
				board[i][j] = ' ';
			}
		}
	}
	/**Determines which player won based on the character associated with that player**/
	private void aPlayerWon(char winChar){
		if(winChar==player1Char){
			playerWin = true;
		} else {
			otherPlayerWin = true;
		}
	}

	/*
	public void makeCopy(Game copy){
		copy.hasMoves = hasMoves;
		copy.numMoves = numMoves;
		copy.numNeededToWin = numNeededToWin;
		copy.player1Char = player1Char;
		copy.otherPlayerWin = otherPlayerWin;
		copy.playerWin = playerWin;
		copy.lastMoveChar = lastMoveChar;
		for(int i=0; i<rows; i++){
			for(int j=0; j<columns; j++){
				copy.board[i][j] = board[i][j];
			}
		}
	}*/
		
	

	
}
