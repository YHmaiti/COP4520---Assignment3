/*
 * This program was written by Yohan Hmaiti as a solution for the Assignment 3 problem 2
 * for the cop4520 class Spring 23.
 * 
 * For more explanation of efficiency, design, correctness, experimental eval and progress guarantee of my program,
 * please read the readMe file on my github repo page for this solution.
 */

// pre-processor directives
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// class that starts the program/simulation and creates the child threads for 
// the main parent thread
public class AtmosphericTemperatureReadingModule {

    // we use 8 threads for this problem as was chosen in the prompt (sensors)
    public static int _ThreadsAMT = 8;

    // we declare the temperature interval (Fahrenheit)
    public static int _TemperatureLowerBound = -100;
    public static int _TemperatureUpperBound = 70;

    // we store the time duration by which a report is generated  (end of every hour)
    public static int _reportAfterEach60Min = 60;

    // we store the frequency if each temperature reading
    public static int _frequencyOfTemperatureReading1Min = 1;

    // the amount/hours of the duration of the simulation were not explicitly stated
    // we set it to a default of 24 hours - 1 day (yet we prompt the user to change it if they want)
    public static int _SimulationHours = 24;

    /* we will record both the time and memory consumption for this problem */
    public static long timeS, timeE, memS, memE;

    // let's declare the driver method 
    public static void main(String[] args) {


        // prompt the user if they want to change the total simulation hours or keep it as default for 24hrs (1day)
        System.out.println("The duration of the simulation is set to default (24 hours), would you like to change the simulation hours or remain as 24hrs? (y/n)");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        if (choice == 'y' || choice == 'Y') {
            System.out.println("Okay, what is the new total simulation hours you want to test with? ");
            _SimulationHours = scanner.nextInt();
            System.out.println("Confirmed! Starting the Atmospheric Temperature Readings for " + _SimulationHours + "hrs.....");
        } else {
            System.out.println("Confirmed! Starting the Atmospheric Temperature Readings for " + _SimulationHours + "hrs.....");
        }
        scanner.close();

        // for this problem to hold the shared memory I decided to use a concurrent hashmap
        // since all threads will need to read and write without any blocking or waiting
        // and the concurrent hashmap can guarantee time optimization and a wait-free behaviour through fine-grained locking,
        // where not the whole map is locked, but only part of it that is accessed by the thread, and since all threads
        // will have their own keys and values, we will not have blocking or waiting technically for read and write. Additionally, we will have the quick 
        // insertion and retrieval since we are using a hashmap.
        // each thread will be a key, and the values willbe the temperature values read by the sensor (thread)
        // source: https://www.developer.com/java/concurrenthashmap-java/#:~:text=Lock%2Dfree%3A%20The%20ConcurrentHashMap%20is,in%20a%20multi%2Dthreaded%20environment.
        // source2: https://www.geeksforgeeks.org/concurrenthashmap-in-java/
        ConcurrentHashMap<Integer, Integer> _sharedMemoryBetweenSensors = new ConcurrentHashMap<Integer, Integer>();

        // let's declare some atomics for the threads to communicate and use 
        AtomicInteger _Finish = new AtomicInteger(0);
        AtomicInteger _timeTracker = new AtomicInteger(0);
        AtomicInteger _tracker = new AtomicInteger(0);
    }
}