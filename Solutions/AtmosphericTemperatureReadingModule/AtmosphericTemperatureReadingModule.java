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
        // Note: some additional workaround and understanding was also inspired from the class book
        ConcurrentHashMap<Integer, Integer> _sharedMemoryBetweenSensors = new ConcurrentHashMap<Integer, Integer>();

        // let's declare some atomics for the threads to communicate and use 
        AtomicInteger _Finish = new AtomicInteger(0);
        AtomicInteger _timeTracker = new AtomicInteger(0);

        // make a threads array and we log the start time and memory consumption at the beginning 
        Thread[] ATRMThreads = new Thread[_ThreadsAMT];
        timeS = System.currentTimeMillis();
        memS = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // we create an instance of the time elapse class to handle the time processing and waits 
        ATRM_timeElapse _timeElapse = new ATRM_timeElapse();

        // loop through and create and start the threads
        for (int i = 0; i < _ThreadsAMT; i++) {
            try {
                ATRMThreads[i] = new Thread(new ATRM_Sensors(_sharedMemoryBetweenSensors, _Finish, _timeTracker, i + 1, _TemperatureLowerBound, _TemperatureUpperBound, _reportAfterEach60Min, _frequencyOfTemperatureReading1Min, _SimulationHours));
                ATRMThreads[i].start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Once the threads are spawned and started, let's try and simulate the minutes and hours
        ATRM_timeElapse.HoursAndMinutesSimulated_ATRM(_sharedMemoryBetweenSensors, _reportAfterEach60Min, _frequencyOfTemperatureReading1Min, _SimulationHours, _Finish, _timeTracker, ATRMThreads);

        // print the status of the simulation before joining
        System.out.println("The finished tracker current status is -> " + _Finish.get());

        // join the threads
        for (int i = 0; i < _ThreadsAMT; i++) {
            try {
                ATRMThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // after the time has passed foe the full hours of the simulation
        // we log the end time and the total memory consumed at the end
        timeE = System.currentTimeMillis();
        memE = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // we print the memory consumption and execution time
        System.out.println("Total memory consumed from start to end of the simulation of " + _simulationHours + "hrs was -> " + (memE - memS) + " (bytes).");
        System.out.println("The duration of the execution of the simulation of temperature recording of " + _simulationHours + "hrs was -> " + (timeE - timeS) + " (ms).");

    }
}

// create a class for the sensors (threads, total 8)
class ATRM_Sensors {

}

// make a class that will take care of the reporting processes and phases
class ATRM_Report {

    ATRM_Report() {
    }

    // handlle the output of the report 
    public void OutputReport(int Hour_ID, ConcurrentHashMap<Integer, Integer> _sharedConcurrentHashMap, int _reportAfterEach60Min, int _frequencyOfTemperatureReading1Min, int _SimulationHours, AtomicInteger _Finish, AtomicInteger _timeTracker, Thread[] ATRMThreads) {
        System.out.println("Report Generation Activated: ----------------------------------------");
        System.out.println("Current Time stamp: " + Hour_ID + "hrs.");


        System.out.println("Report Generation Finished: ----------------------------------------");
    }

}

// make a class to handle the time of the simulation and how it goes
class ATRM_timeElapse {

    // create a report class object 
    public static ATRM_Report _reportHandler;

    ATRM_timeElapse(){
        _reportHandler = new ATRM_Report();
    }

    /* The main strategy to simulate the hours and minutes running will be as follows:
     * loop through the total hours 
     * for each hours we will loop up to 60min
     * since we are not looping for minutes really nor hours, we will make the threads sleep for 10ms
     * Once the hour has elapsed we will generate a report
     */

    public static void HoursAndMinutesSimulated_ATRM(ConcurrentHashMap<Integer, Integer> _sharedConcurrentHashMap, int _reportAfterEach60Min, int _frequencyOfTemperatureReading1Min, int _SimulationHours, AtomicInteger _Finish, AtomicInteger _timeTracker, Thread[] ATRMThreads) {

        int i = 0;

        while (i < _SimulationHours) {
            // just for safety we assign a break here
            if (i == 60) {
                break;
            }

            // for the current hour we execute 60 min
            for (int j = 0; j < 60; j++) {

                if (j == 60) 
                    break;

                // sleep for 10 ms
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } 

                // second approach that can be used:
                /* for (int k = 0; k < ATRMThreads.length; k++) { 
                    try {
                        ATRMThreads[k].sleep(10); 
                    } catch (InterruptedException e) {
                        e.printStackTrace(); 
                    } 
                } */
                
                _timeTracker.getAndIncrement(); // can be done differently

            }

            // before we move to the next hour, let's reset the time tracker
            _timeTracker.set(0);

            // before moving to the next hour let's report the current hour findings
            _reportHandler.OutputReport(i + 1, _sharedConcurrentHashMap, _reportAfterEach60Min, _frequencyOfTemperatureReading1Min, _SimulationHours, _Finish, _timeTracker, ATRMThreads);
        }

        // after all hours of the simulation are elapsed, we update the finish flag to stop the sensors work
        // so we can join the threads and finalize the reporting
        if (i == _SimulationHours) {
            _Finish.set(1);
        }
    }

}