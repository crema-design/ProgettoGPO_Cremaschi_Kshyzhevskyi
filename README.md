# ProgettoGPO_Cremaschi_Kshzhevskyi
# Tetris Java

Versione semplice di **Tetris** sviluppata in Java tramite **Swing**. Il progetto è in un singolo file per praticità, ma è strutturato per permettere facilmente estensioni future e miglioramenti modulari.

## Funzionalità

- Griglia 10x20 con rendering tramite Swing
- Tutti i tetramini standard (I, O, T, S, Z, J, L)
- Movimento orizzontale, caduta rapida e hard drop
- Rotazione oraria e antioraria
- Ghost piece per mostrare la posizione di atterraggio
- Sistema di punteggio con punti bonus per linee multiple
- Incremento automatico del livello
- Hold del pezzo attuale
- Prossimo pezzo in preview
- Gestione stati: pausa, game over, reset
- Controlli completi via tastiera
- Pannello laterale con:
  - punteggio
  - livello
  - numero di righe completate
  - prossimo pezzo
  - pezzo in hold
  - legenda controlli

## Controlli

| Tasto       | Azione                   |
|------------|---------------------------|
| ← / →      | Muovi sinistra/destra     |
| ↓          | Scendi velocemente        |
| ↑ / X      | Ruota orario              |
| Z          | Ruota antiorario          |
| Space      | Hard drop                 |
| C          | Hold                      |
| P          | Pausa / Riprendi          |
| R          | Reset partita             |

## Struttura del Codice

La classe principale **Tetris** estende `JPanel` e implementa `ActionListener` per il loop di gioco tramite `Timer`.  
Il file contiene:

### Gestione della Griglia
- La griglia è una matrice di `Tipo`, con `Tipo.NESSUNO` per celle vuote.
- Ogni cella contiene il colore e la forma del tetramino.

### Gestione dei Pezzi
- I tetramini sono definiti nell’enum `Tipo`, con forma come matrice 0/1.
- Supporta rotazione generica con wall-kick semplice.
- Il pezzo corrente ha posizione (riga, colonna), forma, tipo e preview.

### Movimento e Collisioni
- Funzione `valido()` per controllare contatti con bordi o celle occupate.
- Movimento fluido a ogni tick del timer.

### Hard Drop
- Il pezzo cade fino al limite e assegna punteggio extra.

### Rimozione Linee
- Algoritmo che scansiona la griglia dal basso verso l’alto
- Le righe complete vengono eliminate con shift delle righe superiori
- Punteggio calcolato in base al numero di linee

### Livelli e Velocità
- Aumento del livello ogni 10 righe completate
- Velocità della caduta gestita da `Timer.setDelay()`

### Hold
- Primo utilizzo mette il pezzo in hold
- Utilizzi successivi effettuano lo scambio
- Hold bloccato finché non arriva un nuovo pezzo

### Input da Tastiera
- Gestito tramite `KeyAdapter` nel pannello
- Mappatura semplice e immediata, integrata con la UI

### Rendering Grafico
- Griglia disegnata con linee sottili per leggibilità
- Pezzi renderizzati con colori pieni
- Ghost piece semi-trasparente
- Pannello laterale con testi e miniature dei pezzi

## Possibili Estensioni Future

Il progetto è stato pensato per essere espandibile. Idee per versioni avanzate:

- Implementazione di un menù all'avvio dell'applicativo per la scelta della difficoltà del gioco
- Sistema 7-bag per generazione più equa dei pezzi
- Implementazione completa dello **SRS** (Super Rotation System)
- Animazioni, ombre e effetti grafici
- Modalità multiplayer (locale o online)
- Classifiche salvate in file o database
- Salvataggio/caricamento stato partita
- Skin personalizzate e impostazioni utente
- Replay o registrazione delle mosse
- UI separata in Model–View–Controller

## Requisiti

- Java JDK 8+  
- Supporto Swing (incluso nelle versioni standard del JDK)

## Come Eseguire

Compilazione:

```bash
javac Tetris.java
