package edu.wctc;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiceGame {
    private final List<Player> players;
    private final List<Die> dice;
    private final int maxRolls;
    private Player currentPlayer;

    public DiceGame (int countPlayers, int countDice, int maxRolls) {
        // Constructor that initializes all final instance fields.
        players = new ArrayList<>();
        dice = new ArrayList<>();
        this.maxRolls = maxRolls;

        // Creates the required number of Player objects
        // and Die objects and adds them to the appropriate lists.
        for (int i = 0; i < countPlayers; i++) {
            Player player = new Player();
            players.add(player);
        }
        for (int i = 0; i < countDice; i++) {
            Die side = new Die(6);
            dice.add(side);
        }

        // If the number of players is less than 2, throws an IllegalArgumentException.
        if (countPlayers < 2) {
            throw new IllegalArgumentException();
        }
    }

    private boolean allDiceHeld () {
        // Returns true if all dice are held, false otherwise.
        if(dice.stream().allMatch(d -> d.isBeingHeld()))
            return true;
        return false;
    }

    public boolean autoHold (int faceValue) {
        boolean result = false;
        //If there already is a die with the given face value that is held, just return true.
        if (dice.stream().anyMatch(x -> x.getFaceValue() == faceValue & x.isBeingHeld())) {
            return true;
        }
        //If there is a die with the given face value that is unheld, hold it and return true.
        // (If there are multiple matches, only hold one of them.)
        var unHeldDie = dice.stream().filter(x -> x.getFaceValue() == faceValue & !x.isBeingHeld()).findFirst();
        if(unHeldDie.isPresent()) {
            unHeldDie.get().holdDie();
            result = true;
        }

        //If there is no die with the given face value, return false.
        if(dice.stream().noneMatch(x -> x.getFaceValue() == faceValue))
            result = false;
        return result;
    }

    public boolean currentPlayerCanRoll () {
        // 	Returns true if the current player has any rolls remaining and if not all dice are held.
        boolean canRoll = false;
        if((maxRolls - currentPlayer.getRollsUsed()) > 0)
            canRoll = true;
        else
            allDiceHeld();
        return canRoll;
    }

    public int getCurrentPlayerNumber () {
        // Returns the player number of the current player.
        return currentPlayer.getPlayerNumber();
    }

    public int getCurrentPlayerScore () {
        // Returns the score of the current player.
        return currentPlayer.getScore();
    }

    public String getDiceResults () {
        //Return a string composed by concatenating each Die's toString.
        return dice.stream().map(d -> d.toString()).collect(Collectors.joining());
    }

    public String getFinalWinner () {
        //Finds the player with the most wins and returns its toString.
        List<Player> topList = players.stream()
                .sorted(Comparator.comparingInt(Player::getWins).reversed())
                .collect(Collectors.toList());
        return topList.get(0).toString();
    }

    public String getGameResults () {
        //Sorts the player list field by score, highest to lowest.
        List<Player> highToLow = players.stream().sorted(Comparator.comparingInt(Player::getScore).reversed()).toList();
        //Awards each player that earned the highest score a win and all others a loss.
        var result = players.stream().map(x -> x.toString()).collect(Collectors.joining());
        for (int i = 0; i + 1 < highToLow.size(); i++) {
            if (highToLow.get(i).getScore() == highToLow.get(i + 1).getScore())
                return result;
            else {
                highToLow.get(0).addWin();
                highToLow.stream().forEach(x -> {
                    if (x.getPlayerNumber() != highToLow.get(0).getPlayerNumber()) {
                        x.addLoss();
                    }
                });
            }
        }

        //Returns a string composed by concatenating each Player's toString.
        return players.stream().map(x -> x.toString()).collect(Collectors.joining());
    }

    private boolean isHoldingDie (int faceValue) {
        //Returns true if there is any held die with a matching face value, false otherwise.
        if(dice.stream().filter(x -> x.isBeingHeld()).anyMatch(x -> x.getFaceValue() == faceValue))
            return true;
        return false;
    }

    public boolean nextPlayer () {
        // If there are more players in the list after the current player,
        // updates currentPlayer to be the next player and returns true.
        // check if there's any player's used rolls -> 0 or not?
        if(players.stream().anyMatch(x -> x.getRollsUsed() == 0)) {
            var morePlayer = players.stream().filter(x -> x.getRollsUsed() == 0).toList();
            for (int i = 0; i < morePlayer.size(); i++) {
                currentPlayer = morePlayer.get(i);
                return true;
            }
        }
        // Otherwise, returns false.
        return false;
    }

    public void playerHold (char dieNum) {
        //Finds the die with the given die number (NOT the face value) and holds it.
        dice.stream().filter(x -> x.getDieNum() == dieNum).findFirst().get().holdDie();
    }

    public void resetDice () {
        //Resets each die.
        dice.forEach(Die::resetDie);
    }

    public void resetPlayers () {
        //Resets each player.
        players.forEach(Player::resetPlayer);
    }

    public void rollDice () {
        //Logs the roll for the current player, then rolls each die.
        currentPlayer.roll();
        dice.forEach(Die::rollDie);
    }

    public void scoreCurrentPlayer () {
        //If there is not a 6, 5, and 4 held, assigns no points.
        if(!isHoldingDie(6) && !isHoldingDie(5) && !isHoldingDie(4)) {
            currentPlayer.setScore(0);
        }

        //If there is currently a ship (6), captain (5), and crew (4) die held,
        // adds the points for the remaining two dice (the cargo) to the current player's score.
        // get a new stream list of cargo without the held dice of (6 & 5 & 4), loop the cargo list
        if(isHoldingDie(6) && isHoldingDie(5) && isHoldingDie(4)) {
            var heldSix = dice.stream().filter(x -> x.getFaceValue() == 6 & x.isBeingHeld()).findFirst().get();
            var heldFive = dice.stream().filter(x -> x.getFaceValue() == 5 & x.isBeingHeld()).findFirst().get();
            var heldFour = dice.stream().filter(x -> x.getFaceValue() == 4 & x.isBeingHeld()).findFirst().get();

            var cargo = dice.stream().filter(x -> x != heldSix & x != heldFive & x != heldFour).toList();
            var points = 0;
            for (int i = 0; i < cargo.size(); i++) {
                points += cargo.get(i).getFaceValue();
            }
            currentPlayer.setScore(points);
        }
    }

    public void startNewGame () {
        // Assigns the first player in the list as the current player.
        currentPlayer = players.get(0);
        // The list will still be sorted by score from the previous round,
        players.sort(Comparator.comparingInt(Player::getScore).reversed());
        // so winner will end up going first.
        currentPlayer = players.get(0);
        //Resets all players.
        resetPlayers();
    }
}
