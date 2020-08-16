
# Get-Rich V2.0.0

Get-Rich V2.0.0 was created to boost the depressed economy and help myself get richer:laughing:
It is a highly sophisticated lottery number generator based on real and live quantum numbers retrieved from [ANU Quantum Random Numbers Server](http://qrng.anu.edu.au/). 


## Features and Highlights
* Get-Rich V2.0.0 simulates lotto number selection based on real random quantum numbers, say good-bye to pseudo random numbers!
* Algorithms to rank the 'quality' of selected numbers based on most/least profitable numbers published by [OzLotteries](https://www.ozlotteries.com)
* Enable the black-listing feature if you don't want to see the numbers you don't like.
* Two selection algorithms are supported, see [DrawingAlgorithm](https://github.com/adventure-island/get-rich/blob/master/src/main/java/com/smartj/getrich/quantum/DrawingAlgorithm.java) and [QuantumRandomGenerator](https://github.com/adventure-island/get-rich/blob/master/src/main/java/com/smartj/getrich/quantum/QuantumRandomGenerator.java)  for details.
* Currently 3 types of games are supported, see [GameType](https://github.com/adventure-island/get-rich/blob/master/src/main/java/com/smartj/getrich/quantum/GameType.java) for details.
    * XLOTTO
    * OZLOTTO
    * POWERBALL

## How to play?
Get-Rich V2.0.0 is a Spring Boot app, simply specify the type of game you want to play then run this file: [LabManager](https://github.com/adventure-island/get-rich/blob/master/src/main/java/com/smartj/getrich/main/LabManager.java). Also feel free to play with the configurations defined in  [GameType](https://github.com/adventure-island/get-rich/blob/master/src/main/java/com/smartj/getrich/quantum/GameType.java).

## Sample Output

    Printing final result
    Result row[0]: RandomRecord(count=6, results=[3, 13, 16, 31, 40, 43], rank=3)
    Result row[1]: RandomRecord(count=6, results=[3, 13, 17, 27, 36, 40], rank=2)
    Result row[2]: RandomRecord(count=6, results=[3, 5, 13, 19, 27, 37], rank=2)
    Result row[3]: RandomRecord(count=6, results=[4, 7, 17, 27, 28, 37], rank=2)
    Result row[4]: RandomRecord(count=6, results=[3, 11, 29, 31, 40, 42], rank=2)

## Warnings and Notes
* Please gamble responsibly! The author of this application is not responsible for any financial loss caused by using this application.
* If you win, please contribute back to the open source community and help more devs get rich!