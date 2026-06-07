# Battle City

A Java remake of the classic NES Battle City game, built with Java Swing.

## Features

- Player tank movement and shooting (WASD / Arrow Keys + Space)
- 20 enemy tanks per level with basic AI targeting
- 4 tile types: Brick (destructible), Steel (indestructible), Water (blocks tanks), Bush (hides tanks)
- Eagle base — protect it or it's game over
- Power-ups: Star (upgrade tank), Tank (extra life), Bomb (clear all enemies), Clock (freeze enemies), Shovel (reinforce base), Shield (temporary invincibility)
- 3 built-in levels with increasing difficulty
- Custom map editor — design and save your own maps as CSV
- High scores saved to CSV with name, score, date and time
- Options menu for difficulty (Easy / Medium / Hard)
- NES-accurate sprites extracted from the original sprite sheet

## How to Run

Requires Java 8+. Clone the repo and run from the project root:

```
javac src/*.java -d out
java -cp out GameFrame
```

Or open in Eclipse and run `GameFrame.java` directly.

Make sure the `images/` and `maps/` folders are in the working directory. If `maps/` is missing, the game creates it automatically with 3 default levels on first launch.

## Controls

| Key | Action |
|-----|--------|
| W / A / S / D | Move |
| Arrow Keys | Move (alt) |
| Space | Shoot |

## Map Editor

Open via Menu → Map Editor. Use WASD to move the cursor, Space to cycle tile types, mouse click to jump to a cell. Save and load maps in CSV format.

## Project Structure

```
src/          Java source files
images/       PNG sprites (NES sprite sheet extracts)
maps/         Level CSV files
scores.csv    High score records
```

## About

Developed as a course project for CSE 212 — Canat Koç, Yeditepe University, 2026.
