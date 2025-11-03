import React, { useEffect, useState } from "react";
import WebSocketService from "./WebSocketService";
import toast from "react-hot-toast";
import "./ChessGame.css";

import WR from "./images/WR.png";
import WH from "./images/WH.png";
import WB from "./images/WB.png";
import WQ from "./images/WQ.png";
import WK from "./images/WK.png";
import WP from "./images/WP.png";
import BR from "./images/BR.png";
import BH from "./images/BH.png";
import BB from "./images/BB.png";
import BQ from "./images/BQ.png";
import BK from "./images/BK.png";
import BP from "./images/BP.png";

const ChessGame = () => {
  const [board, setBoard] = useState([]);
  const [turn, setTurn] = useState(true); // true - White, false - Black
  const [selected, setSelected] = useState(null);
  const [possibleMoves, setPossibleMoves] = useState([]);

  const getPieceImage = (piece) => {
    switch (piece) {
      case "WR": return WR;
      case "WH": return WH;
      case "WB": return WB;
      case "WQ": return WQ;
      case "WK": return WK;
      case "WP": return WP;
      case "BR": return BR;
      case "BH": return BH;
      case "BB": return BB;
      case "BQ": return BQ;
      case "BK": return BK;
      case "BP": return BP;
      default: return null;
    }
  };

  useEffect(() => {
    WebSocketService.connect(
      (chessboard) => setBoard(chessboard),
      (updatedBoard) => setBoard(updatedBoard),
      (turnData) => setTurn(turnData)
    );
    return () => WebSocketService.disconnect();
  }, []);

  const handleDragStart = (e, rowIndex, colIndex) => {
    e.dataTransfer.setData("source", `${rowIndex}-${colIndex}`);
  };

  const handleDrop = (e, targetRowIndex, targetColIndex) => {
    try {
      const source = e.dataTransfer.getData("source").split("-");
      const [sourceRowIndex, sourceColIndex] = source.map(Number);
      const piece = board[sourceRowIndex][sourceColIndex];
      if (piece === ".") return;

      const [color] = piece.split("");
      if (!turn && color === "W") toast("Black's move!");
      else if (turn && color === "B") toast("White's move!");
      else {
        WebSocketService.sendMove({
          sourceRow: sourceRowIndex,
          sourceCol: sourceColIndex,
          targetRow: targetRowIndex,
          targetCol: targetColIndex,
        });
        setSelected(null);
        setPossibleMoves([]);
      }
    } catch (err) {
      console.log(err);
    }
  };

  const handleCellClick = (rowIndex, colIndex) => {
    const piece = board[rowIndex][colIndex];
    if (piece !== ".") {
      setSelected({ rowIndex, colIndex });
      // for demo: highlight nearby squares as possible moves
      const moves = [
        [rowIndex + 1, colIndex],
        [rowIndex - 1, colIndex],
        [rowIndex, colIndex + 1],
        [rowIndex, colIndex - 1],
      ].filter(([r, c]) => r >= 0 && r < 8 && c >= 0 && c < 8);
      setPossibleMoves(moves);
    } else {
      setSelected(null);
      setPossibleMoves([]);
    }
  };

  const handleDragOver = (e) => e.preventDefault();

  return (
    <div className="chess-container">
      <div className="chess-board">
        {board.map((row, rowIndex) =>
          row.map((piece, colIndex) => {
            const isPossible = possibleMoves.some(
              ([r, c]) => r === rowIndex && c === colIndex
            );
            const isSelected =
              selected &&
              selected.rowIndex === rowIndex &&
              selected.colIndex === colIndex;

            return (
              <div
                key={`${rowIndex}-${colIndex}`}
                className={`cell ${(rowIndex + colIndex) % 2 === 0 ? "light" : "dark"} 
                  ${isPossible ? "possible-move" : ""} ${isSelected ? "selected" : ""}`}
                onDragOver={handleDragOver}
                onDrop={(e) => handleDrop(e, rowIndex, colIndex)}
                onClick={() => handleCellClick(rowIndex, colIndex)}
              >
                {piece !== "." && (
                  <img
                    src={getPieceImage(piece)}
                    alt={piece}
                    draggable
                    onDragStart={(e) => handleDragStart(e, rowIndex, colIndex)}
                    className="piece"
                  />
                )}
              </div>
            );
          })
        )}
      </div>
          <div className="info-panel">
        <h1>♟️ Beginner Friendly Chess</h1>
        <p>Visualize your possible moves — Learn, Play & Improve!</p>
        <div className="turn-indicator">
          Current Turn: <span>{turn ? "White ♙" : "Black ♟︎"}</span>
        </div>
        <div class="timer" id="timer">00:00</div>
      </div>
    </div>
  );
};

export default ChessGame;
