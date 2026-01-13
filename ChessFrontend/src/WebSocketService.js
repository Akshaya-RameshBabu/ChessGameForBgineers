import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import baseUrl from "./utils";

class WebSocketService {
  constructor() {
    this.client = null;
  }

  connect(onBoard, onMove, onTurn, onGameOver, onCapture,onCheck) {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
      onConnect: () => {
        this.client.subscribe("/topic/board", (msg) => onBoard(JSON.parse(msg.body)));
        this.client.subscribe("/topic/moves", (msg) => onMove(JSON.parse(msg.body)));
        this.client.subscribe("/topic/Turn", (msg) => onTurn(JSON.parse(msg.body)));
        this.client.subscribe("/topic/check", (msg) => onCheck(msg.body));
        this.client.subscribe("/topic/gameOver", (msg) => onGameOver(msg.body));
        this.client.subscribe("/topic/capture", (msg) => onCapture(msg.body));
        this.client.publish({ destination: "/app/chessboard" });
        this.client.publish({ destination: "/app/Turn" });
      },
    });
    this.client.activate();
  }

  sendMove(move) {
    this.client?.publish({ destination: "/app/move", body: JSON.stringify(move) });
  }

  resetGame() {
    this.client?.publish({ destination: "/app/reset" });
  }

  disconnect() {
    this.client?.deactivate();
  }
}

export default new WebSocketService();
