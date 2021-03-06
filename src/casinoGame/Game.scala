package casinoGame

import scala.collection.mutable.Buffer
import scala.Vector

/**
 * Class game represents virtual casino game, which is controlling whole game
 * @param computerPlayers computer players in Buffer, empty if new game is initiated
 * @param human human players in Buffer, empty if new game is initiated
 * @param deck deck of the game, None if new game is initiated
 * @param table table of the game, None if new game is initiated
 * @param humanAmount amount of human players
 * @param computerAmount amount of computer players
 */
class Game(var allPlayers: Buffer[Player], var deck: Option[Deck], var table: Option[Table],humanAmount: Int, computerAmount: Int) {
  
  private var players = Vector[Player]()
  private val cardSuits = Vector("H", "S", "D", "C")
  private var deckHolder = Buffer[Card]()
  var turnCount = 0
  var dealerCount = 0
  
  
  //Constructors initialize new game by creating players and cards
  if(this.allPlayers.isEmpty){
    
    this.deck = Some(new Deck)
    this.table = Some(new Table)
    
    var nameNumber = 1
    for(x <- 0 until computerAmount){
      this.players = this.players :+ new Computer("Computer " + nameNumber)
     nameNumber += 1
    }
    
    nameNumber = 1
    for(x <- 0 until humanAmount){
      this.players = this.players :+ new Human("Human " + nameNumber)
      nameNumber += 1
    }
           
    this.players = scala.util.Random.shuffle(this.players)
    
    this.shuffleAndDeal()
  }
  
  //if game is loaded constructors doesn't have to create new cards and players
  //turnCount and dealerCount values are imported through humanAmount and computerAmount
  //variables, which are not needed when loading game.
  else{
    this.turnCount = humanAmount
    this.dealerCount = computerAmount
    this.players = this.allPlayers.toVector
  }
  
  def shuffleAndDeal(): Unit = {
    for(x <- cardSuits){
   
       for(y <- 1 to 13){
         
         var inHandValue = {
           if(y == 1) 14
           else if(y == 10 && x == "D") 16
           else if(y ==  2 && x == "S") 15
           else y
         }
         this.deckHolder += new Card(y.toString + x, inHandValue, y)
      }
    }

    this.deckHolder = scala.util.Random.shuffle(this.deckHolder)
    this.changeDealer()
    
    this.deckHolder.foreach(this.deck.get.addCards(_))
    this.deckHolder = Buffer[Card]()
    
    for(x <- this.players){
      for(y <- 1 to 4){
        if(this.deck.get.canBeTaken){
        x.addCard(this.deck.get.takeCard)
        }
      }
    }
    
    for(x <- 1 to 4){
      if(this.deck.get.canBeTaken){
      this.table.get.addCard(this.deck.get.takeCard)
      }
    }
    
    players(turnCount%this.players.size).isTurn = true
    
    }
  
  //change players turn
  def changeTurn() : Unit = {
    this.players(turnCount%this.players.size).isTurn = false
    this.turnCount += 1
    this.players(turnCount%this.players.size).isTurn = true
  }
  
  //change dealer
  def changeDealer(): Unit = {
    
    this.players(dealerCount%this.players.size).isDealer = false
    this.players(turnCount%this.players.size).isTurn = false
    this.dealerCount += 1
    this.turnCount = this.dealerCount
    this.players(dealerCount%this.players.size).isDealer = true
    this.changeTurn()
    
  }

    
  def getPlayers : Vector[Player] = this.players
  
  /**
   * human players use this function to give players on table
   * @param card card players wants to set to table
   * @return Boolean tells whether move was correct or not
   */
  def giveCard(card: String) : Boolean = {
    
    val cardInString = card.trim
    if(!this.getCurrentPlayer.getCards.forall(_.name != cardInString)){
      this.table.get.addCard(this.getCurrentPlayer.removeCard(cardInString))
      true
    }
    else false
  }
  
  /**
   * human players use this function to take cards from table
   * @param card string containing cards' names
   * @return Boolean
   */
  def takeCard(card: String) : Boolean =  {
    
    val tableCardText = card.toUpperCase.takeWhile(_ != 'F').trim
    val handCardText = card.toUpperCase.reverse.takeWhile(_ != 'R').reverse.trim
    val cardsInArray = handCardText.split(" ")
    val currentPlayer = this.getCurrentPlayer
    
    if(this.tableContainsCards(this.table.get.getCards, tableCardText) &&
       this.handContainsCards(currentPlayer.getCards, handCardText, currentPlayer) && 
       currentPlayer.getHandValueOfCards(handCardText) == this.table.get.getCardsValues(tableCardText)){
      
        currentPlayer.takeFromTable(tableCardText, this.table.get)
        currentPlayer.removeCardsAndAddToCollectin(cardsInArray)
        this.players.foreach(_.lastToTakeCards = false)
        currentPlayer.lastToTakeCards = true
        true
    }
    
    else false
  }
  
  
  def tableContainsCards(cards: Buffer[Card], cardsInString: String) : Boolean = {
    
    val cardsInArray = cardsInString.split(" ")
    
    cardsInArray.forall(this.table.get.containsCard(_))
  }
  
  def handContainsCards(cards: Buffer[Card], cardsInString: String, player: Player) : Boolean = {
    
    val cardsInArray = cardsInString.split(" ")
    
    cardsInArray.forall(player.containsCard(_))
  }
  
  def getCurrentPlayer = this.players(turnCount%players.size)
  
  def isOver : Boolean = this.players.forall(_.getCards.isEmpty) && !this.deck.get.canBeTaken
  
  /**
   * Method which makes computer players play their turn
   */
  def computerPlayerMakeMove = {
    
    if(this.deck.get.canBeTaken){
      
      this.getCurrentPlayer.addCard(this.deck.get.takeCard)
    }
   
    if(this.getCurrentPlayer.asInstanceOf[Computer].makeMove(this.table.get)) {
      
      this.players.foreach(_.lastToTakeCards = false)
      this.getCurrentPlayer.lastToTakeCards = true
    }
  }
  
  def getPlayerWhoTookLastCard : Player = this.players.find(_.lastToTakeCards).get
    
  def getWinningPlayer : Option[Player] = {
    val bestPlayer = this.players.maxBy(_.getPoints)
    if(bestPlayer.getPoints >= 16) Some(bestPlayer)
    else None
  }
 
  def giveCardsForLastPlayer(): Unit = {
    for(x <- this.table.get.getCards){
      this.getPlayerWhoTookLastCard.addtoCollection(x)
    }
    this.table.get.removeAllCards()
  }
 
  def countPointsForPlayers(): Unit = {
    this.players.foreach(_.countPointsAndSpades)
    this.players.maxBy(_.getSpades).addPoints(1)
  }
  
  
  
  
  
}