package com.tlang.xsjc.jkdk.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.actionsoft.bpms.commons.database.ColumnMapRowMapper;
import com.actionsoft.bpms.server.UserContext;
import com.actionsoft.bpms.server.bind.annotation.Controller;
import com.actionsoft.bpms.server.bind.annotation.Mapping;
import com.actionsoft.bpms.util.DBSql;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tlang.xsjc.jkdk.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**   
* @Title: JkdkProcessController.java  
* @Description: 查询全所考勤及健康打卡分析情况
* @author OnlyWjt
* @date 2020年2月11日 下午1:22:26 
* @version V1.0   
**/
@Controller
public class JkdkProcessController {
	private static Logger LOGGER = LoggerFactory.getLogger(JkdkProcessController.class);

	/**
	 * 
	* @Title: jbApply 
	* @Description: 根据日期查询全所考勤及健康打卡分析情况
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	@Mapping("jch5.dk_queryQsKqAndJkdkInfo")
	public String queryQsKqAndJkdkInfo(String date, UserContext uc) {
    	JSONObject returnData = new JSONObject();
    	try {
    		Map<String,Map<String,Object>> yjMap = new LinkedHashMap<String,Map<String,Object>>() ;
    		Map<String,List<Map<String,Object>>> ejMap  = new LinkedHashMap<String,List<Map<String,Object>>>();
    		String companyId = uc.getCompanyModel().getId();//
    		String queryDkInfoSql = "SELECT\r\n" + 
    				"	a.departmentid,\r\n" + 
    				"	a.sfyxj,\r\n" + 
    				"	a.DEPARTMENTNAME,\r\n" + 
    				"	a.PARENTDEPARTMENTID,\r\n" + 
    				"	a.layer,\r\n" + 
    				"	count( a.userid ) bmzrs,\r\n" + 
    				"	count( c.REPORTUID ) jkdkrs,\r\n" + 
    				"	sum( IFNULL(c.DRSFZG,0) ) dsrs,\r\n" +
    				"	sum( case when c.TWSFZC = '1' then 1 else 0 end) twycrs,\r\n" +
    				"	sum( CASE WHEN c.TWSFZC = '1'  or C.YWKSLTZZ = '0' or SFGL = '0' or GTJZZSFJKAQ = '0' or MQJWZSFAQ = '1' THEN 1 ELSE 0 END ) twyczgrs,\r\n" + 
    				"	orderindex \r\n" + 
    				"FROM\r\n" + 
    				"	(\r\n" + 
    				"	SELECT\r\n" + 
    				"		a.* \r\n" +
    				"	FROM\r\n" + 
    				"		(\r\n" + 
    				"		SELECT DISTINCT\r\n" + 
    				"			a.userid,\r\n" + 
    				"			a.ext3,\r\n" + 
    				"			a.userno,\r\n" + 
    				"			a.USERNAME,\r\n" + 
    				"			a.departmentid,\r\n" + 
    				"			b.PARENTDEPARTMENTID,\r\n" + 
    				"			b.layer,\r\n" + 
    				"			b.DEPARTMENTNAME,\r\n" + 
    				"			b.orderindex,\r\n" + 
    				"			(case when c.id is null then 0 else 1 end ) sfyxj \r\n" +
    				"		FROM\r\n" + 
    				"			orguser a\r\n" + 
    				"			LEFT JOIN orgdepartment b ON b.closed = 0 \r\n" + 
    				"			AND b.COMPANYID = '"+companyId+"' \r\n" +
    				"			AND a.departmentid = b.id\r\n" + 
    				"			LEFT JOIN orgdepartment c ON b.id = c.parentdepartmentid \r\n" + 
    				"			AND c.COMPANYID = '"+companyId+"' \r\n" +
    				"		WHERE\r\n" + 
    				"			a.closed = 0 \r\n" + 
    				"			AND b.DEPARTMENTNAME IS NOT NULL \r\n" + 
    			//	"			AND b.id != 'a1d82b3e-0491-47e6-8632-da5b1e58a361' \r\n" + 
    				"		) a \r\n" +
    				"	ORDER BY\r\n" + 
    				"		a.ORDERINDEX,\r\n" +
    				"		a.ext3 \r\n" +
    				"	) a\r\n" + 
    				"	LEFT JOIN (\r\n" + 
    				"			SELECT\r\n" + 
    				"				t.* \r\n" + 
    				"			FROM\r\n" + 
    				"				BO_ACT_EMP_HEALTH_CARD t\r\n" +
    				"				INNER JOIN ( SELECT max( createdate ) createdate, REPORTDATE, REPORTUID FROM BO_ACT_EMP_HEALTH_CARD  WHERE DATE_FORMAT(REPORTDATE, '%Y-%m-%d')= '"+date+"' GROUP BY REPORTDATE, REPORTUID ) e ON t.createdate = e.createdate \r\n" +
    				"				AND t.REPORTDATE = e.REPORTDATE \r\n" + 
    				"				AND t.REPORTUID = e.REPORTUID \r\n" + 
    				"			) c ON a.userid = c.REPORTUID \r\n" + 
    				"	AND DATE_FORMAT(c.REPORTDATE, '%Y-%m-%d') = '"+date+"' 	\r\n" +
    				"GROUP BY\r\n" + 
    				"	a.departmentid,\r\n" + 
    				"	a.parentdepartmentid,\r\n" + 
    				"	a.sfyxj,\r\n" + 
    				"	a.DEPARTMENTNAME,\r\n" + 
    				"	a.layer,\r\n" + 
    				"	orderindex \r\n" + 
    				"ORDER BY\r\n" + 
    				"	orderindex ASC";
    		List<Map<String, Object>> queryDkInfo = DBSql.query(queryDkInfoSql, new ColumnMapRowMapper(), new Object[] {});
			LOGGER.debug("JkdkProcessController.queryQsKqAndJkdkInfo - companyId={},queryDkInfo={}",companyId,queryDkInfo);
			double qsZrs = 0;//全所总人数
    		double jkBbRs = 0;//健康报备人数
    		double dsRs = 0;//到所人数
    		double ycRs = 0;//体温异常人数
    		double tiYcZgRs = 0;//体温异常在岗人数
    		for (int i = 0; i < queryDkInfo.size(); i++) {//此次数据已经全部出来了(包含一级部门的数据和二级部门的数据)
    			String departmentId = StrUtils.objToStr(queryDkInfo.get(i).get("DEPARTMENTID"));//部门id
    			if(!(departmentId.equals("ed1707a0-4d19-4a80-ac38-f58f5de6e675")//departmentId.equals("77a8ecf7-f7d7-406b-b7cd-05f9f2f5fa44")||
    					||departmentId.equals("2ab88b03-eaaf-4ef6-b9ad-a1f3764499ba") || departmentId.equals("a1d82b3e-0491-47e6-8632-da5b1e58a361"))) {//如果部门是、麟科公司、卫星应用公司，则不加进总人数里
    				qsZrs = qsZrs+Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("BMZRS")));//部门总人数
    				jkBbRs = jkBbRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("JKDKRS")));//健康打卡总人数
    				dsRs = dsRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("DSRS")));//到所人数
    				ycRs = ycRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("TWYCRS")));//体温异常人数
    				tiYcZgRs = tiYcZgRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("TWYCZGRS")));//体温异常人数
    			}
    			String layer = StrUtils.objToStr(queryDkInfo.get(i).get("LAYER"));//层级
    			if(layer.equals("1")) {//当层级是1
    				String departmentid = departmentId;//部门id
    				yjMap.put(departmentid, queryDkInfo.get(i));//一级部门map
    			}else {
    				String parentDepartmentid = StrUtils.objToStr(queryDkInfo.get(i).get("PARENTDEPARTMENTID"));//上级部门id
    				List<Map<String, Object>> erList = ejMap.get(parentDepartmentid);
    				if(erList == null || erList.size() == 0) {//如果没有取到，则新建一个map
    					Map<String, Object> hashMap = new HashMap<String, Object>();
    					hashMap = queryDkInfo.get(i);
    					erList = new ArrayList<Map<String, Object>>();
    					erList.add(hashMap);
    					ejMap.put(parentDepartmentid, erList);
    				}else {//如果取到了，则add一个map
    					erList.add(queryDkInfo.get(i));
    					ejMap.put(parentDepartmentid, erList);
    				}
    			}
			}
//    		yjMap = sortYjBm(yjMap);
    		JSONObject totalData = new JSONObject();//返回的所有信息
    		JSONArray yjBmInfo = new JSONArray();//一级部门信息
    		for(Entry<String, Map<String,Object>> ggYjBmMap:yjMap.entrySet()){
    			JSONObject yjBmTotalInfo = new JSONObject();//返回的所有信息
    			String yjBmId = ggYjBmMap.getKey();//一级部门id
    			//用一级部门id获取下面所有的二级部门数据
    			List<Map<String, Object>> erList = ejMap.get(yjBmId);
    			double yjQsZrs = 0;//全所总人数
    			double yjJkBbRs = 0;//健康报备人数
    			double yjDsRs = 0;//在岗人数
    			double yjYcRs = 0;//体温异常人数
    			double yjTiYcZgRs = 0;//体温异常在岗人数
    			if(erList == null || erList.size() == 0) {//如果没有查到，则将一级部门的数据放进去
    				yjQsZrs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("BMZRS")));//一级部门总人数
					yjJkBbRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("JKDKRS")));//一级部门健康打卡总人数
					yjDsRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("DSRS")));//一级部门在岗人数
					yjYcRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCRS")));//一级部门体温异常人数
					yjTiYcZgRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCZGRS")));//一级部门体温异常人数
					yjBmTotalInfo.put("sfyXjbm", "0");//部门名称  1:是|0:否
    			}else {//如果有二级部门，则将二级部门数据汇总
    				yjBmTotalInfo.put("sfyXjbm", "1");//部门名称  1:是|0:否
    				for (int i = 0; i < erList.size(); i++) {
    					yjQsZrs = yjQsZrs+Double.parseDouble(StrUtils.objToStr(erList.get(i).get("BMZRS")));//一级部门总人数
    					yjJkBbRs = yjJkBbRs + Double.parseDouble(StrUtils.objToStr(erList.get(i).get("JKDKRS")));//一级部门健康打卡总人数
    					yjDsRs = yjDsRs + Double.parseDouble(StrUtils.objToStr(erList.get(i).get("DSRS")));//一级部门在岗人数
    					yjYcRs = yjYcRs + Double.parseDouble(StrUtils.objToStr(erList.get(i).get("TWYCRS")));//一级部门体温异常人数
    					yjTiYcZgRs = yjTiYcZgRs + Double.parseDouble(StrUtils.objToStr(erList.get(i).get("TWYCZGRS")));//一级部门体温在岗异常人数
					}
    				//将一级部门数量也添加进去
    				yjQsZrs = yjQsZrs+Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("BMZRS")));//一级部门总人数
					yjJkBbRs = yjJkBbRs + Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("JKDKRS")));//一级部门健康打卡总人数
					yjDsRs = yjDsRs + Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("DSRS")));//一级部门在岗人数
					yjYcRs = yjYcRs + Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCRS")));//一级部门体温异常人数
					yjTiYcZgRs = yjTiYcZgRs + Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCZGRS")));//一级部门体温在岗异常人数
    			}
    			yjBmTotalInfo.put("bmMc", StrUtils.objToStr(ggYjBmMap.getValue().get("DEPARTMENTNAME")));//部门名称
    			yjBmTotalInfo.put("bmBm", StrUtils.objToStr(ggYjBmMap.getValue().get("DEPARTMENTID")));//部门编码
    			yjBmTotalInfo.put("qsZrs", yjQsZrs);//总人数
    			yjBmTotalInfo.put("jkBbRs", yjJkBbRs);//健康报备人数
    			yjBmTotalInfo.put("jkBbl", numberFormatter(yjJkBbRs,yjQsZrs));//健康报备率
    			yjBmTotalInfo.put("dsRs", yjDsRs);//到所人数
    			yjBmTotalInfo.put("zgl", numberFormatter(yjDsRs,yjQsZrs));//到所率
    			yjBmTotalInfo.put("ycRs", yjYcRs);//体温异常人数
    			yjBmTotalInfo.put("tiYcZgRs", yjTiYcZgRs);//体温异常在岗人数
//    			yjBmTotalInfo.put("sfyXjbm", yjTiYcZgRs);//以及部门信息
    			yjBmInfo.add(yjBmTotalInfo);
    		}
    		totalData.put("qsZrs", qsZrs);//全所总人数
    		totalData.put("jkBbRs", jkBbRs);//健康报备人数
    		totalData.put("jkBbl", numberFormatter(jkBbRs,qsZrs));//健康报备率
    		totalData.put("dsRs", dsRs);//在岗人数
    		totalData.put("zgl", numberFormatter(dsRs,qsZrs));//在岗率
    		totalData.put("ycRs", ycRs);//体温异常人数
    		totalData.put("tiYcZgRs", tiYcZgRs);//体温异常在岗人数
    		totalData.put("yjBmInfo", yjBmInfo.toJSONString());//一级部门信息
    		returnData.put("status", "0");
    		returnData.put("info", totalData);
		} catch (Exception e) {
			returnData.put("status", "1");
			returnData.put("message", e.getMessage());
		}
		return returnData.toString();
	}
	/**
	 * 
	* @Title: jbApply 
	* @Description: 查询下级部门考勤及健康打卡分析情况
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	@Mapping("jch5.dk_queryXjKqAndJkdkInfo")
	public String queryXjKqAndJkdkInfo(String date,String departmentId, UserContext uc) {
    	JSONObject jsonObject = new JSONObject();
		JSONObject returnData = jsonObject;
    	Map<String,Map<String,Object>> yjMap = new LinkedHashMap<String,Map<String,Object>>() ;
		String companyId = uc.getCompanyModel().getId();//
		Map<String,List<Map<String,Object>>> ejMap  = new LinkedHashMap<String,List<Map<String,Object>>>();
    	try {
    		String queryDkInfoSql = "select a.departmentid,a.sfyxj,a.DEPARTMENTNAME,a.PARENTDEPARTMENTID,a.layer,count(a.userid) bmzrs,count(c.REPORTUID) jkdkrs," + 
    				"count(b.zh) zgrs,sum(case when c.TWSFZC = '1' then 1 else 0 end ) twycrs," +
    				"sum(case when c.TWSFZC = '1' and b.zh is not null then 1 else 0 end) twyczgrs,orderindex " + 
    				" from (select * from " + 
    				"(select distinct a.userid,a.ext3,a.userno,a.USERNAME,a.departmentid,b.PARENTDEPARTMENTID,b.layer,b.DEPARTMENTNAME,b.orderindex,(case when c.id is null then 0 else 1 end ) sfyxj " +
    				" from orguser a  left join orgdepartment b on b.closed = 0  and b.COMPANYID = '"+companyId+"'" + 
    				"and a.departmentid = b.id " + 
    				"left join  orgdepartment c on b.id = c.parentdepartmentid  and c.COMPANYID = '"+companyId+"' " + 
    				"where  a.closed = 0 and b.DEPARTMENTNAME is not null " + 
    				") order by ORDERINDEX,ext3) a " + 
    				"left join VIEW_EU_DKSJSM  b on a.userid = b.zh and b.RQ = '"+date+"' " + 
    				"left join  BO_ACT_EMP_HEALTH_CARD  c on a.userid = c.REPORTUID and DATE_FORMAT(c.REPORTDATE, '%Y-%m-%d')='"+date+"'" +
    				"group by a.departmentid,a.parentdepartmentid,a.sfyxj,a.DEPARTMENTNAME,a.layer,orderindex order by orderindex asc";
    		List<Map<String, Object>> queryDkInfo = DBSql.query(queryDkInfoSql, new ColumnMapRowMapper(), new Object[] {});
    		double qsZrs = 0;//全所总人数
    		double jkBbRs = 0;//健康报备人数
    		double zgRs = 0;//在岗人数
    		double twYcRs = 0;//体温异常人数
    		double tiYcZgRs = 0;//体温异常在岗人数
    		for (int i = 0; i < queryDkInfo.size(); i++) {//此次数据已经全部出来了(包含一级部门的数据和二级部门的数据)
    			qsZrs = qsZrs+Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("BMZRS")));//部门总人数
    			jkBbRs = jkBbRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("JKDKRS")));//健康打卡总人数
    			zgRs = zgRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("ZGRS")));//在岗人数
    			twYcRs = twYcRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("TWYCRS")));//体温异常人数
    			tiYcZgRs = tiYcZgRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("TWYCZGRS")));//体温异常人数
    			String layer = StrUtils.objToStr(queryDkInfo.get(i).get("LAYER"));//层级
    			if(layer.equals("1")) {//当层级是1
    				String departmentid = StrUtils.objToStr(queryDkInfo.get(i).get("DEPARTMENTID"));//部门id
    				yjMap.put(departmentid, queryDkInfo.get(i));
    			}else {
    				String parentDepartmentid = StrUtils.objToStr(queryDkInfo.get(i).get("PARENTDEPARTMENTID"));//上级部门id
    				List<Map<String, Object>> erList = ejMap.get(parentDepartmentid);
    				if(erList == null || erList.size() == 0) {//如果没有取到，则新建一个map
    					Map<String, Object> hashMap = new HashMap<String, Object>();
    					hashMap = queryDkInfo.get(i);
    					erList = new ArrayList<Map<String, Object>>();
    					erList.add(hashMap);
    					ejMap.put(parentDepartmentid, erList);
    				}else {//如果取到了，则add一个map
    					erList.add(queryDkInfo.get(i));
    					ejMap.put(parentDepartmentid, erList);
    				}
    			}
			}
    		JSONArray ejInfo = new JSONArray();
    		List<Map<String, Object>> erlist = ejMap.get(departmentId);
    		if(erlist == null || erlist.size() == 0) {
    			returnData.put("status", "1");
    			returnData.put("message", "没有查到此部门下的信息");
    		}else {
    			//先将一级部门放进去
    			JSONObject bBBmJson = new JSONObject(); 
    			Map<String, Object> ggYjBmMap = yjMap.get(departmentId);
    			bBBmJson.put("bmMc", "本部");//部门名称
    			bBBmJson.put("bmBm", "$"+departmentId);//部门编码
    			bBBmJson.put("bmZrs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("BMZRS"))));//部门总人数
    			bBBmJson.put("jkdkRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("JKDKRS"))));//部门编码
    			bBBmJson.put("jkBbl", numberFormatter(Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("JKDKRS"))),Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("JKDKRS")))));//健康报备率
    			bBBmJson.put("zgRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("ZGRS"))));//在岗人数
    			bBBmJson.put("twYcRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("TWYCRS"))));//在岗人数
    			bBBmJson.put("twYcZgRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("TWYCZGRS"))));//在岗人数
    			bBBmJson.put("zgl", numberFormatter(Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("ZGRS"))), Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("BMZRS")))));//在岗率
    			ejInfo.add(bBBmJson);
//				yjJkBbRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("JKDKRS")));//一级部门健康打卡总人数
//				yjZgRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("ZGRS")));//一级部门在岗人数
//				yjTwYcRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCRS")));//一级部门体温异常人数
//				yjTiYcZgRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCZGRS")));//一级部门体温异常人数
    			
    			for (int i = 0; i < erlist.size(); i++) {
    				JSONObject ejBmJson = new JSONObject();
    				ejBmJson.put("bmMc", StrUtils.objToStr(erlist.get(i).get("DEPARTMENTNAME")));//部门名称
    				ejBmJson.put("bmBm", StrUtils.objToStr(erlist.get(i).get("DEPARTMENTID")));//部门编码
    				ejBmJson.put("bmZrs", StrUtils.objToStr(erlist.get(i).get("BMZRS")));//部门总人数
    				ejBmJson.put("jkdkRs", StrUtils.objToStr(erlist.get(i).get("JKDKRS")));//健康打卡总人数
    				ejBmJson.put("jkBbl", numberFormatter(Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("JKDKRS"))),Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("BMZRS")))));//健康报备率
    				ejBmJson.put("zgRs", StrUtils.objToStr(erlist.get(i).get("ZGRS")));//在岗人数
    				ejBmJson.put("twYcRs", StrUtils.objToStr(erlist.get(i).get("TWYCRS")));//体温异常人数
    				ejBmJson.put("twYcZgRs", StrUtils.objToStr(erlist.get(i).get("TWYCZGRS")));//体温异常在岗人数
    				ejBmJson.put("zgl", numberFormatter(Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("ZGRS"))),Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("BMZRS")))));//在岗率
    				ejInfo.add(ejBmJson);
    			}
    			returnData.put("status", "0");
    			returnData.put("info", ejInfo.toString());
    		}
    	}catch (Exception e) {
    		returnData.put("status", "1");
			returnData.put("message", e.getMessage());
    	}
    	return returnData.toString();
	}
	
	
	/**
	 * 
	* @Title: jbApply 
	* @Description: 查询下级部门考勤及健康打卡分析情况
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	@Mapping("jch5.dk_queryXjKqAndJkdkInfoTwo")
	public String queryXjKqAndJkdkInfoTwo(String date,String departmentId, UserContext uc) {
    	JSONObject jsonObject = new JSONObject();
		JSONObject returnData = jsonObject;
		String companyId = uc.getCompanyModel().getId();//

		Map<String,Map<String,Object>> yjMap = new LinkedHashMap<String,Map<String,Object>>() ;
		Map<String,List<Map<String,Object>>> ejMap  = new LinkedHashMap<String,List<Map<String,Object>>>();
    	try {
    		String queryDkInfoSql = "SELECT\r\n" + 
    				"	a.departmentid,\r\n" + 
    				"	a.sfyxj,\r\n" + 
    				"	a.DEPARTMENTNAME,\r\n" + 
    				"	a.PARENTDEPARTMENTID,\r\n" + 
    				"	a.layer,\r\n" + 
    				"	count( a.userid ) bmzrs,\r\n" + 
    				"	count( c.REPORTUID ) jkdkrs,\r\n" + 
    				"	sum( IFNULL(c.DRSFZG,0) ) dsrs,\r\n" +
    				"	sum(case when c.TWSFZC = '1' then 1 else 0 end) ycRs,\r\n" + 
    				"	sum( CASE WHEN c.TWSFZC = '1'  or C.YWKSLTZZ = '0' or SFGL = '0' or GTJZZSFJKAQ = '0' or MQJWZSFAQ = '1' THEN 1 ELSE 0 END ) twyczgrs ,\r\n" + 
    				"	orderindex \r\n" + 
    				"FROM\r\n" + 
    				"	(\r\n" + 
    				"	SELECT\r\n" + 
    				"		a.* \r\n" +
    				"	FROM\r\n" + 
    				"		(\r\n" + 
    				"		SELECT DISTINCT\r\n" + 
    				"			a.userid,\r\n" + 
    				"			a.ext3,\r\n" + 
    				"			a.userno,\r\n" + 
    				"			a.USERNAME,\r\n" + 
    				"			a.departmentid,\r\n" + 
    				"			b.PARENTDEPARTMENTID,\r\n" + 
    				"			b.layer,\r\n" + 
    				"			b.DEPARTMENTNAME,\r\n" + 
    				"			b.orderindex,\r\n" + 
    				"			(case when c.id is null then 0 else 1 end ) sfyxj \r\n" +
    				"		FROM\r\n" + 
    				"			orguser a\r\n" + 
    				"			LEFT JOIN orgdepartment b ON b.closed = 0 \r\n" + 
    				"			AND b.COMPANYID = '"+companyId+"' \r\n" + 
    				"			AND a.departmentid = b.id\r\n" + 
    				"			LEFT JOIN orgdepartment c ON b.id = c.parentdepartmentid \r\n" + 
    				"			AND c.COMPANYID = '"+companyId+"' \r\n" + 
    				"		WHERE\r\n" + 
    				"			a.closed = 0 \r\n" + 
    				"			AND b.DEPARTMENTNAME IS NOT NULL \r\n" + 
    				"		) a  \r\n" +
    				"	ORDER BY\r\n" + 
    				"		a.ORDERINDEX,\r\n" +
    				"		a.ext3 \r\n" +
    				"	) a\r\n" + 
    				"	LEFT JOIN (\r\n" + 
    				"			SELECT\r\n" + 
    				"				t.* \r\n" + 
    				"			FROM\r\n" + 
    				"				 BO_ACT_EMP_HEALTH_CARD  t\r\n" +
    				"				INNER JOIN ( SELECT max( createdate ) createdate, REPORTDATE, REPORTUID FROM  BO_ACT_EMP_HEALTH_CARD  WHERE DATE_FORMAT(REPORTDATE, '%Y-%m-%d') = '"+date+"' GROUP BY REPORTDATE, REPORTUID ) e ON t.createdate = e.createdate \r\n" +
    				"				AND t.REPORTDATE = e.REPORTDATE \r\n" + 
    				"				AND t.REPORTUID = e.REPORTUID \r\n" + 
    				"			) c ON a.userid = c.REPORTUID \r\n" + 
    				"	AND DATE_FORMAT(c.REPORTDATE, '%Y-%m-%d') = '"+date+"' \r\n" +
    				"GROUP BY\r\n" + 
    				"	a.departmentid,\r\n" + 
    				"	a.parentdepartmentid,\r\n" + 
    				"	a.sfyxj,\r\n" + 
    				"	a.DEPARTMENTNAME,\r\n" + 
    				"	a.layer,\r\n" + 
    				"	orderindex \r\n" + 
    				"ORDER BY\r\n" + 
    				"	orderindex ASC";
//    		System.out.println("queryDkInfoSql==="+queryDkInfoSql);
    		List<Map<String, Object>> queryDkInfo = DBSql.query(queryDkInfoSql, new ColumnMapRowMapper(), new Object[] {});
    		double qsZrs = 0;//全所总人数
    		double jkBbRs = 0;//健康报备人数
    		double dsRs = 0;//在岗人数
    		double ycRs = 0;//异常人数
    		double tiYcZgRs = 0;//体温异常在岗人数
    		for (int i = 0; i < queryDkInfo.size(); i++) {//此次数据已经全部出来了(包含一级部门的数据和二级部门的数据)
    			qsZrs = qsZrs+Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("BMZRS")));//部门总人数
    			jkBbRs = jkBbRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("JKDKRS")));//健康打卡总人数
    			dsRs = dsRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("DSRS")));//到所人数
    			ycRs = ycRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("YCRS")));//异常人数
    			tiYcZgRs = tiYcZgRs + Double.parseDouble(StrUtils.objToStr(queryDkInfo.get(i).get("TWYCZGRS")));//体温异常人数
    			String layer = StrUtils.objToStr(queryDkInfo.get(i).get("LAYER"));//层级
    			if(layer.equals("1")) {//当层级是1
    				String departmentid = StrUtils.objToStr(queryDkInfo.get(i).get("DEPARTMENTID"));//部门id
    				yjMap.put(departmentid, queryDkInfo.get(i));
    			}else {
    				String parentDepartmentid = StrUtils.objToStr(queryDkInfo.get(i).get("PARENTDEPARTMENTID"));//上级部门id
    				List<Map<String, Object>> erList = ejMap.get(parentDepartmentid);
    				if(erList == null || erList.size() == 0) {//如果没有取到，则新建一个map
    					Map<String, Object> hashMap = new HashMap<String, Object>();
    					hashMap = queryDkInfo.get(i);
    					erList = new ArrayList<Map<String, Object>>();
    					erList.add(hashMap);
    					ejMap.put(parentDepartmentid, erList);
    				}else {//如果取到了，则add一个map
    					erList.add(queryDkInfo.get(i));
    					ejMap.put(parentDepartmentid, erList);
    				}
    			}
			}
    		JSONArray ejInfo = new JSONArray();
    		List<Map<String, Object>> erlist = ejMap.get(departmentId);
    		if(erlist == null || erlist.size() == 0) {
    			returnData.put("status", "1");
    			returnData.put("message", "没有查到此部门下的信息");
    		}else {
    			//先将一级部门放进去
    			JSONObject bBBmJson = new JSONObject(); 
    			Map<String, Object> ggYjBmMap = yjMap.get(departmentId);
    			bBBmJson.put("bmMc", "本部");//部门名称
    			bBBmJson.put("bmBm", "$"+departmentId);//部门编码
    			bBBmJson.put("bmZrs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("BMZRS"))));//部门总人数
    			bBBmJson.put("jkdkRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("JKDKRS"))));//部门编码
    			bBBmJson.put("jkBbl", numberFormatter(Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("JKDKRS"))),Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("JKDKRS")))));//健康报备率
    			bBBmJson.put("dsRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("DSRS"))));//在岗人数
    			bBBmJson.put("ycRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("YCRS"))));//在岗人数
    			bBBmJson.put("twYcZgRs", Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("TWYCZGRS"))));//在岗人数
//    			bBBmJson.put("zgl", numberFormatter(Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("ZGRS"))), Double.parseDouble(StrUtils.objToStr(ggYjBmMap.get("BMZRS")))));//在岗率
    			ejInfo.add(bBBmJson);
//				yjJkBbRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("JKDKRS")));//一级部门健康打卡总人数
//				yjZgRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("ZGRS")));//一级部门在岗人数
//				yjTwYcRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCRS")));//一级部门体温异常人数
//				yjTiYcZgRs = Double.parseDouble(StrUtils.objToStr(ggYjBmMap.getValue().get("TWYCZGRS")));//一级部门体温异常人数
    			
    			for (int i = 0; i < erlist.size(); i++) {
    				JSONObject ejBmJson = new JSONObject();
    				ejBmJson.put("bmMc", StrUtils.objToStr(erlist.get(i).get("DEPARTMENTNAME")));//部门名称
    				ejBmJson.put("bmBm", StrUtils.objToStr(erlist.get(i).get("DEPARTMENTID")));//部门编码
    				ejBmJson.put("bmZrs", StrUtils.objToStr(erlist.get(i).get("BMZRS")));//部门总人数
    				ejBmJson.put("jkdkRs", StrUtils.objToStr(erlist.get(i).get("JKDKRS")));//健康打卡总人数
    				ejBmJson.put("jkBbl", numberFormatter(Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("JKDKRS"))),Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("BMZRS")))));//健康报备率
    				ejBmJson.put("dsRs", StrUtils.objToStr(erlist.get(i).get("DSRS")));//在岗人数
    				ejBmJson.put("ycRs", StrUtils.objToStr(erlist.get(i).get("YCRS")));//体温异常人数
    				ejBmJson.put("twYcZgRs", StrUtils.objToStr(erlist.get(i).get("TWYCZGRS")));//体温异常在岗人数
//    				ejBmJson.put("zgl", numberFormatter(Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("ZGRS"))),Double.parseDouble(StrUtils.objToStr(erlist.get(i).get("BMZRS")))));//在岗率
    				ejInfo.add(ejBmJson);
    			}
    			returnData.put("status", "0");
    			returnData.put("info", ejInfo.toString());
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
    		returnData.put("status", "1");
			returnData.put("message", e.getMessage());
    	}
    	return returnData.toString();
	}
	
	
	
	public static void main(String[] args) {
		double f = 1.0;
		double x = 2.0;
		numberFormatter(1.0,3.0);
	}
	/**
	 * 
	* @Title: numberFormatter 
	* @Description: 将数值格式化成54%类型
	* @author: OnlyWjt
	* @return String 返回类型 
	* @throws 
	 */
	public static String numberFormatter(double numerator, double denominator) {
		if(denominator == 0) {//如果除数为0，则知己返回0%
			return "0%";
		}
		BigDecimal numeratorBD = new BigDecimal(numerator);
		BigDecimal denominatorBD = new BigDecimal(denominator);
		BigDecimal result = numeratorBD.divide(denominatorBD,2, BigDecimal.ROUND_HALF_UP);
		String reutrndata  = result.multiply(new BigDecimal(100)).toBigInteger().toString()+"%";
		return reutrndata;
	}
	
//	public static Map<String, Map<String, Object>> sortYjBm(Map<String,Map<String,Object>> map) {
//		 List<Map.Entry<String,Map<String,Object>>> list = new ArrayList<Map.Entry<String,Map<String,Object>>>(map.entrySet());
//	        Collections.sort(list,new Comparator<Map.Entry<String,Map<String,Object>>>() {
//	            //升序排序
//	        	@Override
//	            public int compare(Entry<String,Map<String,Object>> o1,
//	                    Entry<String,Map<String,Object>> o2) {
//	                return StrUtils.objToStr(o1.getValue().get("ORDERINDEX")).compareTo(StrUtils.objToStr(o2.getValue().get("ORDERINDEX")));
//	            }
//	        });
//			return map;
//	}
	
	
}
