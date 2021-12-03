package com.tlang.xsjc.jkdk.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.wechat.bean.WechatMessage;
import com.actionsoft.bpms.server.conf.portal.AWSPortalConf;
import com.actionsoft.sdk.local.api.AppAPI;
import com.alibaba.fastjson.JSONObject;
import com.tlang.xsjc.jkdk.consts.StringConsts;
import com.tlang.xsjc.jkdk.controller.JkdkProcessController;
import com.tlang.xsjc.jkdk.utils.StrUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.apps.resource.AppContext;
import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.commons.mvc.view.ResponseObject;
import com.actionsoft.bpms.schedule.IJob;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时器每天7.45自动触发待办事项，健康打卡
 * @author Wulh
 * @date 2020-02-10
 */
public class SendWaitHandleTaskJob implements IJob {
	private static Logger LOGGER = LoggerFactory.getLogger(SendWaitHandleTaskJob.class);

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		try {
			String companyId = SDK.getJobAPI().getJobParameter(jec);
			String queryUsers = "SELECT USERID,USERNAME,EXT5 FROM ORGUSER WHERE DEPARTMENTID IN (SELECT ID FROM ORGDEPARTMENT WHERE COMPANYID = '"+companyId+"')";
			List<Map<String, Object>> usersList = DBSql.query(queryUsers, new ColumnMapRowMapper());
			LOGGER.info("SendWaitHandleTaskJob.execute - usersList={}",usersList);
			if(usersList != null && usersList.size() > 0) {//如果查到数据
				String corpId = SDK.getAppAPI().getProperty(StringConsts.APP_ID, "corpId");
				String agentId = SDK.getAppAPI().getProperty(StringConsts.APP_ID, "agentId");
				LOGGER.info("SendWaitHandleTaskJob.execute - corpId={},agentId={}",corpId,agentId);
				for(Map<String,Object> map : usersList) {//给组织中人员发送健康打卡待办任务
					String userId = StrUtils.objToStr(map.get("USERID"));//用户账号
					String userName = StrUtils.objToStr(map.get("USERNAME"));//用户姓名
					String wechartId = StrUtils.objToStr(map.get("EXT5"));//企业微信账号

//					AppContext notificationApp = SDK.getAppAPI().getAppContext("com.actionsoft.apps.notification");
//			        if (notificationApp != null) {
//			            String sourceAppId = "com.actionsoft.apps.edh";
//			            // 服务地址
//			            String aslp = "aslp://com.actionsoft.apps.notification/sendMessage";
//			            HashMap<String, Object> params = new HashMap<String, Object>();
//			            params.put("sender", "admin");
//			            params.put("targetIds", userId);
//			            params.put("content", userName+"，您好！疫情期间，请按时填写汇报个人健康状况，感谢！");// 自定义变量
//			            params.put("systemName", "健康打卡通知");
//			            HashMap<String, String> data = new HashMap<>();
//			            data.put("content", userName+"，您好！疫情期间，请按时填写汇报个人健康状况，感谢！");// 自定义变量
//			            data.put("data", "健康打卡通知消息");// 自定义变量
//			            params.put("content", JSONObject.toJSONString(data));
//			            params.put("level", "info");
//			            ResponseObject callASLP = SDK.getAppAPI().callASLP(SDK.getAppAPI().getAppContext(sourceAppId), aslp, params);
//						LOGGER.debug("SendWaitHandleTaskJob.execute - callASLP={}",callASLP);
//			        }

					// 调用App
					String sourceAppId = StringConsts.APP_ID;
					// aslp服务地址
					String aslp = "aslp://com.actionsoft.apps.wechat/sendMessage";
					// 参数定义列表
					Map params = new HashMap<String, Object>();
					//企业号应用agentId,必填
					params.put("agentId", agentId);
					//企业号Id,必填
					params.put("corpId", corpId);
					//要发送的消息格式，由WechatMessage对象构建。例如WechatMessage.TEXT.xxx,必填
					WechatMessage.Article article = new WechatMessage.Article();
					article.setTitle("健康打卡提醒");
					article.setDescription(userName+"，您好！疫情期间，请按时填写汇报个人健康状况，感谢！");
					String url = "cmd=com.actionsoft.apps.workbench_mobile_process_start&groupId=obj_80068c4b71d9442ab0ede5ddee5c898f&processDefId=obj_7f04373905f947a0b6363a60ed0b16af";
					article.setUrl(url);
					WechatMessage message = WechatMessage.NEWS().addArticle(article).toUser(wechartId).build();
					message.setAgentId(agentId);
					params.put("message", message.toJson());
					AppAPI appAPI =  SDK.getAppAPI();
					//发送微信消息
					ResponseObject ro = appAPI.callASLP(appAPI.getAppContext(sourceAppId), aslp, params);
					LOGGER.info("SendWaitHandleTaskJob.execute - ro={}",ro);
				}
			}
		} catch (Exception e) {
			LOGGER.error("SendWaitHandleTaskJob.execute - 出错",e);
		}
	}
}
