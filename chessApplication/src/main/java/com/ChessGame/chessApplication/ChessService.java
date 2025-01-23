package com.ChessGame.chessApplication;

import org.springframework.stereotype.Service;

@Service
public class ChessService {
	 private static final String[][] board = initializeBoard();

	    public static GameState processMove(GameMove move) {
	        String piece = board[move.getFromRow()][move.getFromCol()];

	        // Validate the move (basic example)
	        if (piece == null || piece.equals(".")) {
	            throw new IllegalArgumentException("Invalid move: No piece at the source location");
	        }

	        // Perform the move
	        board[move.getFromRow()][move.getFromCol()] = ".";
	        board[move.getToRow()][move.getToCol()] = piece;

	        // Return the updated state
	        return new GameState(board, getNextTurn(piece));
	    }

	    private static String getNextTurn(String piece) {
	        return piece.startsWith("W") ? "black" : "white";
	    }

	    public static String[][] initializeBoard() {
	        String[][] board = new String[8][8];
	        board[0] = new String[]{"WR", "WH", "WB", "WQ", "WK", "WB", "WH", "WR"};
	        board[1] = new String[]{"WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP"};
	        board[6] = new String[]{"BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"};
	        board[7] = new String[]{"BR", "BH", "BB", "BQ", "BK", "BB", "BH", "BR"};
	        return board;
	    }
	  
}
