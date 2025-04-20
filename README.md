#  Bonsai – Digital Board Game Implementation

**Bonsai** is a digital version of the board game *Bonsai*, implemented in Kotlin using the [BoardGameWork (BGW)] framework. The game captures the relaxing yet strategic gameplay of the original, supporting both human and bot players in local and networked matches.

> 🎓 Developed as part of a university project.

---

##  Features

-  **Bot Support**
  - Basic bots with random moves
  - Stronger bots for tournament play
  - Fully automated bot-vs-bot matches
  - Adjustable simulation speed
  - Max. 10 seconds per bot move

-  **Game Modes**
  - Hotseat mode (local multiplayer)
  - BGW-Net support for networked play

-  **Game Flow & Rules**
  - Fully automated rule checking
  - Configurable turn order (manual/random)
  - Configurable objective tiles
  - Undo/Redo (local only)

-  **Persistence**
  - Save/load for local games

---

##  Technologies Used

- **Kotlin**
- **IntelliJ IDEA**
- [BoardGameWork (BGW)](https://github.com/aisbergg/bgwsdk) framework

---

##  Getting Started

### Prerequisites
- Java 17+
- Kotlin
- IntelliJ IDEA (recommended)

### Clone & Run

```bash
git clone https://github.com/yourusername/Bonsai.git
