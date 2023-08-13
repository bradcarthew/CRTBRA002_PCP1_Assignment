package ParallelMonteCarloMini;

import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchParallel extends RecursiveTask<Integer[]>{
    private static int SEQUENTIAL_CUTOFF; //number of searches to be performed sequentially
    private static final AtomicInteger nextId = new AtomicInteger(1); //provides Ids to searches

    private final int num_searches; //number of searches
    private final int rows, columns; //x and y terrain limits
    private int Id = 0; //search identifier
    private int pos_row, pos_col; //position in grid
    private Integer[] local_min = new Integer[3]; //local minimum constituting [height, pos_row, pos_col]

    private final TerrainArea terrain; //object to store the heights and grid points visited by parallel searches
    private final Random rand; //random number generator

    enum Direction {
        STAY_HERE,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    //constructor
    public SearchParallel(int num_searches, int rows, int columns, TerrainArea terrain, Random rand) {
        this.num_searches = num_searches;
        this.rows = rows;
        this.columns = columns;
        this.rand = rand;
        this.terrain = terrain;

        //only assign Ids and random starting positions to objects performing searches
        if (num_searches <= SEQUENTIAL_CUTOFF) {
            this.pos_row = rand.nextInt(rows);
            this.pos_col = rand.nextInt(columns);
            this.Id = nextId.getAndIncrement();
        }
    }

    public Integer[] compute() {
        if (num_searches <= SEQUENTIAL_CUTOFF) {
            this.find_valleys();
            //create search parallel object to perform sequential searches
            SearchParallel sequential_search = new SearchParallel(1, rows, columns, terrain, rand);

            //find local minimum sequentially
            for (int i = 0; i < num_searches-1; i++) {
                sequential_search.find_valleys();

                if (sequential_search.local_min[0] < this.local_min[0]) {
                    this.local_min = sequential_search.local_min;
                }

                sequential_search.reset_sequential_search(rand.nextInt(rows), rand.nextInt(columns));
            }

            //return lowest local minimum
            return this.local_min;
        } else {
            int split = (int) (num_searches / 2.0);
            //create left and right objects
            SearchParallel left = new SearchParallel(split, rows, columns, terrain, rand);
            SearchParallel right = new SearchParallel(num_searches-split, rows, columns, terrain, rand);
            //create new task for left search
            left.fork();
            //compute local minimum within context of current task
            Integer[] right_local_min = right.compute();

            //wait for result from left task
            Integer[] left_local_min = left.join();

            //compare left and right local minimums
            if (right_local_min[0] < left_local_min[0]) {
                return right_local_min;
            }
            else {
                return left_local_min;
            }
        }
    }

    //find and assign local minimum of search parallel object
    public void find_valleys() {
        int height = Integer.MAX_VALUE;
        Direction next = Direction.STAY_HERE;
        while(terrain.visited(pos_row, pos_col)==0) { // stop when hit existing path
            height = terrain.get_height(pos_row, pos_col);
            terrain.mark_visited(pos_row, pos_col, Id); //mark current position as visited
            next = terrain.next_step(pos_row, pos_col);
            switch(next) {
                case STAY_HERE:
                    local_min = new Integer[]{height, pos_row, pos_col};
                    break;
                case LEFT:
                    pos_row--;
                    break;
                case RIGHT:
                    pos_row=pos_row+1;
                    break;
                case UP:
                    pos_col=pos_col-1;
                    break;
                case DOWN:
                    pos_col=pos_col+1;
                    break;
            }
        }
        local_min = new Integer[]{height, pos_row, pos_col};
    }

    //reset the position, Id and local minimum of sequential search object
    private void reset_sequential_search(int pos_row, int pos_col) {
        this.Id = nextId.getAndIncrement();
        this.pos_row = pos_row;
        this.pos_col = pos_col;
        this.local_min = new Integer[3];
    }

    //set the sequential cutoff value
    public void setSEQUENTIAL_CUTOFF(int sequentialCutoff) {
        SEQUENTIAL_CUTOFF = sequentialCutoff;
    }
}