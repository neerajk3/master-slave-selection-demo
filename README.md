master-slave-selection-demo

## Introduction
   This is a Spring Boot based application with scheduling capability where we intent to run scheduler in one of the running instances.
   Zookeeper is being used to select master and master alone would perform the scheduling task.
    
    

## Prerequisite to run the application -
    -Java 8
    -Zookeeper
    -gradle

## How to run -
    Run the multiple instance of this application. If you are running on local machine then run on different ports.
      - Application port is configured in application.properties->server.port

    When you run the application for the first time, a Persistent zk node(parent node) with name "master-slave-selection-demo" will be created having watch enabled.
     
     - To verify, connect to zkcli and execute  - ls / 
     
     [zk: localhost:2181(CONNECTED) 0] ls / 
     output -  [....... ,master-slave-selection-demo] , lis of nodes present at path /

    For every application instance, one Ephemeral Sequential zk node will be created under parent node.
    
     -To verify, connect to zkcli and execute  - ls /master-slave-selection-demo
     
           [zk: localhost:2181(CONNECTED) 0] ls /master-slave-selection-demo
           output - [master-slave-selection-demo_0000000001.........] - list of all child nodes in "master-slave-selection-demo_<10 digit sequence number>"     format.
       

    Ephemeral zknode is attached to client life span. Ephemeral zknode gets pursed once client dies.
    
    Any change parent node space be it addition/deletion of child node(Ephemeral zknode) shall be notified to all the running instance and master re-election process kicks in.Instance with the lowest sequence number will be elected as master.
    
 

## How to verify-
    One can look for log statement - “am I master:true” to indentify master instance. Similarly, for slave it would be “am I master:false”
    
    Following logs can be searched to check master action – "I am the master, will do the action".

  Same way, for slave one can look for  "I am the slave, no action"
