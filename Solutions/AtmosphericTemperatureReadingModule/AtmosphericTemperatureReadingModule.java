/*
 * This program was written by Yohan Hmaiti as a solution for the Assignment 3 problem 2
 * for the cop4520 class Spring 23.
 * 
 * For more explanation of efficiency, design, correctness, experimental eval and progress guarantee of my program,
 * please read the readMe file on my github repo page for this solution.
 */

// pre-processor directives
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Arrays.*;
import java.util.List;


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
        char choice = scanner.next().charAt(0);
        if (choice == 'y' || choice == 'Y') {
            System.out.println("Okay, what is the new total simulation hours you want to test with? ");
            _SimulationHours = scanner.nextInt();
            System.out.println("Confirmed! Starting the Atmospheric Temperature Readings for " + _SimulationHours + "hrs.....");
        } else {
            System.out.println("Confirmed! Starting the Atmospheric Temperature Readings for " + _SimulationHours + "hrs.....");
        }
        scanner.close();

        /* ----An interesting idea I had, but since hashmap might not preserve order, I decided to not follow this and use an arraylist of arraylist---- */

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
        // ConcurrentHashMap<Integer, Integer> _sharedMemoryBetweenSensors = new ConcurrentHashMap<Integer, Integer>();
        
        /* --------------------------------------------------------------------------------------------------------------------------------------------- */

        // we will use this to represent the shared memory since each thread will have an array list of temp recordings for it only which will be a non blocking 
        // approach to handle read and write without wait time or blocking for the sensors (threads available)
        // the arraylist will be initialized with rows equal to the threads amoutn we have
        ArrayList<ArrayList<Integer>> _sharedMemoryBetweenSensors = new ArrayList<ArrayList<Integer>>(_ThreadsAMT);

        // go over the arraylist and initialize the arraylists
        for (int i  = 0; i < _ThreadsAMT; i++) {
            _sharedMemoryBetweenSensors.add(new ArrayList<Integer>());
        }

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
        System.out.println("Total memory consumed from start to end of the simulation of " + _SimulationHours + "hrs was -> " + (memE - memS) + " (bytes).");
        System.out.println("The duration of the execution of the simulation of temperature recording of " + _SimulationHours + "hrs was -> " + (timeE - timeS) + " (ms).");

    }
}

// create a class for the sensors (threads, total 8)
class ATRM_Sensors extends Thread{

    // declare variables relevant to the functioning of the thread
    ArrayList<ArrayList<Integer>> _sharedMemory;
    AtomicInteger _Finish;
    AtomicInteger _timeTracker;
    int _sensorID;
    int _TemperatureLowerBound;
    int _TemperatureUpperBound;
    int _reportAfterEach60Min;
    int _frequencyOfTemperatureReading1Min;
    int _SimulationHours;
    int currentTimeVal = 0;


    ATRM_Sensors(ArrayList<ArrayList<Integer>> _sharedMemory, AtomicInteger _Finish, AtomicInteger _timeTracker, int _sensorID, int _TemperatureLowerBound, int _TemperatureUpperBound, int _reportAfterEach60Min, int _frequencyOfTemperatureReading1Min, int _SimulationHours) {
        this._sharedMemory = _sharedMemory;
        this._Finish = _Finish;
        this._timeTracker = _timeTracker;
        this._sensorID = _sensorID;
        this._TemperatureLowerBound = _TemperatureLowerBound;
        this._TemperatureUpperBound = _TemperatureUpperBound;
        this._reportAfterEach60Min = _reportAfterEach60Min;
        this._frequencyOfTemperatureReading1Min = _frequencyOfTemperatureReading1Min;
        this._SimulationHours = _SimulationHours;
    }

    @Override
    public void run() {
        while (_recordPass())
            recordTemperature();
    }

    public void recordTemperature() {
        currentTimeVal = _timeTracker.get();

        // we record a temperature through the sensor that is from the lower to the upper bound -100 to 70 inclusive
        int _temperature = (int) (Math.random() * (_TemperatureUpperBound - _TemperatureLowerBound + 1) + _TemperatureLowerBound);
        // we store the temperature in the shared memory for the correct thread
/*         if (_sharedMemory.size() < _sensorID)
            _sharedMemory.add(new ArrayList<Integer>()); */
        if (currentTimeVal > 59 || (_timeTracker.get() == 0 && _Finish.get() == 1)) {
            return;
        }
        _sharedMemory.get(_sensorID - 1).add(_temperature);
        // we rint the sensor id and the temperature they recorded 
        // System.out.println("Sensor " + _sensorID + " was activated.\nTemperature recorded -> " + _temperature + ", Time Stamp: " + currentTimeVal + ".");
        // we wait as long as the minute did not pass yet, before executing a future read of the temperature
        _waitFor60();
    }

    public boolean _recordPass() {
        if (_Finish.get() == 1) {
            return false;
        } 
        return true;
    }

    public void _waitFor60() {
        for(;;) {
            if ((_timeTracker.get() != currentTimeVal && _Finish.get() == 0) || _Finish.get() == 1) {
                break;
            }
        }
    }

}

// make a class that will take care of the reporting processes and phases
class ATRM_Report {

    /*
     *  Strategy:
     *  we will proceed with the array lists by time stamps such that when we set our comparisons 
     *  we will pick from all threads at the value at the same index and we will compare them.
     */

    // let's declare a 2D array where the first column will have the smallest found temperature across sensors 
    // the second column will have the largest found temperature across sensors
    // each row represents a time stamp
    int [][] _temperaturesMinMax;

    ATRM_Report() {
        _temperaturesMinMax = new int[60][2];
    }

    // handlle the output of the report 
    public void OutputReport(int Hour_ID, ArrayList<ArrayList<Integer>> _sharedMemory, int _reportAfterEach60Min, int _frequencyOfTemperatureReading1Min, int _SimulationHours, AtomicInteger _Finish, AtomicInteger _timeTracker, Thread[] ATRMThreads) {
        System.out.println("Report Generation Activated: ----------------------------------------");
        System.out.println("Current Time stamp: " + Hour_ID + "hrs.");

        for (int minute = 0; minute < 60; minute++) {
            int min = 71;
            int max = -101;
            for (int sensor = 0; sensor < 8; sensor++) {
                if (_sharedMemory.get(sensor).get(minute) < min) {
                    min = _sharedMemory.get(sensor).get(minute);
                }
                if (_sharedMemory.get(sensor).get(minute) > max) {
                    max = _sharedMemory.get(sensor).get(minute);
                }
            }
            _temperaturesMinMax[minute][0] = min;
            _temperaturesMinMax[minute][1] = max;
        }

        // we will work with 10 minute intervals
        int interval_difference = 10;
        int iterator = 0;
        int highestTempInterval = 0;
        int intervalHighestTemp = 0;
        int _Inde_Interval = 0;
        // we get the top 5 highest temps and top 5 lowest temperatures for the current
        // hour
        int[] _highets5Temps = new int[5];
        int[] _lowest5Temps = new int[5];
        while (iterator + interval_difference <= 60) {
            int min = 71;
            int max = -101;
            for (int i = iterator; i < iterator + interval_difference; i++) {
                if (_temperaturesMinMax[i][0] < min) {
                    min = _temperaturesMinMax[i][0];
                }
                if (_temperaturesMinMax[i][1] > max) {
                    max = _temperaturesMinMax[i][1];
                }
            }
            intervalHighestTemp = max - min;
            if (intervalHighestTemp > highestTempInterval) {
                highestTempInterval = intervalHighestTemp;
                _Inde_Interval = iterator;
            }
            iterator += interval_difference;
        }
        
        // we iterate over the arraylist and we build an arrayList of temperatures then we pick highest 5 and lowest 
        // 5 temperatures 
        ArrayList<Integer> _allTemps = new ArrayList<Integer>();
        _allTemps = _sharedMemory.stream().flatMap(List::stream).collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(_allTemps);
        for (int i = 0; i < 5; i++) {
            _highets5Temps[i] = _allTemps.get(_allTemps.size() - 1 - i);
            _lowest5Temps[i] = _allTemps.get(i);
        }

        System.out.println("The top 5 highest temperatures are -> " + Arrays.toString(_highets5Temps));
        System.out.println("The top 5 lowest temperatures are -> " + Arrays.toString(_lowest5Temps));
        System.out.println("The 10-min interval of time when the largest temperature difference was observed was -> [" + _Inde_Interval + "Min, " + (_Inde_Interval + interval_difference) + "Min].\nThe temperature difference reached -> " + highestTempInterval + " degrees.");
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

    public static void HoursAndMinutesSimulated_ATRM(
            ArrayList<ArrayList<Integer>> _sharedConcurrentMemory, int _reportAfterEach60Min, int _frequencyOfTemperatureReading1Min, int _SimulationHours, AtomicInteger _Finish, AtomicInteger _timeTracker, Thread[] ATRMThreads) {

        int i = 0;

        while (i < _SimulationHours) {
            // just for safety we assign a break here
            if (i == _SimulationHours) {
                break;
            }

            // for the current hour we execute 60 min
            for (int j = 0; j < 60; j++) {

                if (j == 60) 
                    break;

                // sleep for 10 ms
                try {
                    Thread.sleep(30);
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
            

            if (i + 1 == _SimulationHours) {
                _Finish.set(1);
            }

            // before moving to the next hour let's report the current hour findings
            _reportHandler.OutputReport(i + 1, _sharedConcurrentMemory, _reportAfterEach60Min, _frequencyOfTemperatureReading1Min, _SimulationHours, _Finish, _timeTracker, ATRMThreads);

            // increment the hour
            i++;

        }

        // after all hours of the simulation are elapsed, we update the finish flag to stop the sensors work
        // so we can join the threads and finalize the reporting
        if (i == _SimulationHours) {
            _Finish.set(1);
        }
    }

}