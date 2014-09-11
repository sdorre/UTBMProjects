#include <stdio.h>
#include <signal.h>
#include <unistd.h>
#include <sys/mman.h>
#include <native/task.h>

#define ITER 10
int global = 0;

RT_TASK task1;
RT_TASK task2;

void taskOne(long arg)
{
    int i;
    for (i=0; i < ITER; i++)
    {
	printf("I am taskOne and global = %d................\n", ++global);
    }
}
void taskTwo(long arg)
{
    int i;
    for (i=0; i < ITER; i++)
    {
	printf("I am taskTwo and global = %d----------------\n", --global);
    }
}

int main(int argc, char* argv[])
{
/* Avoids memory swapping for this program */
    mlockall(MCL_CURRENT|MCL_FUTURE);
/*
 * Arguments: &task,
 * name,
 * stack size (0=default),
 * priority,
 * mode (FPU, start suspended, ...)
 */
    rt_task_create(&task1, "trivialT1", 0, 99, 0);
    rt_task_create(&task2, "trivialT2", 0, 99, 0);
/*
 * Arguments: &task,
 * task function,
 * function argument
 */
    rt_task_start(&task1, &taskOne, NULL);
    rt_task_start(&task2, &taskTwo, NULL);
    pause();
    rt_task_delete(&taskOne);
    rt_task_delete(&taskTwo);
}
