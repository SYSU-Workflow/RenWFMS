/*
 * Project Ren @ 2018
 * Rinkako, Ariana, Gordan. SYSU SDCS.
 */
package org.sysu.renNameService.restful.controller;
import org.springframework.web.bind.annotation.*;
import org.sysu.renNameService.restful.dto.ReturnElement;
import org.sysu.renNameService.restful.dto.ReturnModel;
import org.sysu.renNameService.restful.dto.ReturnModelHelper;
import org.sysu.renNameService.restful.dto.StatusCode;
import org.sysu.renNameService.utility.TimestampUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Gordan
 * Date  : 2018/1/19
 * Usage : Handle requests about the cluster.
 */

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    /**
     * Doki
     * @param from
     * @param timestamp
     * @param tag
     * @return
     */
    @PostMapping(value = "/doki", produces = {"application/json", "application/xml"})
    @ResponseBody
    public ReturnModel Doki(@RequestParam(value="from", required = false)String from,
                            @RequestParam(value="timestamp", required = false)String timestamp,
                            @RequestParam(value="tag", required = false)String tag) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            List<String> missingParams = new ArrayList<>();
            if (from == null) missingParams.add("from");
            if (timestamp == null) missingParams.add("timestamp");
            if (tag == null) missingParams.add("tag");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }

            rnModel.setCode(StatusCode.OK);
            rnModel.setNs(TimestampUtil.GetTimeStamp() + " 0");
            ReturnElement returnElement = new ReturnElement();
            returnElement.setData("Doki");
            rnModel.setReturnElement(returnElement);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.getClass().getName());
        }

        return rnModel;
    }

    /**
     * Synchronize the cluster.
     * @param from
     * @param timestamp
     * @param tag
     * @return
     */
    @PostMapping(value = "/sync", produces = {"application/json", "application/xml"})
    @ResponseBody
    public ReturnModel Sync(@RequestParam(value="from", required = false)String from,
                            @RequestParam(value="timestamp", required = false)String timestamp,
                            @RequestParam(value="tag", required = false)String tag) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            List<String> missingParams = new ArrayList<>();
            if (from == null) missingParams.add("from");
            if (timestamp == null) missingParams.add("timestamp");
            if (tag == null) missingParams.add("tag");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }

            rnModel.setCode(StatusCode.OK);
            rnModel.setNs(TimestampUtil.GetTimeStamp() + " 0");
            ReturnElement returnElement = new ReturnElement();
            returnElement.setData("Sync");
            rnModel.setReturnElement(returnElement);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.getClass().getName());
        }

        return rnModel;
    }

    /**
     * Flush to the storage.
     * @param from
     * @param to
     * @return
     */
    @PostMapping(value = "/flush", produces = {"application/json", "application/xml"})
    @ResponseBody
    public ReturnModel Flush(@RequestParam(value="from", required = false)String from,
                             @RequestParam(value="to", required = false)String to) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            List<String> missingParams = new ArrayList<>();
            if (from == null) missingParams.add("from");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }

            rnModel.setCode(StatusCode.OK);
            rnModel.setNs(TimestampUtil.GetTimeStamp() + " 0");
            ReturnElement returnElement = new ReturnElement();
            returnElement.setData("Flush");
            rnModel.setReturnElement(returnElement);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.getClass().getName());
        }

        return rnModel;
    }

    /**
     * Finish the service.
     * @param from
     * @param to
     * @return
     */
    @PostMapping(value = "/fin", produces = {"application/json", "application/xml"})
    @ResponseBody
    public ReturnModel Fin(@RequestParam(value="from", required = false)String from,
                           @RequestParam(value="to", required = false)String to) {
        ReturnModel rnModel = new ReturnModel();
        try {
            // miss params
            List<String> missingParams = new ArrayList<>();
            if (from == null) missingParams.add("from");
            if (missingParams.size() > 0) {
                return ReturnModelHelper.MissingParametersResponse(missingParams);
            }

            rnModel.setCode(StatusCode.OK);
            rnModel.setNs(TimestampUtil.GetTimeStamp() + " 0");
            ReturnElement returnElement = new ReturnElement();
            returnElement.setData("Fin");
            rnModel.setReturnElement(returnElement);
        } catch (Exception e) {
            ReturnModelHelper.ExceptionResponse(rnModel, e.getClass().getName());
        }

        return rnModel;
    }

}