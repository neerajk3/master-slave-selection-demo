master-slave-selection-demo

## Introduction
   This is a Spring Boot based application with scheduling capability where we intent to run scheduler in one of the running instances.
   We will use Zookeeper to select master instance and master alone would perform the scheduling task.
    
    

## Prerequisite to run the application -
    -Java 8
    -Zookeeper
    -gradle

## How to run -
    Run the multiple instance of this application. If you are running on local machine then run on different port.
      - Application port is configured in application.properties->server.port

    When you run the application for the first time then a Persistent ZK node(parent node) with name "master-slave-selection-demo" will be created with watch enabled.
     - To verify, connect to zkcli and execute  - 
     
     [zk: localhost:2181(CONNECTED) 0] ls / 
     output -  [....... ,master-slave-selection-demo] , lis of nodes present at path /

    For every application instance one Sequential Ephemeral ZK node will be created in parent node.
     -To verify, connect to zkcli and execute  - ls /master-slave-selection-demo
     
           [zk: localhost:2181(CONNECTED) 0] ls /master-slave-selection-demo
           output - [master-slave-selection-demo_0000000001.........] - list of all child nodes, which will be in
       "master-slave-selection-demo_<10 digit sequence number>" format.
       

    Ephemeral znode are alive until the client is alive.Therefore, when the client gets a disconnect from the zookeeper, it will also be deleted.
    Any change parent node space - child node(Ephemeral znode) addition/deletion will be notified to all the running instance and there will leader
    re-election.Instance with the lowest Sequence number will be selected as master and other will be slave.


## How to verify-
    The instance having Ephemeral znode with lowest sequence number will be the master and would run the scheduler to perfrom action
    you can check for this log statement in logs - "I am the master, will do the action"
