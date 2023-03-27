/*
 * This solution to problem 1 of Assignment 3 for COP 4520 was made by Yohan Hmaiti
 * Spring 2023
 * Some implementations here were inspired by the book and ppt slides from the class
 * Please check the readMe for additional explanations regarding general program design and correctness
 * Also, the read me discusses efficiency, statements and proof of correctness, and experimental evaluation
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

    /* Helper getters and setters */

    public int _getTag() {
        return this.tag;
    }

    public void _setTag(int _NewTag) {
        this.tag = _NewTag;
    }

}

// We make a class for the servants of the Minotaur (each servant is a thread)
class Minotaur_Servants extends Thread{

    /*
     * Summary of the servant based variables and characteristics:
     * 
     * @_ServerID: the ID of the thread (servant)
     * 
     * @_BirthdayDriverThread: instance of the main thread that is passed 
     * to the child threads (servants)
     * 
     * @_UnsortedPresents: the unsorted bag of presents that the servants 
     * will pick from
     * 
     * @List_PresentsChain: the chain of presents that the servants will 
     * remove presents from and write thank you cards to guests
     * 
     * @_headOfChain and @_tailOfChain: head and tail of the chain 
     * 
     * @_ThankYouCardCount: count of thank you cards written by the servants 
     * as they go while processing chain presents 
     * 
     * @_bringFromUnsortedPresents and @_processChainPresent: atomics that 
     * will be used to handle the addition to the chain, and removal from 
     * it to write the letters
     * 
     */
    public int _ServerID;
    public BirthdayPresentsParty _BirthdayDriverThread;
    public ArrayList<Integer> _UnsortedPresents;
    public LFL List_PresentsChain;
    public NodeInChain _headOfChain, _tailOfChain;
    public AtomicInteger _ThankYouCardCount;
    public AtomicInteger _bringFromUnsortedPresents, _processChainPresent;
    Random rand;

    // constructor
    public Minotaur_Servants(LFL List_PresentsChain, int _ID,
        ArrayList<Integer> _UnsortedPresents, AtomicInteger cardsCount, 
        AtomicInteger _bringFromUnsortedPresents, AtomicInteger _processChainPresent,
        BirthdayPresentsParty _BirthdayPilotInstance) {
        
        this._ServerID = _ID;
        this._UnsortedPresents = _UnsortedPresents;
        this.List_PresentsChain = List_PresentsChain;
        this._headOfChain = List_PresentsChain.head;
        this._ThankYouCardCount = cardsCount;
        this._bringFromUnsortedPresents = _bringFromUnsortedPresents;
        this._processChainPresent = _processChainPresent;

        rand = new Random();
        
    }

    /*
     * run method: as long as the bag is not empty and presents were not fullly processed
     * continue working and pick one action out of the three available (based on conditions of course)
     * However, each servant will do one of the three actions in no particular order as specified in the prompt
     */
    @Override
    public void run() {
        while (_UnsortedBagIsEmpty() == false && _StillWorkingOnPresents() == true) {
            _pickOneAction();
        }
    }

    // method to check if the chain of presents is emprt or no
    public boolean _checkIfChainIsEmpty() {
        if (List_PresentsChain.head.next.getReference() == null) {
            System.out.println("The chain of presents is Empty!!!");
            return true;
        }
        return false;
    }

    // method to check if the unsorted bag is empty or no
    public boolean _UnsortedBagIsEmpty() {
        if (this._UnsortedPresents.size() == 0) {
            System.out.println("The original unsorted bag of presents is Empty!!");
            return true;
        }
        return false;
    }

    // method that checks if the servants finished or if there are still presents that need to be processed 
    // either from the unsorted bag to the chain or from the chain itself to write a thank you card
    public boolean _StillWorkingOnPresents() {
        if (_bringFromUnsortedPresents.get() < _UnsortedPresents.size() || _processChainPresent.get() < _UnsortedPresents.size()) {
            return true;
        } 
        return false;
    }

    // method that simulates the Minotaur submitting a request to check if a gift with a particular tag 
    // was present in the chain or no.
    // Note: we pick the tag randomly, we can either use a rand generator or the atomic that tracks the presents that go from the bag to the chain,
    // using a rand generator will sometimes give numbers that won't be found of course, and that is fine, and sometimes it will give chain-presents tags. However,
    // I opted for the atomic, as it at leasts randomizes by picking from the recently moved presents to the chain, which can either be processed from the chain, 
    // waiting in the chain, or still in the process of moving from the bag to the chain.
    public void simulateMinotaurCheck() {
        try {
            int MinotaurCurrentPresent = _UnsortedPresents.get(_bringFromUnsortedPresents.get());
            if (List_PresentsChain.contains(MinotaurCurrentPresent)) {
                System.out.println(
                        "Minotaur is Requesting a check: servant "+ _ServerID + " will take care of it. \nThe chosen present of tag-> " + MinotaurCurrentPresent + " was found in the chain");
            } else {
                System.out.println("Minotaur is Requesting a check: servant " + _ServerID
                        + " will take care of it. \nThe chosen present of tag-> " + MinotaurCurrentPresent
                        + " was not found in the chain");
            }
        } catch (Exception e) {
            // we can print the e stacktrace here but not needed
        }
    }
    
    // method that simulates removing a present from the chain and writing the thank you card to the guest
    // In here, since we know that action are done one of the three in no particular order, we do not trigger the removal
    // until at least some elements were in the chain and the addition occured, which is handled through the transfer atomic of the bag to the chain
    // I chose to deduct 4 just to give some breadth to the addition to the chain and also - 4 since we have 4 servants and 4 actions might be executed and it induces 
    // randomization, since actions will not be executed in order but in no particular order.
    public void simulateRemoval() {
        if (_processChainPresent.get() >= _bringFromUnsortedPresents.get() - 4) return;
        try {
            int currentPresent_ToBeRemoved =_UnsortedPresents.get(_processChainPresent.getAndIncrement());
            _ThankYouCardCount.getAndIncrement();
            List_PresentsChain.remove(currentPresent_ToBeRemoved);
        } catch (Exception e) {
            // we can print the e stacktrace here but not needed
        }
    }

    // we simulate the addition to the chain of presents 
    public void simulateAddition() {
        try {
            int currentPresent_ToBeAdded = _UnsortedPresents.get(_bringFromUnsortedPresents.getAndIncrement());
            List_PresentsChain.add(currentPresent_ToBeAdded);
        } catch (Exception e) {
            // we can print the e stacktrace here but not needed
        }
    }

    // we pick one action in no particular order from the ones available that can be done by the servant (handled by conditions inside each call)
    public void _pickOneAction() {
        simulateAddition();
        simulateRemoval();
        if (_WillTheMinotaurAsk()) {
            simulateMinotaurCheck();
        }
    }

    // add some randomness to when the minotaur will submit a request
    // to check the chain for a present and it's status
    public boolean _WillTheMinotaurAsk() {
        int MinotaurCheck = rand.nextInt(1000000);
        if (MinotaurCheck < 50) {
            return true;
        }
        return false;
    }

}

// let's implement a lock free list based on the chapter 9 from the 
// "The Art of Multiprocessor Programming" and other chapters from the same book
// along with the slides covered in the class
class LFL {

    // declare a head to the list
    public NodeInChain head = new NodeInChain(Integer.MIN_VALUE);
    // declare a tail for the list
    AtomicMarkableReference<NodeInChain> tail = new AtomicMarkableReference<NodeInChain>(new NodeInChain(Integer.MAX_VALUE), false);
    // we initialize the head and connect it to the tail that will be the next node the head points to
    public LFL() {
        head.next = tail;
    }

    // implement the contains method same as the book ch.9
    public boolean contains(int _NodeTag) {
        boolean[] marked = {false};
        NodeInChain curr = head;

        while (curr._getTag() < _NodeTag) {
            curr = curr.next.getReference();

            curr.next.get(marked);
        }

        return (curr._getTag() == _NodeTag && !marked[0]);
    }

    // implement the remove method based on the book ch.9
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
                snip = curr.next.attemptMark(succ, true);
                if (!snip) {
                    continue;
                }
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    // implement the add method based on the book ch.9
    public boolean add(int _NodeTag) {

        while (true) {
            Window window  = find(head, _NodeTag);
            NodeInChain pred = window.pred;
            NodeInChain curr = window.curr;

            if (curr._getTag() == _NodeTag) {
                return false;
            } else {
                NodeInChain node = new NodeInChain(_NodeTag);
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

    // based on the problem we have 4 servants for the Minotaur, we store that
    public static int servant_threads_cnt = 4;

    // total presents is 500 thousand
    public static int allPresents_cnt = 500000;

    // declare the ecxecution time vars
    public static long start, end;

    // main driver method
    public static void main(String[] args) throws InterruptedException{

        // we create the unorganized unsorted bag of presents that will be processed by servants
        // total amnt of presents is 500 thousand, also we declare an array of threads that represents the servants
        ArrayList<Integer> Unorganized_Unsorted_Bag = new ArrayList<Integer>(500000);
        Thread[] servants_Threads = new Thread[servant_threads_cnt];


        // let's assign the presents to the bag
        for (int i = 1; i <= allPresents_cnt; i++) {
            Unorganized_Unsorted_Bag.add(i);
        }
        
        // shuffle to simulate disorder in the bag
        Collections.shuffle(Unorganized_Unsorted_Bag);

        // we use atomics that will communciate states of addition and deletion from the chain of presents
        // we set their value as 0
        AtomicInteger _bringFromUnsortedPresents = new AtomicInteger(0);
        AtomicInteger _processChainPresent = new AtomicInteger(0);
        AtomicInteger cardsCount = new AtomicInteger(0);

        // create an instance of the current driver class
        BirthdayPresentsParty _MAINPILOT = new BirthdayPresentsParty();

        // we create the lock free list here
        LFL _list = new LFL();

        // set up the array of threads that we will start later
        for (int i = 0; i < servant_threads_cnt; i++) {
            servants_Threads[i] = new Minotaur_Servants(_list, i + 1, Unorganized_Unsorted_Bag, cardsCount, _bringFromUnsortedPresents, _processChainPresent, _MAINPILOT);
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
        System.out.println("500 000 presents were processed, all thank you cards were written to the guests, servants did the job!\n Total time of execution: " + (end - start) + " ms.");
    }
    
}
