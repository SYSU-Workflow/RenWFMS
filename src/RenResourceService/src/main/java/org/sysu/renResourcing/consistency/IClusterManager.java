/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renResourcing.consistency;

import java.util.List;

/**
 * Author: Rinkako
 * Date  : 2018/4/6
 * Usage : Interface for operations of cluster management.
 */
public interface IClusterManager {

    /**
     * Write a string data to a cluster global data node.
     * @param path global data path
     * @param content content of node
     */
    void WriteNodeByString(String path, String content) throws Exception;

    /**
     * Read a string data from a cluster global data node.
     * @param path global data path
     * @return content of node
     */
    String ReadNodeByString(String path) throws Exception;

    /**
     * Write data to a cluster global data node.
     * @param path global data path
     * @param content content of node
     */
    void WriteNode(String path, byte[] content) throws Exception;

    /**
     * Read data from a cluster global data node.
     * @param path global data path
     * @return content of node
     */
    byte[] ReadNode(String path) throws Exception;

    /**
     * Get all children path of a specific data node.
     * @param path global data path
     * @return a List of string of children path
     */
    List<String> GetChildren(String path) throws Exception;

    /**
     * Remove a data node from cluster.
     * @param path global data path
     */
    void RemoveNode(String path) throws Exception;

    /**
     * Check if a data node exists in cluster.
     * @param path global data path
     * @return true if node exist
     */
    boolean Contains(String path) throws Exception;

    /**
     * Lock a global node path.
     * @param path global data path
     */
    void Lock(String path);

    /**
     * Try to lock a global node path.
     * @param path global data path
     * @return true if get locked by this method
     */
    boolean TryLock(String path);

    /**
     * Unlock a global node path.
     * @param path global data path
     */
    void Unlock(String path);

    /**
     * Cluster data namespace.
     */
    String NameSpace = "RenResourceService";
}
