package concurrencytest;

import java.util.Calendar;

// class for data to be processed
class LotOfData {
    double[] x;     // base
    double[] n;     // exponent
    double[] y;     // result (y = x^n)
    int sub;        // size of each logical subarray
    int next;       // starting index for the next chunk      
    int remaining;  // elements waiting to be processed       
    
    // constructor receives the sizes of arrays
    public LotOfData(int size) {  
        x = new double[size];               // size of array limited to ~2.14 bil
        n = new double[size];
        y = new double[size];
        sub = 1000;                         // default to 1000
        next = 0;                           
        remaining = size;                   
        
        // read in random double (base) values to array x
        //System.out.print("Creating random doubles for bases...");
        for(int i=0; i<size; i++) 
            x[i] = Math.random() * 240.0 + 10.0;
        //System.out.print("done. ");

        // read in random double (exp) values to array n
        //System.out.print("Creating random doubles for exponents...");
        for(int j=0; j<size; j++)
            n[j] = Math.random() * 20.0 + 5.0;
        //System.out.println("done.");
    }
    
    // For the threads to retrieve the indices of the next chunk
    synchronized public void passIndex(TwoVarComp tvc) {
        if(remaining == 0) tvc.done = true;     // if no more calculations we're done
        else {                                  // else get start index
            tvc.a = next;                       
            if(remaining < sub) {               // if # remaining is less than sub size
                tvc.b = x.length;                // stop at end of array, and no
                remaining = 0;                   // further calculations will be done
            }                                    // after that
            else {                              // otherwise
                tvc.b = next+=sub;               // break off another chunk
                remaining-=sub;                  // decrease # remaining
            }
        }
        notifyAll();
    }
}

// Two-variable computation class
class TwoVarComp implements Runnable {
    LotOfData ref;      // reference to the data to be processed
    int a, b;          // start index and end boundry        
    Thread thr;
    boolean done = false;
    
    TwoVarComp(LotOfData lod) {
        ref = lod;
        thr = new Thread(this);
        thr.start();
    }
    
    public void run() {
        //System.out.println(thr.getName() + " going to work.");
        ref.passIndex(this);
        while(!done) {
            for(int i=a; i<b; i++)
                ref.y[i] = Math.pow(ref.x[i], ref.n[i]);
            ref.passIndex(this);
        }
    }
}

public class ConcurrencyTest {
    // div: size of subarrays and t: # of threads to be employed
    public static void test(int div, int t) { 
        int N = 10000000;                        // size of the arrays
        long start, stop;                        // start/stop times for the job 
        LotOfData lod = new LotOfData(N);
        TwoVarComp[] threads = new TwoVarComp[t];            // set of t threads
        
        lod.sub = div;                       // set the subarray size to be used
        
        start = Calendar.getInstance().getTimeInMillis();
        
        for(int i=0; i<t; i++) 
            threads[i] = new TwoVarComp(lod);            // unleash the threads!
        try {
            for(int j=0; j<t; j++)
                threads[j].thr.join();      // suspend main until threads finish
        } catch(InterruptedException exc) { 
            System.out.println("Main interrupted"); }
        
        stop = Calendar.getInstance().getTimeInMillis();
        
        //System.out.println("Work done!");
        System.out.println("To process the data set of size 10,000,000 in chunks"
                + " of " + div + " by " + t + " threads took " + (stop - start)
                + " milliseconds.");
    }
    
    public static void main(String[] args) {
        
        test(1000, 1); test(1000, 2);               // call test() with the
        test(1000, 4); test(1000, 8);               // subarray size and
                                                    // # of threads to use
        test(10000, 1); test(10000, 2);             
        test(10000, 4); test(10000, 8);
        
        test(100000, 1); test(100000, 2);
        test(100000, 4); test(100000, 8);
        
        test(1000000, 1); test(1000000, 2);
        test(1000000, 4); test(1000000, 8); 
    }
}
/*
To process the data set of size 10,000,000 in chunks of 1000 by 1 threads took 1417 milliseconds.
To process the data set of size 10,000,000 in chunks of 1000 by 2 threads took 785 milliseconds.
To process the data set of size 10,000,000 in chunks of 1000 by 4 threads took 768 milliseconds.
To process the data set of size 10,000,000 in chunks of 1000 by 8 threads took 782 milliseconds.
To process the data set of size 10,000,000 in chunks of 10000 by 1 threads took 1327 milliseconds.
To process the data set of size 10,000,000 in chunks of 10000 by 2 threads took 835 milliseconds.
To process the data set of size 10,000,000 in chunks of 10000 by 4 threads took 755 milliseconds.
To process the data set of size 10,000,000 in chunks of 10000 by 8 threads took 748 milliseconds.
To process the data set of size 10,000,000 in chunks of 100000 by 1 threads took 1289 milliseconds.
To process the data set of size 10,000,000 in chunks of 100000 by 2 threads took 760 milliseconds.
To process the data set of size 10,000,000 in chunks of 100000 by 4 threads took 775 milliseconds.
To process the data set of size 10,000,000 in chunks of 100000 by 8 threads took 783 milliseconds.
To process the data set of size 10,000,000 in chunks of 1000000 by 1 threads took 1358 milliseconds.
To process the data set of size 10,000,000 in chunks of 1000000 by 2 threads took 775 milliseconds.
To process the data set of size 10,000,000 in chunks of 1000000 by 4 threads took 766 milliseconds.
To process the data set of size 10,000,000 in chunks of 1000000 by 8 threads took 760 milliseconds.

Slowest with only 1 thread on the job; 2, 4 or 8 threads take about the same amount of time.
Random values are generated for each test, but I believe 2 threads to be fastest.
Middle chunk sizes of 10,000 or 100,000 appear slightly quicker.
*/