package gameoflife;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Board class that contains a representation of a board for the Game of life, along with several useful methods for manipulating said board
 * 
 * @author Folz
 *
 */
public class Board implements Serializable{
	private int xLength = 35;
	private int yLength = 35;
	private boolean[][] board;
	private boolean[][] nextBoard;
	
	/**
	 * Constructor that defines the size of the board to make.
	 * 
	 * @param xLength Size in the X direction.
	 * @param yLength Size in the Y direction.
	 */
	public Board(int xLength, int yLength){
		this.xLength = xLength;
		this.yLength = yLength;

		board = new boolean[yLength][xLength];
		nextBoard = new boolean[yLength][xLength];
	}
	
	/**
	 * Applies an impulse to the board to switch the value of a random selection of squares
	 * 
	 * @param percent Strength of the impulse, should be between 0 and 1.
	 */
	public void impulse(double percent){ //there's probably a better thing to call this than "impulse"
		for(int x = 0; x < xLength; ++x){
			for(int y = 0; y < yLength; ++y){
				if(Math.random() < percent){
					board[y][x] = !board[y][x];
				}
			}
		}
	}
	
	/**
	 * Method to initialize the board with random values.
	 * 
	 */
	public void setup(){
		for(int x = 0; x < xLength; ++x){
			for(int y = 0; y < yLength; ++y){
				board[y][x] = Math.random() < .2;
			}
		}
	}
	
	/**
	 * Iterates the generation of the board by one.
	 * 
	 */
	public void refreshBoard(){
		
		for(int x = 0; x < xLength; ++x){
			for(int y = 0; y < yLength; ++y){
				nextBoard[y][x] = checkNeighbours(x, y);
			}
		}

		for(int x = 0; x < xLength; ++x){
			for(int y = 0; y < yLength; ++y){
				board[y][x] = nextBoard[y][x];
			}
		}
	}
	
	/**
	 * Counts the number of Neighbours a given cell has, then determines the fate of that cell.
	 * 
	 * @param x X location of cell to check
	 * @param y Y location of cell to check
	 * @return Returns if the given cell should live or die
	 */
	private boolean checkNeighbours(int x, int y){
		int neighbourCount = 0;
		int[] XTargetArr = {x-1, x+0, x+1};
		int[] YTargetArr = {y-1, y+0, y+1};
		boolean result;
		//assume default, change for special cases
		
		//borders are special case
		if(x == 0){
			XTargetArr[0] = xLength-1;
		}else if(x == xLength-1){
			XTargetArr[2] = 0;
		}
		
		if(y == 0){
			YTargetArr[0] = yLength-1;
		}else if(y == yLength-1){
			YTargetArr[2] = 0;
		}
		
		for(int XTarget: XTargetArr){
			for(int YTarget: YTargetArr){
				if( XTarget == x && YTarget == y){
					continue;
				}
				if(board[YTarget][XTarget]){
					neighbourCount++;
				}
			}
		}
		
		if(board[y][x]){ 
			if(neighbourCount == 2 || neighbourCount == 3){
				result = true;
			} else {
				result = false;
			}
		} else { 
			result = neighbourCount == 3;
		}
		return result;
	}
	
	/**
	 * Clears the board of all living cells.
	 */
	public void clearBoard(){
		board = new boolean[yLength][xLength];
	}
	
	public boolean[][] getBoard(){
		return board;
	}

	public int getXLength() {
		return xLength;
	}

	public int getYLength() {
		return yLength;
	}
	
	public void setBoardSize(int xLength, int yLength){
		this.xLength = xLength;
		this.yLength = yLength;
		boolean[][] temp = new boolean[this.yLength][this.xLength];
		for(int i = 0; i < this.yLength; ++i){
			try{
				temp[i] = Arrays.copyOf(board[i], this.xLength);
			}catch(ArrayIndexOutOfBoundsException e){
				temp[i] = new boolean[this.xLength];
			}
		}
		board = temp;
		nextBoard = new boolean[this.yLength][this.xLength];
	}
}
