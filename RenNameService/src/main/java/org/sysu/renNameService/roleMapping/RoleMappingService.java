/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService.roleMapping;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sysu.renNameService.entity.RenRolemapArchivedEntity;
import org.sysu.renNameService.entity.RenRolemapEntity;
import org.sysu.renNameService.utility.HibernateUtil;
import org.sysu.renNameService.utility.LogUtil;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import static org.sysu.renNameService.roleMapping.RoleMapParser.Parse;

/**
 * Author: Rinkako
 * Date  : 2018/1/16
 * Usage : All business role map service will be handled in this service module.
 */
public final class RoleMappingService {
    // For Resource Service

    /**
     * Get resource description JSON involved in a process.
     * @param rtid process rtid
     * @return String of COrgan response JSON
     */
    public static String GetInvolvedResource(String rtid) {
        return null;
    }

    /**
     * Get global id of organization workers by business role name.
     * @param rtid process rtid
     * @param bRoleName business role id name
     * @return ArrayList of Worker global id string
     */
    public static ArrayList<String> GetWorkerByBusinessRole(String rtid, String bRoleName) {
        return null;
    }

    /**
     * Get business role name by a organization entity global id.
     * @param rtid process rtid
     * @param globalId organization entity global id
     * @return ArrayList of business role name string
     */
    public static ArrayList<String> GetBusinessRoleByGlobalId(String rtid, String globalId) {
        return null;
    }

    // For BO Engine

    /**
     * Finish role mapping service and dispose cache.
     * @param rtid process rtid
     */
    @SuppressWarnings("unchecked")
    public static void FinishRoleMapService(String rtid) {
        // remove cache
        RoleMapCachePool.Remove(rtid);
        // remove relations in steady memory
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            List qRet = session.createQuery(String.format("from RenRolemapEntity where rtid = '%s'", rtid)).list();
            for (Object rre : qRet) {
                RenRolemapArchivedEntity rrae = RoleMappingService.AchieveRoleMap((RenRolemapEntity) rre);
                session.save(rrae);
                session.delete(rre);
            }
            transaction.commit();
        }
        catch (Exception ex) {
            LogUtil.Log("When finish role map service, exception occurred, " + ex.toString() + ", service rollback",
                    RoleMappingService.class.getName(), LogUtil.LogLevelType.ERROR);
            transaction.rollback();
        }
    }

    // For Master Control Panel

    /**
     * Register role map service for a specific process runtime.
     * @param rtid process rtid
     * @param descriptor register parameter descriptor string
     */
    public static void RegisterRoleMapService(String rtid, String descriptor) {
        ArrayList<AbstractMap.SimpleEntry<String, String>> parsedList = RoleMapParser.Parse(descriptor);
        for (AbstractMap.SimpleEntry<String, String> kvp : parsedList) {
            // todo
        }
    }

    /**
     * Generate an achieve role map entity.
     * @param rre role map entity
     * @return role map achieved entity
     */
    private static RenRolemapArchivedEntity AchieveRoleMap(RenRolemapEntity rre) {
        RenRolemapArchivedEntity rrae = new RenRolemapArchivedEntity();
        rrae.setMapId(rre.getMapId());
        rrae.setRtid(rre.getRtid());
        rrae.setBroleName(rre.getBroleName());
        rrae.setCorganGid(rre.getCorganGid());
        rrae.setMappedGid(rre.getMappedGid());
        rrae.setDataVersion(rre.getDataVersion());
        rrae.setTransactionIsolation(rre.getTransactionIsolation());
        return rrae;
    }
}
