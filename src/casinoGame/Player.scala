package casinoGame

import scala.collection.mutable.Buffer

/**
 * Player class represents players in casino game
 */
abstract class Player(val name: String) {
  
  private var points = 0
  
  //cards which player is holding in his hand
  private var cards = Buffer[Card]()
  
  //cars which player has collected from table
  private var collectedCards = Buffer[Card]()
  private var amountOfSpades = 0
  var isTurn = false
  var isDealer = false
  
  //Boolean var indicating whether player took last card or not 
  //with this var it is determined which player will be given last cards from table
  var lastToTakeCards = false
  
  
  def getPoints: Int = this.points
  
  def getSpades = this.amountOfSpades
  
  def addPoints(points: Int) : Unit =  this.points += points
  
  def addCard(card: Card) : Unit = this.cards += card
  
  def takeCard(card: Card) : Unit = if(this.cards.contains(card)) this.cards -= card
  
  def getCards : Buffer[Card] = this.cards
  
  def addtoCollection(card: Card) : Unit = this.collectedCards += card
  
  def getCollection : Buffer[Card] = this.collectedCards
  
  def removeCardsAndAddToCollectin(cardsInArray : Array[String]) = {
    for(x <- cardsInArray){
      this.addtoCollection(this.removeCard(x))
    }
  }
  
  /**
   * Takes cards from players hand and adds them to players collection of cards
   * @param cardsInString cards which are wanted to take from hand in string
   * for example "2S 10D"
   * @return Unit
   */
  def addToCollectionFromString(cardsInString: String) : Unit = {
    
    var cardsInArray = cardsInString.split(" ")
    var holder = Buffer[Card]()
    
    for(x <- this.cards){
      for(y <- cardsInArray)
        if(x.name == y) holder += x       
    }
    
    for(x <- holder){
      this.addtoCollection(x)
      this.takeCard(x)
    }
  }
  
  /**
   * 
   
  @param card assumes card is a string containing cards that player might have
  @return the sum of the hand values of the cards
  * 
  */
  def getHandValueOfCards(card: String) : Int = {
    
    var holder = Buffer[Card]()
    var cardsInArray = card.split(" ")
    
    for(x <- this.cards){
      for(y <- cardsInArray){
        if(x.name == y) holder += x
      }
    }
    var count = 0
    
    for(x <- holder){
      count += x.inHandValue
    }
    count
  }

  def containsCard(cardText: String) : Boolean =  !cards.forall(_.name != cardText)
  
  def removeCardsAndSpades : Unit = {
    this.amountOfSpades = 0 
    this.collectedCards = Buffer[Card]()
    this.cards = Buffer[Card]()
  }
  
  def countPointsAndSpades : Int = {
    
    for(x <- this.collectedCards){
      if(x.inHandValue == 14) this.addPoints(1)
      else if(x.inHandValue == 16) this.addPoints(2)
      else if(x.inHandValue == 15 && x.name.contains("S")) this.addPoints(1)
      
      if(x.name.contains("S")) this.amountOfSpades += 1
    }
    this.points
  }
  
  /**
   * Removes card from players' cards and returns it, assumes
   * that wanted card exists in players' cards
   * @param cardText card's name
   * @return Removed card
   */
  def removeCard(cardText: String) : Card = {
    
    var toBeRemovedIndex = 0
    
    for(x <-0 until cards.length){
      if(cards(x).name == cardText){
        toBeRemovedIndex = x
      }
    }
    this.cards.remove(toBeRemovedIndex)
  }
  
   def takeFromTable(cardsInString: String, table: Table ) : Unit = {
    
    val cardsInArray = cardsInString.split(" ")
   
    for(x <- cardsInArray){
      this.addtoCollection(table.removeCard(x))
    }
    if(table.getCards.isEmpty) this.addPoints(1)
    
  }
   
   
  
}