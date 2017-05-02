package edu.fudan.jliu.db;

import java.util.List;

import edu.fudan.jliu.model.TopologyBasicInfo;
import edu.fudan.jliu.model.TopologyComponentInfo;
import edu.fudan.jliu.model.TopologyWorkerInfo;

/**
 * Created by IntelliJ IDEA.
 * User: jkyan
 * Time: 5/1/17 - 5:29 PM
 * Description:
 */
public interface TopologyDao {
    boolean addTopologyBasicInfo(TopologyBasicInfo info);
    boolean deleteTopologyBasicInfo(String topoName);
    TopologyBasicInfo getTopologyBasicInfo(String topoName);

    boolean addTopologyWorkerInfo(List<TopologyWorkerInfo> workerInfoList);
    boolean addTopologyWorkerInfo(TopologyWorkerInfo workerInfo);
    boolean deleteAllTopologyWorkerInfo(String topoName);
    double getTopologyTotalCpuUsage(String topoName);
    double getTopologyTotalMemoryUsage(String topoName);

    boolean addTopologyComponentInfo(List<TopologyComponentInfo> componentInfoList);
    boolean addTopologyComponentInfo(TopologyComponentInfo componentInfo);
    boolean deleteAllTopologyComponentInfo(String topoName);
    List<TopologyComponentInfo> getAllTopologyComponentInfo(String topoName);
}
