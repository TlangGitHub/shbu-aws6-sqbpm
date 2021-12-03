package com.tlang.xsjc.jkdk.controller;

import java.util.HashMap;
import java.util.Map;
import com.tlang.xsjc.jkdk.consts.StringConsts;
import com.actionsoft.bpms.commons.htmlframework.HtmlPageTemplate;
import com.actionsoft.bpms.commons.mvc.view.ResponseObject;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.sdk.local.SDK;
import com.actionsoft.sdk.local.api.AppAPI;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description 企业微信控制类
 * @author Tlang
 * @date 2020年5月15日 下午2:24:21
 */
@Controller
public class WechatCoreController {
    /**
     * 
     * @Description 
     * @author Tlang
     * @date 2020年5月15日 下午2:27:09
     * @param me
     * @param redirectUri
     * @return
     */
    @Mapping("com.awspaas.user.apps.ht.base_wechatOpenPage")
    public String wechatOpenPage(UserContext me, String redirectUri) {
        Map<String, Object> macroLibraries = new HashMap<>();
        try {
            macroLibraries.put("userid", me.getUID());
            macroLibraries.put("username", me.getUserName());
            macroLibraries.put("departmentId", me.getDepartmentModel().getId());
            macroLibraries.put("departmentName",me.getDepartmentModel().getName());
           //redirectUri = new String(Base64.decode(redirectUri), "UTF-8");
            redirectUri = SDK.getAppAPI().getProperty(StringConsts.APP_ID, redirectUri);
            redirectUri =  SDK.getRuleAPI().executeAtScript(redirectUri, me);
            macroLibraries.put("status", "0");
            macroLibraries.put("redirectUri", redirectUri);
        } catch (Exception e) {
            e.printStackTrace();
            macroLibraries.put("status", "1");
            macroLibraries.put("message", "程序异常，请联系管理员，原因为【"+e.getMessage()+"】");
        }
        return HtmlPageTemplate.merge(StringConsts.APP_ID, "wechatTempOpenPage.html", macroLibraries);
    }
    @Mapping("com.awspaas.user.apps.ht.base_testSchedule")
    public String testSchedule(UserContext me) {
    	JSONObject result = new JSONObject();
    	// 调用App 
		String sourceAppId = "com.awspaas.user.apps.workattendance";
		// aslp服务地址 
		String aslp = "aslp://com.actionsoft.apps.calendar/queryScheduleByUids";
		// 参数定义列表  
		Map params = new HashMap<String, Object>();
		params.put("sid", me.getSessionId());
		//是否显示分享日程，“0”：不显示；“1”：显示(默认显示),非必填 
		params.put("showShareSchedule", 0);
		//日程的所属人Id(多个用逗号隔开),必填 
		params.put("userIds", me.getUID());
		//搜索开始时间(yyyy-MM-dd HH:mm:ss),非必填 
		params.put("beginTime", "");
		//搜索结束时间(yyyy-MM-dd HH:mm:ss),非必填 
		params.put("endTime", "");
		//日程标题,非必填 
		params.put("title", "");
		//频道Id(多个用逗号隔开，不传则查询全部日程),非必填 
		params.put("channelIds", "");
		AppAPI appAPI =  SDK.getAppAPI(); 
		//查询多人日程
		ResponseObject ro = appAPI.callASLP(appAPI.getAppContext(sourceAppId), aslp, params);
		result.put("message", ro);
		return result.toString();
	}
}
