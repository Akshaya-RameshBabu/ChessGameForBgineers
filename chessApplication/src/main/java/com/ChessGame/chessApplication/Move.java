package com.ChessGame.chessApplication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Move {
	private int sourceRow;
    private int sourceCol;
    private int targetRow;
    private int targetCol;

}
