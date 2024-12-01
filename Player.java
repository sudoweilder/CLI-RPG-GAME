import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Player extends Entity{
    
    public static final int PLAYER_LINES = 4;

    // general data variables
    public int savePoint = 0;
    private int index = -1;
    String name = "you";
    int health = 500;

    // abilities variables
    Inventory inventory = new Inventory();
    public String[] abilities = {"Peashooter", "Wand", "Sword", null, null, null, null};// null slots added to expand skills
    public int peashooterAmmo = 20;
    
    
    // This function prompts the user to enter a number that corresponds to an attack printed in the terminal and then uses that
    // input to determine an attack.
    public void attack(Enemy enemy, Random random, Scanner console){
	int choice = -1;
	System.out.println("Here are your abilities:\n");
	
	int moveCounter = 0;

	while (moveCounter < abilities.length && !(abilities[moveCounter] == null)){
	    System.out.println((moveCounter + 1) + "." + abilities[moveCounter]);
	    moveCounter++;
	}
	
	System.out.println("\nPeashooter ammo: " + peashooterAmmo);
	System.out.print("Enter the number in front of the attack you want to use: ");
	
	choice = General.getInt(console, 1, moveCounter) - 1;

	if (abilities[choice].startsWith("Peashooter")) peashooter(enemy, random);
	else if (abilities[choice].startsWith("Wand")) wand(enemy, random);
	else if (abilities[choice].startsWith("Sword")) sword(enemy);	
	
	// servant attack
	servants.attack(random, enemy, console);
    }



/*------------------------------------- Below are weapons functions for the player.------------------------------------------------*/
// weapons are called in the attack method


    // The peashooter function uses a random object to decude whether the player will or won't hit the enemy. 
    public void peashooter(Enemy enemy, Random random){
    	int damage = 45;
	int hitNumber = random.nextInt(100);
	
	if (peashooterAmmo > 0){
	    if (hitNumber < 50){
		System.out.println("You dealt " + damage + " damage to" + enemy.name);
		enemy.health -= damage;
	    } else System.out.println("You missed.");

	    peashooterAmmo --;
	} else System.out.println("You're out of ammunition");//possibly add this to the player attack function.
    }


    // The wand function adds a random negative effect to the enemy.
    public void wand (Enemy enemy, Random random){ // if we have time, it may be intersting to add more effects, like mind control
	int decider = random.nextInt(1,101);
	int baseDamage = 25;
	if (decider <= 30){ // bonus damage
	    System.out.println("You dealt 90 damage to the " + enemy.name);
	    enemy.health -= 90;
	} else if (decider <= 66){// burn attack
	    int burnCounter = decider - 10;
	    System.out.println("You dealt " + baseDamage + " damage and burnt the " + enemy.name + ".");
	    enemy.burn += burnCounter;
	    enemy.health -= baseDamage;
	} else { // poison attack
	    int poisonCounter = decider - 66;
	    System.out.println("You dealt " + baseDamage + "damage and poisoned the " + enemy.name + ".");
	    enemy.poison += poisonCounter;
	    enemy.health -= baseDamage;
	}
    }
    
    public void sword (Enemy enemy){ //possibly add something interesting to this part of the code?
	int damage = 35;
	System.out.println("You dealt " + damage + " damage to the " + enemy.name);
	enemy.health -= damage;
    }


/*-----------------------------------------Here are the effects functions for the player-------------------------------------------*/
    // This function processes the effects variables.
    public void dealEffects(){
	int damage;
	if (burn > 0){
	    damage = 3;
	    burn --;
	    health -= damage;
	    System.out.println("You have been burnt for " + damage + " damage.");
	}
	if (poison > 0){
	    damage = poison * 5;
	    poison --;
	    health -= damage;
	    System.out.println("The poison in your body dealt " + damage + " damage to you.");
	}
	if (regeneration > 0){
	    int bonusHealth = 5 * regeneration;
	    regeneration --;
	    health += bonusHealth;
	    System.out.println(name + " regenerated " + bonusHealth + " health.");
	}

	servants.dealEffects();
	    
    } 
/*----------These functions are the ones that end the game once a player dies, wants to exit at a savepoint, or wins---------------*/

    public void kill(Enemy enemy) throws FileNotFoundException{
	File dragonServants = new File("DragonServants.txt");
	Scanner servantReader = new Scanner(dragonServants);

	System.out.println("You were slain by " + enemy.name + ".");
	System.out.println("As you die, you feel the Elder Dragon's power turning you into a part of the dungeon.");
	// printDeathImage(enemy.type) will come later

	String[] dragonServantLines = new String[100];
	
	// puts current information in the file in the array
	int i = 0;
	while (servantReader.hasNextLine()){
	    if (i == dragonServantLines.length){
		dragonServantLines = Arrays.copyOf(dragonServantLines, dragonServantLines.length + 100);
	    }
	    dragonServantLines[i] = servantReader.nextLine();
	    i++;
	}

	// adds player data to DragonServants.txt
	dragonServantLines[i] = name + " " + enemy.type;

	PrintStream servantPrinter = new PrintStream(dragonServants);
	
	for (int j = 0; j <= i; j++){
	    servantPrinter.println(dragonServantLines[j]);
	}

	System.exit(0);
    }
    
    // This method updates the savePoint variable, saves the player information to the Players.txt file, and prompts the player on 
    // whether he or she wants to exit the game. 
    public void save(int savePoint, Scanner console) throws FileNotFoundException, InterruptedException {
	File playerFile = new File("Players.txt");
	Scanner playerReader = new Scanner(playerFile);
	
	this.savePoint = savePoint; // sets savePoint to the correct value
	
	// the next few lines write the player information to the PLAYERS.txt file. 
	String[][] playerData = new String[PLAYER_LINES][10000];

	int playerIndex = 0;
	while (playerReader.hasNextLine()){
	    for (int i = 0; i < PLAYER_LINES; i++){
		playerData[i][playerIndex] = playerReader.nextLine();
	    }
	    playerIndex++;
	}


	if (index == -1) index = playerIndex; // prevents the player from being written twice in the same file or overwriting data
	// stores general data line in the array
	playerData[0][index] = name + " " + health + " " + savePoint + " " + poison + " " + burn + " " + peashooterAmmo;
	
	// stores the abilities line in the array
	playerData[1][index] = abilities[0] + " ";
	
	boolean atEndOfAbilities = false;
	int abilitiesIndex = 1;
	while (!atEndOfAbilities){
	    playerData[1][index] += abilities[abilitiesIndex] + " ";
	    abilitiesIndex ++;
	    atEndOfAbilities = abilitiesIndex == abilities.length || abilities[abilitiesIndex] == null;
	}
	
	// stores the items in an array
	playerData[2][index] = "";
	for (int i = 0; i < inventory.size; i++){
	    String item = inventory.items[i];
	    if (item.indexOf(' ') != -1) item.replace(" ", "_");
	    playerData[2][index] += item + " ";
	}

	playerData[3][index] = "";
	for (int i = 0; i < servants.servants.length; i++){
	    Enemy servant = servants.servants[i];
	    String type = servant.type;
	    int health = servant.health;
	    int poison = servant.poison;
	    int burn = servant.burn;
	    int regeneration = servant.regeneration;
	    playerData[3][index] += type + " " + health +  " " + poison + " " + burn + " " + regeneration + " ";
	}

	PrintStream playerWriter = new PrintStream(playerFile);
	// Writes everything to the file
	for (int i = 0; i <= playerIndex; i++){
	    for (int j = 0; j < PLAYER_LINES; j++){
		playerWriter.println(playerData[j][i]);
	    }
	    if (playerData[0][i + 1] == null) i = playerIndex + 1;
	}
	
	// sees if user wants to exit the game
	System.out.print("You have reached a savepoint. Do you want to continue? (y/n): ");
	
	char response = console.next().toLowerCase().charAt(0);
	
	while (response != 'y' && response != 'n'){
	    System.out.print("Please type \"y\" or \"n\": ");
	    response = console.next().toLowerCase().charAt(0);
	}

	if (response == 'n') {
	    System.out.println("You are exiting the game.\n");
	    System.exit(0);
	} else {
	    System.out.println("You have chosen to continue...\n");
	    Thread.sleep(5000);
	}
    }


/*----------------------------Constructor for player object and necessary functions--KEEP AT END-----------------------------------*/
    private void newPlayer(Scanner console){
	System.out.print("Enter the name you want your character to have: ");
	name = console.nextLine();
	
	// input verification
	while (name.indexOf(" ") != -1){
	    System.out.print("Your name must have no spaces. Please try again: ");
	    name = console.nextLine();
	}
    }

    public Player(Scanner console, Random random) throws FileNotFoundException{
	File playerFile = new File("Players.txt");
	Scanner playerData = new Scanner(playerFile);
	String[][] playerInfo = new String[PLAYER_LINES][10000]; // 10000 is present because it is an impossibly high number that 
								 // nobody is likely to hit
	// prints welcome ASCII art
	General.printText("Printable_Text.txt", 0);
	
	// checks to see if data is in file. If so, then the program prompts the user on whether he or she wants to pull up a profile
	if (playerData.hasNext()){
	    System.out.print("Do you have a profile on this computer? (y/n): ");
	    char choice = (console.next()).charAt(0);
	    console.nextLine();
	    
	    // what happens if a player wants to pull a character out of the player file.
	    if (choice == 'y'){
		System.out.println("Here are the stored characters:\n");
		int playerCounter = 0; // counts how many players are stored in the file

		// lists the names of all players in the file
		while (playerData.hasNextLine()){

		    for (int i = 0; i < PLAYER_LINES; i++){
			playerInfo[i][playerCounter] = playerData.nextLine();
		    }

		    String name = playerInfo[0][playerCounter].substring(0, playerInfo[0][playerCounter].indexOf(' '));
		    playerCounter++;
		    System.out.println(playerCounter + ". " + name);
		}
		
		System.out.println();
		System.out.print("Select the number for the character you want to play: ");
		
		int selection = General.getInt(console, 1, playerCounter) - 1;

		// general information is assigned assigned to the object in the lines below
		playerData = new Scanner(playerInfo[0][selection]);
		name = playerData.next();
		health = playerData.nextInt();
		savePoint = playerData.nextInt();
		poison = playerData.nextInt();
		burn = playerData.nextInt();
		peashooterAmmo = playerData.nextInt();
		index = selection - 1;
		    
		// assigns abilities based on what is in the file. assertion made that the abilities array has enough feilds.
		Scanner abilitiesData = new Scanner(playerInfo[1][selection]);
		int abilitiesCounter = 0;
		while (abilitiesData.hasNext()){
		    abilities[abilitiesCounter] = abilitiesData.next();
		    abilitiesCounter++;
		}

		Scanner items = new Scanner(playerInfo[2][selection]);
		while (items.hasNext()){
		    String newItem = items.next();
		    if (newItem.indexOf('_') != -1){
			newItem.replace("_", " ");
		    }
		    inventory.addItem(newItem, console, this, random);
		}
			
		Scanner servants = new Scanner(playerInfo[3][selection]);
		while (servants.hasNext()){
		    String newType = servants.next();
		    String newElement = null;
		    if (newType.equals("dragon")) newElement = servants.next();
		    int newHealth = servants.nextInt();
		    int newPoison = servants.nextInt();
		    int newBurn = servants.nextInt();
		    this.servants.addServant(newType, null, newElement, true);
		    Enemy newServant = this.servants.servants[this.servants.servants.length - 1];
		    newServant.health = newHealth;
		    newServant.poison = newPoison;
		    newServant.burn = newBurn;
		}     
	    }else newPlayer(console);
	}else newPlayer(console);

	System.out.println("\nWelcome, " + name + ".\n");
    }
}
