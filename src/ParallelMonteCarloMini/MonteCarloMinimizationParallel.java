package ParallelMonteCarloMini;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class MonteCarloMinimizationParallel {
    static long startTime = 0;
    static long endTime = 0;

    //timers
    private static void tick() { startTime = System.currentTimeMillis(); }
    private static void tock() { endTime = System.currentTimeMillis(); }

    public static void main(String[] args) {
        int rows, columns; //grid size
        double xmin, xmax, ymin, ymax; //x and y terrain limits
        TerrainArea terrain; //object to store the heights and grid points visited by parallel searches
        double searches_density; //number of Monte Carlo searches per grid position

        int num_searches; //number of searches
        int num_cores; //number of available processors
        Random rand = new Random(); //random number generator
        Integer[] global_min = new Integer[3]; //global minimum constituting [height, pos_row, pos_col]

        if (args.length!=7) {
            System.out.println("Incorrect number of command line arguments provided.");
            System.exit(0);
        }

        /* Read argument values */
        rows = Integer.parseInt(args[0]);
        columns = Integer.parseInt(args[1]);
        xmin = Double.parseDouble(args[2]);
        xmax = Double.parseDouble(args[3]);
        ymin = Double.parseDouble(args[4]);
        ymax = Double.parseDouble(args[5]);
        searches_density = Double.parseDouble(args[6]);

        // initialize
        terrain = new TerrainArea(rows, columns, xmin, xmax, ymin, ymax);
        num_searches = (int)(rows*columns*searches_density);
        num_cores = Runtime.getRuntime().availableProcessors();
        ForkJoinPool pool = new ForkJoinPool(num_cores);
        SearchParallel searches = new SearchParallel(num_searches, rows, columns, terrain, rand);

        // set sequential cutoff
        searches.setSEQUENTIAL_CUTOFF(Math.max((int)(num_searches/(num_cores*2)),1));

        //start timer
        tick();

        //find global minimum
        global_min = pool.invoke(searches);

        //end timer
        tock();

        /*Run parameters */
        System.out.printf("Run parameters\n");
        System.out.printf("\t Rows: %d, Columns: %d\n", rows, columns);
        System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax );
        System.out.printf("\t Search density: %f (%d searches)\n", searches_density,num_searches );

        /*  Total computation time */
        System.out.printf("Time: %d ms\n",endTime - startTime );
        int tmp = terrain.getGrid_points_visited();
        System.out.printf("Grid points visited: %d  (%2.0f%s)\n",tmp,(tmp/(rows*columns*1.0))*100.0, "%");
        tmp = terrain.getGrid_points_evaluated();
        System.out.printf("Grid points evaluated: %d  (%2.0f%s)\n",tmp,(tmp/(rows*columns*1.0))*100.0, "%");

        /* Results */
        System.out.printf("Global minimum: %d at x=%.1f y=%.1f\n\n", global_min[0], terrain.getXcoord(global_min[1]), terrain.getYcoord(global_min[2]));
    }
}