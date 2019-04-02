package org.sysu.workflow.service;

import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.workflow.GlobalContext;
import org.sysu.workflow.dao.RenServiceInfoDAO;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;

/**
 * Created by Skye on 2019/1/2.
 */

@Slf4j
@Service
public class ApplicationRunningHelper {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @Autowired
    private Environment environment;

    private String URL;


    @PostConstruct
    public void postConstruct() {
        try {
            URL = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + environment.getProperty("server.port");
            renServiceInfoDAO.saveOrUpdate(new RenServiceInfo(GlobalContext.ENGINE_GLOBAL_ID, URL));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void preDestroy() {
        renServiceInfoDAO.deleteByInterpreterId(GlobalContext.ENGINE_GLOBAL_ID);
    }

    /**
     * Update engine information per 10 seconds.
     */
    @Scheduled(fixedRate = 10000)
    public void MonitorRunner() {
        try {
            // CPU Load
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpuValue = operatingSystemMXBean.getProcessCpuLoad();
            log.info("当前CPU占用率：" + (cpuValue * 100) + "%");

            // Memory Usage
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
            double memoryResult = heapMemory.getUsed() * 1.0 / heapMemory.getCommitted();
            log.info("当前Memory占用率：" + (memoryResult * 100) + "%");

            // Tomcat Threads
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("Tomcat:name=\"http-nio-10232\",type=ThreadPool");
            AttributeList list = mBeanServer.getAttributes(name, new String[]{"currentThreadsBusy"});
            Attribute att = (Attribute)list.get(0);
            Integer tomcatValue  = (Integer)att.getValue();
            double maxThreads = Double.valueOf(environment.getProperty("server.tomcat.max-threads"));
            double tomcatResult = tomcatValue / maxThreads;
            log.info("当前Tomcat并发数：" + tomcatValue);

            RenServiceInfo serviceInfo = renServiceInfoDAO.findByInterpreterId(GlobalContext.ENGINE_GLOBAL_ID);
            serviceInfo.setCpuOccupancyRate(cpuValue);
            serviceInfo.setMemoryOccupancyRate(memoryResult);
            serviceInfo.setTomcatConcurrency(tomcatResult);
            serviceInfo.updateBusiness();
            renServiceInfoDAO.saveOrUpdate(serviceInfo);
        } catch (MalformedObjectNameException | InstanceNotFoundException | ReflectionException e) {
            e.printStackTrace();
        }
    }
}
