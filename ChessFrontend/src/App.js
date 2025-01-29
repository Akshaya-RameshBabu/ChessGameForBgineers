import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import ChessGame from "./ChessGame";

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<ChessGame />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;

