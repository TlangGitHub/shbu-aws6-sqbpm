package com.tlang.xsjc.jkdk.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.actionsoft.bpms.client.notification.NotificationMessageFormatter;
import com.actionsoft.bpms.commons.mvc.view.ResponseObject;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.conf.portal.AWSPortalConf;
import com.alibaba.fastjson.JSONObject;

/**   
* @Title: JkdkFormatter.java  
* @Description: 
* @author OnlyWjt
* @date 2020年2月10日 下午6:16:59 
* @version V1.0   
**/
public class JkdkFormatter implements NotificationMessageFormatter {

	 public final static String DEMO_NAME = "健康打卡通知";

	    /**
	     * @param user 通知查看人
	     * @return ResponseObject，包含content和buttons两个变量
	     */
	    @Override
		public ResponseObject parser(UserContext user, String data) {
	    	//AppContext appContext =  SDK.getAppAPI().getAppContext("com.actionsoft.apps.meetingmanager");
	        // 逻辑处理，开发者自定义的格式，见发送时的封装
	        JSONObject json = JSONObject.parseObject(data);
	        String content = json.getString("content");
//	        String url = json.getString("url");
	        // 封装结果
	        String url = AWSPortalConf.getMobileUrl()+ "/r/w?sid="+user.getSessionId()+"&cmd=com.actionsoft.apps.workbench_mobile_process_start&groupId=obj_80068c4b71d9442ab0ede5ddee5c898f&processDefId=obj_7f04373905f947a0b6363a60ed0b16af";
	        ResponseObject ro = ResponseObject.newOkResponse();
	        ro.put("content", content);
	        List<Map<String, String>> buttons = new ArrayList<>();
	        Map<String, String> button1 = new HashMap<>();
	        button1.put("name", "查看");
	        button1.put("action", url);
	        button1.put("target", "mainFrame");// 新窗口，不常用。只允许三个常量：_blank/mainFrame/ajax
	        button1.put("color", "blue");// 只允许三个常量：blue/white/red
	        buttons.add(button1);
	        ro.put("buttons", buttons);
	        return ro;
	    }

}
