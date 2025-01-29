import React, { useEffect, useState } from "react";
import WebSocketService from "./WebSocketService";
import WR from './images/WR.png'; // White Rook
import WH from './images/WH.png'; // White Knight
import WB from './images/WB.png'; // White Bishop
import WQ from './images/WQ.png'; // White Queen
import WK from './images/WK.png'; // White King
import WP from './images/WP.png'; // White Pawn
import BR from './images/BR.png'; // Black Rook
import BH from './images/BH.png'; // Black Knight
import BB from './images/BB.png'; // Black Bishop
import BQ from './images/BQ.png'; // Black Queen
import BK from './images/BK.png'; // Black King
import BP from './images/BP.png'; // Black Pawn
import toast from 'react-hot-toast';
const ChessGame = () => {
  const [board, setBoard] = useState([]);
  const getPieceImage = (piece) => {
    switch (piece) {
      case 'WR': return WR;
      case 'WH': return WH;
      case 'WB': return WB;
      case 'WQ': return WQ;
      case 'WK': return WK;
      case 'WP': return WP;
      case 'BR': return BR;
      case 'BH': return BH;
      case 'BB': return BB;
      case 'BQ': return BQ;
      case 'BK': return BK;
      case 'BP': return BP;
      default: return null;
    }
  };
  useEffect(() => {
    WebSocketService.connect((chessboard) => {
      setBoard(chessboard);
    });

    return () => {
      WebSocketService.disconnect();
    };
  }, []);
  useEffect(() => {
    WebSocketService.connect(
      (chessboard) => {
        setBoard(chessboard);
      },
      (updatedBoard) => {
        setBoard(updatedBoard);
      }
    );

    return () => {
      WebSocketService.disconnect();
    };
  }, []);
  const [highlightedSquare, setHighlightedSquare] = useState(null); // Store cursor position

  const handleMouseEnter = (rowIndex, colIndex) => {
    setHighlightedSquare({ row: rowIndex, col: colIndex });

    // Send cursor position to the server via WebSocket
    socket.emit('cursorMove', { row: rowIndex, col: colIndex });
  };
  const handleDragStart = (e, rowIndex, colIndex) => {
    e.dataTransfer.setData("source", `${rowIndex}-${colIndex}`);
  };

  const handleDrop = (e, targetRowIndex, targetColIndex) => {
    try {
      const source = e.dataTransfer.getData("source").split("-");
      const sourceRowIndex = parseInt(source[0]);
      const sourceColIndex = parseInt(source[1]);

      const newBoard = [...board];
      const piece = newBoard[sourceRowIndex][sourceColIndex];

      if (piece !== ".") {
          // Send move to server
          WebSocketService.sendMove({
            sourceRow: sourceRowIndex,
            sourceCol: sourceColIndex,
            targetRow: targetRowIndex,
            targetCol: targetColIndex,
          });
       
      }
    } catch (err) {
      console.log(err);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(8, 50px)', gap: '1px' }}>
      {board.map((row, rowIndex) =>
        row.map((piece, colIndex) => (
          <div
            key={`${rowIndex}-${colIndex}`}
            style={{
              width: '50px',
              height: '50px',
              backgroundColor: (rowIndex + colIndex) % 2 === 0 ? 'white' : 'gray',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              border: '1px solid #333',
            }}
            onDragOver={handleDragOver}
            onDrop={(e) => handleDrop(e, rowIndex, colIndex)}
          >
            {piece !== '.' && (
              <img
                src={getPieceImage(piece)}
                alt={piece}
                draggable
                onDragStart={(e) => handleDragStart(e, rowIndex, colIndex)}
                style={{ width: '40px', height: '40px' }}
              />
            )}
          </div>
        ))
      )}
    </div>
  );
};

export default ChessGame;
