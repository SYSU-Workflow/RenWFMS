/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService.nameSpacing;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sysu.renNameService.entity.RenBoEntity;
import org.sysu.renNameService.entity.RenProcessEntity;
import org.sysu.renNameService.entity.RenRuntimerecordEntity;
import org.sysu.renNameService.utility.HibernateUtil;
import org.sysu.renNameService.utility.LogUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Author: Rinkako
 * Date  : 2018/1/26
 * Usage : All name space service will be handled in this service module.
 */
public class NameSpacingService {
    /**
     * Generate a new RTID for new process launching.
     * @return rtid string
     */
    public static String GenerateRTID() {
        return String.format("RTID_%s", UUID.randomUUID());
    }

    /**
     * Create a new process.
     * @param renid creator renid
     * @param processName process unique name for a specific renid
     * @param mainBOName process entry point BO name
     * @return process pid
     */
    public static String CreateProcess(String renid, String processName, String mainBOName) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            RenProcessEntity rpe = new RenProcessEntity();
            String pid = "Process_" + UUID.randomUUID().toString();
            rpe.setPid(pid);
            rpe.setCreateTimestamp(new Timestamp(System.currentTimeMillis()));
            rpe.setCreatorRenid(renid);
            rpe.setMainBo(mainBOName);
            rpe.setProcessName(processName);
            session.save(rpe);
            transaction.commit();
            return pid;
        }
        catch (Exception ex) {
            LogUtil.Log("Create process but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            transaction.rollback();
        }
        return "";
    }

    /**
     * Upload a BO for a specific process
     * @param pid belong to pid
     * @param name BO name
     * @param content BO content string
     * @return boid
     */
    public static String UploadBOContent(String pid, String name, String content) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            String boid = "BO_" + UUID.randomUUID().toString();
            RenBoEntity rbe = new RenBoEntity();
            rbe.setBoid(boid);
            rbe.setPid(pid);
            rbe.setBoName(name);
            rbe.setBoContent(content);
            session.save(rbe);
            transaction.commit();
            return boid;
        }
        catch (Exception ex) {
            LogUtil.Log("Upload BO but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            transaction.rollback();
        }
        return "";
    }

    /**
     * Get all processes of one ren user.
     * @param renid ren user id
     * @return a list of process
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<RenProcessEntity> GetProcessByRenId(String renid) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            ArrayList<RenProcessEntity> qRet = (ArrayList<RenProcessEntity>) session.createQuery(String.format("FROM RenProcessEntity WHERE creator_renid = '%s' AND state = 0", renid)).list();
            transaction.commit();
            return qRet;
        }
        catch (Exception ex) {
            LogUtil.Log("Get Processes of Ren User but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            transaction.rollback();
        }
        return null;
    }

    /**
     * Get the BOs in a process.
     * @param pid process id
     * @return a list of BO in the specific process
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Object> GetProcessBONameList(String pid) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            ArrayList<Object> qRet = (ArrayList<Object>) session.createQuery(String.format("SELECT boid, bo_name FROM RenBoEntity WHERE pid = '%s'", pid)).list();
            transaction.commit();
            return qRet;
        }
        catch (Exception ex) {
            LogUtil.Log("Get BO in Process but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            transaction.rollback();
        }
        return null;
    }

    /**
     * Check if a process name is already existing in a ren user process list.
     * @param renid ren user id
     * @param processName process name
     * @return boolean for process name existence
     */
    @SuppressWarnings("unchecked")
    public static boolean ContainProcess(String renid, String processName) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            ArrayList<RenProcessEntity> qRet = (ArrayList<RenProcessEntity>) session.createQuery(String.format("FROM RenProcessEntity WHERE creator_renid = '%s' AND process_name = '%s", renid, processName)).list();
            transaction.commit();
            return qRet.size() > 0;
        }
        catch (Exception ex) {
            LogUtil.Log("Get BO in Process but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, "");
            transaction.rollback();
        }
        return false;
    }

    /**
     * Get a BO entity by its id.
     * @param boid BO unique id
     * @return {@code RenBoEntity} instance
     */
    public static RenBoEntity GetBO(String boid, String rtid) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            RenBoEntity rbe = session.get(RenBoEntity.class, boid);
            transaction.commit();
            return rbe;
        }
        catch (Exception ex) {
            LogUtil.Log("Get BO entity but exception occurred, service rollback, " + ex, NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            transaction.rollback();
        }
        return null;
    }

    /**
     * Submit a process launch request.
     * @param pid process id to be launched
     * @param rtid prepared rtid
     * @param from launch platform
     * @param renid ren user id
     * @param authoritySession service authority session id
     * @param bindingType resource binding type
     * @param binding resource binding source, only useful when static XML binding
     * @return Runtime record package
     */
    public static RenRuntimerecordEntity SubmitLaunchProcess(String pid,
                                                             String rtid,
                                                             String from,
                                                             String renid,
                                                             String authoritySession,
                                                             Integer bindingType,
                                                             String binding) {
        Session session = HibernateUtil.GetLocalThreadSession();
        Transaction transaction = session.beginTransaction();
        try {
            RenProcessEntity rpe = session.get(RenProcessEntity.class, pid);
            RenRuntimerecordEntity rrte = new RenRuntimerecordEntity();
            rrte.setRtid(rtid);
            rrte.setLaunchFrom(from);
            rrte.setLaunchAuthorityId(renid);
            rrte.setSessionId(authoritySession);
            rrte.setProcessId(pid);
            rrte.setProcessName(rpe.getProcessName());
            rrte.setResourceBindingType(bindingType);
            rrte.setResourceBinding(binding);
            rrte.setLaunchTimestamp(new Timestamp(System.currentTimeMillis()));
            session.save(rrte);
            transaction.commit();
            // todo: send request to BOEngine to launch here
            return rrte;
        }
        catch (Exception ex) {
            LogUtil.Log(String.format("Submit launch process but exception occurred(pid: %s), service rollback, %s", pid, ex), NameSpacingService.class.getName(), LogUtil.LogLevelType.ERROR, rtid);
            transaction.rollback();
        }
        return null;
    }
}