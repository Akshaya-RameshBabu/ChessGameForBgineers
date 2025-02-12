package com.ChessGame.chessApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
	 @Autowired
	    private SimpMessagingTemplate messagingTemplate;
	 
    private static String[][] board = initializeBoard(); 
    private static boolean  turn=false; //true=White ,false=Black
    @MessageMapping("/chessboard")
    @SendTo("/topic/board")
    public String[][] sendChessboard() {
        return board;
    }

    @MessageMapping("/Turn")
    @SendTo("/topic/Turn")
    public boolean GetTurn() {
        return turn; // Send updated board to all clients
    }
    @MessageMapping("/move")
    @SendTo("/topic/moves")
    public String[][] handleMove(Move move) { 
        int sourceRow = move.getSourceRow();
        int sourceCol = move.getSourceCol();
        int targetRow = move.getTargetRow();
        int targetCol = move.getTargetCol();
        System.out.println("Source=("+sourceRow+","+sourceCol+")");
        System.out.println("Target=("+targetRow+","+targetCol+")");
        if (board[sourceRow][sourceCol].equals(".") ) {
        	return board;
        }
            char color= board[sourceRow][sourceCol].charAt(0);
            char piece=board[sourceRow][sourceCol].charAt(1);
            String target=board[targetRow][targetCol];
            char targetColor='.';
            char targetPiece='.';
            if(target!=".") {
            	 targetColor=target.charAt(0);
            	 targetPiece=target.charAt(1);
            }
            if((!turn && color=='W') ||(turn && color=='B')|| (targetColor==color)) {
            	return board;
            }
          boolean isValid=false;
            switch(piece) {
	            case 'P':
			      isValid=validatePawnMove(sourceRow, sourceCol, targetRow, targetCol, color, targetColor);
	            	break;
	            case 'R':
	            	System.out.println("here");
	            	isValid=validateRookMove(sourceRow, sourceCol, targetRow, targetCol);
	            	System.out.println();
	              break;
	            case 'B':
	            	isValid=validateBishopMove(sourceRow, sourceCol, targetRow, targetCol);
	            	break;
	            case 'H':
	            	isValid=validateHorseMove(sourceRow, sourceCol, targetRow, targetCol);
	            	System.out.println("horse"+isValid);
	            	break;
	            case 'Q':
	            	isValid=validateQueenMove(sourceRow, sourceCol, targetRow, targetCol);
	            	break;
	            case'K':
	            	isValid=validateKingMove(sourceRow, sourceCol, targetRow, targetCol);
	            	break;
			    default:
			        System.out.println("Unknown Piece"+piece);
            }
        if(isValid) {
            board[targetRow][targetCol] = board[sourceRow][sourceCol];
            board[sourceRow][sourceCol] = ".";
            turn = !turn;
            messagingTemplate.convertAndSend("/topic/Turn", turn);
        }
        return board;
      
        // Send updated board to all clients
    }
    private boolean validateHorseMove(int sourceRow, int sourceCol, int targetRow, int targetCol) {
        int rowDiff = Math.abs(targetRow - sourceRow);
        int colDiff = Math.abs(targetCol - sourceCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean validateQueenMove(int sourceRow, int sourceCol, int targetRow, int targetCol) {
        return validateRookMove(sourceRow, sourceCol, targetRow, targetCol) || validateBishopMove(sourceRow, sourceCol, targetRow, targetCol);
    }
    private boolean validateKingMove(int sourceRow, int sourceCol, int targetRow, int targetCol) {
        return Math.abs(targetRow - sourceRow) <= 1 && Math.abs(targetCol - sourceCol) <= 1;
    }

    private boolean validateBishopMove(int sourceRow, int sourceCol, int targetRow, int targetCol) {
        if (Math.abs(targetRow - sourceRow) != Math.abs(targetCol - sourceCol)) return false;

        return isPathClear(sourceRow, sourceCol, targetRow, targetCol);
    }

    private boolean isPathClear(int sourceRow, int sourceCol, int targetRow, int targetCol) {
        int rowStep = Integer.compare(targetRow, sourceRow);
        int colStep = Integer.compare(targetCol, sourceCol);

        int row = sourceRow + rowStep;
        int col = sourceCol + colStep;

        while (row != targetRow || col != targetCol) {
            if (!board[row][col].equals(".")) return false;
            row += rowStep;
            col += colStep;
        }

        return true;
    }

    
    //for Rook (Elephant)
    private boolean validateRookMove(int sourceRow, int sourceCol, int targetRow, int targetCol) {
        if (sourceRow != targetRow && sourceCol != targetCol) return false;
        
        return isPathClear(sourceRow, sourceCol, targetRow, targetCol);
    }

    private boolean validatePawnMove(int sourceRow, int sourceCol, int targetRow, int targetCol, char color, char targetColor) {
    	int direction = (color == 'W') ? 1 : -1; // White moves down (+1), Black moves up (-1)
        // Normal move (one square forward)
        if (sourceCol == targetCol && targetColor == '.' && targetRow == sourceRow + direction) {
            return true;
        }
        // Double move from starting position
     // Pawn Double Step Move Logic (Only from starting position)
        else if (sourceCol == targetCol 
            && board[targetRow][targetCol].equals(".") // Target square must be empty
            && board[sourceRow + direction][sourceCol].equals(".") // Middle square must be empty
            && ((color == 'B' && sourceRow == 6 && targetRow==4) || (color == 'W' && sourceRow == 1 && targetRow==3)) // Pawn at starting row
           ) { 
                return true;
        }
        // Capture move (diagonal)
        else if (Math.abs(targetCol - sourceCol) == 1 && targetColor != '.' && targetColor != color && targetRow == sourceRow + direction) {
            return true;
        }
        return false;
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


