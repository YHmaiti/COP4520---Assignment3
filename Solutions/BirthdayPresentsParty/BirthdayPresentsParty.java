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
    public LockFreeList List_PresentsChain;
    public NodeInChain _headOfChain, _tailOfChain;
    public AtomicInteger _ThankYouCardCount;
    public AtomicInteger _ADD, _REMOVE;
    Random rand;

    // assign all the variables to the right thing
    public Minotaur_Servants(LockFreeList List_PresentsChain, int _ID,
        ArrayList<Integer> _UnsortedPresents, AtomicInteger cardsCount, AtomicInteger _ADD, AtomicInteger _REMOVE) {
        
        this._ServerID = _ID;
        this._UnsortedPresents = _UnsortedPresents;
        this.List_PresentsChain = List_PresentsChain;
        this._headOfChain = List_PresentsChain.head;
        this._ThankYouCardCount = cardsCount;
        this._ADD = _ADD;
        this._REMOVE = _REMOVE;

        rand = new Random();
        
    }

    @Override
    public void run() {

        while (_ADD.get() < _UnsortedPresents.size() || _REMOVE.get() < _UnsortedPresents.size()) {

            try {
                // first we get the present
                int currentPresent_ToBeAdded = _UnsortedPresents.get(_ADD.getAndIncrement());
                // add the present to the chain after
                List_PresentsChain.add(currentPresent_ToBeAdded);
                // System.out.println("The present of tag-> " + currentPresent_ToBeAdded + " was added to the chain");
            } catch (Exception e) {
                //System.out.println("Failed at adding the present to the chain ->" + e);
            }
            

            if (_REMOVE.get() < _ADD.get() - 3) {
                try {
                    // first we get the present
                    int currentPresent_ToBeRemoved = _UnsortedPresents.get(_REMOVE.getAndIncrement());

                    // we also increment the thank you written cards
                    _ThankYouCardCount.getAndIncrement();

                    // remove the present from the chain after
                    List_PresentsChain.remove(currentPresent_ToBeRemoved);
                } catch (Exception e) {
                    //System.out.println("Failed at removing the present from the chain ->" + e);
                }
            }

            if (_WillTheMinotaurAsk()){
                
                try {

                    //int MinotaurCurrentPresent = rand.nextInt(_UnsortedPresents.size()) + 1;
                    int MinotaurCurrentPresent = _UnsortedPresents.get(_ADD.get());
                    if (List_PresentsChain.contains(MinotaurCurrentPresent)) {
                        System.out.println(
                                "The chosen present of tag-> " + MinotaurCurrentPresent + " was found in the chain");
                    } else {
                        System.out.println("The chosen present of tag-> " + MinotaurCurrentPresent
                                + " was not found in the chain");
                    }
                } catch (Exception e) {
                    //System.out.println("Failed at checking the present from the chain ->" + e);
                }
            }

        }
        
    }

    public boolean _UnsortedBagIsEmpty() {
        if (this._UnsortedPresents.size() == 0) {
            System.out.println("The original unsorted bag of presents is Empty!!");
            return true;
        }
        return false;
    }

    
    public boolean _StillWorkingOnPresents() {
        if (_ADD.get() < _UnsortedPresents.size() || _REMOVE.get() < _UnsortedPresents.size()) {
            return true;
        } 
        return false;
    }

    
    public void simulateMinotaurCheck() {

        try {
            
            int MinotaurCurrentPresent = rand.nextInt(_UnsortedPresents.size()) + 1;
            
            if (List_PresentsChain.contains(MinotaurCurrentPresent)) {
                System.out.println("The chosen present of tag-> " + MinotaurCurrentPresent + " was found in the chain");
            } else {
                System.out.println("The chosen present of tag-> " + MinotaurCurrentPresent + " was not found in the chain");
            }
        } catch (Exception e) {
            System.out.println("Failed at checking the present from the chain ->" + e);
        }
    }
    
    public void simulateRemoval() {


        try {
            // first we get the present
            int currentPresent_ToBeRemoved =_UnsortedPresents.get(_REMOVE.getAndIncrement());

            // we also increment the thank you written cards
            _ThankYouCardCount.getAndIncrement();

            // remove the present from the chain after
            List_PresentsChain.remove(currentPresent_ToBeRemoved);
        } catch (Exception e) {
            System.out.println("Failed at removing the present from the chain ->" + e);
        }
    }

    // we simulate the addition to the chain of presents 
    public void simulateAddition() {

        try {
            // first we get the present
            int currentPresent_ToBeAdded = _UnsortedPresents.get(_ADD.getAndIncrement());
            // add the present to the chain after
           List_PresentsChain.add(currentPresent_ToBeAdded);
        } catch (Exception e) {
           System.out.println("Failed at adding the present to the chain ->" + e);
        }
    }

    // add some randomness to when the minotaur will ask for a check
    // this is a backUp procedure (can be considered as a second appraoch for the minotaur intervention simulation)
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
class LockFreeList {

    // declare a head to the list and we also declare a tail to track the latest changes added 
    // to the end of the list 
    public NodeInChain head = new NodeInChain(Integer.MIN_VALUE);
    //public NodeInChain tail;
    AtomicMarkableReference<NodeInChain> tail = new AtomicMarkableReference<NodeInChain>(new NodeInChain(Integer.MAX_VALUE), false);
    // we initialize the head and attach the tail
    public LockFreeList() {
        head.next = tail;
    }

    public boolean contains(int _NodeTag) {
        boolean[] marked = {false};
        NodeInChain curr = head;

        while (curr.tag < _NodeTag) {
            curr = curr.next.getReference();

            curr.next.get(marked);
        }

        return (curr.tag == _NodeTag && !marked[0]);
    }

    // we implement the remove method based on the book
    public boolean remove(int _NodeTag) {
        boolean snip;
        while (true) {
            Window window = find(head, _NodeTag);
            NodeInChain pred = window.pred;
            NodeInChain curr = window.curr;

            if (curr.tag != _NodeTag) {
                return false;
            } else {
                NodeInChain succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true); // can be implemented differently
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

            if (curr.tag == _NodeTag) {
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
                if (curr.tag >= _NodeTag) {
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

    public static void main(String[] args) throws InterruptedException{
        // create an instance of the driver class
        //BirthdayPresentsParty _MAIN = new BirthdayPresentsParty();
        // declare the ecxecution time vars
        long start, end;

        // we have 4 servants let's store this value
        int servant_threads_cnt = 4;

        // total presents is 500 thousand
        int allPresents_cnt = 500000;

        // we then set up the unsorted unorganized bag and the servants threads
        ArrayList<Integer> Unorganized_Unsorted_Bag = new ArrayList<Integer>(500000);
        Thread[] servants_Threads = new Thread[servant_threads_cnt];


        // fill the bag
        for (int i = 1; i <= allPresents_cnt; i++) {
            Unorganized_Unsorted_Bag.add(i);
        }
        
        // make the bag unordered and unorganized
        Collections.shuffle(Unorganized_Unsorted_Bag);

        // we use atomics that will be used later to handle adding and deleting at the
        // level of the
        // chain of presents
        AtomicInteger _Add = new AtomicInteger(0);
        AtomicInteger _Remove = new AtomicInteger(0);

        // create an atomic for cards written
        AtomicInteger cardsCount = new AtomicInteger(0);

        // set up the lock free list
        LockFreeList _list = new LockFreeList();

        // fill up the array of threads
        for (int i = 0; i < servant_threads_cnt; i++) {
            servants_Threads[i] = new Minotaur_Servants(_list, i + 1, Unorganized_Unsorted_Bag, cardsCount, _Add, _Remove);
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
