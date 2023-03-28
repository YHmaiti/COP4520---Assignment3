# COP4520---Assignment3
# Problem 1:
*To answer the following: What could have gone wrong? Can we help the Minotaur and his servants improve their strategy for writing “Thank you” notes? Design and implement a concurrent linked-list that can help the Minotaur’s 4 servants with this task.* 

-> What could have gone wrong is issues with synchronization between the servants that add presents and the ones that remove them for the current action cycle. Fpr example, servant A could attempt to add B after A, but if another servant attempts a removal of A, then a synchronization and communication issue between threads occurs, since now adding B doesn't have the right prev based on the initialized addition action. Also, another potential issue that could have happened is that both adding and removing can happen at the same time, such that servant 1 can attempt to add present A, and another servant let's say servant 3 can try and remove that same present A, while it's chain addition was not completed yet to be fail safe. Of course several issues in this sense could have occured also, and yes, we can help by making a way better and more fail safe strategy as will be discussed through the remarks and proof of correcteness/efficiency/evaluation sections. 


*Remarks and Notes:*

-> Based on the instructions we have 4 servants (thus, 4 threads are used). Additionally, we have a number of presents that is equal to 500,000 that were received by the Minotaur. Consequently, these values were used directly as is for the number of threads and the amount of presents. However, feel free to change them directly if needed for more testing/exploration.

-> The book and class slides (chapter 9) were used to gain more knowledge and implement a lock free list that was used in my solution. 

-> Efficient use of atomics was achieved as we used them to communicate adding from the unorganized bag to the chain, and also another to communicate taking from the chain (removal) to write a thank you letter for the guest.

-> When the miotaur asked to check if a present is on the chain or not we used the contain method for the chain (lock free list), and we passed as a parametter the adding tracker, the reason behind this is that it is still random as threads will update it's value/will still work on the chain, and additionally it will trigger a mix of found and not found responses, plus I wanted to check for recently processed presents from the bag as that makes much more sense for the Minotaur to ask about.

-> To Compile and Run: 
```
javac BirthdayPresentsParty.java 
java BirthdayPresentsParty 
``` 

**Proof Of Correctness and Efficiency:**

For the design of my solution, I have relied on using a lock free list based on the chapter 9 in the book, class slides and also online resources. Additionally, in this implementation, I have relied on using atomics, including the use of Atomic Markable References (https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/AtomicMarkableReference.html)(https://www.baeldung.com/java-atomicmarkablereference). The use of this type of atomic allows not only to have access to the node itself and its content, but also to mark it in case a thread is doing something with it, so other threads would know. Additionally, I tried to come up with a strategy where the threads will not wait to remove from the chain untill all bag presents were present in the chain. Thus, besides using the atomic markable references in my node and lock free list implementation, I also relied on using three trackers. These trackers were three atomic variables, one for tracking the addition from the unorganized bag to the chain, one for tracking the removal from the chain, and one for tracking the writing of thank you cards to the guests. This strategy allows threads to communicate efficiently and handle the potential edge cases as is needed. Moreover, to handle the Moinotaur's random presents check in the chain,  

**Experimental Evaluation and Efficiency:**

# Problem 2:

*Remarks and Notes:*

**Proof Of Correctness and Efficiency:**

**Experimental Evaluation and Efficiency:**
