# COP4520---Assignment3
# Problem 1:

*Remarks and Notes:*

-> Based on the instructions we have 4 servants (thus, 4 threads are used). Additionally, we have a number of presents that is equal to 500,000 that were received by the Minotaur. Consequently, these values were used directly as is for the number of threads and the amount of presents. However, feel free to change them directly if needed for more testing/exploration.

-> The book and class slides (chapter 9) were used to gain more knowledge and implement a lock free list that was used in my solution. 

-> Efficient use of atomics was achieved as we used them to communicate adding from the unorganized bag to the chain, and also another to communicate taking from the chain (removal) to write a thank you letter for the guest.

-> When the miotaur asked to check if a present is on the chain or not we used the contain method for the chain (lock free list), and we passed as a parametter the adding tracker, the reason behind this is that it is still random as threads will update it's value/will still work on the chain, and additionally it will trigger a mix of found and not found responses, plus I wanted to check for recently processed presents from the bag as that makes much more sense for the Minotaur to ask about.

**Proof Of Correctness and Efficiency:**

**Experimental Evaluation and Efficiency:**

# Problem 2:

*Remarks and Notes:*

**Proof Of Correctness and Efficiency:**

**Experimental Evaluation and Efficiency:**
