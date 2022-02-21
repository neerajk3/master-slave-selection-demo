package com.demo.masterslave.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author neeraj
 *
 */
@Component
@Slf4j
public class ZooKeeperClient implements Watcher {

    private static final String FORWARD_SLASH = "/";

    private static final String NODE_NAME_PREFIX = "master-slave-selection-demo_";

    @Value("${zookeeper.zkNode.path}")
    private String parentNodePath;

    @Value("${zookeeper.servers}")
    private String zooKeeperServer;

    @Value("${zookeeper.session.timeout}")
    private int sessionTimeout;

    private ZooKeeper zooKeeper;

    @Autowired
    private ZkNode zkNode;

    /**
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    @PostConstruct
    public void registerNode() throws IOException, InterruptedException, KeeperException {
        log.debug("Connecting to zooKeeper server:{}", zooKeeperServer);
        zooKeeper = new ZooKeeper(zooKeeperServer, sessionTimeout, this);
        // registering current node in the parent node name space
        register();

    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {

        case NodeChildrenChanged:
        case NodeCreated:
        case NodeDataChanged:
        case NodeDeleted:
            log.info("NodeEvent - {},path:{}", event.getType(), event.getPath());
            try {
                electMaster();
            } catch (InterruptedException | KeeperException e) {
                log.error("Exception occurred while selecting master, exception:{}", e.getMessage());
            }
            break;

        case None:
            switch (event.getState()) {

            case Disconnected:
                break;

            case Expired:
                break;

            case SyncConnected:
                break;

            default:
                log.info("Unknown event state");
            }
        }
    }

    public boolean isCurrentNodeMaster() {
        return zkNode.isMyselfMaster();
    }

    /**
     * create parent zookeeper node(Persistent node) if doesn't exist and
     * register the current instance of application as Ephemeral node and elect
     * master among the ephemeral child nodes
     * 
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void register() throws KeeperException, InterruptedException {

        /**
         * check parent zkNode exists if not then create persistent node and
         * starting watching the node
         */
        Stat stat = zooKeeper.exists(parentNodePath, true);
        if (stat == null) {
            log.info("parent zkNode:{} doesn't exist so creating it", parentNodePath);
            String parentNode = zooKeeper.create(parentNodePath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            log.info("parent zkNode:{} created", parentNode);
        }

        /**
         * 
         * register the current instance of application as Ephemeral node.
         * 
         * An ephemeral node will be removed by the ZooKeeper automatically when
         * the session associated with the creation of the node expires.
         * 
         */
        String childPath = parentNodePath + FORWARD_SLASH + NODE_NAME_PREFIX;

        log.debug("creating child zkNode at:{}", childPath);
        childPath = zooKeeper.create(childPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        log.info("child zkNode:{}, created", childPath);
        zkNode.setMyPath(childPath);

        // Re-electing master after adding the current node
        electMaster();
    }

    /**
     * Elect master among the child nodes
     */
    private void electMaster() throws InterruptedException, KeeperException {

        // get all the child nodes present at the specified parent node path
        List<String> children = zooKeeper.getChildren(parentNodePath, true);

        Collections.sort(children);

        // ephemeral master node selection
        String masterAmongNodes = parentNodePath + FORWARD_SLASH + children.get(0);

        Stat status = zooKeeper.exists(masterAmongNodes, false);

        log.info("current master zkNode is :{}, zknode id:{}", masterAmongNodes, status.getEphemeralOwner());

        updateCurrentNodeStatus(masterAmongNodes);
    }

    /**
     * 
     * this method updates current node status as master/slave
     * 
     * @param masterAmongNodes
     * 
     */
    private void updateCurrentNodeStatus(String masterAmongNodes) {
        if (StringUtils.equals(zkNode.getMyPath(), masterAmongNodes)) {
            zkNode.setMyselfMaster(true);
        }
        else {
            zkNode.setMyselfMaster(false);
        }
        zkNode.setCurrentMasterPath(masterAmongNodes);
        log.info("am I master:{}", zkNode.isMyselfMaster());
    }

}
