/**
 * 
 */
package com.demo.masterslave.zk;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * ZkNode- The data about the current node - currentMasterPath, current node path
 * and is current node leader
 *
 * @author neeraj
 *
 */
@Component
@Data
public class ZkNode {

    private String currentMasterPath;

    private String myPath;

    private volatile boolean myselfMaster;

}
