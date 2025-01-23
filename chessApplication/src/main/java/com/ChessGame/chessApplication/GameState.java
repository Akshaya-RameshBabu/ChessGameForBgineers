package com.ChessGame.chessApplication;

import java.util.Arrays;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
public class GameState {
	 private String[][] board;
	    private String currentTurn;

	    public GameState(String[][] board, String currentTurn) {
	        // Deep copy the board for safety
	        this.board = Arrays.stream(board).map(String[]::clone).toArray(String[][]::new);
	        this.currentTurn = currentTurn;
	    }

	    public String[][] getBoard() {
	        return board;
	    }

	    public String getCurrentTurn() {
	        return currentTurn;
	    }
}
