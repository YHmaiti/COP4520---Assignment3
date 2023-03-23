// authorship statement and summary:
/*
 * This solution to problem 1 of Assignment 3 for COP 4520 was made by Yohan Hmaiti
 * Spring 2023
 */


// pre-processor directives
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.ArrayList;
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

    // assign all the variables to the right thing
    public Minotaur_Servants(LockFreeList List_PresentsChain, int _ID, BirthdayPresentsParty _DriverThread,
        ArrayList<Integer> _UnsortedPresents, AtomicInteger cardsCount, AtomicInteger _ADD, AtomicInteger _REMOVE) {
        
        this._ServerID = _ID;
        this._BirthdayDriverThread = _DriverThread;
        this._UnsortedPresents = _UnsortedPresents;
        this.List_PresentsChain = List_PresentsChain;
        this._headOfChain = this.List_PresentsChain.head;
        this._tailOfChain = this.List_PresentsChain.tail;
        this._ThankYouCardCount = cardsCount;
        this._ADD = _ADD;
        this._REMOVE = _REMOVE;
        this._UnsortedPresents_Size = this._UnsortedPresents.size();

    }

    // run method representing the action choices of each servant of the Minotaur
    // we will also implement here the random checks of the Minotaur for presence of a present
    // (action 3)
    public void run() {

        // we will only stop if all the presents from the unordered bag were processed
        // Also, we will only stop if all the thank you cards were written to the guests 
        // Consequently, all the presents would be then processed from the chain too.
        while (_StillWorkingOnPresents()) {

            /* *** The servant does one action out of the three at each time in no particular order */

            // in the first ever iteration we know the chain is empty so we first set the addition 
            // action from the unordered bag to the chain

            

            // the second action we will set is the remove present from chain action
            // once removed a thank you card will be written for the guest that gave the present

            // Since as the prompt states, the following can happen at random: 
            // "Per the Minotaur’s request, check whether a gift with a particular tag was
            // present in the chain or not; without adding or removing a new gift, a servant
            // would scan through the chain and check whether a gift with a particular tag
            // is already added to the ordered chain of gifts or not."
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
    public boolean _ChainOfPresentsSizeStatus() {
        if (this.List_PresentsChain.head.next.getReference() == null && this.List_PresentsChain.tail == null) {
            System.out.println("No Present Added To The Chain Yet!!");
            return true;
        } 
        System.out.println("The chain of presents has some content at least!!");
        return false;
    }

    // check if the servants are still working:
    // if the presents in the chain are less than the total amount of presents -> return true
    // if the thank you cards written or the amount of presents processed from the chain are less than the total num 
    // of presents -> return true
    // if all presents were added to the chain or all presents were processed from the chain -> return false (no need to continue working)
    public boolean _StillWorkingOnPresents() {
        if (this._ADD < this._UnsortedPresentsSize) {
            return True;
        } else if (this._ThankYouCardCount < this._UnsortedPresentsSize && this._REMOVE < this._UnsortedPresentsSize) {
            return True;
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
    public NodeInChain tail;

    // we initialize the head and attach the tail
    public LockFreeList() {
        this.head = new NodeInChain(Integer.MIN_VALUE);
        this.tail = new NodeInChain(Integer.MAX_VALUE);
        this.head.next.set(this.tail, false);

        // check if head next is tail
        if (this.head.next.getReference() != this.tail) {
            System.out.println("From the constructor call of lock free list, head next doesn't point to tail...");
            System.exit(-1);
        }
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

        while (1) {
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


public class BirthdayPresentsParty {
    
}
