package com.ChessGame.chessApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api")
public class ChessController {
	@Autowired
	private ChessService chessService;
	 // Handle incoming moves from players
    @MessageMapping("/move")
    @SendTo("/topic/game-state")
    public GameState handleMove(GameMove move) {
        // Process the move (e.g., update the board)
    	System.out.println("HIIII");
        GameState gameState = ChessService.processMove(move);

        // Broadcast the updated game state to all clients
        return gameState;
    }
    @GetMapping("/board")
    public GameState getInitialBoard() {
    	System.out.println("initialize board");
        // Return the initial board setup with currentTurn
        return new GameState(
        		chessService.initializeBoard(),
            "white" // Initial turn
        );
    }
}
