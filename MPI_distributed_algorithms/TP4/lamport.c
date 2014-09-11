// this algorithm require (N-1) REQUEST, (N-1) ACK, and (N-1) FREE 
// --> total of 3(N-1) messages 
//
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <mpi.h>
#include <unistd.h>

#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))

//tag used
int REQUEST = 0;
int FREE = 1;
int ACK = 2;

struc queue_item {
    int tag;
    int origin;
    int clock;
};

int _rank, _numprocs, _ret;

int _clock, _requested;
double _time = 0.0f;

struct queue_item* _queue;


int process_master() {
    return 0;
}

int process_slave() {
    return 0;
}

/** function to get a pseudo random access to the ressource **/
void random_requested() {

    int i;
    _requested = (_rank == 2 || _rank ==0);

    if (!_requested) return;

    _time = MPI_Wtime();
    printf("[%f] REQUEST from %i\n", _time, _rank);

    // Broadcast a request
    for(i = 0; i < _numprocs; i++)
        if (i != _rank)
            MPI_Send(&_clock, 1, MPI_INT, i, REQUEST, MPI_COMM_WORLD);

    // Add message in the local queue
    struct queue_item item;
    item.tag = REQUEST;
    item.origin = _rank;
    item.clock = _clock;
    _queue[_rank] = item;

    // update of the local clock
    _clock++;
}

void process() {
    MPI_Status status;
    int i, ok=1;

    // Clock Initialization
    _clock = 0;

    // Initialization of the local queue
    _queue = malloc(_numprocs*sizeof(struct queue_item));
    for(i=0; i<_numprocs; i++){
        _queue[i].tag = FREE;
        _queue[i].origin = i;
        _queue[i].clock=0;
    }

    // procedure to call access to the critical ressource
    random_requested();
    int count = 0;
    int running = 1;
   
    while(running) {
        int remote_clock;
        MPI_Recv(&remote_clock, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);

        int source = status.MPI_SOURCE;
        
        if(status.MPI_TAG== REQUEST) {
                _time = MPI_Wtime();
                printf("[%f] Got REQUEST! at %i from %i\n", _time, _rank, source);

                // Fix local clock
                _clock = MAX(_clock,remote_clock) + 1;

                // Add message in the local queue
                struct queue_item itemr;
                itemr.tag = REQUEST;
                itemr.origin = source;
                itemr.clock = remote_clock;
                _queue[source] = itemr;

                _time = MPI_Wtime();
                printf("[%f] Send ACK from %i to %i\n", _time, _rank, source);

                // ackowledge the reception of the request
                MPI_Send(&_clock, 1, MPI_INT, source, ACK, MPI_COMM_WORLD);

                _clock++;

        }else if (status.MPI_TAG==ACK){
                
            _time = MPI_Wtime();
            printf("[%f] Got ACK! at %i from %i\n", _time, _rank, source);
            count++;

            // Fix local clock
            _clock = MAX(_clock,remote_clock) + 1;

            if(_queue[source].tag != REQUEST) {
                // Add message in the local queue
                struct queue_item itemaa;
                itemaa.tag = ACK;
                itemaa.origin = source;
                itemaa.clock = remote_clock;
                _queue[source] = itemaa;
            }

            //if i request an access and all process send me back an ACK
            if(_requested && count==(_numprocs-1)){
                   
                // and if I'm the first in the local queue, it's OK, I can take the ressource
                for(i=0;i< _numprocs;i++){
                    //printf(" rank %d tag:%d clock:%d\n", i, _queue[i].tag, _queue[i].clock);
                    if(i!=_rank && _queue[i].tag==REQUEST && _queue[i].clock>_queue[_rank].clock){
                        ok=0;
                    }
                }
                    
                if(ok){
                    _time = MPI_Wtime();
                    printf("[%f] Access to the critical ressource by %i\n", _time, _rank);

                    // free the ressource
                    for(i = 0; i < _numprocs; i++)
                        if (i != _rank)
                            MPI_Send(&_clock, 1, MPI_INT, i, FREE, MPI_COMM_WORLD);

                    _requested = 0;
                    count=0;
                    
                    // Add message in the local queue
                    struct queue_item itema;
                    itema.tag = FREE;
                    itema.origin = _rank;
                    itema.clock = _clock;
                    _queue[_rank] = itema;

                    // update the local clock
                    _clock++;
                }
            }


        }else if (status.MPI_TAG ==FREE){

                _time = MPI_Wtime();
                printf("[%f] Got FREE! at %i from %i\n", _time, _rank, source);

                // Fix local clock
                _clock = MAX(_clock,remote_clock) + 1;

                // Ajout du message dans la file locale
                struct queue_item itemf;
                itemf.tag = FREE;
                itemf.origin = source;
                itemf.clock = remote_clock;
                _queue[source] = itemf;
                
                //if there are other process that access to the ressource before
                //we need to wait until it frees the ressource
                if(_requested && ok==0){
                    _time = MPI_Wtime();
                    printf("[%f] Accès à la ressource de %i\n", _time, _rank);

                    // Frre the critical ressource
                    for(i = 0; i < _numprocs; i++)
                        if (i != _rank)
                            MPI_Send(&_clock, 1, MPI_INT, i, FREE, MPI_COMM_WORLD);

                }


        } else {
        }
    }
}


int main(int argc, char *argv[]) {

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &_numprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &_rank);

    process();

    if(_rank == 0)
        _ret = process_master();
    else
        _ret = process_slave();

    if(_ret != 0) goto end;

end:
    MPI_Finalize();
}
