# Peg Game

This Java application implements a graphical version of the classic **Peg Solitaire** game using the **Model-View-Controller (MVC)** architecture. Players remove pegs by jumping over adjacent pegs until no more valid moves remain.

---

## 🧩 Game Overview

**Peg Solitaire** is a board game for one player involving movement of pegs on a board with holes. A valid move is to jump a peg over an adjacent peg into an empty hole, removing the jumped peg. The goal is to eliminate all but one peg.

---

## 📁 Project Structure

```
peggame 4/
├── Controller/
│   └── [Event handlers for user actions]
├── Model/
│   ├── PegGame.java         # Core game logic
│   ├── BoardReader.java     # Loads board configuration
│   ├── Peg.java, Move.java, Location.java  # Game data models
│   ├── GameState.java       # Game state enum
│   └── board.txt            # Sample game board configuration
├── View/
│   └── PegGameView.java     # GUI rendering (JavaFX)
```

---

## 🚀 Getting Started

### Prerequisites

- Java JDK 8 or higher
- IDE (e.g., IntelliJ, Eclipse) or command-line tools

### Compilation

1. Navigate to the project folder:
   ```bash
   cd "peggame 4"
   ```
2. Compile all Java files:
   ```bash
   javac Controller/*.java Model/*.java View/*.java
   ```
3. Run the application:
   ```bash
   java View.PegGameView
   ```

---

## 📦 Components

### Model
- **PegGame.java**: Core logic for moves and game state.
- **BoardReader.java**: Loads board configuration from `board.txt`.
- **Peg.java, Move.java, Location.java**: Data models for gameplay elements.
- **GameState.java**: Enum defining possible game states.

### View
- **PegGameView.java**: JavaFX-based GUI that displays the board and pegs.

### Controller
- Event handlers for:
  - Peg selection and movement
  - Saving game state
  - Pop-up and exit actions

---

## 📃 Configuration

The game reads the initial board state from:

```
Model/board.txt
```

You can customize this file to change the layout of the game board.

---

## ❗ Known Issues

- Only supports square board format.
- No support for undo/redo functionality.

---

## 🛠️ Future Improvements

- Add sound effects and animations
- Implement save/load feature with file I/O
- Add difficulty levels or board presets

---

## 📜 License

This project is for educational purposes and is not yet licensed. Contact the author for permission before reuse.

---