import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import GameBoard from "./GameBoard";

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<GameBoard />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
