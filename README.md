# Tetris - Swing (versione semplice)

Repository: implementazione semplice di Tetris scritta in Java usando Swing.

Descrizione

Questa repository contiene un singolo file Java che implementa una versione semplificata di Tetris usando le classi di GUI di Swing (JPanel, JFrame, Timer, ecc.). Il progetto è pensato come base didattica: il gioco funziona, è completo delle meccaniche principali e include numerosi punti di estensione per aggiungere feature più avanzate (salvataggio, 7-bag, SRS, multiplayer, UI separata, ecc.).

Il codice è volutamente in un unico file per facilitare la lettura e la comprensione. Per progetti reali o più grandi è comunque consigliabile separare modello, vista e controller in file/classi distinte.

Caratteristiche principali

Griglia di gioco 10x20 (classico Tetris).

Set di tetramini I, O, T, S, Z, J, L con colori distinti.

Movimento sinistra/destra, rotazione (orario/antiorario), soft drop, hard drop.

Meccanica di hold (scambio con il pezzo in riserva) con restrizione di uso per spawn.

Calcolo e rimozione di linee complete, aggiornamento punteggio e livello.

Ghost piece (visualizzazione della posizione di atterraggio).

Pannello informativo a destra (punteggio, livello, righe, prossimo, hold, controlli).

Pausa, reset partita, gestione Game Over.

Sistema di aumento della velocità con il livello (timer Swing).

Dipendenze

Java SE (JDK) 8 o superiore.

Nessuna libreria esterna: tutto si basa su javax.swing e java.awt (inclusi gli eventi KeyEvent e il Timer Swing).

Nota: il codice usa javax.swing.* e java.awt.*. Per compilare ed eseguire basta un JDK aggiornato.

Struttura del progetto

Questo progetto è un esempio minimale e contiene un unico file principale (es. Tetris.java).

Struttura suggerita:

/ (root)
 ├─ README.md
 ├─ src/
 │   └─ Tetris.java   // classe che estende JPanel e contiene tutta la logica
 └─ assets/ (opzionale)

Per progetti più grandi: creare pacchetti model, view, controller e separare Tipo / definizione dei pezzi, GameState, Renderer, e InputHandler.

Come compilare ed eseguire

Da linea di comando (assumendo Tetris.java in src/):

# compila
javac -d out src/Tetris.java


# esegui
java -cp out Tetris

Oppure apri la cartella del progetto in Visual Studio Code (con estensione Java) e usa i comandi di build/launch forniti dall'IDE.

Entrypoint

La main() crea un JFrame, ci aggiunge l'istanza del pannello Tetris e mostra la finestra. L'esecuzione avviene sul Event Dispatch Thread (SwingUtilities.invokeLater).

Panoramica tecnica della classe principale

La classe principale (es. public class Tetris extends JPanel implements ActionListener) è organizzata in sezioni:

Costanti di configurazione: colonne, righe, dimensione cella, punti per righe, velocità iniziale/minima.

Enumerazione Tipo: definisce i vari tetramini con la loro forma base (matrice 0/1) e colore. Fornisce una copia profonda della forma tramite getForma().

Stato del gioco (model): matrice griglia che contiene i blocchi fissi, informazioni sul pezzo corrente (formaPezzo, tipoPezzo, posizione), prossimo, hold, punteggio, livello, righe totali, flag di stato (inCorso, pausa, gameOver, puoHold) e Timer per il loop di gioco.

Costruttore: imposta dimensioni del pannello, colore background, key listener e avvia nuovaPartita().

Loop di gioco: implementato con Timer Swing che chiama actionPerformed a intervalli definiti dalla variabile velocita.

Gestione pezzi:

nuovoPezzo(): trasferisce il prossimo come attuale e genera un nuovo prossimo.

valido(...): verifica collisioni e bordi.

muovi(...), ruota(...), hardDrop(), usaHold() per le azioni del giocatore.

Locking / clearing: blocca() fissa il pezzo nella griglia, rileva e rimuove righe complete, aggiorna punteggio e livello e regola la velocità del timer.

Rendering: paintComponent disegna la griglia, le celle bloccate, il pezzo corrente, il ghost piece e il pannello informativo (prossimo/hold/punteggio/controlli). Viene disegnato anche un overlay per Pausa / Game Over.

Input: KeyAdapter che cattura i tasti e chiama gestisciTasto(int k) per mappare i comandi.

Controlli (tastiera)

← → : muovi orizzontalmente

↓ : soft drop (scendi più veloce)

↑ o X : ruota orario

Z : ruota antiorario

SPACE : hard drop

C : hold

P : pausa / riprendi

R : reset partita

Punteggio e progressione

I punteggi per linee seguite sono definiti nell'array PUNTI (es. 0, 100, 300, 500, 800).

Il livello aumenta ogni 10 righe rimosse (righeTotal / 10 + 1).

Al salire del livello la velocità aumenta riducendo il delay del Timer, fino a un valore minimo (VELOCITA_MIN).

Limitazioni note e possibili miglioramenti

Random: attuale generazione completamente casuale; implementare 7-bag per una distribuzione più equa.

Sistema di rotazione: non implementa lo SRS ufficiale (System Rotation Standard). Il wall-kick è semplificato.

Separazione delle responsabilità: tutto è in un singolo file; separare GameState, Renderer, Input rende il codice più manutenibile e testabile.

Audio, opzioni, salvataggi: manca il supporto per suoni, impostazioni, salvataggio/recupero stato.

Multiplayer / leaderboard: possibile estensione con networking o integrazione REST per classifiche.

Testing: non sono presenti test automatici. Estrarre la logica in classi testabili facilita JUnit.

Idee per estensioni (roadmap suggerita)

Refactor: separare modello / vista / controller.

Aggiungere 7-bag e implementare SRS.

Implementare replay / registrazione mosse.

Supporto per skins e audio.

Implementare UI di menu e impostazioni con InputMap/ActionMap.

Leaderboard locale o online.

Come contribuire

Fai fork del repository.

Crea un branch per la tua feature (feature/nome-feature).

Fai commit chiari e apri una pull request.

Se possibile aggiungi test e descrivi il comportamento atteso.

Licenza

Questo progetto è fornito sotto licenza MIT (placeholder). Se vuoi un'altra licenza, aggiorna il file LICENSE.

Credits

Autore originale del codice: (inserisci nome/handle).

Grazie per aver usato questo esempio — buon divertimento e buon hacking!