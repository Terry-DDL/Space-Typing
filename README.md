# Space Typing

A small **Java typing shooter** game. Aliens drift through space carrying words. Type a word exactly and the cannon auto‑aims and fires a bullet at the matching UFO. Survive as long as you can before one reaches the bottom of the screen.

---

## Gameplay

* Game opens on a menu screen.
* Press **S** to start.
* Aliens spawn near the top and fall slowly; speed increases over time.
* Type letters to build the word shown above an alien.
* Press **Backspace** to erase the last letter.
* When your typed word exactly matches a live alien's word, the cannon rotates toward that alien and **fires automatically**.
* Hit = alien "pops" and immediately respawns somewhere else with a new random word.
* If any alien reaches the bottom, the game ends.
* Game Over screen shows elapsed time. Press R to restart.
* Mouse is **not** used.

---

## Required Assets

The code currently loads assets using **absolute file paths** to your Desktop (e.g., `/Users/mac/Desktop/galaxy1.jpg`). For sharing this repo, switch to **relative paths** so others can run the game.

---

## Word List

A built‑in pool of \~50 words (fruit, science, space terms) lives in the code. Edit `wordPool` to practice spelling sets (e.g., physics vocab, French verbs, SAT words).

