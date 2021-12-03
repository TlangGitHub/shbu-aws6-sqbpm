package com.tlang.xsjc.jkdk.wechart;

import java.util.HashMap;
import java.util.Map;

import com.actionsoft.bpms.bo.engine.BO;
import com.actionsoft.bpms.commons.wechat.oauth.WechatUserTransfer;
import com.actionsoft.bpms.server.DispatcherRequest;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;
import com.alibaba.fastjson.JSONObject;
import com.tlang.xsjc.jkdk.utils.StrUtils;

/**   
* @Title: trasferWeChat.java  
* @Description: 	
* @author OnlyWjt
* @date 2019年12月27日 下午2:49:01 
* @version V1.0   
**/
public class TrasferWeChat implements WechatUserTransfer {

	@Override
	public String getAWSUidFromWechatUser(String weChatUid) {
		String cookies = DispatcherRequest.getContext().getExchange().getHeader().get("Cookies");
		String corpId = null;
		System.out.println("header:"+DispatcherRequest.getContext().getExchange().getHeader().toString());
		if (cookies != null) {
			JSONObject cookieJson = JSONObject.parseObject(cookies);
			corpId = cookieJson.getString("wechatCorpId");
			System.out.println("corpId:"+corpId+"| weChatUid:"+weChatUid);
			if(corpId ==null ) {
				System.out.println("没有获取到可用的企业微信id");
			}
		}
	    System.out.println("weChatUid"+weChatUid);
	    String getAwsUidSql = "select USERID from ORGUSER where EXT5 = '"+weChatUid+"'";
        String userId = StrUtils.objToStr(DBSql.getString(getAwsUidSql, "USERID"));
	    if(userId.equals("")){//此时说明该用户和aws系统没有绑定关系，需要设置绑定关系
	        JSONObject weChatUserInfo = WeChatCoreUtil.getWeChatUserInfoByWeChatUid(weChatUid);
//	        BO bo = new BO();
	        System.out.println("weChatUid"+weChatUid+"~weChatUserInfo"+weChatUserInfo.toString()+"~USERID"+userId);
	        String mobile = weChatUserInfo.getString("mobile");
	        getAwsUidSql = "select USERID from ORGUSER where MOBILE = '"+mobile+"'";
	        userId = StrUtils.objToStr(DBSql.getString(getAwsUidSql, "USERID"));
//	        bo.set("LOGINFO", "weChatUid"+weChatUid+"~weChatUserInfo"+weChatUserInfo.toString()+"~USERID"+userId);
//            SDK.getBOAPI().createDataBO("BO_EU_YW_LOG", bo, UserContext.fromUID("admin"));
	        String ext5Tmp =UserContext.fromUID(userId).getUserModel().getExt5();
	        if(ext5Tmp.equals("")){
	            Map<String, Object> attrs = new HashMap<>();
	            attrs.put("ext5", weChatUid);
	            SDK.getORGAPI().updateUser(userId, attrs);
	        }
	    }
	    return userId;
	}

	@Override
	public String getWechatUidFromAWSUser(String userId) {
	    String getAwsUidSql = "select  EXT5 from ORGUSER where USERID = '"+userId+"'";
        String ext5 = StrUtils.objToStr(DBSql.getString(getAwsUidSql, "EXT5"));
        return ext5;
	}

}
