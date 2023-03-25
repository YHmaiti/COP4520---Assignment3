// authorship statement and summary:
/*
 * This solution to problem 1 of Assignment 3 for COP 4520 was made by Yohan Hmaiti
 * Spring 2023
 */


// pre-processor directives
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

// This class will represents the presents that will be added to the chain of presents
// by the servants (Linked-List format)
class NodeInChain {
    
    public AtomicMarkableReference<NodeInChain> next;
    public int tag;
    
    public NodeInChain(int _tag) {
        this.tag = _tag;
        this.next = new AtomicMarkableReference<NodeInChain>(null, false);
    }

    public int _getTag() {
        return this.tag;
    }

    public void _setTag(int _NewTag) {
        this.tag = _NewTag;
    }

}

// This class is for the Minotaur
class Minotaur_Servants extends Thread{

    public int _ServerID;
    public BirthdayPresentsParty _BirthdayDriverThread;
    public ArrayList<Integer> _UnsortedPresents;
    public int _UnsortedPresents_Size;
    public LockFreeList List_PresentsChain;
    public NodeInChain _headOfChain, _tailOfChain;
    public int currentChoiceOfAction_Servant;
    public AtomicInteger _ThankYouCardCount;
    public AtomicInteger _ADD, _REMOVE;
    Random rand;

    // assign all the variables to the right thing
    public Minotaur_Servants(LockFreeList List_PresentsChain, int _ID, BirthdayPresentsParty _DriverThread,
        ArrayList<Integer> _UnsortedPresents, AtomicInteger cardsCount, AtomicInteger _ADD, AtomicInteger _REMOVE) {
        
        this._ServerID = _ID;
        this._BirthdayDriverThread = _DriverThread;
        this._UnsortedPresents = _UnsortedPresents;
        this.List_PresentsChain = List_PresentsChain;
        this._headOfChain = this.List_PresentsChain.head;
        //this._tailOfChain = this.List_PresentsChain.tail;
        this._ThankYouCardCount = cardsCount;
        this._ADD = _ADD;
        this._REMOVE = _REMOVE;
        this._UnsortedPresents_Size = this._UnsortedPresents.size();

        // since we have three possible actions we will set the choice of the servant first to 0
        this.currentChoiceOfAction_Servant = 0;
        rand = new Random();
        
        // print the total size 
        System.out.println("The total size of the unsorted bag of presents is: " + this._UnsortedPresents_Size);
        // total cards count
        System.out.println("The total number of thank you cards to be written is: " + this._ThankYouCardCount);

        // print add and remove
        System.out.println("The total number of ADD operations is: " + this._ADD);
        System.out.println("The total number of REMOVE operations is: " + this._REMOVE);
    }

    // run method representing the action choices of each servant of the Minotaur
    // we will also implement here the random checks of the Minotaur for presence of a present
    // (action 3)
    @Override
    public void run() {

        // we will only stop if all the presents from the unordered bag were processed
        // Also, we will only stop if all the thank you cards were written to the guests 
        // Consequently, all the presents would be then processed from the chain too.
        while (_StillWorkingOnPresents()) {

            /* *** The servant does one action out of the three at each time in no particular order */
            // let's randomly pick an action for the servant 
            //this.currentChoiceOfAction_Servant = rand.nextInt(2) + 1;
            // in the first ever iteration we know the chain is empty so we first set the addition 
            // action from the unordered bag to the chain
            //if (this.currentChoiceOfAction_Servant ==1)
            /* if(_ADD.get() < _UnsortedPresents_Size) */
                simulateAddition();
            

            // the second action we will set is the remove present from chain action
            // once removed a thank you card will be written for the guest that gave the present
            // if (this.currentChoiceOfAction_Servant == 2)
            if (_REMOVE.get() < _ADD.get() - 3)
                simulateRemoval();

            // Since as the prompt states, the following can happen at random: 
            // "Per the Minotaur’s request, check whether a gift with a particular tag was
            // present in the chain or not; without adding or removing a new gift, a servant
            // would scan through the chain and check whether a gift with a particular tag
            // is already added to the ordered chain of gifts or not."
            if (_WillTheMinotaurAsk())
                simulateMinotaurCheck();

            // after this is done we set back the choice of the servant to 0
            this.currentChoiceOfAction_Servant = 0;
        }
        
    }

    // method that checks if the Unsorted Bag is Empty or no
    public boolean _UnsortedBagIsEmpty() {
        if (this._UnsortedPresents.size() == 0) {
            System.out.println("The original unsorted bag of presents is Empty!!");
            return true;
        }
        return false;
    }

    // method that checks if the chain of presents is empty or being filled 


    // check if the servants are still working:
    // if the presents in the chain are less than the total amount of presents -> return true
    // if the thank you cards written or the amount of presents processed from the chain are less than the total num 
    // of presents -> return true
    // if all presents were added to the chain or all presents were processed from the chain -> return false (no need to continue working)
    public boolean _StillWorkingOnPresents() {
        if (this._ADD.get() < _UnsortedPresents_Size || this._REMOVE.get() < _UnsortedPresents_Size) {
            return true;
        } 
        return false;
    }

    // we simulate the minotaur's random check
    public void simulateMinotaurCheck() {
        this.currentChoiceOfAction_Servant = 3;
        try {
            // since based on the prompt it was not specified which gift:
            // "check whether a gift with a particular tag was present in the chain or not"
            // so we will randomly check for a tag
            int MinotaurCurrentPresent = rand.nextInt(_UnsortedPresents_Size) + 1;
            // evaluate if the presents is in the chain or no
            if (this.List_PresentsChain.contains(MinotaurCurrentPresent)) {
                System.out.println("The chosen present of tag-> " + MinotaurCurrentPresent + " was found in the chain");
            } else {
                System.out.println("The chosen present of tag-> " + MinotaurCurrentPresent + " was not found in the chain");
            }
        } catch (Exception e) {
            System.out.println("Failed at checking the present from the chain ->" + e);
        }
    }
    // we simulate the removal of a present from the chain of presents
    public void simulateRemoval() {
/*         if (this._REMOVE.get() >= _UnsortedPresents_Size)
            return; */
        this.currentChoiceOfAction_Servant = 2;
        try {
            // first we get the present
            int currentPresent_ToBeRemoved = this._UnsortedPresents.get(_REMOVE.getAndIncrement());

            // we also increment the thank you written cards
            this._ThankYouCardCount.getAndIncrement();
            // remove the present from the chain after
            this.List_PresentsChain.remove(currentPresent_ToBeRemoved);
        } catch (Exception e) {
            System.out.println("Failed at removing the present from the chain ->" + e);
        }
    }

    // we simulate the addition to the chain of presents 
    public void simulateAddition() {
        /* if (this._ADD.get() >= _UnsortedPresents_Size)
            return; */
        this.currentChoiceOfAction_Servant = 1;
        try {
            // first we get the present
            int currentPresent_ToBeAdded = this._UnsortedPresents.get(_ADD.getAndIncrement());
            // add the present to the chain after
            this.List_PresentsChain.add(currentPresent_ToBeAdded);
        } catch (Exception e) {
            System.out.println("Failed at adding the present to the chain ->" + e);
        }
    }

    // add some randomness to when the minotaur will ask for a check
    // this is a backUp procedure (can be considered as a second appraoch for the minotaur intervention simulation)
    public boolean _WillTheMinotaurAsk() {
        int MinotaurCheck = rand.nextInt(10000);
        if (MinotaurCheck < 5) {
            return true;
        }
        return false;
    }

}

// let's implement a lock free list based on the chapter 9 from the 
// "The Art of Multiprocessor Programming" and other chapters from the same book
// along with the slides covered in the class
class LockFreeList {

    // declare a head to the list and we also declare a tail to track the latest changes added 
    // to the end of the list 
    public NodeInChain head;
    //public NodeInChain tail;
    AtomicMarkableReference<NodeInChain> tail = new AtomicMarkableReference<NodeInChain>(new NodeInChain(Integer.MAX_VALUE), false);
    // we initialize the head and attach the tail
    public LockFreeList() {
        this.head = new NodeInChain(Integer.MIN_VALUE);
        //this.tail = new NodeInChain(Integer.MAX_VALUE);
        //this.head.next.set(this.tail, false);
        head.next = tail;
        // check if head next is tail

    }

    /*Note *** When Implementing these methods from the book, I changed the passed parameters mostly to be the tag of the node directly instead of passing the full node. *** */

    // we implement the contains method based on the book
    public boolean contains(int _NodeTag) {
        boolean[] marked = {false};
        NodeInChain curr = head;

        while (curr._getTag() < _NodeTag) {
            curr = curr.next.getReference();
            if (curr == null) {
                System.out.println("Stopped, current is null!...");
                return false;
            }
            curr.next.get(marked);
        }

        return (curr._getTag() == _NodeTag && !marked[0]);
    }

    // we implement the remove method based on the book
    public boolean remove(int _NodeTag) {
        boolean snip;
        while (true) {
            Window window = find(head, _NodeTag);
            NodeInChain pred = window.pred;
            NodeInChain curr = window.curr;

            if (curr._getTag() != _NodeTag) {
                return false;
            } else {
                NodeInChain succ = curr.next.getReference();
                snip = curr.next.attemptMark(succ, true); // can be implemented differently
                if (!snip) {
                    continue;
                }
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    // we implement the add method based on the book
    public boolean add(int _NodeTag) {

        while (true) {
            Window window  = find(head, _NodeTag);
            NodeInChain pred = window.pred;
            NodeInChain curr = window.curr;

            if (curr._getTag() == _NodeTag) {
                return false;
            } else {
                NodeInChain node = new NodeInChain(_NodeTag);
                /* node.next.set(curr, false); */
                node.next = new AtomicMarkableReference<NodeInChain>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }
    }

    /* *** Window Class And Find Method Implementation *** */

    // we declare a Window class similar to the book
    class Window {
        public NodeInChain pred;
        public NodeInChain curr;

        /*
         * @param p and c: predecessor and current nodes
         * @summary: constructor that updates the window object with the pred and curr nodes
         */
        public Window(NodeInChain p, NodeInChain c) {
            this.pred = p; this.curr = c;
        }
    }

    // we implement the find method of Window type based on the book
    public Window find(NodeInChain head, int _NodeTag) {
        NodeInChain pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;
        retry: while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) {
                        continue retry;
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr._getTag() >= _NodeTag) {
                    return new Window(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
    }

}

// class that contains the driver process
public class BirthdayPresentsParty {

    public static void main(String[] args) {
        // create an instance of the driver class
        BirthdayPresentsParty _MAIN = new BirthdayPresentsParty();
        // declare the ecxecution time vars
        long start, end;

        // we have 4 servants let's store this value
        int servant_threads_cnt = 4;

        // total presents is 500 thousand
        int allPresents_cnt = 500000;

        // we then set up the unsorted unorganized bag and the servants threads
        ArrayList<Integer> Unorganized_Unsorted_Bag = new ArrayList<Integer>(500000);
        Thread[] servants_Threads = new Thread[servant_threads_cnt];

        // set up the lock free list 
        LockFreeList _chain = new LockFreeList();

        // we use atomics that will be used later to handle adding and deleting at the level of the
        // chain of presents
        AtomicInteger _Add = new AtomicInteger(0);
        AtomicInteger _Remove = new AtomicInteger(0);

        // create an atomic for cards written
        AtomicInteger cardsCount = new AtomicInteger(0);

        // fill the bag
        for (int i = 1; i <= allPresents_cnt; i++) {
            Unorganized_Unsorted_Bag.add(i);
        }
        
        // make the bag unordered and unorganized
        Collections.shuffle(Unorganized_Unsorted_Bag);

        // fill up the array of threads
        for (int i = 0; i < servant_threads_cnt; i++) {
            servants_Threads[i] = new Minotaur_Servants(_chain, i + 1, _MAIN, Unorganized_Unsorted_Bag, cardsCount, _Add, _Remove);
        }

        // log the start time
        start = System.currentTimeMillis();

        // start the simulation
        for (int i = 0; i < servant_threads_cnt; i++) {
            try {
                servants_Threads[i].start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // join the threads
        for (int i = 0; i < servant_threads_cnt; i++) {
            try {
                servants_Threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // log the end time
        end = System.currentTimeMillis();

        // print the execution time of the simulation and the time it took the 
        // servants to process all presents and write the cards to guests
        _summaryOutput(start, end);
    
    }

    // output the execution time and summary
    public static void _summaryOutput(long start, long end) {
        System.out.println("500 000 presents were processed, all thank you cards were written to the guests, servants did the job! Total time of execution: " + (end - start) + " ms");
    }
    
}
