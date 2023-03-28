# COP4520---Assignment3
# Problem 1:
*To answer the following: What could have gone wrong? Can we help the Minotaur and his servants improve their strategy for writing “Thank you” notes? Design and implement a concurrent linked-list that can help the Minotaur’s 4 servants with this task.* 

-> What could have gone wrong is issues with synchronization between the servants that add presents and the ones that remove them for the current action cycle. Fpr example, servant A could attempt to add B after A, but if another servant attempts a removal of A, then a synchronization and communication issue between threads occurs, since now adding B doesn't have the right prev based on the initialized addition action. Also, another potential issue that could have happened is that both adding and removing can happen at the same time, such that servant 1 can attempt to add present A, and another servant let's say servant 3 can try and remove that same present A, while it's chain addition was not completed yet to be fail safe. Of course several issues in this sense could have occured also, and yes, we can help by making a way better and more fail safe strategy as will be discussed through the remarks and proof of correcteness/efficiency/evaluation sections. 


*Remarks and Notes:*

-> Based on the instructions we have 4 servants (thus, 4 threads are used). Additionally, we have a number of presents that is equal to 500,000 that were received by the Minotaur. Consequently, these values were used directly as is for the number of threads and the amount of presents. However, feel free to change them directly if needed for more testing/exploration.

-> The book and class slides (chapter 9) were used to gain more knowledge and implement a lock free list that was used in my solution. 

-> Efficient use of atomics was achieved as we used them to communicate adding from the unorganized bag to the chain, and also another to communicate taking from the chain (removal) to write a thank you letter for the guest.

-> When the miotaur asked to check if a present is on the chain or not we used the contain method for the chain (lock free list), and we passed as a parametter the adding tracker, the reason behind this is that it is still random as threads will update it's value/will still work on the chain, and additionally it will trigger a mix of found and not found responses, plus I wanted to check for recently processed presents from the bag as that makes much more sense for the Minotaur to ask about.

-> In the solution we rely on the use of methods such as contains() from the lock free list implemenation, getAndIncrement(), attemptMark(), compareAndSet() and so on.

-> To Compile and Run: 
```
javac BirthdayPresentsParty.java 
java BirthdayPresentsParty 
``` 

**Proof Of Correctness and Efficiency:**

For the design of my solution, I have relied on using a lock free list based on the chapter 9 in the book, class slides and also online resources. Additionally, in this implementation, I have relied on using atomics, including the use of Atomic Markable References (https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/AtomicMarkableReference.html) && (https://www.baeldung.com/java-atomicmarkablereference). The use of this type of atomic allows not only to have access to the node itself and its content, but also to mark it in case a thread is doing something with it, so other threads would know. Additionally, I tried to come up with a strategy where the threads will not wait to remove from the chain untill all bag presents were present in the chain. Thus, besides using the atomic markable references in my node and lock free list implementation, I also relied on using three trackers. These trackers were three atomic variables, one for tracking the addition from the unorganized bag to the chain, one for tracking the removal from the chain, and one for tracking the writing of thank you cards to the guests. This strategy allows threads to communicate efficiently and handle the potential edge cases as is needed. Moreover, to handle the Moinotaur's random presents check in the chain,  I did set it to be based on a randomly generated value if it falls under a certain threshold, additionally, the tag chosen for the check will be based on the add tracker, this will simulate a realistic scenario, since the Minotaur will not ask at each iteration, and also using the add tracker was to pick from the recently added presents to the chain, since threads are continuously adding and removing, there is randomization for checks, and it makes more sense to check for what's recently added rather than using a random tag based on the unordered bag. Also, to add more optimization, a condition that I added for the removal was that a removal would be triggered officialy (can occur randomly too) if the remove tracker is late by 4 presents compared to adding, the reason behind this choice is that we have 4 threads, and 3 possible actions, so technically if 4 servants chose to add, and we don't want to wait till the chain is full to remove, so when there is a 4 presents in the chain, we will trigger a removal directly, yet again removals can still occur randomly too!

**Experimental Evaluation and Efficiency:** 
 
We tested with different inputs, debugged with several prints and by manually tracing, the results can be shown as follows, and they reflect the efficiency of my solution: 
note: I also printed which servant ID is checking for the minotaur to show parallelism: 
For 100,000 presents -> 186ms. 
For 500,000 presents -> 823ms. 
For 1,000,000 presents -> 2404ms. 
 
For a Java based implementation and based on my solution, we definitely solved the issue of the Minotaur and made 
him and his servants save time and effort. 
Note: -> as we are using randomization in the process and for presents, the time of execution can relatively change 
by a bit but the scope will be the same and still very fast.

Note: I have kept some print statements for clarity, yet when removed the execution time definitely goes down. 


# Problem 2:

*Remarks and Notes:* 
-> To Compile and Run: 
```
javac AtmosphericTemperatureReadingModule.java 
java AtmosphericTemperatureReadingModule 
```  

-> we have 8 sensors (threads) that need to be used. 
-> we need to generate a report for every hour. 
-> the report will have the top lowest 5 and top highest 5 temperatures recorded for that hour. 
-> the report will also have the interval of 10 min where the highest temperature was recorded for that hour. 
-> we prompt the user for the number of hours otherwise they can chose to have it for 24 hrs (default value).
-> threads share the same memory for reading and writing temperatures
-> temperatures are collected each minute by the sensors and written to the shared memory
-> we use atomics to handle communication between threads and to track the time
-> to simulate a minute ongoing we make the threads sleep for 30ms
-> during the recording process we report the time and memory consumption
-> Note: 
*More details in the following sections.
*I used several debug statements please use them as is needed for evaluation

**Proof Of Correctness and Efficiency:**  

In my solution implementation I use an array list of array lists to be the shared memory, such that each thread will have its own array list that it writes and reads to and that way no thread will need to wait on the other to read and write from the shared memory. Additionally, each thread will store in its array list the temperatures recorded each minute by the sensor for the current hour, after the hour passes a report is generated as described above, and if the hours count reached the maximum set for the simulation the full process is done. To simulate a minute we make the thread sleep for 30ms before looping to the next minute. After 60 minutes we set the minute tracker to 0 again. Temperature recorded is between -100 and 70. My process ensures that threads will not have to wait for each other to read and write from the shared memory, additionally when it comes to processing the report content, such as the top5 and bottom5 temperatures we use helper data strcuture array list after sorting the copied data to it. For the intervl of 10 min where the highest temperature was recorded for the hour, we used helper arrays to find mins and maxes and evaluated the differences to determine the highest interval amongst all the 10 min intervals in that hour. As discussed above, we used several atomics for tracking and one of them is for checking the completion of all the hours, once that is set to true all threads stop and we finish the simulation.


**Experimental Evaluation and Efficiency:** 

-> I tested it with different amounts of hours ranging from 1 to 3 to 24 and as I mentioned before I decided to record both the execution time and memory consumption to show the efficiency of my work. 
-> I use a lot of debug statements, removing them will definitely make this faster, but I am keeping them for a clear and better evaluation. 

-> for 1hr: 2914 ms and 3146392 bytes 
-> for 3hrs: 10206 ms and 3688832 bytes 
-> for 24hrs: 77422 ms and 6934296 bytes  

*note:* if you add or use the debug statements in that case the time and memory might be lower/bigger slightly, and since this simulation involves some randomization, in each run the execution time and memory consumption might change, but it will still be relative and close and reflective of the speed and efficiency of the solution.  
 
Here is a sample output: 
```
The duration of the simulation is set to default (24 hours), would you like to change the simulation hours or remain as 24hrs? (y/n)
y
Okay, what is the new total simulation hours you want to test with? 
3
Confirmed! Starting the Atmospheric Temperature Readings for 3hrs.....
Report Generation Activated: ----------------------------------------
Current Time stamp: 1hrs.
The top 5 highest temperatures are -> [70, 70, 70, 69, 69]
The top 5 lowest temperatures are -> [-99, -99, -98, -98, -96]
The 10-min interval of time when the largest temperature difference was observed was -> [30Min, 40Min].
The temperature difference reached -> 169 degrees.
Report Generation Finished: ----------------------------------------
Report Generation Activated: ----------------------------------------
Current Time stamp: 2hrs.
The top 5 highest temperatures are -> [70, 70, 70, 69, 69]
The top 5 lowest temperatures are -> [-99, -99, -97, -95, -95]
The 10-min interval of time when the largest temperature difference was observed was -> [40Min, 50Min].
The temperature difference reached -> 169 degrees.
Report Generation Finished: ----------------------------------------
Report Generation Activated: ----------------------------------------
Current Time stamp: 3hrs.
The top 5 highest temperatures are -> [70, 70, 70, 70, 69]
The top 5 lowest temperatures are -> [-99, -99, -98, -98, -98]
The 10-min interval of time when the largest temperature difference was observed was -> [0Min, 10Min].
The temperature difference reached -> 169 degrees.
Report Generation Finished: ----------------------------------------
The finished tracker current status is -> 1
Total memory consumed from start to end of the simulation of 3hrs was -> 3688832 (bytes).
The duration of the execution of the simulation of temperature recording of 3hrs was -> 10206 (ms).
``` 

-> My solution is definitely efficient as can be seen through the results even with the presence of a bunch of debug statements, additionally my solution is not only optimized for runtime but also for memory consumption. 

 
