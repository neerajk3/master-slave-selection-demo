package com.demo.masterslave.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.demo.masterslave.zk.ZooKeeperClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SchedulerService is used to perform some task at the regular interval
 * 
 * @author neeraj
 *
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {

    private final ZooKeeperClient zkClient;

    /**
     * This method will be executed at the specified interval
     * and perform the action if the current instance is master else no action
     * 
     */
    @Scheduled(cron = "${scheduler.cron.expression}")
    public void run() {
        if (zkClient.isCurrentNodeMaster()) {
            log.info("I am the master, will do the action");

            // call your methods to perform the action
        }
        else {
            log.info("I am the slave, no action");
        }
    }

}
