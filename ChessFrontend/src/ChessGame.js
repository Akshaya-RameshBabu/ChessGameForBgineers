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
  const [whiteLost, setWhiteLost] = useState([]);
const [blackLost, setBlackLost] = useState([]);
const[gameOver,setgameOver]=useState(false);
const[winner,setWinner]=useState("");
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
      setBoard,
      setBoard,
      setTurn,
      handleGameOver,
      handleCapture,
       handleCheck
    );
    return () => WebSocketService.disconnect();
  }, []);
  
const handleCheck = (message) => {
  toast(message);
};

  const handleCapture = (piece) => {
    if (piece.startsWith("W")) setWhiteLost((prev) => [...prev, piece]);
    else setBlackLost((prev) => [...prev, piece]);
  };


const handleGameOver = (winnerName) => {
  console.log("Game Over: " + winnerName);
  setWinner(winnerName);
  setgameOver(true);

  toast(
    <div style={{ textAlign: "center", fontSize: "1.8rem", fontWeight: "bold" }}>
      🏁 {winnerName} 🎉
    </div>,
    {
      position: "top-center",
      autoClose: false,
      closeOnClick: false,
      pauseOnHover: false,
      draggable: false,
      theme: "colored",
      style: {
    background: "linear-gradient(135deg, #f7971e, #ffd200, #00c6ff, #0072ff)",
        color: "#000",
        borderRadius: "1rem",
        padding: "1.5rem",
      },
    }
  );

  setTimeout(() => restartGame(), 5000);
};

const restartGame = () => {
  WebSocketService.resetGame();
  setWinner("");
  setgameOver(false);
  setWhiteLost([]);
  setBlackLost([]);
   toast.dismiss();
};

  const handleDragStart = (e, rowIndex, colIndex) => {
    e.dataTransfer.setData("source", `${rowIndex}-${colIndex}`);
  };

const handleDrop = (e, targetRowIndex, targetColIndex) => {
  try {
    if(gameOver){
      toast("Game is over. Please restart to play again.");
      return;
    }
    const source = e.dataTransfer.getData("source").split("-");
    const [sourceRowIndex, sourceColIndex] = source.map(Number);
    const piece = board[sourceRowIndex][sourceColIndex];
    if (piece === ".") return;

    const [color] = piece.split("");
    const targetPiece = board[targetRowIndex][targetColIndex];

    // 🔹 Turn validation
    if (!turn && color === "W") {
      toast("Black's move!");
      return;
    } else if (turn && color === "B") {
      toast("White's move!");
      return;
    }

   

    // 🔹 Send move to backend
    WebSocketService.sendMove({
      sourceRow: sourceRowIndex,
      sourceCol: sourceColIndex,
      targetRow: targetRowIndex,
      targetCol: targetColIndex,
    });

    setSelected(null);
    setPossibleMoves([]);
  } catch (err) {
    console.error(err);
  }finally{
    console.log(whiteLost);
  console.log(blackLost);
  }
};


  const handleCellClick = (rowIndex, colIndex) => {
  const piece = board[rowIndex][colIndex];
  if (piece === ".") {
    setSelected(null);
    setPossibleMoves([]);
    return;
  }

  const [color, type] = piece.split("");
  const moves = [];
  const inBounds = (r, c) => r >= 0 && r < 8 && c >= 0 && c < 8;

  // Helper to check cells
  const isEmpty = (r, c) => board[r][c] === ".";
  const isOpponent = (r, c) => board[r][c] !== "." && board[r][c][0] !== color;

  // ➤ Pawn moves
 if (type === "P") {
  const dir = color === "W" ? -1 : 1; // White moves up, Black moves down
  const next = rowIndex + dir;
  if (inBounds(next, colIndex) && isEmpty(next, colIndex))
    moves.push([next, colIndex]);

  // Double step
  const startRow = color === "W" ? 6 : 1;
  const doubleStep = rowIndex + 2 * dir;
  if (rowIndex === startRow && isEmpty(next, colIndex) && isEmpty(doubleStep, colIndex))
    moves.push([doubleStep, colIndex]);

  // Diagonal captures
  for (const dc of [-1, 1]) {
    const r = rowIndex + dir, c = colIndex + dc;
    if (inBounds(r, c) && isOpponent(r, c))
      moves.push([r, c]);
  }
}


  // ➤ Rook
  if (type === "R" || type === "Q") {
    const dirs = [[1, 0], [-1, 0], [0, 1], [0, -1]];
    for (const [dr, dc] of dirs) {
      for (let i = 1; i < 8; i++) {
        const r = rowIndex + dr * i, c = colIndex + dc * i;
        if (!inBounds(r, c)) break;
        if (isEmpty(r, c)) moves.push([r, c]);
        else {
          if (isOpponent(r, c)) moves.push([r, c]);
          break;
        }
      }
    }
  }

  // ➤ Bishop
  if (type === "B" || type === "Q") {
    const dirs = [[1, 1], [1, -1], [-1, 1], [-1, -1]];
    for (const [dr, dc] of dirs) {
      for (let i = 1; i < 8; i++) {
        const r = rowIndex + dr * i, c = colIndex + dc * i;
        if (!inBounds(r, c)) break;
        if (isEmpty(r, c)) moves.push([r, c]);
        else {
          if (isOpponent(r, c)) moves.push([r, c]);
          break;
        }
      }
    }
  }

  // ➤ Knight
  if (type === "H") {
    const jumps = [
      [2, 1], [2, -1], [-2, 1], [-2, -1],
      [1, 2], [1, -2], [-1, 2], [-1, -2],
    ];
    for (const [dr, dc] of jumps) {
      const r = rowIndex + dr, c = colIndex + dc;
      if (inBounds(r, c) && (isEmpty(r, c) || isOpponent(r, c)))
        moves.push([r, c]);
    }
  }

  // ➤ King
  if (type === "K") {
    const dirs = [
      [1, 0], [-1, 0], [0, 1], [0, -1],
      [1, 1], [1, -1], [-1, 1], [-1, -1],
    ];
    for (const [dr, dc] of dirs) {
      const r = rowIndex + dr, c = colIndex + dc;
      if (inBounds(r, c) && (isEmpty(r, c) || isOpponent(r, c)))
        moves.push([r, c]);
    }
  }

  setSelected({ rowIndex, colIndex, color });
  setPossibleMoves(moves);
};


  const handleDragOver = (e) => e.preventDefault();

  return (
    <div className="chess-container">
      <div className="chess-board">
    {board.map((row, rowIndex) =>
  row.map((piece, colIndex) => {
    const isPossible = possibleMoves.some(([r, c]) => r === rowIndex && c === colIndex);
    const isSelected =
      selected && selected.rowIndex === rowIndex && selected.colIndex === colIndex;
    const isCapture =
      isPossible &&
      board[rowIndex][colIndex] !== "." &&
      board[rowIndex][colIndex][0] !== selected?.color;

    return (
      <div
        key={`${rowIndex}-${colIndex}`}
        className={`cell ${(rowIndex + colIndex) % 2 === 0 ? "light" : "dark"}
          ${isSelected ? "selected" : ""}
          ${isPossible ? (isCapture ? "capture-move" : "possible-move") : ""}`}
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
      <div className="wrapper">
          <div className="info-panel">
        <h1>♟️ Beginner Friendly Chess</h1>
        <p>Visualize your possible moves — Learn, Play & Improve!</p>
        <div className="turn-indicator">
          Current Turn: <span>{turn ? "White ♙" : "Black ♟︎"}</span>
        </div>
      </div>
      <div className="captured-pieces-container">
  <div className="captured-section white-lost">
    <h3>⚪ White Captured</h3>
    <div className="captured-list">
      {whiteLost.length > 0 ? (
        whiteLost.map((p, i) => (
          <img
            key={i}
            src={{
              WR, WH, WB, WQ, WK, WP,
              BR, BH, BB, BQ, BK, BP
            }[p]}
            alt={p}
            className="captured-piece"
          />
        ))
      ) : (
        <div className="no-capture">No pieces captured yet</div>
      )}
    </div>
  </div>

  <div className="captured-section black-lost">
    <h3>⚫ Black Captured</h3>
    <div className="captured-list">
      {blackLost.length > 0 ? (
        blackLost.map((p, i) => (
          <img
            key={i}
            src={{
              WR, WH, WB, WQ, WK, WP,
              BR, BH, BB, BQ, BK, BP
            }[p]}
            alt={p}
            className="captured-piece"
          />
        ))
      ) : (
        <div className="no-capture">No pieces captured yet</div>
      )}
    </div>
  </div>
</div>

    </div>
    </div>
  );
};

export default ChessGame;
