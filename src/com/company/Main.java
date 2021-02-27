package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------Player Class
//----------------------------------------------------------------------------------------------------------------------

class Player
{

    String name;
    int numberOfUnits;
    String color;
    ArrayList<Region> ownedRegions;
    static ArrayList<String> colorArray = new ArrayList<>(Arrays.asList(ConsoleColors.RED, ConsoleColors.BLUE, ConsoleColors.CYAN, ConsoleColors.GREEN, ConsoleColors.PURPLE, ConsoleColors.YELLOW));

    //--------------------------------------------------------------------------------

    Player(String name)
    {
        this.name = name;
        ownedRegions = new ArrayList<Region>();
        color = pickRandomColor();
    }

    //--------------------------------------------------------------------------------

    /*Player(String name ,int numberOfUnits)
    {
        this.name = name;
        this.numberOfUnits = numberOfUnits;
        ownedRegions = new ArrayList<Region>();
        color = pickRandomColor();
    }*/

    //--------------------------------------------------------------------------------

    static String pickRandomColor()
    {
        int i = new Random().nextInt(colorArray.size());
        return colorArray.remove(i);
    }

    //--------------------------------------------------------------------------------

    public int Dice()
    {
        return new Random().nextInt(6) + 1;
    }

    //--------------------------------------------------------------------------------

    public void Battle(Region Attacker , Region Defender)
    {
        Scanner scan = new Scanner(System.in);
        int attackerDice ;
        int defenderDice ;

        while(true)
        {
            if (Attacker.unitsToUseInBattle == 0)
            {
                System.out.println("Defender Won");

                break;
            }
            else if(Defender.unitsInRegion == 0)
            {
                Attacker.owner.ownedRegions.add(Defender);
                Defender.owner.ownedRegions.remove(Defender);
                Defender.owner = Attacker.owner;

                Defender.unitsInRegion = Attacker.unitsToUseInBattle;

                System.out.println("Attackers Won");
                break;
            }

            attackerDice = Dice();

            defenderDice = Dice();

            if(attackerDice > defenderDice)
            {
                Defender.unitsInRegion--;
                System.out.println("Attackers Won the Engagement");
            }
            else if(defenderDice >= attackerDice)
            {
                Attacker.unitsToUseInBattle--;
                System.out.println("Defenders Won the Engagement");
            }
        }
    }


    //--------------------------------------------------------------------------------

    void setNumberOfUnits(int numberOfUnits)
    {
        this.numberOfUnits = numberOfUnits;
    }

}

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------Human Player Class (Extends Player)
//----------------------------------------------------------------------------------------------------------------------

class HumanPlayer extends Player {

    HumanPlayer(String name)
    {
        super(name);
    }
}
//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------AI Agent Class (Extends Player)
//----------------------------------------------------------------------------------------------------------------------

class Move
{
    //double moveScore;
    int moveScore;
    Region bestRegionToAttackFrom; //Us (attacker)
    Region bestRegionToAttack; //Enemy (defender)
}

    //--------------------------------------------------------------------------------


class AIPlayer extends Player
{

    static ArrayList<String> AgentNameList = new ArrayList<>(Arrays.asList("Jack", "Sara", "Mary", "Arya", "Zeus","Eddy"));

    AIPlayer(String name)
    {
        super(name);
    }

    //--------------------------------------------------------------------------------

    static String pickRandomName()
    {
        int i = new Random().nextInt(AgentNameList.size());
        return AgentNameList.remove(i);
    }

    //--------------------------------------------------------------------------------

    double CalculateNBSR(Region r)
    {
        int BST = 0;
        for (Region enemyRegion : r.adjRegions){   // calculating bst.
            if (!enemyRegion.owner.equals(r.owner))
                BST = BST + enemyRegion.unitsInRegion;

        }
        double BSR = (double) (BST / r.unitsInRegion);

        int [] BSRs = new int[r.owner.ownedRegions.size()];
        int bsti = 0;
        int setIndex = 0;

        for (Region forPlayerRegion : r.owner.ownedRegions){

            for (Region enemyRegion : forPlayerRegion.adjRegions){   // calculating bst.

                if (!enemyRegion.owner.equals(r.owner))
                    bsti = bsti+ enemyRegion.unitsInRegion;

            }
            BSRs[setIndex] = bsti / forPlayerRegion.unitsInRegion;
            setIndex++;
        }

        double sumBSRS = 0.0;
        for (int i = 0; i < BSRs.length; i++){
            sumBSRS = sumBSRS + BSRs[i];
        }

        return BSR / sumBSRS;
    }

    //--------------------------------------------------------------------------------

    Region bestMoveReinforceSelect() {

        Region bestRegionToReinforce = null;
        double bestNBSR = -Double.MIN_VALUE;

        for (int i = 0; i < this.ownedRegions.size() ; i++)
        {
            Region regionToReinforce = ownedRegions.get(i);
            double NBSR = CalculateNBSR(regionToReinforce);

            if (NBSR > bestNBSR)
            {
                bestNBSR = NBSR;
                bestRegionToReinforce = regionToReinforce;
            }

        }
        return bestRegionToReinforce;

    }

    //--------------------------------------------------------------------------------

    Move bestMoveAttackSelect() //Minimax
    {
       int stopCounter = 0;
       int adjCounter = 0;
        Move bestMove = new Move();
        bestMove.moveScore = Integer.MIN_VALUE;
        Region temp;

        for(int i = 0; i < this.ownedRegions.size(); i++)
        {
            adjCounter = 0;

            Region attackerRegion = this.ownedRegions.get(i);

            for (int j = 0; j < attackerRegion.adjRegions.size(); j++)
            {
                Region defenderRegion = attackerRegion.adjRegions.get(j);

                if(attackerRegion.owner.name.equals(defenderRegion.owner.name))
                    continue;

                temp = attackerRegion;
                Move nextMove = attackMiniMax(attackerRegion , defenderRegion , 0, true);
                attackerRegion = temp;

//                if (nextMove.moveScore > 0.5 && nextMove.moveScore > bestMove.moveScore) //if the score was better    // method 2.
//                {
//                    bestMove = nextMove;
//                    adjCounter++;
//                }


                if (nextMove.moveScore > 0 && nextMove.moveScore > bestMove.moveScore) //if the score was better
                {
                    bestMove = nextMove;
                    adjCounter++;
                }

            }

            if (adjCounter == 0){
                stopCounter++;
            }

            if (stopCounter == this.ownedRegions.size()){
                return new Move();
            }
        }
        return bestMove;
    }

    //--------------------------------------------------------------------------------

    Move attackMiniMax(Region attackerRegion , Region defenderRegion, int depth, boolean isMax)
    {
        Move nextMove = new Move();

        nextMove.moveScore =  Probability.getProbabilty2(attackerRegion , defenderRegion);
        nextMove.bestRegionToAttack = defenderRegion;
        nextMove.bestRegionToAttackFrom = attackerRegion;

        return nextMove;

    }
}

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------Full Game Class and Functions
//----------------------------------------------------------------------------------------------------------------------


class RiskGame {

   static Region board[][];

    //--------------------------------------------------------------------------------

    void initializeGameBoard(int m, int n)
    {
        this.board = new Region[m][n];
    }

    //--------------------------------------------------------------------------------

    void setCommandersRandomOnBoard(Player[] players, int AIPlayers, int humanPlayers) // method 1 for Alireza
    {
        int playersListCounter = 0;
        Random rand = new Random();
        int i = 0;
        while (i < board.length * board[0].length)
        {
            if (playersListCounter == players.length)
                playersListCounter = 0;

            int randomRow = rand.nextInt(10);
            int randomCol = rand.nextInt(10);

            if (board[randomRow][randomCol] != null)  // if it was full..
                continue;
            else
            {
                board[randomRow][randomCol] = new Region(randomRow, randomCol, players[playersListCounter]);
            }
            playersListCounter++;
            i++;
        }
    }

    //--------------------------------------------------------------------------------

    void GameControl()
    {
        Scanner scan = new Scanner(System.in);

        Player[] PlayersList;
        int humanPlayers, AIPlayers;
        int turn = 0;

        boolean gameOnGoing = true;
        boolean initializeGame = true;

        initializeGameBoard(10, 10);

        System.out.println("Enter the number of Human Players");
        humanPlayers = scan.nextInt();

        System.out.println("Enter the number of AI Players:");
        AIPlayers = scan.nextInt();

        PlayersList = new Player[AIPlayers + humanPlayers];

        for (int i = 0; i < humanPlayers; i++)
        {
            System.out.println("Enter human number " + (i + 1) + "'s name:");
            String name = scan.next();
            PlayersList[i] = new HumanPlayer(name);
        }

        for (int j = humanPlayers; j < AIPlayers + humanPlayers; j++)
        {
            PlayersList[j] = new AIPlayer(AIPlayer.pickRandomName());
        }

        setCommandersRandomOnBoard(PlayersList,AIPlayers,humanPlayers);

        for (int i = 0; i < board.length; i++)
        {
            for (int j = 0; j < board[i].length ; j++)
            {
                board[i][j].setAdjs(board);
            }
        }

        printBoard();


        //##################################################################################################### startLoop
        //#####################################################################################################


        int [] numberOfUnitsForPlayer = new int[PlayersList.length];

        for (int i = 0; i < numberOfUnitsForPlayer.length; i++)
        {
            numberOfUnitsForPlayer[i] = PlayersList[i].ownedRegions.size();
        }

        //int [] numberOfUnitsForPlayer = {3 , 3};

        while(initializeGame)
        {
            if (turn == PlayersList.length)
            {
                turn = 0;
            }

            if (PlayersList[turn] instanceof HumanPlayer)
            {

                System.out.println(PlayersList[turn].name + "'s Supply Turn");

                System.out.println("Distribute your troops in your Regions : ");

                System.out.println("Choose a Region with X and Y : ( " + numberOfUnitsForPlayer[turn] + " Troops left to distribute )");

                    int x = scan.nextInt();
                    int y = scan.nextInt();

                    while(board[x][y].owner != PlayersList[turn]){
                        System.out.println("you can not add unit in this region enter new coordinates.");
                        x = scan.nextInt();
                        y = scan.nextInt();
                    }

                        board[x][y].unitsInRegion++;
                        printBoard();
                        numberOfUnitsForPlayer[turn]--;

             }


            if (PlayersList[turn] instanceof AIPlayer)
            {
                System.out.println(PlayersList[turn].name + "'s Supply Turn");

                Region selectedRegion;

                 selectedRegion = ((AIPlayer) PlayersList[turn]).bestMoveReinforceSelect();

                    System.out.println();
                    System.out.println(PlayersList[turn].name + " has chosen a Region (" + selectedRegion.posX + "," + selectedRegion.posY + ")" + "( " + numberOfUnitsForPlayer[turn] + " Troops left to distribute )");
                    System.out.println();
                    int x = selectedRegion.posX; //selected by AI
                    int y = selectedRegion.posY;

                    board[x][y].unitsInRegion++;
                    numberOfUnitsForPlayer[turn]--;
                    printBoard();

             }

            if (numberOfUnitsForPlayer[PlayersList.length - 1] == 0)
            {
                initializeGame = false;
            }
            turn++;
        }

        turn = 0;

        //############################################################################################################## GameLoop
        //##############################################################################################################

        while (gameOnGoing)
        {

            if (turn == PlayersList.length)
            {
                turn = 0;
            }

            //########################################################################################################## reinforce

            //--------------------------------------------------------------------------------------------------------  human reinforce

            if (PlayersList[turn] instanceof HumanPlayer)
            {
                int numberOfUnitsToAdd = PlayersList[turn].ownedRegions.size();
                int counter = 0;
                System.out.println(PlayersList[turn].name + "'s Reinforce Turn");

                System.out.println("Distribute your troops in your Regions : ");
                //0numberOfUnitsToAdd = 1;
                while(counter < numberOfUnitsToAdd)
                {
                    System.out.println("Choose a Region with X and Y : ( " + (numberOfUnitsToAdd - counter) + " Troops left to distribute )");
                    int x = scan.nextInt();
                    int y = scan.nextInt();

                    if (PlayersList[turn].name.equals(board[x][y].owner.name))
                    {
                        board[x][y].unitsInRegion ++;
                        counter++;
                        printBoard();
                    }
                    else
                    {
                        System.out.println("Invalid Region , Choose one of your own !");
                        continue;
                    }
                }
            }

            //--------------------------------------------------------------------------------------------------------- AI reinforce

            else if (PlayersList[turn] instanceof AIPlayer) {
                int numberOfUnitsToAdd = PlayersList[turn].ownedRegions.size();
                int counter = 0;
                System.out.println(PlayersList[turn].name + "'s Reinforce Turn");

                Region selectedRegion;

                while (counter < numberOfUnitsToAdd) {

                    selectedRegion = ((AIPlayer) PlayersList[turn]).bestMoveReinforceSelect();

                    System.out.println();
                    System.out.println(PlayersList[turn].name + " has chosen a Region (" + selectedRegion.posX + "," + selectedRegion.posY + ")" + "( " + (numberOfUnitsToAdd - counter) + " Troops left to distribute )");
                    System.out.println();

                    int x = selectedRegion.posX; //selected by AI
                    int y = selectedRegion.posY;

                        board[x][y].unitsInRegion++;
                        counter++;
                        printBoard();

                }
            }

            //########################################################################################################## attack

            //--------------------------------------------------------------------------------------------------------- Human attack

                int chosenDefenderID;
                boolean continueAttack = true;

                if (PlayersList[turn] instanceof HumanPlayer) //############### human attack
                {
                    Region attacker;
                    System.out.println(PlayersList[turn].name + "'s Attack Turn");
                    while (continueAttack)
                    {

            //-----------------------------------------------Enter the place you want to attack from

                       while (true)
                       {
                           System.out.println("Enter The Region you want to Attack from with X and Y : ");
                           int x = scan.nextInt();
                           int y = scan.nextInt();
                           if (PlayersList[turn].name.equals(board[x][y].owner.name))
                           {
                               if (board[x][y].haveEnemyAdjs())
                               {
                                   if (board[x][y].unitsInRegion > 1)
                                   {
                                       attacker = board[x][y];
                                       break;
                                   }
                                   else
                                       System.out.println("Region Troop count is 1 , You can't attack from here !");
                               }
                               else
                                   System.out.println("The Region you are choosing doesn't have any Enemies");
                           }
                           else
                               System.out.println("Choose your own region!");
                       }
                        System.out.println("Attacker Name : " + attacker.owner.name + " | Number of Troops : " + attacker.unitsInRegion);


                       //---------------------------------------Search the proper adjacents of the current attack region

                        for (int i = 0; i < attacker.adjRegions.size(); i++)
                        {
                            System.out.println(i + ") Defender Name : " + attacker.adjRegions.get(i).owner.name + " | Number of Troops : " + attacker.adjRegions.get(i).unitsInRegion + " | (X , Y) = (" + attacker.adjRegions.get(i).posX + "," +  attacker.adjRegions.get(i).posY + ")" );
                        }

                        while (true)
                        {
                            System.out.println("Choose one Enemy Region to attack: ");
                            chosenDefenderID = scan.nextInt();

                            if (attacker.owner.name.equals(attacker.adjRegions.get(chosenDefenderID).owner.name))
                                System.out.println("You can't attack your own regions!");
                            else
                                break;
                        }

                        //-------------------------------------choose number of units you want to attack with and attack

                        System.out.println("How many troops do you want to attack with from this Region to the other one ?");
                        while(true)
                        {
                            attacker.unitsToUseInBattle = scan.nextInt();
                            if(attacker.unitsToUseInBattle < attacker.unitsInRegion && attacker.unitsToUseInBattle > 0)
                            {
                                attacker.unitsInRegion = attacker.unitsInRegion - attacker.unitsToUseInBattle;
                                break;
                            }
                            else
                                System.out.println("Invalid Number , type a number between (numberOfTroopsInRegion - 1) and (0)");

                        }

                        //################################################################################# Battle

                        attacker.owner.Battle(attacker, attacker.adjRegions.get(chosenDefenderID));

                        //################################################################################# update board

                        printBoard();

                        //----------------------------------------------------------Continue or Not to

                        while(true)
                        {
                            System.out.println("Continue Attacking ? (Yes/No)");
                            String YesOrNo = scan.next();
                            if (YesOrNo.equals("Yes"))
                            {
                                continueAttack = true;
                                break;
                            }
                            else if (YesOrNo.equals("No"))
                            {
                                continueAttack = false;
                                break;
                            }
                            else
                                System.out.println("Invalid Input write 'Yes' or 'No' to continue");
                        }

                     }

                }

                //----------------------------------------------------------------------------------------- AI attack

                else if (PlayersList[turn] instanceof AIPlayer)
                //if (PlayersList[turn] instanceof AIPlayer)
                {
                    System.out.println(PlayersList[turn].name + "'s Attack Turn");

                    Move bestMove;

                    while(true)
                    {
                        bestMove = ((AIPlayer) PlayersList[turn]).bestMoveAttackSelect();

                        if (bestMove.bestRegionToAttackFrom == null)
                            break;

                        bestMove.bestRegionToAttackFrom.unitsToUseInBattle = bestMove.bestRegionToAttackFrom.unitsInRegion - 1;
                        bestMove.bestRegionToAttackFrom.unitsInRegion = 1;

                        System.out.println("AI " + PlayersList[turn].name + " has chosen Region (" + bestMove.bestRegionToAttackFrom.posX + "," + bestMove.bestRegionToAttackFrom.posY + ") to start attack from");
                        System.out.println("AI " + PlayersList[turn].name + " has chosen Region (" + bestMove.bestRegionToAttack.posX + "," + bestMove.bestRegionToAttack.posY + ") to attack");

                        //################################################################################# Battle

                        bestMove.bestRegionToAttackFrom.owner.Battle(bestMove.bestRegionToAttackFrom, bestMove.bestRegionToAttack);

                        printBoard(); //############################################# update board
                    }
                }

            if (PlayersList[turn].ownedRegions.size() == 10 * 10)   // must be m*n. //----------------------------------------------------End Point
            {
                System.out.println(PlayersList[turn].name + " Won !");
                System.out.println("The Game has Ended !");
                break;
            }

                turn++; //############################################# turn counter



        }
    }

    //-------------------------------------------------------------------------------------------------------------

    void printBoard() {

        System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        for (int i = 0; i < board.length; i++)
        {
            System.out.print("| ");
            for (int j = 0; j < board[i].length; j++)
            {

                System.out.print(board[i][j] + " | ");
            }
            System.out.println();
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------Region and Territory Class
//----------------------------------------------------------------------------------------------------------------------

class Region {

    int posX;
    int posY;

    String pattern = "-";
    Player owner;
    int unitsInRegion;
    int unitsToUseInBattle = 0;

    ArrayList<Region> adjRegions;

    //-------------------------

    Region(int x, int y, Player player)
    {
        this.posX = x;
        this.posY = y;

        this.owner = player;

        unitsInRegion = 1;

        adjRegions = new ArrayList<Region>();
        player.ownedRegions.add(this);
    }

    boolean haveEnemyAdjs()
    {
        for (int i = 0; i < adjRegions.size() ; i++) {
            if(!adjRegions.get(i).owner.equals(this.owner))
                return true;
        }
        return false;
    }

    void setAdjs(Region[][] board) {

        if (posY < board[this.posX].length - 1)
        {
            adjRegions.add(board[this.posX][this.posY + 1]);
        }

        if (posX < board.length - 1)
        {
            adjRegions.add(board[this.posX + 1][this.posY]);
        }

        if (posY > 0)
        {
            adjRegions.add(board[this.posX][this.posY - 1]);
        }

        if (posX > 0)
        {
            adjRegions.add(board[this.posX - 1][this.posY]);

        }

    }

    //-------------------------

    @Override
    public String toString() {

        return owner.color + owner.name + "( " + this.unitsInRegion + " )" + ConsoleColors.RESET;
    }
}

//----------------------------------------------------------------------------------------------------------------------Probability
//----------------------------------------------------------------------------------------------------------------------

class Probability
{
    private static double winProb[][]=
    {
            {0.417,0.106,0.027,0.007,0.002,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000},
            {0.754,0.363,0.206,0.091,0.049,0.021,0.011,0.005,0.003,0.001,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000},
            {0.916,0.656,0.470,0.315,0.206,0.134,0.084,0.054,0.033,0.021,0.010,0.010,0.000,0.000,0.000,0.000,0.000,0.000,0.000,0.000},
            {0.972,0.785,0.642,0.477,0.359,0.253,0.181,0.123,0.086,0.057,0.040,0.030,0.020,0.010,0.010,0.000,0.000,0.000,0.000,0.000},
            {0.990,0.890,0.769,0.638,0.506,0.397,0.297,0.224,0.162,0.118,0.080,0.060,0.040,0.030,0.020,0.010,0.010,0.010,0.000,0.000},
            {0.997,0.934,0.857,0.745,0.638,0.521,0.423,0.329,0.258,0.193,0.150,0.110,0.080,0.060,0.040,0.030,0.020,0.010,0.010,0.010},
            {0.999,0.967,0.910,0.834,0.736,0.640,0.536,0.446,0.357,0.287,0.220,0.170,0.130,0.100,0.070,0.050,0.040,0.030,0.020,0.020},
            {1.000,0.980,0.947,0.888,0.818,0.730,0.643,0.547,0.464,0.380,0.310,0.250,0.200,0.150,0.120,0.090,0.070,0.050,0.040,0.030},
            {1.000,0.990,0.967,0.930,0.873,0.808,0.726,0.646,0.558,0.480,0.400,0.330,0.270,0.220,0.170,0.140,0.110,0.080,0.060,0.050},
            {1.000,0.994,0.981,0.954,0.916,0.861,0.800,0.724,0.650,0.568,0.490,0.420,0.350,0.290,0.240,0.190,0.160,0.120,0.100,0.070},
            {1.000,1.000,0.990,0.970,0.940,0.910,0.850,0.790,0.720,0.650,0.580,0.510,0.430,0.370,0.310,0.260,0.210,0.170,0.140,0.110},
            {1.000,1.000,0.990,0.980,0.960,0.930,0.900,0.840,0.790,0.720,0.660,0.580,0.520,0.450,0.390,0.330,0.280,0.230,0.190,0.150},
            {1.000,1.000,1.000,0.990,0.980,0.960,0.930,0.890,0.840,0.790,0.720,0.660,0.590,0.530,0.460,0.400,0.340,0.290,0.240,0.200},
            {1.000,1.000,1.000,0.990,0.980,0.970,0.950,0.920,0.880,0.830,0.780,0.720,0.670,0.600,0.540,0.470,0.420,0.360,0.310,0.260},
            {1.000,1.000,1.000,1.000,0.990,0.980,0.960,0.940,0.910,0.880,0.830,0.780,0.730,0.670,0.610,0.550,0.480,0.430,0.370,0.320},
            {1.000,1.000,1.000,1.000,0.990,0.990,0.980,0.960,0.940,0.910,0.870,0.830,0.780,0.730,0.670,0.610,0.560,0.490,0.440,0.380},
            {1.000,1.000,1.000,1.000,1.000,0.990,0.980,0.970,0.950,0.930,0.900,0.870,0.820,0.780,0.730,0.680,0.620,0.570,0.500,0.450},
            {1.000,1.000,1.000,1.000,1.000,0.990,0.990,0.780,0.970,0.950,0.930,0.900,0.870,0.820,0.780,0.730,0.680,0.620,0.570,0.510},
            {1.000,1.000,1.000,1.000,1.000,1.000,0.990,0.990,0.980,0.970,0.950,0.920,0.890,0.860,0.820,0.780,0.730,0.680,0.630,0.580},
            {1.000,1.000,1.000,1.000,1.000,1.000,1.000,0.990,0.980,0.970,0.960,0.940,0.920,0.890,0.860,0.820,0.780,0.730,0.690,0.630},
            {1.000,1.000,1.000,1.000,1.000,1.000,1.000,0.990,0.990,0.980,0.970,0.960,0.940,0.920,0.890,0.860,0.820,0.780,0.730,0.690},
            {1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,0.990,0.990,0.980,0.970,0.960,0.940,0.920,0.890,0.860,0.820,0.780,0.740},
            {1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,0.990,0.990,0.980,0.970,0.950,0.930,0.910,0.880,0.860,0.820,0.780},
            {1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,0.990,0.990,0.980,0.980,0.960,0.950,0.930,0.910,0.880,0.850,0.820},
            {1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,1.000,0.990,0.990,0.980,0.970,0.960,0.950,0.930,0.910,0.880,0.850}
    };

    static double getProbabilty(Region attacker, Region defender) //max 25*25
    {
        return winProb[attacker.unitsInRegion][defender.unitsInRegion];
    }

    static int getProbabilty2(Region attacker, Region defender){

        return attacker.unitsInRegion - defender.unitsInRegion;
    }
}


//----------------------------------------------------------------------------------------------------------------------Color Codes
//----------------------------------------------------------------------------------------------------------------------

 class ConsoleColors {
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String RED_BRIGHT = "\033[0;91m";    // RED
    public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
}

//----------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------Main
//----------------------------------------------------------------------------------------------------------------------

public class Main
{
    public static void main(String[] args)
    {
        RiskGame rg = new RiskGame();
        rg.GameControl();
        // ;)
    }
}
