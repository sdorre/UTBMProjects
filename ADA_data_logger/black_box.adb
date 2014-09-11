with Ada.Text_IO;
use Ada.Text_IO;

with Ada.Numerics.Discrete_Random;
with Ada.Strings.Unbounded;
use Ada.Strings.Unbounded;

procedure black_box is
  
    type Rand_Range is range 1..100;
    package Rand_Int is  new Ada.Numerics.Discrete_Random(Rand_Range);

    running : Boolean := true;
    clk_value : Natural := 0;


    task clock is
        entry init( tick : in Duration);
    end clock;

    task body clock is
        tmp : Duration;
    begin
        accept init( tick : in Duration) do
            tmp := tick;
        end init;

        while (running) loop
            delay(tmp);
            clk_value := clk_value + 1;
        end loop;
    end clock;


    task log_generator is
        entry write(message: in String);
    end log_generator;

    task body log_generator is
        file : File_type;
    begin
        put_line("wait for data");

        Create(file, Out_File, "black_box.log");
        while (running) loop
            accept write (message : in String )do
                Put_Line(file, message);
            end write;
        end loop;
        Close(file);
    end log_generator;


    task type sensor is
        entry init( name1 : in String; period1 : in Duration);
    end sensor;

    task body sensor is
        name : Unbounded_String;
        period : Duration;
        seed : Rand_Int.Generator;
        value : Rand_Range;
    begin
        accept init( name1 : in String; period1 : in Duration) do
            name := to_unbounded_string(name1);
            period := period1;
            Rand_Int.reset(seed);
        end init;

        while(running) loop
            value := Rand_Int.random(seed);
            log_generator.write("[" & Natural'Image(clk_value) & "] - " & to_string(name) & " : " & Rand_Range'Image(value));
            delay period;
        end loop;
    end sensor;

  
    task1, task2, task3 : sensor;

begin
    put_line("clock initialization"); 
    clock.init(0.0001);
    put_line("task 1 initialization"); 
    task1.init("one", 1.0);
    put_line("task 2 initialization"); 
    task2.init("two", 0.1);
    put_line("task 3 initialization"); 
    task3.init("three", 0.03);

    put_line("this is the main program");

    delay 4.0;
    running := false;
    log_generator.write("");
    put_line("end."); 
end black_box;
