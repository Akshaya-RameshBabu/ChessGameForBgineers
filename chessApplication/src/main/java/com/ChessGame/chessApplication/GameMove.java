package com.ChessGame.chessApplication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class GameMove {
	 private int fromRow;
	    private int fromCol;
	    private int toRow;
	    private int toCol;
}
