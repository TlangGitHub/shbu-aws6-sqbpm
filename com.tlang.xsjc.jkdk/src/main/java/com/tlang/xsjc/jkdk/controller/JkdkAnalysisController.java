package com.tlang.xsjc.jkdk.controller;

import java.util.List;
import java.util.Map;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.actionsoft.sdk.local.SDK;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tlang.xsjc.jkdk.utils.StrUtils;

/**   
* @Title: JkdkAnalysisController.java  
* @Description: 
* @author OnlyWjt
* @date 2021-1-26 14:49:23 
* @version V1.0   
**/
@Controller
public class JkdkAnalysisController {
	/**
	* @Title: mobileFromsJkdk 
	* @Description: 查询0:出沪人数|1:隔离人数|2:高风险人数|3:体温异常 详情
	* @author: OnlyWjt
	* @param type 0:出沪人数|1:隔离人数|2:高风险人数|3:体温异常
	* @parm date
	* @return String 返回类型 
	* @throws 
	 */
	@Mapping("com.actionsoft.apps.edh_jkdkAny")
	public static String mobileFromsJkdk(UserContext uc,String type,String date) {
		JSONObject returnJson = new JSONObject();
		String companyId = uc.getCompanyModel().getId();//
		try {
			JSONArray jsonArr = new JSONArray();
			//出沪人员详情
			if(type.equals("0")) {
				return getChFf(companyId,date).toString();
			}
			//隔离人数
			if(type.equals("1")) {
				return getGlFf(companyId,date).toString();
			}
			//高风险人员
			if(type.equals("2")) {
				return getGfxFf(companyId,date).toString();
			}
			//体温异常人数
			if(type.equals("3")) {
				return getTwycFf(companyId,date).toString();
			}
			returnJson.put("status", "0");
			returnJson.put("info", jsonArr);
			return returnJson.toString();
		} catch (Exception e) {
			returnJson.put("status", "1");
			returnJson.put("message", e.getMessage());
			return returnJson.toString();
		}
	}
	
	/**
	* @Title: mobileFromsJkdk 
	* @Description: 获取异常相关信息接口
	* @author: OnlyWjt
	* @parm date
	* @return String 返回类型 
	* @throws 
	 */
	@Mapping("com.actionsoft.apps.edh_jkdkAny_chglgfxtwyc")
	public static String getChGlGfxTw(UserContext uc,String date) {
		JSONObject returnJson = new JSONObject();
		try {
			//出沪人员信息
			JSONObject chRyJson = new JSONObject();
			String companyId = uc.getCompanyModel().getId();//
			String getChrsSql = "select count(1) chrs,IFNULL(sum(case when a.CID is null then 0 else 1 end),0) bbrs from (select a.CHRUSERNAME,a.CHRID,a.CHSJ,"
					+ "a.YJFHSJ,a.CFCS,a.DDCS,a.TJCSFXDJ,a.CHJTFS,a.HBCCH,max(c.id) cid from BO_ACT_CHSPB a inner join orguser b on a.CHRID = b.userid "
					+ " inner join ORGDEPARTMENT o on o.id = b.departmentid left join BO_ACT_EMP_HEALTH_CARD c on a.APPLYDATE = c.REPORTDATE "
					+ "and a.userid = c.REPORTUID where DATE_FORMAT(APPLYDATE, '%Y-%m-%d') = '"+date+"' and o.COMPANYID = '"+companyId+"' and a.isend = 1 "
							+ " group by a.CHRUSERNAME,a.CHRID,a.CHSJ,a.YJFHSJ,a.CFCS,a.DDCS,a.TJCSFXDJ,a.CHJTFS,a.HBCCH) a ";
			List<Map<String, Object>> chRyDataList = DBSql.query(getChrsSql, new ColumnMapRowMapper(), new Object[] {});
			if(chRyDataList == null || chRyDataList.isEmpty()) {
				chRyJson.put("chRy", 0);
				chRyJson.put("chBbRy", 0);
			}else {
				//出沪人数
				String chRs = StrUtils.objToStr(chRyDataList.get(0).get("CHRS"));
				//出沪报备人数
				String bbRs = StrUtils.objToStr(chRyDataList.get(0).get("BBRS"));
				chRyJson.put("chRy", chRs);
				chRyJson.put("chBbRy", bbRs);
			}
			JSONObject GlRyJson = new JSONObject(); 
			//隔离人员数量
			String getGlRySumSql = "select count(1) glrs,IFNULL(sum(case when a.BID is null then 0 else 1 end),0) glbbrs from (select a.INSULATEUSERID,a.INSULATEUSERNAME,"
					+ "a.GLKSSJ,a.GLTS,a.GLRYBMMC,max(b.id) bid,b.FHSJ,b.YJSBSJ,b.GLYY,b.GLSYTS from BO_ACT_INSULATE a inner join orguser g on a.INSULATEUSERID = g.userid "
					+ "inner join ORGDEPARTMENT o on g.departmentid = o.id left join (\r\n" + 
					"			SELECT\r\n" + 
					"				t.* \r\n" + 
					"			FROM\r\n" + 
					"				 BO_ACT_EMP_HEALTH_CARD  t\r\n" +
					"				INNER JOIN ( SELECT max( createdate ) createdate, REPORTDATE, REPORTUID FROM  BO_ACT_EMP_HEALTH_CARD  WHERE DATE_FORMAT(REPORTDATE, '%Y-%m-%d')= '"+date+"' GROUP BY REPORTDATE, REPORTUID ) e ON t.createdate = e.createdate \r\n" +
					"				AND t.REPORTDATE = e.REPORTDATE \r\n" + 
					"				AND t.REPORTUID = e.REPORTUID \r\n" + 
					"			) b on a.INSULATEUSERID = b.REPORTUID and b.isend = 1 and a.GLKSSJ <= b.REPORTDATE and a.glkssj+a.GLTS>= b.REPORTDATE where "
					+ " STR_TO_DATE('"+date+"', '%Y-%m-%d,%H:%i:%s') >=a.glkssj and STR_TO_DATE('"+date+"','%Y-%m-%d,%H:%i:%s') <=a.glkssj+GLTS  and "
					+ "a.sfgl = 0 and o.COMPANYID = '"+companyId+"' group by a.INSULATEUSERID,a.INSULATEUSERNAME,a.GLKSSJ,a.GLTS,"
					+ "a.GLRYBMMC,b.FHSJ,b.YJSBSJ,b.GLYY,b.GLSYTS) a ";
			List<Map<String, Object>> GlRyDataList = DBSql.query(getGlRySumSql, new ColumnMapRowMapper(), new Object[] {});
			if(GlRyDataList == null || GlRyDataList.isEmpty()) {
				GlRyJson.put("glRy", 0);
				GlRyJson.put("glBbRy", 0);
			}else {
				//出沪人数
				String glRs = StrUtils.objToStr(GlRyDataList.get(0).get("GLRS"));
				//出沪报备人数
				String glbbRs = StrUtils.objToStr(GlRyDataList.get(0).get("GLBBRS"));
				GlRyJson.put("glRy", glRs);
				GlRyJson.put("glBbRy", glbbRs);
			}
			//高风险人数
			JSONObject gfxJson = new JSONObject();
			String getGfxRySumSql = "select count(1) GFXRS,IFNULL(sum(case when a.DRSFZG = '1' then 1 else 0 end),0) GFXDSRS from (select a.* from BO_ACT_EMP_HEALTH_CARD a "
					+ "inner join orguser g on a.REPORTUID = g.userid inner join ORGDEPARTMENT o on g.departmentid = o.id "
					+ "inner join (select max(createdate) createdate,REPORTDATE,REPORTUID from BO_ACT_EMP_HEALTH_CARD  group by REPORTDATE,REPORTUID) b "
					+ "on a.createdate = b.createdate and a.REPORTUID = b.REPORTUID where DATE_FORMAT(a.REPORTDATE, '%Y-%m-%d') = '"+date+"' and SFTJZGFXDQ=0 "
							+ "and o.COMPANYID = '"+companyId+"') a ";
			List<Map<String, Object>> gfxRyDataList = DBSql.query(getGfxRySumSql, new ColumnMapRowMapper(), new Object[] {});
			if(gfxRyDataList == null || gfxRyDataList.isEmpty()) {
				gfxJson.put("gfxRy", 0);
				gfxJson.put("gfxDsRy", 0);
			}else {
				//出沪人数
				String gfxRs = StrUtils.objToStr(gfxRyDataList.get(0).get("GFXRS"));
				//出沪报备人数
				String gfxbbRs = StrUtils.objToStr(gfxRyDataList.get(0).get("GFXDSRS"));
				gfxJson.put("gfxRy", gfxRs);
				gfxJson.put("gfxDsRy", gfxbbRs);
			}
			//体温异常人数
			JSONObject twycJson = new JSONObject();
			String gettwycrySumSql = "select count(1) TWYCRS,IFNULL(sum(case when a.DRSFZG = '1' then 1 else 0 end),0) TWYCDSRS from (select a.* from BO_ACT_EMP_HEALTH_CARD a "
					+ "inner join orguser g on a.REPORTUID = g.userid inner join ORGDEPARTMENT o on g.departmentid = o.id "
					+ "inner join (select max(createdate) createdate,REPORTDATE,REPORTUID from BO_ACT_EMP_HEALTH_CARD  group by REPORTDATE,REPORTUID) b "
					+ "on a.createdate = b.createdate and a.REPORTUID = b.REPORTUID where DATE_FORMAT(a.REPORTDATE, '%Y-%m-%d')  = '"+date+"' and TWCLZ>37.2 "
					+ "and o.COMPANYID = '"+companyId+"') a";
			List<Map<String, Object>> twycDataList = DBSql.query(gettwycrySumSql, new ColumnMapRowMapper(), new Object[] {});
			if(twycDataList == null || twycDataList.isEmpty()) {
				twycJson.put("twycRy", 0);
				twycJson.put("twycDsRy", 0);
			}else {
				//出沪人数
				String gfxRs = StrUtils.objToStr(twycDataList.get(0).get("TWYCRS"));
				//出沪报备人数
				String gfxbbRs = StrUtils.objToStr(twycDataList.get(0).get("TWYCDSRS"));
				twycJson.put("twycRy", gfxRs);
				twycJson.put("twycDsRy", gfxbbRs);
			}
			
			returnJson.put("status", "0");
			returnJson.put("chRs", chRyJson);
			returnJson.put("glRs", GlRyJson);
			returnJson.put("gfxRs", gfxJson);
			returnJson.put("twycRs", twycJson);
			return returnJson.toString();
		} catch (Exception e) {
			returnJson.put("status", "1");
			returnJson.put("message", e.getMessage());
			return returnJson.toString();
		}
	}
	
	
	/**
	 * 
	* @Title: getChFf 
	* @Description: 获取出沪人员信息
	* @author: OnlyWjt
	* @return JSONObject 返回类型 
	* @throws 
	 */
	public static JSONObject getChFf(String companyId,String date) {
		JSONObject json = new JSONObject();
		try {
			JSONArray jsonArray = new JSONArray();
			String getChRySql = "select a.CHRUSERNAME,a.CHRID,a.CHSJ,a.YJFHSJ,a.CFCS,a.DDCS,a.TJCSFXDJ,a.CHJTFS,a.HBCCH,max(c.id) cid,a.RESON from BO_ACT_CHSPB a  "
					+ "inner join orguser b on a.CHRID = b.userid  inner join ORGDEPARTMENT o on o.id = b.departmentid "
					+ " left join BO_ACT_EMP_HEALTH_CARD c on a.APPLYDATE = c.REPORTDATE and a.userid = c.REPORTUID "
					+ "where DATE_FORMAT(APPLYDATE, '%Y-%m-%d')  = '"+date+"' and o.COMPANYID = '"+companyId+"' and a.isend = 1"
							+ " group by a.CHRUSERNAME,a.CHRID,a.CHSJ,a.YJFHSJ,a.CFCS,a.DDCS,a.TJCSFXDJ,a.CHJTFS,a.HBCCH,a.RESON";
			List<Map<String, Object>> chRyData = DBSql.query(getChRySql, new ColumnMapRowMapper(), new Object[] {});
			for (int i = 0; i < chRyData.size(); i++) {
				JSONObject userInfo = new JSONObject();
				Map<String, Object> map = chRyData.get(i);
				//出沪人员名称
				String userName = StrUtils.objToStr(map.get("CHRUSERNAME"));
				//出沪人员id
				String userId = StrUtils.objToStr(map.get("CHRID"));
				//部门名称
				String deptName = SDK.getORGAPI().getDepartmentByUser(userId).getName();
				String chSj = StrUtils.objToStr(map.get("CHSJ")).equals("")?"":StrUtils.objToStr(map.get("CHSJ")).substring(0,10);
				//预计返回时间
				String yjFhSj = StrUtils.objToStr(map.get("YJFHSJ")).equals("")?"":StrUtils.objToStr(map.get("YJFHSJ")).substring(0,10);
				//出发城市
				String cfCs = StrUtils.objToStr(map.get("CFCS"));
				//到达城市
				String ddCs = StrUtils.objToStr(map.get("DDCS"));
				//途径城市风险等级
				String tjCsFxDj = StrUtils.objToStr(map.get("TJCSFXDJ"));
				//出沪交通方式
				String chjtFs = StrUtils.objToStr(map.get("CHJTFS"));
				//航班/车次号
				String hbCch = StrUtils.objToStr(map.get("HBCCH"));
				//出沪原因
				String reson = StrUtils.objToStr(map.get("RESON"));
				String resonStr = "";
				if(reson.equals("1")) {
					resonStr = "因公出差";
				}else if(reson.equals("2")) {
					resonStr = "因私";
				}
				//是否健康报备
				String cid = StrUtils.objToStr(map.get("CID"));
				String jkbb = "是";
				if(cid.equals("")) {
					jkbb = "否";
				}
				userInfo.put("sfJkBb", jkbb);
				userInfo.put("userName", userName);
				userInfo.put("deptName", deptName);
				userInfo.put("yjFhSj", yjFhSj);
				String fxDj = "";
				if(tjCsFxDj.equals("1")) {
					fxDj = "高";
				}else if(tjCsFxDj.equals("1")){
					fxDj = "中";
				}else {
					fxDj = "低";
				}
				userInfo.put("fxDj", fxDj);
				userInfo.put("qzCs", cfCs+"至"+ddCs);
				String jtFs = "";
				if(chjtFs.equals("1")) {
					jtFs = "飞机";
				}else if(chjtFs.equals("2")) {
					jtFs = "火车";
				}else if(chjtFs.equals("3")) {
					jtFs = "单位派车";
				}else if(chjtFs.equals("4")) {
					jtFs = "自驾";
				}else if(chjtFs.equals("5")) {
					jtFs = "其他";
				}
				userInfo.put("chjtFs", jtFs);
				userInfo.put("hbCch", hbCch);
				userInfo.put("chSj", chSj);
				userInfo.put("reson", resonStr);
				jsonArray.add(userInfo);
			}
			json.put("status", "0");
			json.put("info", jsonArray);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			json.put("status", "1");
			json.put("message", e.getMessage());
			return json;
		}
		
	}
	/**
	 * 
	* @Title: getGlFf 
	* @Description: 获取隔离人员信息
	* @author: OnlyWjt
	* @return JSONObject 返回类型 
	* @throws 
	 */
	public static JSONObject getGlFf(String companyId,String date) {
		JSONObject json = new JSONObject();
		try {
			JSONArray jsonArray = new JSONArray();
			String getChRySql = "select a.INSULATEUSERID,a.INSULATEUSERNAME,a.GLKSSJ,a.GLTS,a.GLRYBMMC,max(b.id) bid,b.FHSJ,b.YJSBSJ,b.GLYY,b.GLSYTS from "
					+ "BO_ACT_INSULATE a inner join orguser g on a.INSULATEUSERID = g.userid inner join ORGDEPARTMENT o on "
					+ "g.departmentid = o.id left join (\r\n" + 
					"			SELECT\r\n" + 
					"				t.* \r\n" + 
					"			FROM\r\n" + 
					"				 BO_ACT_EMP_HEALTH_CARD  t\r\n" +
					"				INNER JOIN ( SELECT max( createdate ) createdate, REPORTDATE, REPORTUID FROM  BO_ACT_EMP_HEALTH_CARD  WHERE DATE_FORMAT(REPORTDATE, '%Y-%m-%d')= '"+date+"' GROUP BY REPORTDATE, REPORTUID ) e ON t.createdate = e.createdate \r\n" +
					"				AND t.REPORTDATE = e.REPORTDATE \r\n" + 
					"				AND t.REPORTUID = e.REPORTUID \r\n" + 
					"			) b on a.INSULATEUSERID = b.REPORTUID and b.isend = 1 and a.GLKSSJ <= b.REPORTDATE "
					+ "and a.glkssj+a.GLTS>= b.REPORTDATE where STR_TO_DATE('"+date+"','%Y-%m-%d %H:%i:%s')>=a.glkssj "
					+ "and STR_TO_DATE('"+date+"','%Y-%m-%d %H:%i:%s') <=a.glkssj+GLTS and DATE_FORMAT(REPORTDATE, '%Y-%m-%d')= '"+date+"' and a.sfgl = 0"
					+ " and o.COMPANYID = '"+companyId+"' group by a.INSULATEUSERID,a.INSULATEUSERNAME,"
					+ "a.GLKSSJ,a.GLTS,a.GLRYBMMC,b.FHSJ,b.YJSBSJ,b.GLYY,b.GLSYTS";
			List<Map<String, Object>> glRyData = DBSql.query(getChRySql, new ColumnMapRowMapper(), new Object[] {});
			for (int i = 0; i < glRyData.size(); i++) {
				JSONObject userInfo = new JSONObject();
				Map<String, Object> map = glRyData.get(i);
				//隔离人员名称
				String userName = StrUtils.objToStr(map.get("INSULATEUSERNAME"));
				//隔离人员部门名称
				String deptName = StrUtils.objToStr(map.get("GLRYBMMC"));
				//隔离开始时间
				String glKssj = StrUtils.objToStr(map.get("GLKSSJ")).equals("")?"" :StrUtils.objToStr(map.get("GLKSSJ")).substring(0,10);
				//返沪时间
				String fhSj = StrUtils.objToStr(map.get("FHSJ")).equals("")?"":StrUtils.objToStr(map.get("FHSJ")).substring(0, 10);
				//预计上班时间
				String yjsbSj = StrUtils.objToStr(map.get("YJSBSJ")).equals("")?"": StrUtils.objToStr(map.get("YJSBSJ")).substring(0,10);
				//隔离原因
				String glYy = StrUtils.objToStr(map.get("GLYY"));
				//隔离剩余天数
				String glSyts = StrUtils.objToStr(map.get("GLSYTS"));
				String bid = StrUtils.objToStr(map.get("bid"));
				userInfo.put("userName", userName);
				userInfo.put("deptName", deptName);
				userInfo.put("glKssj", glKssj);
				userInfo.put("fhSj", fhSj);
				userInfo.put("yjsbSj", yjsbSj);
				String jkbb = "是";
				if(bid.equals("")) {
					jkbb = "否";
				}
				userInfo.put("sfJkBb", jkbb);
				userInfo.put("glSyts", glSyts);
				String finallyglYy = "";
				//1:外地归沪|2:同住人|3:其他
				if(glYy.equals("1")) {
					finallyglYy = "外地归沪";
				}else if(glYy.equals("2")) {
					finallyglYy = "同住人";
				}else if(glYy.equals("3")) {
					finallyglYy = "其他";
				}
				userInfo.put("reson", finallyglYy);
				jsonArray.add(userInfo);
			}
			json.put("status", "0");
			json.put("info", jsonArray);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			json.put("status", "1");
			json.put("message", e.getMessage());
			return json;
		}
		
	}
	/**
	 * 
	* @Title: getGfxFf 
	* @Description: 获取体温异常人员信息
	* @author: OnlyWjt
	* @return JSONObject 返回类型 
	* @throws 
	 */
	public static JSONObject getTwycFf(String companyId,String date) {
		JSONObject json = new JSONObject();
		try {
			JSONArray jsonArray = new JSONArray();
			String getGfxRySql = "select a.* from BO_ACT_EMP_HEALTH_CARD a inner join orguser g on a.REPORTUID = g.userid inner join ORGDEPARTMENT o on g.departmentid = o.id  "
					+ "inner join (select max(createdate) createdate,REPORTDATE,REPORTUID "
					+ "from BO_ACT_EMP_HEALTH_CARD  group by REPORTDATE,REPORTUID) b on a.createdate = b.createdate and "
					+ "a.REPORTUID = b.REPORTUID where DATE_FORMAT(a.REPORTDATE, '%Y-%m-%d') = '"+date+"' and TWCLZ>37.2 and o.COMPANYID = '"+companyId+"'";
			List<Map<String, Object>> glRyData = DBSql.query(getGfxRySql, new ColumnMapRowMapper(), new Object[] {});
			for (int i = 0; i < glRyData.size(); i++) {
				JSONObject userInfo = new JSONObject();
				Map<String, Object> map = glRyData.get(i);
				//人员名称
				String userName = StrUtils.objToStr(map.get("REPORTUSER"));
				//部门
				String deptName = StrUtils.objToStr(map.get("REPORTDEPT"));
				//今日是否来闵行航天城
				String sfDs = StrUtils.objToStr(map.get("DRSFZG"));
				//是否隔离
				String sfGl = StrUtils.objToStr(map.get("SFGL"));
				//人员名称
				String ywKs = StrUtils.objToStr(map.get("YWKSLTZZ")).equals("0")?"有":"无";
				//共同居住者是否健康安全
				String gtSfjk = StrUtils.objToStr(map.get("GTJZZSFJKAQ"));
				//人员名称
				String tw = StrUtils.objToStr(map.get("TWCLZ"));
				userInfo.put("userName", userName);
				userInfo.put("deptName", deptName);
				String mhHtc = "否";
				if(sfDs.equals("1")) {
					mhHtc = "是";
				}
				userInfo.put("sfDs", mhHtc);
				String sfGlstr = "是";
				if(sfGl.equals("1")) {
					sfGlstr = "否";
				}
				userInfo.put("sfGl", sfGlstr);
				String gtSfjkStr = "是";
				if(gtSfjk.equals("1")) {
					gtSfjkStr = "否";
				}
				userInfo.put("gtSfjk", gtSfjkStr);
				userInfo.put("gtSfjk", gtSfjkStr);
				userInfo.put("ywKs", ywKs);
				userInfo.put("tw", tw);
				jsonArray.add(userInfo);
			}
			json.put("status", "0");
			json.put("info", jsonArray);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			json.put("status", "1");
			json.put("message", e.getMessage());
			return json;
		}
	}
	
	/**
	 * 
	* @Title: getGfxFf 
	* @Description: 获取高风险人员信息
	* @author: OnlyWjt
	* @return JSONObject 返回类型 
	* @throws 
	 */
	public static JSONObject getGfxFf(String companyId,String date) {
		JSONObject json = new JSONObject();
		try {
			JSONArray jsonArray = new JSONArray();
			String getGfxRySql = "select a.* from BO_ACT_EMP_HEALTH_CARD a inner join orguser g on a.REPORTUID = g.userid inner join ORGDEPARTMENT o on g.departmentid = o.id "
					+ "inner join (select max(createdate) createdate,REPORTDATE,REPORTUID "
					+ "from BO_ACT_EMP_HEALTH_CARD  group by REPORTDATE,REPORTUID) b on a.createdate = b.createdate and "
					+ "a.REPORTUID = b.REPORTUID where DATE_FORMAT(a.REPORTDATE, '%Y-%m-%d') = '"+date+"' and SFTJZGFXDQ=0 and o.COMPANYID = '"+companyId+"'";
			List<Map<String, Object>> glRyData = DBSql.query(getGfxRySql, new ColumnMapRowMapper(), new Object[] {});
			for (int i = 0; i < glRyData.size(); i++) {
				JSONObject userInfo = new JSONObject();
				Map<String, Object> map = glRyData.get(i);
				//人员名称
				String userName = StrUtils.objToStr(map.get("REPORTUSER"));
				//部门
				String deptName = StrUtils.objToStr(map.get("REPORTDEPT"));
				//今日是否来闵行航天城
				String sfDs = StrUtils.objToStr(map.get("DRSFZG"));
				//是否隔离
				String sfGl = StrUtils.objToStr(map.get("SFGL"));
				//人员名称
				String ywKs = StrUtils.objToStr(map.get("YWKSLTZZ")).equals("0")?"有":"否";
				//共同居住者是否健康安全
				String gtSfjk = StrUtils.objToStr(map.get("GTJZZSFJKAQ"));
				//人员名称
				String tw = StrUtils.objToStr(map.get("TWCLZ"));
				userInfo.put("userName", userName);
				userInfo.put("deptName", deptName);
				String mhHtc = "否";
				if(sfDs.equals("1")) {
					mhHtc = "是";
				}
				userInfo.put("sfDs", mhHtc);
				String sfGlstr = "是";
				if(sfGl.equals("1")) {
					sfGlstr = "否";
				}
				userInfo.put("sfGl", sfGlstr);
				String gtSfjkStr = "是";
				if(gtSfjk.equals("1")) {
					gtSfjkStr = "否";
				}
				userInfo.put("gtSfjk", gtSfjkStr);
				userInfo.put("ywKs", ywKs);
				userInfo.put("tw", tw);
				
				jsonArray.add(userInfo);
			}
			json.put("status", "0");
			json.put("info", jsonArray);
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			json.put("status", "1");
			json.put("message", e.getMessage());
			return json;
		}
		
	}
}
