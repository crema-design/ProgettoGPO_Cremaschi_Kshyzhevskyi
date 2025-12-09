# ProgettoGPO_Cremaschi_Kshyzhevskyi
# Tetris Java

Versione semplice di Tetris sviluppata in **Java** usando **Swing**. Include le funzionalità principali del gioco e alcune estensioni facili da implementare in futuro.

## Funzionalità

- Griglia 10x20 con tutti i tetramini standard (I, O, T, S, Z, J, L)
- Movimento e rotazione dei pezzi (oraria e antioraria)
- Hard drop e caduta naturale dei pezzi
- Hold per scambiare il pezzo corrente
- Calcolo e visualizzazione di:
  - Punteggio
  - Livello
  - Linee totali completate
- Ghost piece per visualizzare la posizione di atterraggio
- Pannello informazioni a destra (prossimo pezzo, hold, controlli)
- Pause e reset della partita

## Controlli

| Tasto        | Azione                  |
|--------------|------------------------|
| ← / →        | Muovi sinistra/destra  |
| ↓            | Scendi velocemente     |
| ↑ / X        | Ruota orario           |
| Z            | Ruota antiorario       |
| Space        | Hard drop              |
| C            | Hold                   |
| P            | Pausa / Riprendi       |
| R            | Reset partita          |

## Requisiti

- Java 8 o superiore
- Libreria standard Swing (inclusa in Java SE)

## Come eseguire

```bash
javac Tetris.java
java Tetris