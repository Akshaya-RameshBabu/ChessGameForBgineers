import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import baseUrl from "./utils";

class WebSocketService {
  constructor() {
    this.client = null;
  }

  connect(onBoardReceived, onMoveReceived,onTurnReceived) {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
      debug: (str) => console.log(str),
      onConnect: () => {
        console.log("Connected to WebSocket");

        // Subscribe to board initialization
        this.client.subscribe("/topic/board", (message) => {
          if (onBoardReceived) {
            onBoardReceived(JSON.parse(message.body));
          }
        });

        // Subscribe to move updates
        this.client.subscribe("/topic/moves", (message) => {
          if (onMoveReceived) {
            onMoveReceived(JSON.parse(message.body));
          }
        });
        this.client.subscribe("/topic/Turn", (message) => {
          if (onTurnReceived) {
            onTurnReceived(JSON.parse(message.body));
          }
        });
        // Request the initial chessboard
        this.client.publish({ destination: "/app/chessboard" });
        this.client.publish({ destination: "/app/Turn" });
      },
      onStompError: (frame) => {
        console.error("WebSocket Error", frame);
      },
    });

    this.client.activate();
  }

  sendMove(move) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: "/app/move",
        body: JSON.stringify(move),
      });
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }
}

export default new WebSocketService();

