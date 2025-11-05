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
    private static boolean turn = true; // true = White, false = Black
    private static boolean gameOver = false;

    /* ===================== WebSocket Endpoints ===================== */

    @MessageMapping("/chessboard")
    @SendTo("/topic/board")
    public String[][] sendChessboard() {
        return board;
    }

    @MessageMapping("/Turn")
    @SendTo("/topic/Turn")
    public boolean getTurn() {
        return turn;
    }

    @MessageMapping("/reset")
    public void resetGame() {
        board = initializeBoard();
        turn = true;
        gameOver = false;
        messagingTemplate.convertAndSend("/topic/board", board);
        messagingTemplate.convertAndSend("/topic/Turn", turn);
        messagingTemplate.convertAndSend("/topic/reset", "Game has been reset."); 
        System.out.println("♻️ Game reset.");
    }

    @MessageMapping("/move")
    @SendTo("/topic/moves")
    public String[][] handleMove(Move move) {
        if (gameOver) {
            System.out.println("🏁 Game over. No more moves allowed.");
            return board;
        }

        int sr = move.getSourceRow();
        int sc = move.getSourceCol();
        int tr = move.getTargetRow();
        int tc = move.getTargetCol();

        if (board[sr][sc].equals(".")) return board;

        char color = board[sr][sc].charAt(0);
        char piece = board[sr][sc].charAt(1);
        String target = board[tr][tc];
        char targetColor = target.equals(".") ? '.' : target.charAt(0);

        // === Turn & same-color capture validation ===
        if ((turn && color != 'W') || (!turn && color != 'B')) return board;
        if (targetColor == color) return board;

        boolean valid = false;
        switch (piece) {
            case 'P' -> valid = validatePawnMove(sr, sc, tr, tc, color, targetColor);
            case 'R' -> valid = validateRookMove(sr, sc, tr, tc);
            case 'B' -> valid = validateBishopMove(sr, sc, tr, tc);
            case 'H' -> valid = validateHorseMove(sr, sc, tr, tc);
            case 'Q' -> valid = validateQueenMove(sr, sc, tr, tc);
            case 'K' -> valid = validateKingMove(sr, sc, tr, tc);
        }
        if (!valid) {
            System.out.println("❌ Invalid move attempted for " + board[sr][sc]);
            return board;
        }

        // === Try move temporarily ===
        String temp = board[tr][tc];
        board[tr][tc] = board[sr][sc];
        board[sr][sc] = ".";

        // === Prevent self-check ===
        if (isKingInCheck(color)) {
            board[sr][sc] = board[tr][tc];
            board[tr][tc] = temp;
            String msg = (color == 'W' ? "White" : "Black") + " King is in Check! Move blocked.";
            messagingTemplate.convertAndSend("/topic/check", msg);
            System.out.println("🚫 " + msg);
            return board;
        }

        // === Notify capture ===
        if (!temp.equals(".")) {
            messagingTemplate.convertAndSend("/topic/capture", temp);
            System.out.println("💥 Capture: " + temp + " taken by " + board[tr][tc]);
        }

        // === After every move, check for check & checkmate for both sides ===
        boolean whiteInCheck = isKingInCheck('W');
        boolean blackInCheck = isKingInCheck('B');
        boolean whiteCheckmate = isCheckmate('W');
        boolean blackCheckmate = isCheckmate('B');

        if (whiteCheckmate) {
            gameOver = true;
            messagingTemplate.convertAndSend("/topic/gameOver", "Black wins by Checkmate!");
            System.out.println("🏆 Black wins by Checkmate!");
            return board;
        }
        if (blackCheckmate) {
            gameOver = true;
            messagingTemplate.convertAndSend("/topic/gameOver", "White wins by Checkmate!");
            System.out.println("🏆 White wins by Checkmate!");
            return board;
        }

        // === Check warnings ===
        if (whiteInCheck && !whiteCheckmate) {
            messagingTemplate.convertAndSend("/topic/check", "⚠️ White King is in Check!");
            System.out.println("⚠️ White King is in Check!");
        }
        if (blackInCheck && !blackCheckmate) {
            messagingTemplate.convertAndSend("/topic/check", "⚠️ Black King is in Check!");
            System.out.println("⚠️ Black King is in Check!");
        }

        // === Switch Turn ===
        if (!gameOver) {
            turn = !turn;
            messagingTemplate.convertAndSend("/topic/Turn", turn);
            System.out.println("🔁 Turn switched. Now " + (turn ? "White's" : "Black's") + " move.");
        }

        return board;
    }

    /* ===================== Validation Methods ===================== */

    private boolean validateHorseMove(int sr, int sc, int tr, int tc) {
        int dr = Math.abs(tr - sr), dc = Math.abs(tc - sc);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    private boolean validateQueenMove(int sr, int sc, int tr, int tc) {
        return validateRookMove(sr, sc, tr, tc) || validateBishopMove(sr, sc, tr, tc);
    }

    private boolean validateKingMove(int sr, int sc, int tr, int tc) {
        return Math.abs(tr - sr) <= 1 && Math.abs(tc - sc) <= 1;
    }

    private boolean validateBishopMove(int sr, int sc, int tr, int tc) {
        if (Math.abs(tr - sr) != Math.abs(tc - sc)) return false;
        return isPathClear(sr, sc, tr, tc);
    }

    private boolean validateRookMove(int sr, int sc, int tr, int tc) {
        if (sr != tr && sc != tc) return false;
        return isPathClear(sr, sc, tr, tc);
    }

    private boolean validatePawnMove(int sr, int sc, int tr, int tc, char color, char targetColor) {
        // Correct direction: White moves up (−1), Black moves down (+1)
        int dir = (color == 'W') ? -1 : 1;
        int startRow = (color == 'W') ? 6 : 1;

        // Move forward
        if (sc == tc && targetColor == '.' && tr == sr + dir) return true;

        // Double move from starting row
        if (sc == tc && targetColor == '.' && sr == startRow && tr == sr + 2 * dir && board[sr + dir][sc].equals("."))
            return true;

        // Capture move
        if (Math.abs(tc - sc) == 1 && targetColor != '.' && tr == sr + dir)
            return true;

        return false;
    }

    private boolean isPathClear(int sr, int sc, int tr, int tc) {
        int rowStep = Integer.compare(tr, sr);
        int colStep = Integer.compare(tc, sc);
        int r = sr + rowStep, c = sc + colStep;
        while (r != tr || c != tc) {
            if (!board[r][c].equals(".")) return false;
            r += rowStep; c += colStep;
        }
        return true;
    }

    /* ===================== Check & Checkmate ===================== */

    private boolean isKingInCheck(char color) {
        int kr = -1, kc = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c].equals(color + "K")) {
                    kr = r; kc = c; break;
                }
            }
        }
        if (kr == -1) return false;

        char opponent = (color == 'W') ? 'B' : 'W';
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String piece = board[r][c];
                if (piece.equals(".") || piece.charAt(0) != opponent) continue;
                char p = piece.charAt(1);

                boolean attack = switch (p) {
                    case 'P' -> validatePawnMove(r, c, kr, kc, opponent, color);
                    case 'R' -> validateRookMove(r, c, kr, kc);
                    case 'B' -> validateBishopMove(r, c, kr, kc);
                    case 'H' -> validateHorseMove(r, c, kr, kc);
                    case 'Q' -> validateQueenMove(r, c, kr, kc);
                    case 'K' -> validateKingMove(r, c, kr, kc);
                    default -> false;
                };
                if (attack) return true;
            }
        }
        return false;
    }

    private boolean isCheckmate(char color) {
        if (!isKingInCheck(color)) return false;

        System.out.println("♟ Checking if " + (color == 'W' ? "White" : "Black") + " is in checkmate...");

        for (int sr = 0; sr < 8; sr++) {
            for (int sc = 0; sc < 8; sc++) {
                if (board[sr][sc].equals(".") || board[sr][sc].charAt(0) != color) continue;

                char piece = board[sr][sc].charAt(1);

                for (int tr = 0; tr < 8; tr++) {
                    for (int tc = 0; tc < 8; tc++) {
                        if (sr == tr && sc == tc) continue;

                        String dest = board[tr][tc];
                        char targetColor = dest.equals(".") ? '.' : dest.charAt(0);
                        if (targetColor == color) continue;

                        boolean valid = switch (piece) {
                            case 'P' -> validatePawnMove(sr, sc, tr, tc, color, targetColor);
                            case 'R' -> validateRookMove(sr, sc, tr, tc);
                            case 'B' -> validateBishopMove(sr, sc, tr, tc);
                            case 'H' -> validateHorseMove(sr, sc, tr, tc);
                            case 'Q' -> validateQueenMove(sr, sc, tr, tc);
                            case 'K' -> validateKingMove(sr, sc, tr, tc);
                            default -> false;
                        };

                        if (!valid) continue;

                        // Simulate the move
                        String from = board[sr][sc];
                        board[tr][tc] = from;
                        board[sr][sc] = ".";

                        boolean stillInCheck = isKingInCheck(color);

                        // Undo the move
                        board[sr][sc] = from;
                        board[tr][tc] = dest;

                        // Special handling for the king:
                        if (piece == 'K') {
                            // If the move itself puts the king in check (like moving into attacked square)
                            // then it is NOT a valid escape.
                            if (stillInCheck) continue;
                        }

                        // If after move the king is safe → not checkmate
                        if (!stillInCheck) {
                            System.out.println("♻️ Escape found: " + from + " from (" + sr + "," + sc + ") to (" + tr + "," + tc + ")");
                            return false;
                        }
                    }
                }
            }
        }

        System.out.println("✅ Checkmate confirmed for " + (color == 'W' ? "White" : "Black"));
        return true;
    }


    /* ===================== Board Setup ===================== */

    private static String[][] initializeBoard() {
        return new String[][] {
            { "BR", "BH", "BB", "BQ", "BK", "BB", "BH", "BR" },
            { "BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP" },
            { ".", ".", ".", ".", ".", ".", ".", "." },
            { ".", ".", ".", ".", ".", ".", ".", "." },
            { ".", ".", ".", ".", ".", ".", ".", "." },
            { ".", ".", ".", ".", ".", ".", ".", "." },
            { "WP", "WP", "WP", "WP", "WP", "WP", "WP", "WP" },
            { "WR", "WH", "WB", "WQ", "WK", "WB", "WH", "WR" }
        };
    }
}
