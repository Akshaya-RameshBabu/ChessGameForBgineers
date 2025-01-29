package com.ChessGame.chessApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static String[][] board = initializeBoard(); 

    @MessageMapping("/chessboard")
    @SendTo("/topic/board")
    public String[][] sendChessboard() {
        return board;
    }

    @MessageMapping("/move")
    @SendTo("/topic/moves")
    public String[][] handleMove(Move move) {
        int sourceRow = move.getSourceRow();
        int sourceCol = move.getSourceCol();
        int targetRow = move.getTargetRow();
        int targetCol = move.getTargetCol();

        if (!board[sourceRow][sourceCol].equals(".") && board[targetRow][targetCol].equals(".")) {
            // Move piece
            board[targetRow][targetCol] = board[sourceRow][sourceCol];
            board[sourceRow][sourceCol] = ".";
        }

        return board; // Send updated board to all clients
    }
    
    private static String[][] initializeBoard() {
        String[][] board = {
        	    { "WR", "WH", "WB", "WQ", "WK", "WB", "WH", "WR" },
        	    { "WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP" },
        	    { ".", ".", ".", ".", ".", ".", ".", "." },
        	    { ".", ".", ".", ".", ".", ".", ".", "." },
        	    { ".", ".", ".", ".", ".", ".", ".", "." },
        	    { ".", ".", ".", ".", ".", ".", ".", "." },
        	    { "BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP" },
        	    { "BR", "BH", "BB", "BQ", "BK", "BB", "BH", "BR" }
        	};


        return board;
    }
}


