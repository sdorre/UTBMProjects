// vim: ai et ts=4 sts=4 sw=4 ft=cpp

#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <string.h>
#include <math.h>
#include <mpi.h>
#include <unistd.h>

#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))

int REQUEST = 0;
int TOKEN = 2;

int _rank, _numprocs, _ret;

double _time = 0.0f;

// last known site which want to access
int last=-1;

// successor which want the ressource
int next=-1;

// boolean, true if I want to access to the critical ressource
int applicant=0;

//boolean, true if I take the token
int privilege=0;

int process_master() {
    return 0;
}

int process_slave() {
    return 0;
}

/** function to simulate a random access to the ressource **/
int random_access(){
    int result=0;

    int r = rand()%_numprocs;
    if(_rank==r){
        result=1;
    }
    return result;
}

/** query to the ressource **/
void want_token(){
    applicant = 1;
    
    last=0;
    if(last!=-1){
        _time = MPI_Wtime();
        printf("[%f] Send REQUEST! to %i from %i\n", _time, last, _rank);
        MPI_Send(NULL, 0, MPI_INT, last, REQUEST, MPI_COMM_WORLD);
        last=-1;
    }
    //wait for privilege with true
    
}

void release_token(){
    //release ressource
    applicant=0;

    // send to the next applicant, come back to initial state and delete the next applicant
    if(next!=-1){
        _time = MPI_Wtime();
        printf("[%f] Send TOKEN! to %i from %i\n", _time, next, _rank);
        MPI_Send(NULL, 0, MPI_INT, next, TOKEN, MPI_COMM_WORLD);
        next=-1;
        privilege=0;
    }
}


void process() {
    MPI_Status status;
    int i, ok=1;
    
    int running = 1;
   
    while(running) {
        //receive any message and work with type of received message
        MPI_Recv(NULL, 0, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);

        int source = status.MPI_SOURCE;
        
        if(status.MPI_TAG== REQUEST) {
                _time = MPI_Wtime();
                printf("[%f] Got REQUEST! at %i from %i\n", _time, _rank, source);

                if(last==-1){
                    if(applicant){
                        next = source;
                    }else{
                        _time = MPI_Wtime();
                        printf("[%f] Send TOKEN from %i to %i\n", _time, _rank, source);
                        MPI_Send(NULL, 0, MPI_INT, source, TOKEN, MPI_COMM_WORLD);
                        privilege=0;
                    }
                }else{
                    last=source;
                    _time = MPI_Wtime();
                    printf("[%f] Send TOKEN from %i to %i\n", _time, _rank, last);
                
                    // send token to the last known applicant
                    MPI_Send(NULL, 0, MPI_INT, last, REQUEST, MPI_COMM_WORLD);
                }


        }else if (status.MPI_TAG==TOKEN){
                
            // I receive the token
            _time = MPI_Wtime();
            printf("[%f] Got TOKEN! at %i from %i\n", _time, _rank, source);

            privilege=1;

        } else {
        }
    }
}


int main(int argc, char *argv[]) {


    srand(time(NULL));
    
    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &_numprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &_rank);

    if(random_access()==1){
        printf("process %d - ask for ressource\n", _rank);
        want_token();
    }else{
        printf("process %d - don't want critical ressource\n", _rank);
    }

    process();

    if(_rank == 0)
        _ret = process_master();
    else
        _ret = process_slave();

    if(_ret != 0) goto end;

end:
    MPI_Finalize();
}
