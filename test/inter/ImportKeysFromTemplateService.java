package com.yum.boh.acp.service.activitymanage;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yum.boh.acp.dao.activitymanage.ImportKeysFromTemplateMapper;
import com.yum.boh.acp.dao.activitymanage.PopularizeInfoMapper;
import com.yum.boh.acp.dao.systemmanage.BasicArchivesMapper;
import com.yum.boh.acp.dao.systemmanage.BasicSellChannelMapper;
import com.yum.boh.acp.dao.systemmanage.PriceTypeInfoMapper;
//import com.yum.boh.acp.dao.systemmanage.ProductHierarchyUsingInfoMapper;
import com.yum.boh.acp.model.activitymanage.ActivityInfo;
import com.yum.boh.acp.model.activitymanage.ActivityKeyMapping;
import com.yum.boh.acp.model.activitymanage.ComboContentInfo;
import com.yum.boh.acp.model.activitymanage.ComboInfo;
import com.yum.boh.acp.model.activitymanage.KeyInfo;
import com.yum.boh.acp.model.activitymanage.OutsideOrderInfo;
import com.yum.boh.acp.model.activitymanage.PopularizeInfo;
import com.yum.boh.acp.model.activitymanage.PriceInfo;
import com.yum.boh.acp.model.activitymanage.ProductShiledDate;
import com.yum.boh.acp.model.activitymanage.ProductShiledTime;
import com.yum.boh.acp.model.activitymanage.SellPointInTimeInfo;
import com.yum.boh.acp.model.systemmanage.BasicArchivesInfo;
import com.yum.boh.acp.model.systemmanage.ComboTypeInfo;
import com.yum.boh.acp.model.systemmanage.PriceTypeInfo;
//import com.yum.boh.acp.model.systemmanage.ProductHierarchyUsingInfo;
import com.yum.boh.acp.util.constant.ACPConstants;
import com.yum.boh.acp.util.helper.BrandHelper;
import com.yum.boh.acp.util.helper.ExcelHelper;
import com.yum.boh.core.IceException;
import com.yum.boh.core.handler.IHandler;
import com.yum.boh.core.helper.PageContext;
import com.yum.boh.core.helper.PageResult;
import com.yum.boh.core.helper.SpringConfigHelper;
import com.yum.boh.core.model.ModelBase;
import com.yum.boh.core.service.ServiceBase;
import com.yum.boh.core.util.LogService;
import com.yum.boh.core.util.ResourceUtil;
import com.yum.boh.core.util.StringUtil;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 从外部excel导入设键服务
 * 
 */
@Service("com.yum.boh.acp.service.activitymanage.ImportKeysFromTemplateService")
@Transactional
public class ImportKeysFromTemplateService extends ServiceBase {

	private final static LogService LOGGER = LogService.getLogger(ExportKeysTemplateService.class);

	private final static String[] SELL_DATE = { "1", "2", "3", "4", "5", "6", "7" };

	private final static String DINNER_ACTIVITY_TYPE = "堂食";

	private final static String OUTSIDE_ACTIVITY_TYPE = "外送";

	@Resource(name = "com.yum.boh.acp.dao.activitymanage.ImportKeysFromTemplateMapper")
	private ImportKeysFromTemplateMapper importKeysFromTemplateMapper;

	@Resource(name = "com.yum.boh.acp.dao.systemmanage.BasicArchivesMapper")
	private BasicArchivesMapper basicArchivesMapper;

	@Resource(name = "com.yum.boh.acp.dao.systemmanage.PriceTypeInfoMapper")
	private PriceTypeInfoMapper priceTypeInfoMapper;
	@Resource(name = "com.yum.boh.acp.dao.activitymanage.PopularizeInfoMapper")
	private PopularizeInfoMapper popularizeInfoMapper;
	@Resource(name = "com.yum.boh.acp.dao.systemmanage.BasicSellChannelMapper")
	private BasicSellChannelMapper sellChannelMapper;
	
//	@Resource(name = "com.yum.boh.acp.dao.systemmanage.ProductHierarchyUsingInfoMapper")
//	private ProductHierarchyUsingInfoMapper productHierarchyUsingInfoMapper;

	/**
	 * 用户邮箱，GUID对照
	 */
	private Map<String, String> USER_EMAIL_MAP = new HashMap<String, String>();

	/**
	 * 活动名称，GUID对照
	 */
	private Map<String, String> ACTIVITY_NAME_ID_MAP = new HashMap<String, String>();

	/**
     * 活动名称，GUID对照(数据库里的数据)
     */
    private Map <String, String>   ACTIVITY_NAME_DB_MAP  = new HashMap <String, String>();

	/**
	 * 设键名称，GUID对照
	 */
	private Map<String, String> KEY_NAME_ID_MAP = new HashMap<String, String>();

	/**
	 * COMBO_TYPE_LIST
	 */
	private List<ComboTypeInfo> COMBO_TYPE_LIST = new ArrayList<ComboTypeInfo>();

	/**
	 * 设键分类，GUID对照
	 */
	private Map<String, String> KEY_CLASSFIY_ID_MAP = new HashMap<String, String>();

	/**
	 * 设键类型，GUID对照
	 */
	private Map<String, String> KEY_TYPE_ID_MAP = new HashMap<String, String>();

	/**
	 * 营运市场，GUID对照
	 */
	private Map<String, String> MARKET_ID_MAP = new HashMap<String, String>();

	/**
	 * 销售渠道，GUID对照
	 */
	private Map<String, String> SELLCHANNEL_ID_MAP = new HashMap<String, String>();

	/**
	 * 网上显示分类，GUID对照
	 */
	private Map<String, String> ONLINE_CLASS_ID_MAP = new HashMap<String, String>();

	/**
	 * 手机终端，GUID对照
	 */
	private Map<String, String> MOBILE_ID_MAP = new HashMap<String, String>();

	/**
	 * 量词，GUID对照
	 */
	private Map<String, String> QUANTIFIER_ID_MAP = new HashMap<String, String>();

	/**
	 * 产品，GUID对照
	 */
	private Map<String, String> PRODUCT_ID_MAP = new HashMap<String, String>();
	
	/** pre-order类别，GUID对照 */
	private Map<String, String> PRE_ORDER_MAP = new HashMap<String, String>();

	@Override
	@Transactional
	public PageResult doServiceWithoutHandler(PageContext pc){

		final String templateFilePath = pc.getBizdDataValueByKey("templateFilePath");
		final String[] templateFilePathArr = templateFilePath.split(",");
		final String xlsFilePath = ExcelHelper.copyFile(templateFilePathArr[templateFilePathArr.length - 1],
				ResourceUtil.getMessageByKey("ACP_KeyConfigAddressPath", new String[] { File.separator }));
        try {
            final Map <Integer, String> excelContentMap = ExcelHelper.readExcelContentWithTitleEx(new FileInputStream(xlsFilePath));
            validateCellValueFormat(excelContentMap);
            validateCellValueBusinessLogic(excelContentMap);
            importExcelContent(pc, excelContentMap, getPriceTypeList(pc));
        } catch (Exception e) {
            throw new IceException("-1", e.getMessage());
        } finally {
            // Map在下次上传时会保留上次的数据，具体原因还没弄清楚，目前暂时在这里清掉所有Map数据
            USER_EMAIL_MAP.clear();
            ACTIVITY_NAME_ID_MAP.clear();
            ACTIVITY_NAME_DB_MAP.clear();
            KEY_NAME_ID_MAP.clear();
            COMBO_TYPE_LIST.clear();
            KEY_CLASSFIY_ID_MAP.clear();
            KEY_TYPE_ID_MAP.clear();
            MARKET_ID_MAP.clear();
            SELLCHANNEL_ID_MAP.clear();
            ONLINE_CLASS_ID_MAP.clear();
            MOBILE_ID_MAP.clear();
            QUANTIFIER_ID_MAP.clear();
            PRODUCT_ID_MAP.clear();
        }
        
		return PageResult.success(UUID.randomUUID().toString(), "操作成功");
	}

	@Override
	public boolean isUseHandler() {

		return false;
	}

	/**
	 * 将collection转换为sql的in条件
	 * 
	 * @param values
	 * @return
	 */
	private String joinString2SqlInCondition(Set<String> values) {

		if (CollectionUtils.isEmpty(values)) {
			return "''";
		}

		String result = "";
		for (String value : values) {
			result += (value + "','");
		}
		return result.substring(0, result.length() - 3);
	}

	/**
	 * 将collection转换为前台alert的提示信息
	 * 
	 * @param values
	 * @return
	 */
	private String joinString2AlertMessage(List<String> values) {

		if (CollectionUtils.isEmpty(values)) {
			return "''";
		}

		String result = "";
		for (String value : values) {
			result += (value + ",");
		}
		return result.substring(0, result.length() - 1);
	}

	/**
	 * 验证excel中所有数据的格式是否正确（格式上的验证）
	 * 
	 * 采用向上抛异常的方式处理验证结果
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateCellValueFormat(Map<Integer, String> excelContentMap) throws Exception {

		if (excelContentMap.size() < 2) {
			throw new IceException("-1", "请先填写待导入活动数据");
		}

		final String brandCode = BrandHelper.getBrandCode();
		final String brandName = getBrandCodeName(brandCode);
		// 标题行
		final String[] titleRow = excelContentMap.get(0).split("#", excelContentMap.get(0).length());
		
		final Map<String, Object> params = new HashMap<String, Object>();
        params.put("brandCode", brandCode);
		int priceTypeNum_back = priceTypeInfoMapper.queryPriceTypeListSimple(params).size(); //后台根据品牌查询价格类型的个数,定义在循环外。
		
		for (int i = 1; i < excelContentMap.size(); i++) {

			String activityType = "";
			String keyClassify="";
			String keyType = "";
			String cnName = "";
            boolean promotFlag = false; //为防止变量在行数间交叉影响，所以定义在行循环内
            boolean isPreOrder = false;
            
            int priceTypeNum_front = 0;//前台根据excel价格列统计出的价格类型个数，统计一行即可，故定义在行循环内
            int priceTypeEmptyNum = 0;// 前台导入时价格类型值为空的个数，每行统计出一个数据，定义在行循环内

			final String[] contentRow = excelContentMap.get(i).split("#", excelContentMap.get(i).length());
			for (int j = 0; j < titleRow.length; j++) {
			    
				final String titleValue = titleRow[j].trim();
				final String contentValue = contentRow[j].trim();
				if (titleValue.equals("活动名称")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的活动名称");
					}
				} else if (titleValue.equals("活动类型")) {
                    if (StringUtils.isEmpty(contentValue)) {
                        throw new IceException("-1", "存在空的活动类型");
                    }
                    if (((ACPConstants.brandCode.KFC.equals(brandCode) || ACPConstants.brandCode.ED.equals(brandCode)) && (!contentValue
                            .equals(DINNER_ACTIVITY_TYPE) && !contentValue.equals(OUTSIDE_ACTIVITY_TYPE)))
                            || ((ACPConstants.brandCode.PHDI.equals(brandCode)) && !contentValue.equals(DINNER_ACTIVITY_TYPE))
                            || ((ACPConstants.brandCode.PHHS.equals(brandCode)) && !contentValue.equals(OUTSIDE_ACTIVITY_TYPE))) {
                        throw new IceException("-1", brandName + "品牌存在错误活动类型[" + contentValue + "]");
                    }
					activityType = contentValue;
				} else if (titleValue.equals("活动申请人邮箱")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的活动申请人邮箱");
					}
				} else if (titleValue.equals("活动审批人邮箱")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的活动审批人邮箱");
					}
				} else if (titleValue.equals("设键分类")) {
                    if (StringUtils.isEmpty(contentValue)) {
                        throw new IceException("-1", "存在空的设键分类");
                    }else if(!"折价凭券".equals(contentValue) && !"折价不凭券".equals(contentValue) && !"套餐".equals(contentValue)){
                        throw new IceException("-1", "设键分类["+contentValue+"]错误，设键分类只能为“折价凭券”或“折价不凭券” ！");
                    }else{
                        keyClassify = contentValue;
                    }
                } else if (titleValue.equals("设键类型")) {
                    if (StringUtils.isEmpty(contentValue)) {
                        throw new IceException("-1", "存在空的设键类型");
                    }else if("折价凭券".equals(keyClassify)){
                        if(!"多个产品一个键位".equals(contentValue) && !"多个产品多个键位".equals(contentValue) && !"单个产品单个按键".equals(contentValue)){
                         throw new IceException("-1", "设键类型[" +contentValue+ "]错误，设键分类是“折价凭券”时，设键类型只能为“多个产品一个键位”或 “多个产品多个键位”或“单个产品单个按键”这三种类型之一"); 
                        }
                    }else if("折价不凭券".equals(keyClassify)){
                        if(!"多个产品一个键位".equals(contentValue) && !"多个产品多个键位".equals(contentValue) 
                                && !"屏蔽原价的产品促销".equals(contentValue) && !"单个产品单个按键".equals(contentValue)){
                         throw new IceException("-1", "设键类型[" +contentValue+ "]错误，设键分类是“折价不凭券”时，设键类型只能为“多个产品一个键位”或 “多个产品多个键位”或“屏蔽原价的产品促销”或“单个产品单个按键”这四种类型之一"); 
                        }
                    }else if("套餐".equals(keyClassify)){
                        if(!"单个按键形式".equals(contentValue)){
                            throw new IceException("-1", "设键类型["+contentValue+"]错误，设键分类是“套餐”时，设键类型只能为“单个按键形式” ！");
                        }
                    }
                   
                     keyType = contentValue;
                    
                } else if (titleValue.equals("中文名称")) {
                    if (StringUtils.isEmpty(contentValue)) {
                        throw new IceException("-1", "存在空的中文名称");
                    }
                    if (characterLength(contentValue) >  ACPConstants.TWENTY) {
                        throw new IceException("-1", "存在超过10个字符的中文名称[" + contentValue + "]");
                    }
                    cnName = contentValue;
                } else if(titleValue.equals("BOH名称")){
                    if(StringUtils.isNotEmpty(contentValue)){
                        cnName = contentValue;
                    }
                }
                else if (titleValue.equals("简称")) {
                    if (ACPConstants.brandCode.KFC.equals(brandCode) && StringUtils.isEmpty(contentValue)) {
                        throw new IceException("-1", "存在空的简称");
                    }
                    if (characterLength(contentValue) > ACPConstants.ELEVEN) {
                        throw new IceException("-1", "存在超过5.5个字符的简称[" + contentValue + "]");
                    }
                }else if (titleValue.equals("开始时间")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的开始时间");
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					try {
						sdf.parse(contentValue);
					} catch (Exception e) {
						throw new IceException("-1", "存在错误开始时间[" + contentValue + "],格式应为yyyy-MM-dd");
					}
				} else if (titleValue.equals("结束时间")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的结束时间");
					}
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					try {
						sdf.parse(contentValue);
					} catch (Exception e) {
						throw new IceException("-1", "存在错误结束时间[" + contentValue + "],格式应为yyyy-MM-dd");
					}
				} else if (titleValue.equals("推广范围")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的推广范围");
					}
				} else if (titleValue.equals("售卖时间")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的售卖时间");
					}
					try {
						for (String sellDate : contentValue.split(",")) {
							boolean exists = false;
							for (String s : SELL_DATE) {
								if (sellDate.equals(s)) {
									exists = true;
									break;
								}
							}
							if (!exists) {
								throw new IceException("-1", "存在错误售卖时间[" + contentValue + "]，格式参考1,2,4,5(表示周一、周二、周四、周五售卖)");
							}
						}
					} catch (Exception e) {
						throw new IceException("-1", "存在错误售卖时间[" + contentValue + "]，格式参考1,2,4,5(表示周一、周二、周四、周五售卖)");
					}
				} else if (titleValue.equals("国定假日是否售卖")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的国定假日是否售卖");
					}
					if (!contentValue.equals("是") && !contentValue.equals("否")) {
						throw new IceException("-1", "存在错误的国定假日是否售卖[" + contentValue + "]，应录入是/否");
					}
				} else if (titleValue.equals("百胜卡赠送品项")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的百胜卡赠送品项");
					}
					if (!contentValue.equals("是") && !contentValue.equals("否")) {
						throw new IceException("-1", "存在错误的百胜卡赠送品项[" + contentValue + "]，应录入是/否");
					}
				} else if (titleValue.equals("是否允许被打折")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的是否允许被打折");
					}
					if (!contentValue.equals("是") && !contentValue.equals("否")) {
						throw new IceException("-1", "存在错误的是否允许被打折[" + contentValue + "]，应录入是/否");
					}
				} else if (titleValue.equals("是否适用Pre-Order") && DINNER_ACTIVITY_TYPE.equals(activityType)) {
					if (ACPConstants.brandCode.KFC.equals(brandCode) && StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的是否适用Pre-Order");
					}
					if (!contentValue.equals("是") && !contentValue.equals("否")) {
						throw new IceException("-1", "存在错误的是否适用Pre-Order[" + contentValue + "]，应录入是/否");
					}
					if(ACPConstants.brandCode.KFC.equals(brandCode) && contentValue.equals("是")){
                        isPreOrder = true;
                    }
				}else if(titleValue.equals("Pre-Order类别")){
				    if(ACPConstants.brandCode.KFC.equals(brandCode) && isPreOrder){
				        if(StringUtils.isEmpty(contentValue)){
				            throw new IceException("-1", "是否适用Pre-Order为是时，Pre-Order类别不能为空");
				        }
				    }
				}
				
				else if (ACPConstants.brandCode.PHHS.equals(brandCode) && titleValue.equals("是否适用RBD餐厅")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的是否适用RBD餐厅");
					}
					if (!contentValue.equals("是") && !contentValue.equals("否")) {
						throw new IceException("-1", "存在错误的是否适用RBD餐厅[" + contentValue + "]，应录入是/否");
					}
				} else if (titleValue.equals("售卖时间点开始时间")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的售卖时间点开始时间");
					}
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
					try {
						sdf.parse(contentValue);
					} catch (Exception e) {
						throw new IceException("-1", "存在错误售卖时间点开始时间[" + contentValue + "]，格式应为HH:mm");
					}
				} else if (titleValue.equals("售卖时间点结束时间")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的售卖时间点结束时间");
					}
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
					try {
						sdf.parse(contentValue);
					} catch (Exception e) {
						throw new IceException("-1", "存在错误售卖时间点结束时间[" + contentValue + "]，格式应为HH:mm");
					}
				}  else if (titleValue.equals("组合内容-序号")) {
				    if(!"屏蔽原价的产品促销".equals(keyType)){
				        if (StringUtils.isEmpty(contentValue)) {
	                        throw new IceException("-1", "存在空的组合内容-序号");
	                    }
	                    try {
	                        if (Integer.parseInt(contentValue) < 1) {
	                            throw new IceException("-1", "存在错误组合内容-序号[" + contentValue + "]，应大于等于1");
	                        }
	                    } catch (Exception e) {
	                        throw new IceException("-1", "存在错误组合内容-序号[" + contentValue + "]，应为整数");
	                    }
				    }
				} else if (titleValue.equals("组合内容-份数")) {
				    if(!"屏蔽原价的产品促销".equals(keyType)){
				        if (StringUtils.isEmpty(contentValue)) {
	                        throw new IceException("-1", "存在空的组合内容-份数");
	                    }
	                    try {
	                        if (Integer.parseInt(contentValue) < 1) {
	                            throw new IceException("-1", "存在错误组合内容-份数[" + contentValue + "]，应大于等于1");
	                        }
	                        if (Integer.parseInt(contentValue) > ACPConstants.TEN) {
	                            throw new IceException("-1", "存在错误组合内容-份数[" + contentValue + "]，不能超过10");
	                        }
	                    } catch (Exception e) {
	                        throw new IceException("-1", "存在错误组合内容-份数[" + contentValue + "]，应为整数");
	                    } 
				    }
					
				} else if (titleValue.equals("组合内容-系数")) {
				    if(!"屏蔽原价的产品促销".equals(keyType)){
				        if (StringUtils.isEmpty(contentValue)) {
	                        throw new IceException("-1", "存在空的组合内容-系数");
	                    }
	                    try {
	                        if (Double.parseDouble(contentValue) < 0) {
	                            throw new IceException("-1", "存在错误组合内容-系数[" + contentValue + "]，应大于等于0");
	                        }
	                        if (Double.parseDouble(contentValue) > ACPConstants.TEN) {
	                            throw new IceException("-1", "存在错误组合内容-系数[" + contentValue + "]，不能超过10");
	                        }
	                    } catch (Exception e) {
	                        throw new IceException("-1", "存在错误组合内容-系数[" + contentValue + "]，应为整数或小数");
	                    }
				    }
					
				} else if (titleValue.equals("组合内容-套餐中新名称")) {
					if (!"屏蔽原价的产品促销".equals(keyType) && StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的组合内容-套餐中新名称");
					}
					if(!"屏蔽原价的产品促销".equals(keyType) && StringUtils.isNotEmpty(contentValue)){
					    if (characterLength(contentValue) >  ACPConstants.TWENTY) {
	                        throw new IceException("-1", "存在超过10个字符的组合内容-套餐中新名称[" + contentValue + "]");
	                    }
					}
				} else if (titleValue.equals("饮料是否可换")) {
					if (!StringUtils.isEmpty(contentValue)) {
						if (!contentValue.equals("是") && !contentValue.equals("否")) {
							throw new IceException("-1", "存在错误饮料是否可换[" + contentValue + "]，应为是或否");
						}
					}
				} else if (titleValue.equals("SellCategory")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的SellCategory");
					}
				} else if (titleValue.equals("ComboType")) {
					if (StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的ComboType");
					}
				} else if (titleValue.equals("备注")) {
				    // 因为“备注”在价格后，且每个品牌都有，所以在这里判断价格的输入，表打人,我也不想的%>_<%
				    if(i == 1){
				        if(priceTypeNum_front!=priceTypeNum_back){
				            throw new IceException("-1", "当前模板的价格类型组不是最新，请重新生成模板后导入！");
	                    }
				    }
				    if(priceTypeEmptyNum == priceTypeNum_back){
				        throw new IceException("-1", "设键["+cnName+ "]请至少填写一处价格信息！");
				    }
					if (StringUtils.isNotEmpty(contentValue)) {
						if (characterLength(contentValue) > ACPConstants.ONE_THOUSAND) {
							throw new IceException("-1", "存在超过500个字符的备注");
						}
					}
				} else if (titleValue.equals("英文名称")) {
					if (!ACPConstants.brandCode.PHHS.equals(brandCode) && activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
					    if(includeChinese(contentValue)){
					        throw new IceException("-1", "英文名称[" + contentValue + "]存在中文字符");
					    }
						if (StringUtils.isNotEmpty(contentValue) && contentValue.length() > ACPConstants.TWENTY) {
							throw new IceException("-1", "活动类型为外送，存在超过20个字符的英文名称");
						}
					}
				} else if (titleValue.equals("中文描述")) {
					if (activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
						if (StringUtils.isEmpty(contentValue)) {
							throw new IceException("-1", "活动类型为外送时，存在为空的中文描述");
						}
						if (characterLength(contentValue) > ACPConstants.FOUR_HUNDRED) {
							throw new IceException("-1", "活动类型为外送，存在超过200个字符的中文描述");
						}
					}
				} else if (titleValue.equals("英文描述")) {
					if (activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
					    if(includeChinese(contentValue)){
					        throw new IceException("-1", "英文描述[" + contentValue + "]存在中文字符");
					    }
						if (contentValue.length() > ACPConstants.TWO_HUNDRED) {
							throw new IceException("-1", "活动类型为外送，存在超过200个字符的英文描述");
						}
					}
				} else if (titleValue.equals("销售渠道")) {
					if (!ACPConstants.brandCode.PHHS.equals(brandCode) && activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
						if (StringUtils.isEmpty(contentValue)) {
							throw new IceException("-1", "活动类型为外送，存在为空的销售渠道");
						}
					}
				} else if (titleValue.equals("网上基础产品分类")) {
					if (activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
						if (StringUtils.isEmpty(contentValue)) {
							throw new IceException("-1", "活动类型为外送，存在为空的网上基础产品分类");
						}
					}
				} else if (titleValue.equals("PromotionArea")) {
					if (!ACPConstants.brandCode.PHHS.equals(brandCode) && activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
						if (characterLength(contentValue) > ACPConstants.ONE_HUNDRED) {
							throw new IceException("-1", "活动类型为外送，存在超过50个字符的PromotionArea");
						}
					}
				} else if (titleValue.equals("显示分类开始日期")) {
					if (!ACPConstants.brandCode.PHHS.equals(brandCode) && activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
						if (StringUtils.isEmpty(contentValue)) {
							throw new IceException("-1", "活动类型为外送，存在为空的显示分类开始日期");
						}
						try {
							new SimpleDateFormat("yyyy-MM-dd").parse(contentValue);
						} catch (Exception e) {
							throw new IceException("-1", "存在错误显示分类开始日期[" + contentValue + "]，格式应为yyyy-MM-dd");
						}
					}
				} else if (titleValue.equals("显示分类结束日期")) {
					if (!ACPConstants.brandCode.PHHS.equals(brandCode) && activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
						if (StringUtils.isEmpty(contentValue)) {
							throw new IceException("-1", "活动类型为外送，存在为空的显示分类结束日期");
						}
						try {
							new SimpleDateFormat("yyyy-MM-dd").parse(contentValue);
						} catch (Exception e) {
							throw new IceException("-1", "存在错误显示分类结束日期[" + contentValue + "]，格式应为yyyy-MM-dd");
						}
					}
				} else if (titleValue.equals("网上显示顺序")) {
					if (!ACPConstants.brandCode.PHHS.equals(brandCode) && activityType.equals(OUTSIDE_ACTIVITY_TYPE)) {
						if (StringUtils.isEmpty(contentValue)) {
							throw new IceException("-1", "活动类型为外送，存在为空的网上显示顺序");
						}
						try {
							Integer.parseInt(contentValue);
						} catch (Exception e) {
							throw new IceException("-1", "存在错误的网上显示顺序[" + contentValue + "],应为整数");
						}
					}
				} else if (titleValue.contains("-价格")) {
				    if(i == 1){
				        priceTypeNum_front++;
				    }
					if (!StringUtils.isEmpty(contentValue)) {
						try {
							Double price = Double.parseDouble(contentValue);
							if (price < 0) {
								throw new IceException("-1", "存在小于0的价格[" + contentValue + "]");
							}
						} catch (Exception e) {
							throw new IceException("-1", "存在错误的价格[" + contentValue + "],应为整数或小数");
						}
					}else{
					    priceTypeEmptyNum++;
					}
				} else if (titleValue.contains("组合内容-套餐中品项对应单品ITEMNAME")) {
					if (!"屏蔽原价的产品促销".equals(keyType) && StringUtils.isEmpty(contentValue)) {
						throw new IceException("-1", "存在空的组合内容-套餐中品项对应单品ITEMNAME");
					}
				}
				if(StringUtils.isNotEmpty(keyType) && "屏蔽原价的产品促销".equals(keyType)){
				    if("是否屏蔽原价产品".equals(titleValue)){
				        if (!contentValue.equals("是") && !contentValue.equals("否")) {
	                        throw new IceException("-1", "存在错误的是否屏蔽原价产品[" + contentValue + "]，应录入是/否");
	                    }else if("是".equals(contentValue)){
	                        promotFlag = true;
	                    }
				    }
				}
				// 如果是屏蔽原价的产品名称，且是否屏蔽原价产品选择“是”
				if(promotFlag){
				    if("原价产品名称".equals(titleValue)){
				        if (StringUtils.isEmpty(contentValue)) {
				            throw new IceException("-1", "屏蔽原价产品时，存在空的原价产品名称"); 
				        }
				    }else if("原价产品系数".equals(titleValue)){
				        if (StringUtils.isEmpty(contentValue)) {
                            throw new IceException("-1", "屏蔽原价产品时，存在空的原价产品系数"); 
                        } else if(!isNumberic(contentValue)){
                            throw new IceException("-1", "原价产品系数["+contentValue+"]存在非数字的字符"); 
                        } else if(str2Num(contentValue,1) > ACPConstants.NINETY_NINE){
                            throw new IceException("-1", "原价产品系数["+contentValue+"]不能大于99"); 
                        }
				    }else if("原价产品份数".equals(titleValue)){
				        if (StringUtils.isEmpty(contentValue)) {
                            throw new IceException("-1", "屏蔽原价产品时，存在空的原价产品份数"); 
                        }else if(!isNumberic(contentValue)){
                            throw new IceException("-1", "原价产品份数["+contentValue+"]存在非数字的字符"); 
                        } else if(str2Num(contentValue,1) > ACPConstants.NINETY_NINE){
                            throw new IceException("-1", "原价产品份数["+contentValue+"]不能大于99"); 
                        }
				    }
				}
				
				if(ACPConstants.brandCode.PHHS.equals(brandCode) && activityType.equals(OUTSIDE_ACTIVITY_TYPE)){
				    if("BOH名称".equals(titleValue) && StringUtils.isEmpty(contentValue)){
				        throw new IceException("-1", "存在空的BOH名称");
				    }else if("SUS水单名称".equals(titleValue) && StringUtils.isEmpty(contentValue)){
				        throw new IceException("-1", "存在空的SUS水单名称");
				    }else if("OS/IOS中文名称".equals(titleValue) && StringUtils.isEmpty(contentValue)){
				        throw new IceException("-1", "存在空的OS/IOS中文名称");
				    }else if("OS/IOS英文名称".equals(titleValue)){
				        if(StringUtils.isEmpty(contentValue)){
				            throw new IceException("-1", "存在空的OS/IOS英文名称");
				        }else if(includeChinese(contentValue)){
				            throw new IceException("-1", "OS/IOS英文名称存在中文字符");
				        }
				        
				    }
				}
			}
		}
	}

	/**
	 * 业务逻辑上的验证
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateCellValueBusinessLogic(Map<Integer, String> excelContentMap) throws Exception {

		validateActivity(excelContentMap);
		validatePopularize(excelContentMap);
		validateKeyType(excelContentMap);
		validateKeyName(excelContentMap);
		validateSellCategoryAndComboType(excelContentMap);
		validateComboContent(excelContentMap);
		validateComboContentName(excelContentMap);

		// 验证外送信息的一些码表
		final Map<String, Object> params = new HashMap<String, Object>();
		final String brandCode = BrandHelper.getBrandCode();
		params.put("brandCode", brandCode);
		params.put("archivesTypeStrs", "'0','1','2','11'");
		List<BasicArchivesInfo> archives = basicArchivesMapper.queryList(params);
		validateOutBasicArchives(excelContentMap, archives, ACPConstants.BasicArchivesType.ONLINE_DISPLAY_CLASS, "网上基础产品分类");
		if (!ACPConstants.brandCode.PHDI.equals(brandCode)) {
		    // 验证销售渠道
			validateOutSellChannels(excelContentMap);
		}
		if (ACPConstants.brandCode.KFC.equals(brandCode) || ACPConstants.brandCode.ED.equals(brandCode)) {
			validateOutBasicArchives(excelContentMap, archives, ACPConstants.BasicArchivesType.MOBILE_TERMINAL_TYPE, "支持的手机终端");
		}
		if (!ACPConstants.brandCode.PHHS.equals(brandCode)) {
			validateOutBasicArchives(excelContentMap, archives, ACPConstants.BasicArchivesType.QUANTIFIER, "量词");
		}
		if(ACPConstants.brandCode.KFC.equals(brandCode)){
		    validateOutBasicArchives(excelContentMap, archives, ACPConstants.BasicArchivesType.PRE_ORDER_CODE, "Pre-Order类别");
		}
	}

	/**
	 * 验证活动
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateActivity(Map<Integer, String> excelContentMap) throws Exception {

		// 1.获取活动数据
		Set<String> activityNameSet = getAllColumnValue("活动名称", excelContentMap);
		Set<String> emailSet = getAllColumnValue("活动申请人邮箱", excelContentMap);
		emailSet.addAll(getAllColumnValue("活动审批人邮箱", excelContentMap));

		// 1.1验证活动名称是否重复
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("brandCode", BrandHelper.getBrandCode());
		params.put("condition", joinString2SqlInCondition(activityNameSet));
		final List<HashMap> existsActivityNameList = importKeysFromTemplateMapper.checkActivityNameExists(params);
		List<String> forbidActivity = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(existsActivityNameList)) {
		    // 理论上根据一个活动名称应该只能搜到一条活动记录(调整前后的要过滤为最新的，也是一条)
		    for(HashMap map : existsActivityNameList){
		        if(!"00001".equals(map.get("ACTIVITY_STATUS"))){
		            forbidActivity.add(map.get("ACTIVITY_NAME").toString());
		        }else{
		            ACTIVITY_NAME_DB_MAP.put(map.get("ACTIVITY_NAME").toString(), map.get("GUID").toString());
		        }
		        
		    }
		    if(forbidActivity.size() > 0){
		        throw new IceException("-1", "以下活动[" + joinString2AlertMessage(forbidActivity) + "]为非草稿状态，不可以导入数据");
		    }
//			throw new IceException("-1", "活动名称[" + joinString2AlertMessage(existsActivityNameList) + "]已存在");
		    
		}

		// 1.2验证邮箱是否存在
		params.put("emailAddr", joinString2SqlInCondition(emailSet));
		List<HashMap> emailAndUserIdList = importKeysFromTemplateMapper.getUserIdByUserEmail(params);
		List<String> notExistsEmail = new ArrayList<String>();
		for (String email : emailSet) {
			boolean exists = false;
			for (HashMap emailAndUserId : emailAndUserIdList) {
				final String emailAddr = emailAndUserId.get("EMAIL_ADDR").toString();
				if (emailAddr.equals(email)) {
					exists = true;
					USER_EMAIL_MAP.put(emailAddr, emailAndUserId.get("GUID").toString());
					break;
				}
			}
			if (!exists) {
				notExistsEmail.add(email);
			}
		}
		if (CollectionUtils.isNotEmpty(notExistsEmail)) {
			throw new IceException("-1", "以下邮箱[" + joinString2AlertMessage(notExistsEmail) + "]不存在对应用户");
		}
	}

	/**
	 * 验证推广范围
	 * 
	 * @param excelContentMap
	 */
	private void validatePopularize(Map<Integer, String> excelContentMap) throws Exception {

		try {
			final Set<String> opsMarketSet = new HashSet<String>();
			final Set<String> storeSet = new HashSet<String>();
			final Set<String> citySet = new HashSet<String>();
			// 推广范围的格式为：营运市场=华东,武汉;城市=南京市,上海市;餐厅=NJG001
			// 需验证营运市场、餐厅是否存在，城市不用验证（城市没有码表）
			final Set<String> popularizeSet = getAllColumnValue("推广范围", excelContentMap);
			for (String popularize : popularizeSet) {
				if (popularize.equals("全国")) {
					continue;
				}
				for (String marketCityStore : popularize.split(";")) {
					final String[] popu = marketCityStore.split("=");
					if (popu[0].equals("营运市场")) {
						for (String opsMarket : popu[1].split(",")) {
							opsMarketSet.add(opsMarket);
						}
					} else if (popu[0].equals("餐厅")) {
						for (String store : popu[1].split(",")) {
							storeSet.add(store);
						}
					} else if (popu[0].equals("城市")) {
						for (String city : popu[1].split(",")) {
							citySet.add(city);
						}
					} else {
						throw new IceException("-1", "存在错误推广范围[" + popularize + "]，参考格式营运市场=南京,上海;城市=北京市;餐厅=BJN001");
					}
				}
			}

			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("brandCode", BrandHelper.getBrandCode());
			params.put("opsMarketName", joinString2SqlInCondition(opsMarketSet));
			params.put("storeCode", joinString2SqlInCondition(storeSet));
			// 验证营运市场是否存在
			List<HashMap> opsMarketCodeAndNameList = importKeysFromTemplateMapper.getOpsMarketCodeByName(params);
			List<String> notExistsOpsMarket = new ArrayList<String>();
			for (String opsMarket : opsMarketSet) {
				boolean exists = false;
				for (HashMap opsMarketCodeAndName : opsMarketCodeAndNameList) {
					final String opsMarketName = opsMarketCodeAndName.get("OPS_MARKET_NAME").toString();
					if (opsMarketName.equals(opsMarket)) {
						exists = true;
						MARKET_ID_MAP.put(opsMarketName, opsMarketCodeAndName.get("OPS_MARKET_CODE").toString());
						break;
					}
				}
				if (!exists) {
					notExistsOpsMarket.add(opsMarket);
				}
			}
			if (CollectionUtils.isNotEmpty(notExistsOpsMarket)) {
				throw new IceException("-1", "以下营运市场[" + joinString2AlertMessage(notExistsOpsMarket) + "]不存在");
			}

			// 验证餐厅是否存在
			List<HashMap> storeCodeAndNameList = importKeysFromTemplateMapper.getStoreCodeByName(params);
			List<String> notExistsStore = new ArrayList<String>();
			for (String store : storeSet) {
				boolean exists = false;
				for (HashMap storeCodeAndName : storeCodeAndNameList) {
					final String storeCode = storeCodeAndName.get("STORE_CODE").toString();
					if (storeCode.equals(store)) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					notExistsStore.add(store);
				}
			}
			if (CollectionUtils.isNotEmpty(notExistsStore)) {
				throw new IceException("-1", "以下餐厅[" + joinString2AlertMessage(notExistsStore) + "]不存在");
			}
		} catch (Exception e) {
			if (e instanceof IceException) {
				throw e;
			}
			throw new IceException("-1", "营运市场格式不正确");
		}
	}

	/**
	 * 验证设键分类、类型是否存在
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateKeyType(Map<Integer, String> excelContentMap) throws Exception {

		final Set<String> keyClassifySet = getAllColumnValue("设键分类", excelContentMap);
		final Set<String> keyTypeSet = getAllColumnValue("设键类型", excelContentMap);

		final Map<String, Object> params = new HashMap<String, Object>();
		// 验证设键分类是否存在
		params.put("codeName", joinString2SqlInCondition(keyClassifySet));
		List<HashMap> keyClassifyList = importKeysFromTemplateMapper.getKeyClassifyByName(params);
		List<String> notExistsKeyClassify = new ArrayList<String>();
		for (String keyClassify : keyClassifySet) {
			boolean exists = false;
			for (HashMap keyClassifyCodeAndName : keyClassifyList) {
				final String keyClassifyName = keyClassifyCodeAndName.get("CODE_NAME").toString();
				if (keyClassifyName.equals(keyClassify)) {
					exists = true;
					KEY_CLASSFIY_ID_MAP.put(keyClassify, keyClassifyCodeAndName.get("CODE").toString());
					break;
				}
			}
			if (!exists) {
				notExistsKeyClassify.add(keyClassify);
			}
		}
		if (CollectionUtils.isNotEmpty(notExistsKeyClassify)) {
			throw new IceException("-1", "以下设键分类[" + joinString2AlertMessage(notExistsKeyClassify) + "]不存在");
		}

		// 验证设键类型是否存在
		params.put("codeName", joinString2SqlInCondition(keyTypeSet));
		List<HashMap> keyTypeList = importKeysFromTemplateMapper.getKeyTypeByName(params);
		List<String> notExistsKeyType = new ArrayList<String>();
		for (String keyType : keyTypeSet) {
			boolean exists = false;
			for (HashMap keyTypeCodeAndName : keyTypeList) {
				final String keyTypeName = keyTypeCodeAndName.get("CODE_NAME").toString();
				if (keyTypeName.equals(keyType)) {
					exists = true;
					KEY_TYPE_ID_MAP.put(keyType, keyTypeCodeAndName.get("CODE").toString());
					break;
				}
			}
			if (!exists) {
				notExistsKeyType.add(keyType);
			}
		}
		if (CollectionUtils.isNotEmpty(notExistsKeyType)) {
			throw new IceException("-1", "以下设键类型[" + joinString2AlertMessage(notExistsKeyType) + "]不存在");
		}
	}

	/**
	 * 验证中文名称是否重复
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateKeyName(Map<Integer, String> excelContentMap) throws Exception {

		Set<String> keyNameSet = getAllColumnValue("中文名称", excelContentMap);
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("brandCode", BrandHelper.getBrandCode());
		params.put("cnName", joinString2SqlInCondition(keyNameSet));
		List<String> existsKeyNameList = importKeysFromTemplateMapper.checkKeyNameExists(params);
		if (CollectionUtils.isNotEmpty(existsKeyNameList)) {
			throw new IceException("-1", "中文名称[" + joinString2AlertMessage(existsKeyNameList) + "]已存在");
		}
		
		keyNameSet.clear();
		keyNameSet = getAllColumnValue("BOH名称", excelContentMap);
		params.put("cnName", joinString2SqlInCondition(keyNameSet));
		existsKeyNameList.clear();
		existsKeyNameList = importKeysFromTemplateMapper.checkKeyNameExists(params);
		if (CollectionUtils.isNotEmpty(existsKeyNameList)) {
            throw new IceException("-1", "BOH名称[" + joinString2AlertMessage(existsKeyNameList) + "]已存在");
        }
	}

	/**
	 * 验证SellCategory和ComboType是否存在
	 * 
	 * 必须成对验证
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateSellCategoryAndComboType(Map<Integer, String> excelContentMap) throws Exception {

		final String[] titleRow = excelContentMap.get(0).split("#", excelContentMap.get(0).length());
		for (int i = 1; i < excelContentMap.size(); i++) {
			String sellCategory = "";
			String comboType = "";
			final String[] contentRow = excelContentMap.get(i).split("#", excelContentMap.get(i).length());
			for (int j = 0; j < titleRow.length; j++) {
				if ("SellCategory".equals(titleRow[j].trim())) {
					sellCategory = contentRow[j].trim();
				} else if ("ComboType".equals(titleRow[j].trim())) {
					comboType = contentRow[j].trim();
				}
			}

			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("brandCode", BrandHelper.getBrandCode());
			params.put("sellType", sellCategory);
			params.put("comboType", comboType);
			final List<HashMap> sellCategoryAndComboTypeList = importKeysFromTemplateMapper.getSellCategoryAndComboTypeByName(params);
			if (CollectionUtils.isEmpty(sellCategoryAndComboTypeList)) {
				throw new IceException("-1", "SellCategory[" + sellCategory + "], ComboType[" + comboType + "]不存在");
			} else {
				final ComboTypeInfo comboTypeInfo = new ComboTypeInfo();
				comboTypeInfo.setSellTypeId(sellCategoryAndComboTypeList.get(0).get("SELL_TYPE_ID").toString());
				comboTypeInfo.setSellType(sellCategory);
				comboTypeInfo.setGuid(sellCategoryAndComboTypeList.get(0).get("COMBO_TYPE_ID").toString());
				comboTypeInfo.setComboType(comboType);
				COMBO_TYPE_LIST.add(comboTypeInfo);
			}
		}
	}

	/**
	 * 验证基础档案数据是否存在
	 * 
	 * @param excelContentMap
	 */
	private void validateOutBasicArchives(Map<Integer, String> excelContentMap, List<BasicArchivesInfo> archives, int archivesType, String message) {
		final List<String> notDistributionChannelSet = new ArrayList<String>();
		final Set<String> distributionChannelSet = getAllColumnValue(message, excelContentMap);
		for (String distributionChannel : distributionChannelSet) {

			for (String dc : distributionChannel.split(ACPConstants.DOT)) {
				boolean exists = false;
				for (BasicArchivesInfo arch : archives) {
					if (archivesType == arch.getArchivesType() && dc.equals(arch.getArchivesName())) {
						exists = true;

						if (archivesType == ACPConstants.BasicArchivesType.ONLINE_DISPLAY_CLASS) {
							ONLINE_CLASS_ID_MAP.put(arch.getArchivesName(), arch.getGuid());
						} else if (archivesType == ACPConstants.BasicArchivesType.MOBILE_TERMINAL_TYPE) {
							MOBILE_ID_MAP.put(arch.getArchivesName(), arch.getGuid());
						} else if (archivesType == ACPConstants.BasicArchivesType.QUANTIFIER) {
							QUANTIFIER_ID_MAP.put(arch.getArchivesName(), arch.getGuid());
						} 
//						else if (archivesType == ACPConstants.BasicArchivesType.DISTRIBUTION_CHANNEL_TYPE) {
//							SELLCHANNEL_ID_MAP.put(arch.getArchivesName(), arch.getGuid());
//						}
						//销售渠道不再从基础档案表中获取，而是从销售渠道表ACP_T_BASIC_SELL_CHANNEL中获取,故代码不在此进行
						else if (archivesType == ACPConstants.BasicArchivesType.PRE_ORDER_CODE){
						    PRE_ORDER_MAP.put(arch.getArchivesName(), arch.getGuid());
						}

						break;
					}
				}
				if (!exists && StringUtils.isNotEmpty(dc)) {
					notDistributionChannelSet.add(dc);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(notDistributionChannelSet)) {
			throw new IceException("-1", "以下" + message + "[" + joinString2AlertMessage(notDistributionChannelSet) + "]不存在");
		}
	}
	/**
	 * <p>根据销售渠道名称判断是否存在对应的销售渠道信息，如果不存在，给出提示</p>
	 *
	 * @param excelContentMap
	 */
    private void validateOutSellChannels(Map <Integer, String> excelContentMap) {

        final List <String> notSellChannelSet = new ArrayList <String>();
        final Set <String> sellChannelSet = getAllColumnValue("销售渠道", excelContentMap);
        Map <String, Object> params = new HashMap <String, Object>();
        StringBuffer sbf = new StringBuffer();
        for (String channelStr : sellChannelSet) {
            
            for (String cs : channelStr.split(ACPConstants.DOT)) {
                sbf.delete(0, sbf.length());
                sbf.append("'").append(cs).append("'");
                params.put("channelNames", sbf.toString());
                params.put("brandCode", BrandHelper.getBrandCode());
                List <Map> productNameAndIdList = sellChannelMapper.querySellChannelCodeByNames(params);
                if (CollectionUtils.isEmpty(productNameAndIdList) || productNameAndIdList.size() == 0) {
                    notSellChannelSet.add(cs);
                } else {
                    for (Map map : productNameAndIdList) {
                        //由于销售渠道后来改为名称直接存储，所以这里取名称
                        SELLCHANNEL_ID_MAP.put(cs, map.get("YOUHUI_CHANNEL_DESCRIPTION").toString());
                    }

                }
            }
        }

        if (CollectionUtils.isNotEmpty(notSellChannelSet)) {
            throw new IceException("-1", "销售渠道[" + joinString2AlertMessage(notSellChannelSet) + "]不存在");
        }

    }

	/**
	 * 验证组合内容
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateComboContent(Map<Integer, String> excelContentMap) throws Exception {
		Set<String> productNameSet = getAllColumnValue("组合内容-套餐中品项对应单品ITEMNAME", excelContentMap);

		final Map<String, Object> params = new HashMap<String, Object>();
		// 验证组合内容对应产品是否存在
		params.put("productName", joinString2SqlInCondition(productNameSet));
		List<HashMap> productNameAndIdList = importKeysFromTemplateMapper.getProductIdByName(params);
		List<String> notExistsProduct = new ArrayList<String>();
		for (String productName : productNameSet) {
			boolean exists = false;
			for (HashMap productNameAndId : productNameAndIdList) {
				final String pName = productNameAndId.get("PRODUCT_NAME").toString();
				if (productName.equals(pName)) {
					exists = true;
					PRODUCT_ID_MAP.put(productName, productNameAndId.get("PRODUCT_ID").toString());
					break;
				}
			}
			if (!exists) {
				notExistsProduct.add(productName);
			}
		}
		if (CollectionUtils.isNotEmpty(notExistsProduct)) {
			throw new IceException("-1", "以下设键组合内容对应的产品[" + joinString2AlertMessage(notExistsProduct) + "]不存在");
		}
		
		
		productNameSet.clear();
		productNameSet = getAllPromotValue("原价产品名称", excelContentMap);
		params.put("brandCode",BrandHelper.getBrandCode());
		for(String str : productNameSet){
		    String[] splitStr = str.split(ACPConstants.COMMA);
		    params.put("activityType", splitStr[0]) ;
		    params.put("productName",splitStr[1]);
		    List<HashMap> primeProductList = importKeysFromTemplateMapper.getPrimeProductByName(params);
		    if(CollectionUtils.isEmpty(primeProductList) || primeProductList.size() == 0){
		        throw new IceException("-1", "原价产品名称[" + splitStr[1] + "]不存在");
		    }else{
		        HashMap primeProduct = primeProductList.get(0); //理论上一个产品名称查出一条产品[{GUID=XXX, PRODUCT_NAME=XXX, KEY_ID=XXX}]
		        String productId = primeProduct.get("GUID").toString();
		        PRODUCT_ID_MAP.put(splitStr[1], productId);
		    }
		}
		 
	}
	
//	private List<HashMap>  getProductIdByName(String productName,String activityTypeName){
//	    String brandCode = BrandHelper.getBrandCode();
//	    String activityType ="";
//	    // 根据品牌查询产品层级的启用情况
//	    Map<String, Object> queryMap = new HashMap<String,Object>();
//	    queryMap.put("brandCode", brandCode);
//	    List<ProductHierarchyUsingInfo>  hierarchyList = productHierarchyUsingInfoMapper.queryList(queryMap);
//	    String endHierarchyUsing = "";// 取层级最低层
//	    if(CollectionUtils.isNotEmpty(hierarchyList) && hierarchyList.size() > 0){
//	        ProductHierarchyUsingInfo productHierarchyUsingInfoValue = hierarchyList.get(0);
//	        if ("Y".equals(productHierarchyUsingInfoValue.getIsHierarchyFiveUsing())) {
//	            endHierarchyUsing = ACPConstants.DinnerProductSelectType.HIERARCHY_FIVE;
//	        } else if ("Y".equals(productHierarchyUsingInfoValue.getIsHierarchyFourUsing())) {
//	            endHierarchyUsing = ACPConstants.DinnerProductSelectType.HIERARCHY_FOUR;
//	        } else if ("Y".equals(productHierarchyUsingInfoValue.getIsHierarchyThreeUsing())) {
//	            endHierarchyUsing = ACPConstants.DinnerProductSelectType.HIERARCHY_THREE;
//	        } else if ("Y".equals(productHierarchyUsingInfoValue.getIsHierarchyTwoUsing())) {
//	            endHierarchyUsing = ACPConstants.DinnerProductSelectType.HIERARCHY_TWO;
//	        } else if ("Y".equals(productHierarchyUsingInfoValue.getIsHierarchyOneUsing())) {
//	            endHierarchyUsing = ACPConstants.DinnerProductSelectType.HIERARCHY_ONE;
//	        }
//	    }
//	    //TODO
//	    queryMap.put("activityType", activityType);
//	    queryMap.put("endHierarchyUsing", endHierarchyUsing);
//	    return null;
//	    
//	}

	/**
	 * 
	 * @param excelContentMap
	 * @throws Exception
	 */
	private void validateComboContentName(Map<Integer, String> excelContentMap) throws Exception {

	    // 业务校验“组合内容-套餐中新名称”
		final Set<String> comboContentNameSet = getAllColumnValue("组合内容-套餐中新名称", excelContentMap);
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("comboContentNewName", joinString2SqlInCondition(comboContentNameSet));
		params.put("brandCode", BrandHelper.getBrandCode());
		List<String> existsComboContentNameList = importKeysFromTemplateMapper.checkComboContentNameExists(params);
		if (CollectionUtils.isNotEmpty(existsComboContentNameList)) {
			throw new IceException("-1", "组合内容-套餐中新名称[" + joinString2AlertMessage(existsComboContentNameList) + "]已存在");
		}
		
		// 业务校验“原价产品名称”  目前页面没有校验重复，所以这里也不校验
//		final Set<String> productNameSet =  getAllColumnValue("原价产品名称", excelContentMap);
//		params.clear();
//        params.put("comboContentNewName", joinString2SqlInCondition(productNameSet));
//        params.put("brandCode", BrandHelper.getBrandCode());
//        existsComboContentNameList.clear();
//        existsComboContentNameList = importKeysFromTemplateMapper.checkComboContentNameExists(params);
//        if (CollectionUtils.isNotEmpty(existsComboContentNameList) && existsComboContentNameList.size() > 0) {
//            throw new IceException("-1", "原价产品名称[" + joinString2AlertMessage(existsComboContentNameList) + "]已存在");
//        }
	}

	/**
	 * 获取excel表格中某列的所有数据
	 * 
	 * @param titleValue
	 * @param excelContentMap
	 * @return
	 */
	private Set<String> getAllColumnValue(String titleValue, Map<Integer, String> excelContentMap) {
		// 标题行
		final String[] titleRow = excelContentMap.get(0).split("#", excelContentMap.get(0).length());
		Set<String> resultSet = new HashSet<String>();
		for (int i = 1; i < excelContentMap.size(); i++) {
			final String[] contentRow = excelContentMap.get(i).split("#", excelContentMap.get(i).length());
			for (int j = 0; j < titleRow.length; j++) {
				if (titleValue.equals(titleRow[j].trim()) && StringUtils.isNotEmpty(contentRow[j].trim())) {
					resultSet.add(contentRow[j].trim());
				}
			}
		}
		return resultSet;
	}
	
	/**
	 * <p>屏蔽原价产品选是时的相关显示的字段获取</p>
	 *
	 * @param titleValue
	 * @param excelContentMap
	 * @return
	 */
	private Set<String> getAllPromotValue(String titleValue,Map<Integer, String> excelContentMap){
	    // 标题列
        final String[] titleCol = excelContentMap.get(0).split("#", excelContentMap.get(0).length());
        Set<String> resultSet = new HashSet<String>();
        for (int i = 1; i < excelContentMap.size(); i++) {
            boolean promotFlag = false;
            String activityType = "";
            final String[] contentCol = excelContentMap.get(i).split("#", excelContentMap.get(i).length());
            String titleColVal = "";
            for (int j = 0; j < titleCol.length; j++) {
                titleColVal = titleCol[j];
                if("活动类型".equals(titleColVal)){
                    if("堂食".equals(contentCol[j])){
                        activityType = "00001";
                    }else{
                        activityType = "00002";
                    }
                    
                }
                if("是否屏蔽原价产品".equals(titleColVal)){
                    if("是".equals(contentCol[j])){
                        promotFlag = true;
                    }
                }
                if(promotFlag && titleValue.equals(titleColVal)){
                    resultSet.add(activityType + ACPConstants.COMMA + contentCol[j].trim());
                }
            }

        }
        return resultSet;
	}

	/**
	 * excel数据进库
	 * 
	 * @param pc
	 * @param excelContentMap
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	private void importExcelContent(PageContext pc, Map<Integer, String> excelContentMap, List<PriceTypeInfo> priceTypeList) throws Exception {

	    final String brandCode = BrandHelper.getBrandCode();
		final List<ActivityInfo> activityList = new ArrayList<ActivityInfo>();
		final List<KeyInfo> keyList = new ArrayList<KeyInfo>();
		final List<ActivityKeyMapping> activityKeyMappingList = new ArrayList<ActivityKeyMapping>();
		final List<PopularizeInfo> popularizeList = new ArrayList<PopularizeInfo>();
		final List<SellPointInTimeInfo> sellPointList = new ArrayList<SellPointInTimeInfo>();
		final List<ComboContentInfo> comboContentList = new ArrayList<ComboContentInfo>();
		final List<ComboInfo> comboList = new ArrayList<ComboInfo>();
		final List<OutsideOrderInfo> outsideList = new ArrayList<OutsideOrderInfo>();
		final List<PriceInfo> priceList = new ArrayList<PriceInfo>();
		final List<ProductShiledDate> shiledDateList = new ArrayList<ProductShiledDate>();
		final List<ProductShiledTime> shiledTimeList = new ArrayList<ProductShiledTime>();

		final String[] titleRow = excelContentMap.get(0).split("#", excelContentMap.get(0).length());
		
		String lastKeyId = ""; // 上个设键
		int pdtSeqId = 0; // 组合内容中产品的序号
		int lastComboSeqId = 0; // 组合内容的序号
		for (int i = 1; i < excelContentMap.size(); i++) {
			final String[] contentRow = excelContentMap.get(i).split("#", excelContentMap.get(i).length());

			Date beginDate = new Date(); // 活动开始时间
			Date endDate = new Date(); // 活动结束时间

			String activityId = UUID.randomUUID().toString();
			String keyId = UUID.randomUUID().toString();
			String keyType = "";
			String cnName = "";
			String shortName="";
            boolean promotFlag = false; //为防止变量在行数间交叉影响，所以定义在行级的for循环内
            boolean activityExistInDB = false; //根据活动名称判断活动在系统DB中是否存在
            boolean activityExists = false; //活动名称判断活动在临时待保存Map中是否存在
            
			// 待保存活动
			final ActivityInfo activity = new ActivityInfo();
			activity.setGuid(activityId);
			activity.setBrandCode(brandCode);
			activity.setActivityClass(ACPConstants.ActivityClass.NATION_WIDE);
			activity.setActivityStatus(ACPConstants.ActivityStatus.DRAFT);
			activity.setApproveType(ACPConstants.ApproveType.NEW_ACTIVITY);

			// 待保存设键
			final KeyInfo key = new KeyInfo();
			key.setGuid(keyId);
			key.setUnderWayStep(ACPConstants.UnderWayStep.FINISH);

			// 待保存活动设键对照
			final ActivityKeyMapping activityKeyMapping = new ActivityKeyMapping();
			activityKeyMapping.setGuid(UUID.randomUUID().toString());
			activityKeyMapping.setIsMainActivity(ACPConstants.YES);
			activityKeyMapping.setKeyStatus(ACPConstants.KeyStatus.CREATE);
			activityKeyMapping.setKeySource(ACPConstants.KeySource.NEW);
			activityKeyMapping.setUnderWayStep(ACPConstants.UnderWayStep.FINISH);

			// 待保存外送
			final OutsideOrderInfo outside = new OutsideOrderInfo();
			outside.setGuid(UUID.randomUUID().toString());
			outside.setKeyId(keyId);

			// 待保存组合内容
			final ComboContentInfo comboContent = new ComboContentInfo();
			comboContent.setGuid(UUID.randomUUID().toString());
			comboContent.setKeyId(keyId);
			comboContent.setProductGroupSeqId(ACPConstants.ONE);
//			comboContent.setProductSeqId(ACPConstants.ONE);
			
			comboContent.setProductComboSeqId(ACPConstants.ONE);
			comboContent.setProductType(String.valueOf(ACPConstants.ZERO));
			comboContent.setComboType(String.valueOf(ACPConstants.ZERO));

			// 待保存套餐
			final ComboInfo combo = new ComboInfo();
			combo.setGuid(UUID.randomUUID().toString());
			combo.setKeyId(keyId);
			combo.setSeqId(ACPConstants.ONE);
			combo.setHasNonFood(String.valueOf(ACPConstants.ZERO));

			// 待保存售卖时间
			final SellPointInTimeInfo sellPointInTime = new SellPointInTimeInfo();
			sellPointInTime.setGuid(UUID.randomUUID().toString());
			sellPointInTime.setKeyId(keyId);
			sellPointInTime.setSeqId(String.valueOf(ACPConstants.ONE));
			
			// 待保存屏蔽售卖日期(注意：保存至db时需要判断是否应该保存.只有屏蔽原价的产品设键且屏蔽原价产品选“是”时保存)
			final ProductShiledDate shiledDate = new ProductShiledDate();
			shiledDate.setGuid(UUID.randomUUID().toString());
			shiledDate.setKeyId(keyId);
			// 带保存屏蔽售卖时间(注意点同上)
			final ProductShiledTime shiledTime = new ProductShiledTime();
			shiledTime.setGuid(UUID.randomUUID().toString());
			shiledTime.setKeyId(keyId);
			
			String activityType = ""; //活动类型，用于后续外送活动的判断使用，放在行循环内
			String comboProductName = ""; //组合内容所引用的产品名称
			boolean isPreOrder = false;
			
			for (int j = 0; j < titleRow.length; j++) {
			    
				final String titleValue = titleRow[j].trim();
				final String contentValue = contentRow[j].trim();
				if (titleValue.equals("活动名称")) {
					activity.setActivityName(contentValue);
					
                    // 从数据库的Map里面取activityId 
                    if (ACTIVITY_NAME_DB_MAP.get(contentValue) != null) {
                        activityId = ACTIVITY_NAME_DB_MAP.get(contentValue);
                        activity.setGuid(activityId);
                        activityExistInDB = true;
                        activityExists=true;
                    }
                    // 如果数据库Map里面没有取到activityId，则从临时待保存Map里面取activityId
                    if (!activityExistInDB) {
                        if (ACTIVITY_NAME_ID_MAP.get(contentValue) != null) {
                            activityId = ACTIVITY_NAME_ID_MAP.get(contentValue);
                            activity.setGuid(activityId);
                            activityExists = true;
                        } else {
                            ACTIVITY_NAME_ID_MAP.put(contentValue, activity.getGuid());
                        }
                    }
					
				} else if (titleValue.equals("活动类型")) {
					if (DINNER_ACTIVITY_TYPE.equals(contentValue)) {
					    activityType = ACPConstants.ActivityType.DINNER;
						activity.setActivityType(activityType);
					} else {
					    activityType = ACPConstants.ActivityType.OUTSIDE;
						activity.setActivityType(activityType);
					}
				} else if (titleValue.equals("活动申请人邮箱")) {
					activity.setApplicantId(USER_EMAIL_MAP.get(contentValue));
					activity.setCreateId(USER_EMAIL_MAP.get(contentValue));
				} else if (titleValue.equals("活动审批人邮箱")) {
					activity.setFirstApproverId(USER_EMAIL_MAP.get(contentValue));
				} else if (titleValue.equals("设键分类")) {
                    key.setKeyClassify(KEY_CLASSFIY_ID_MAP.get(contentValue));
                } else if (titleValue.equals("设键类型")) {
                    keyType = KEY_TYPE_ID_MAP.get(contentValue);
                    key.setKeyType(keyType);
                    
                } else if (!ACPConstants.brandCode.PHHS.equals(brandCode) && titleValue.equals("中文名称")) {
                    cnName= contentValue;
                    key.setCnName(contentValue);
                    if(ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(keyType)){
                        // 多个产品一个键位，才往comboinfo表中插入
                        combo.setComboNewName(contentValue);
                    }
                    if (!"00027".equals(keyType)){
                        // 非屏蔽原价产品，塞入值； 屏蔽原价产品不在此入值。
                        comboContent.setComboContentNewName(contentValue);
                    }
                    

                    if (KEY_NAME_ID_MAP.get(contentValue) != null) {
                        keyId = KEY_NAME_ID_MAP.get(contentValue);
                        key.setGuid(keyId);
                        combo.setKeyId(keyId);
                        comboContent.setKeyId(keyId);
                        sellPointInTime.setKeyId(keyId);
                    }
                    KEY_NAME_ID_MAP.put(contentValue, key.getGuid());
                } else if (!ACPConstants.brandCode.PHHS.equals(brandCode) && titleValue.equals("英文名称")) {
                    key.setEnName(contentValue);
                } else if (ACPConstants.brandCode.KFC.equals(brandCode) && titleValue.equals("简称")) {
                    shortName = contentValue;
                    key.setShortName(contentValue);
                    if(ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(keyType)){
                        combo.setComboShortName(contentValue);
                    }else{
                        comboContent.setComboContentNewShortName(contentValue);
                    }
                }else if (titleValue.equals("开始时间")) {
					beginDate = new SimpleDateFormat("yyyy-MM-dd").parse(contentValue);
				} else if (titleValue.equals("结束时间")) {
					endDate = new SimpleDateFormat("yyyy-MM-dd").parse(contentValue);
                } else if (titleValue.equals("推广范围")) {
                    List <PopularizeInfo> activityPopularizeListDB = null;
                    boolean activityPopularizeExist = false;//活动导入前是否已有活动级别的推广范围
                    if (activityExistInDB) {
                        HashMap <String, Object> acpp = new HashMap <String, Object>();
                        acpp.put("activityId", activityId);
                        activityPopularizeListDB = popularizeInfoMapper.queryActivityPopularizeInfo(acpp);
                        if (CollectionUtils.isNotEmpty(activityPopularizeListDB) && activityPopularizeListDB.size() > 0) {
                            activityPopularizeExist = true;
                        }
                    }
                    for (String popularize : contentValue.split(";")) {
                        if (popularize.equals("全国")) {
                            //如果导入前活动自身已有推广信息，那么不再添加活动级别的推广信息，否则添加(与目前页面录入的逻辑保持一致)。
                            if (!activityPopularizeExist) {
                                final PopularizeInfo activityPopularize = createPopularizeModel(activityId, null, beginDate, endDate, null, null,
                                        null);
                                if (!(checkPopularizeExists(popularizeList, activityPopularize, true))) {
                                    popularizeList.add(activityPopularize);
                                }
                            }
                            final PopularizeInfo keyPopularize = createPopularizeModel(activityId, keyId, beginDate, endDate, null, null, null);
                            if (!checkPopularizeExists(popularizeList, keyPopularize, false)) {
                                popularizeList.add(keyPopularize);
                            }
                        }
                        for (String marketCityStore : popularize.split(";")) {
                            final String[] popu = marketCityStore.split("=");
                            if (popu[0].equals("营运市场")) {
                                for (String opsMarket : popu[1].split(",")) {
                                    final String opsMarketCode = MARKET_ID_MAP.get(opsMarket);
                                    //如果导入前活动自身已有推广信息，那么不再添加活动级别的推广信息，否则添加。
                                    if (!activityPopularizeExist) {
                                        final PopularizeInfo activityPopularize = createPopularizeModel(activityId, null, beginDate, endDate,
                                                opsMarketCode, null, null);
                                        if (!(checkPopularizeExists(popularizeList, activityPopularize, true))) {
                                            popularizeList.add(activityPopularize);
                                        }
                                    }

                                    final PopularizeInfo keyPopularize = createPopularizeModel(activityId, keyId, beginDate, endDate, opsMarketCode,
                                            null, null);
                                    if (!checkPopularizeExists(popularizeList, keyPopularize, false)) {
                                        popularizeList.add(keyPopularize);
                                    }
                                }
                            } else if (popu[0].equals("城市")) {
                                for (String city : popu[1].split(",")) {
                                    //如果导入前活动自身没有推广信息，那么添加活动级别的推广信息(DB里面keyId等于null的那条推广记录)。
                                    if (!activityPopularizeExist) {
                                        final PopularizeInfo activityPopularize = createPopularizeModel(activityId, null, beginDate, endDate, null,
                                                city, null);
                                        if (!(checkPopularizeExists(popularizeList, activityPopularize, true))) {
                                            popularizeList.add(activityPopularize);
                                        }
                                    }

                                    final PopularizeInfo keyPopularize = createPopularizeModel(activityId, keyId, beginDate, endDate, null, city,
                                            null);
                                    if (!checkPopularizeExists(popularizeList, keyPopularize, false)) {
                                        popularizeList.add(keyPopularize);
                                    }
                                }
                            } else if (popu[0].equals("餐厅")) {
                                for (String store : popu[1].split(",")) {
                                    //如果导入前活动自身没有推广信息，那么添加活动级别的推广信息(DB里面keyId等于null的那条推广记录)。
                                    if (!activityPopularizeExist) {
                                        final PopularizeInfo activityPopularize = createPopularizeModel(activityId, null, beginDate, endDate, null,
                                                null, store);
                                        if (!(checkPopularizeExists(popularizeList, activityPopularize, true))) {
                                            popularizeList.add(activityPopularize);
                                        }
                                    }

                                    final PopularizeInfo keyPopularize = createPopularizeModel(activityId, keyId, beginDate, endDate, null, null,
                                            store);
                                    if (!checkPopularizeExists(popularizeList, keyPopularize, false)) {
                                        popularizeList.add(keyPopularize);
                                    }
                                }
                            }
                        }
                    }

                } else if (titleValue.equals("售卖时间")) {
					String weeklySellTime = "";
					List sellTimeList = Arrays.asList(contentValue.split(","));
					for (int z = 1; z <= 7; z++) {
						if (sellTimeList.contains(String.valueOf(z))) {
							weeklySellTime += "1";
						} else {
							weeklySellTime += "0";
						}
					}
					key.setWeeklySellTime(weeklySellTime);
					activityKeyMapping.setWeeklySellTime(weeklySellTime);
				} else if (titleValue.equals("国定假日是否售卖")) {
					key.setNationalHolidayIsSell(contentValue.equals("是") ? "1" : "0");
					activityKeyMapping.setNationalHolidayIsSell(key.getNationalHolidayIsSell());
				} else if (titleValue.equals("百胜卡赠送品项")) {
					key.setYumCard(contentValue.equals("是") ? "Y" : "N");
				} else if (ACPConstants.brandCode.PHDI.equals(brandCode) && titleValue.equals("是否允许被打折")) {
					key.setIsAllowDiscount(contentValue.equals("是") ? "1" : "0");
				} else if (ACPConstants.brandCode.KFC.equals(brandCode) && titleValue.equals("是否适用Pre-Order")) {
				    isPreOrder = contentValue.equals("是") ? true : false;
					key.setIsPreOrder(contentValue.equals("是") ? "1" : "0");
				} else if(titleValue.equals("Pre-Order类别")){
				    if(isPreOrder){
				        final StringBuffer sb = new StringBuffer();
                        for (String s : contentValue.split(ACPConstants.DOT)) {
                            sb.append(PRE_ORDER_MAP.get(s)).append(ACPConstants.COMMA);
                        }
                        if (sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                            sb.deleteCharAt(sb.length() - 1);
                        }
                        key.setPreOrder(sb.toString());
				    }
				}
				else if (ACPConstants.brandCode.PHHS.equals(brandCode) && titleValue.equals("是否适用RBD餐厅")) {
					key.setIsSuitrbdStore(contentValue.equals("是") ? "Y" : "N");
				} else if (titleValue.equals("售卖时间点开始时间")) {
					sellPointInTime.setBeginTime(new SimpleDateFormat("HH:mm").parse(contentValue));
				} else if (titleValue.equals("售卖时间点结束时间")) {
					sellPointInTime.setEndTime(new SimpleDateFormat("HH:mm").parse(contentValue));
				} else if (titleValue.equals("SellCategory")) {
                    for (ComboTypeInfo c : COMBO_TYPE_LIST) {
                        if (c.getSellType().equals(contentValue)) {
                            key.setSellTypeId(c.getSellTypeId());
                            break;
                        }
                    }
                } else if (titleValue.equals("ComboType")) {
                    for (ComboTypeInfo c : COMBO_TYPE_LIST) {
                        if (key.getSellTypeId().equals(c.getSellTypeId()) && c.getComboType().equals(contentValue)) {
                            key.setComboTypeId(c.getGuid());
                            break;
                        }
                    }
                } else if (titleValue.equals("备注")) {
                    key.setRemark(contentValue);
                } else if (titleValue.contains("-价格")) {
                    // 所有价格列
                    // 取价格类型
                    if (StringUtils.isNotEmpty(contentValue)) {
                        final String priceTypeName = titleValue.replace("-价格", "");
                        String priceTypeId = "";
                        for (PriceTypeInfo priceType : priceTypeList) {
                            if (priceType.getPriceType().equals(priceTypeName)) {
                                priceTypeId = priceType.getGuid();
                                break;
                            }
                        }
                        String fkGuid = keyId;
                        if (ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(key.getKeyType())) {
                            fkGuid = combo.getGuid();
                        }
                        final PriceInfo price = createPriceModel(activityId, fkGuid, priceTypeId, contentValue);
                        boolean priceExists = false;
                        for (PriceInfo p : priceList) {
                            if (p.getActivityId().equals(activity.getGuid()) && p.getFkGuid().equals(fkGuid) && p.getPriceTypeId().equals(priceTypeId)) {
                                priceExists = true;
                            }
                        }
                        if (!priceExists) {
                            priceList.add(price);
                        }
                    }
                }
				    
				    
				if(ACPConstants.ActivityType.OUTSIDE.equals(activityType)){
				    if (titleValue.equals("中文描述")) {
	                    outside.setCnDescription(contentValue);
	                } else if (titleValue.equals("英文描述")) {
	                    outside.setEnDescription(contentValue);
	                } else if (titleValue.equals("销售渠道")) {
	                    final StringBuffer sb = new StringBuffer();
	                    for (String s : contentValue.split(ACPConstants.DOT)) {
	                        sb.append(SELLCHANNEL_ID_MAP.get(s)).append(ACPConstants.COMMA);
	                    }
	                    if (sb.length() > 0) {
	                        sb.deleteCharAt(sb.length() - 1);
	                        sb.deleteCharAt(sb.length() - 1);
	                    }
	                    outside.setDistributionChannel(sb.toString());

	                } else if (titleValue.equals("网上基础产品分类")) {
	                    final StringBuffer sb = new StringBuffer();
	                    for (String s : contentValue.split(ACPConstants.DOT)) {
	                        sb.append(ONLINE_CLASS_ID_MAP.get(s)).append(ACPConstants.COMMA);
	                    }
	                    if (sb.length() > 0) {
	                        sb.deleteCharAt(sb.length() - 1);
	                        sb.deleteCharAt(sb.length() - 1);
	                    }
	                    outside.setOnLineShowClassifyId(sb.toString());
	                }
				    
				    if(ACPConstants.brandCode.KFC.equals(brandCode) || ACPConstants.brandCode.ED.equals(brandCode)){
				        // KFC和ED的外送才有的外送字段
				        if (titleValue.equals("支持的手机终端")) {
	                        outside.setPhoneTerminal(MOBILE_ID_MAP.get(contentValue));
	                    } else if (titleValue.equals("PromotionArea")) {
	                        outside.setPromotionarea(contentValue);
	                    } else if (titleValue.equals("量词")) {
	                        outside.setQuantifierId(QUANTIFIER_ID_MAP.get(contentValue));
	                    } else if (titleValue.equals("网上显示顺序")) {
	                        outside.setOnLineShowOrder(contentValue);
	                    } else if (titleValue.equals("显示分类开始日期")) {
	                        outside.setOnLineShowBeginDate(new SimpleDateFormat("yyyy-MM-dd").parse(contentValue));
	                    } else if (titleValue.equals("显示分类结束日期")) {
	                        outside.setOnLineShowEndDate(new SimpleDateFormat("yyyy-MM-dd").parse(contentValue));
	                    }
				    }
				}
				if(ACPConstants.brandCode.PHHS.equals(brandCode)){
                    if("BOH名称".equals(titleValue)){
                        cnName = contentValue;
                        key.setCnName(contentValue);
                        if(ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(keyType)){
                            combo.setComboNewName(contentValue);
                        }else{
                            comboContent.setComboContentNewName(contentValue);
                        }
                        if (KEY_NAME_ID_MAP.get(contentValue) != null) {
                            keyId = KEY_NAME_ID_MAP.get(contentValue);
                            key.setGuid(keyId);
                            combo.setKeyId(keyId);
                            comboContent.setKeyId(keyId);
                            sellPointInTime.setKeyId(keyId);
                        }
                        KEY_NAME_ID_MAP.put(contentValue, key.getGuid());
                        
                    }else if("SUS水单名称".equals(titleValue)){
                        key.setSusName(contentValue);
                    }else if("OS/IOS英文名称".equals(titleValue)){
                        key.setEnName(contentValue);
                    }else if("OS/IOS中文名称".equals(titleValue)){
                        shortName = contentValue;
                        key.setShortName(contentValue);
                        comboContent.setComboContentNewShortName(contentValue);
                    }                   
                }
				if(!ACPConstants.KeyType.PRODUCT_PROMOT.equals(keyType)){
                    if (titleValue.equals("组合内容-序号")) {
                        if(lastComboSeqId == 0){
                            // 初始化
                            lastComboSeqId = Integer.parseInt(contentValue);
                            pdtSeqId = 1;
                        }else{
                            if(lastKeyId.equals(keyId) && lastComboSeqId == Integer.parseInt(contentValue)){
                                pdtSeqId ++;
                            }else{
                                pdtSeqId = 1;
                            }
                            lastComboSeqId = Integer.parseInt(contentValue);// 这一次赋值，供下一次调用
                        }
                        comboContent.setComboSeqId(Integer.parseInt(contentValue));
                        comboContent.setProductSeqId(pdtSeqId);
                    } else if (titleValue.equals("组合内容-套餐中品项对应单品ITEMNAME")) {
                        comboContent.setProductId(PRODUCT_ID_MAP.get(contentValue));
                        comboProductName = contentValue;
                    } else if (titleValue.equals("组合内容-份数")) {
                        int amount = Integer.parseInt(contentValue);
                        comboContent.setAmount(amount);
                        if("00011".equals(keyType)){
                            //多个产品一个键位，才往comboinfo表中塞入数据
                            String comboName = comboProductName + "*" + amount;
                            combo.setComboName(comboName);
                            combo.setInnerComboName(comboName);
                            combo.setTmpInt(comboContent.getComboSeqId());
                        }
                        if (!"00027".equals(keyType)){
                            String comboContentName = cnName + "中"+comboProductName;
                            comboContent.setComboContentName(comboContentName);
                        }
                    } else if (titleValue.equals("组合内容-系数")) {
                        comboContent.setFactor(Double.parseDouble(contentValue));
                    } else if (titleValue.equals("组合内容-套餐中新名称")) {
                        if(StringUtils.isNotEmpty(contentValue)){
                            comboContent.setComboContentNewName(contentValue);
                        }
                    } 
                }
				
				if(ACPConstants.KeyType.PRODUCT_PROMOT.equals(keyType)){
				    if("是否屏蔽原价产品".equals(titleValue)){
                        if("是".equals(contentValue)){
                            promotFlag = true;
                            key.setIsShieldOldProduct("Y");
                        }else{
                            key.setIsShieldOldProduct("Y");
                        }
                    }
				}
				if(promotFlag){
				    if("原价产品名称".equals(titleValue)){
				        comboContent.setComboContentName(cnName + "中"+contentValue);
				        comboContent.setComboContentNewName(cnName);
				        comboContent.setComboContentNewShortName(shortName);
				        key.setShieldOldProductId(PRODUCT_ID_MAP.get(contentValue));
				        comboContent.setProductId(PRODUCT_ID_MAP.get(contentValue));
				        comboContent.setComboSeqId(ACPConstants.ONE);
				    }else if("屏蔽产品系数".equals(titleValue)){
				        comboContent.setFactor(Double.parseDouble(contentValue));
				    }else if("屏蔽产品份数".equals(titleValue)){
				        comboContent.setAmount(Integer.parseInt(contentValue));
                    }else if("屏蔽售卖时间".equals(titleValue)){
                        String weeklySellTime = "";
                        List sellTimeList = Arrays.asList(contentValue.split(","));
                        for (int z = 1; z <= 7; z++) {
                            if (sellTimeList.contains(String.valueOf(z))) {
                                weeklySellTime += "1";
                            } else {
                                weeklySellTime += "0";
                            }
                        }
                        key.setShieldWeeklySellTime(weeklySellTime);
                    }else if("屏蔽国定假日".equals(titleValue)){
                        if("是".equals(contentValue)){
                            key.setShieldNationalHolidayIsSell("1");
                        }else{
                            key.setShieldNationalHolidayIsSell("0");
                        }
                    }else if("屏蔽原价产品开始日期".equals(titleValue)){
                        shiledDate.setShieldBeginDate(new SimpleDateFormat("yyyy-MM-dd").parse(contentValue));//开始日期结束日期保存在表ACP_T_RPODUCT_SHILED_DATE，关联key_id即可
                    }else if("屏蔽原价产品结束日期".equals(titleValue)){
                        shiledDate.setShieldEndDate(new SimpleDateFormat("yyyy-MM-dd").parse(contentValue));
                    }else if("屏蔽原价产品开始时间".equals(titleValue)){
                        shiledTime.setShieldBeginTime(contentValue);//开始时间结束时间保存在表ACP_T_RPODUCT_SHILED_TIME，关联key_id即可
                    }else if("屏蔽原价产品结束时间".equals(titleValue)){
                        shiledTime.setShieldEndTime(contentValue);
                    }
				}
				
				
			}
			// 活动在待保存list中是否存在，如果不存在，则加入到待保存活动list里
            if (!activityExists) {
                activityList.add(activity);
            }
			

			// 如果设键不存在，则加入到待保存设键list里
			boolean keyExists = false;
			for (KeyInfo k : keyList) {
				if (k.getCnName().equals(key.getCnName())) {
					keyExists = true;
					break;
				}
			}
			if (!keyExists) {
				keyList.add(key);

				activityKeyMapping.setActivityId(activityId);
				activityKeyMapping.setKeyId(keyId);
				activityKeyMappingList.add(activityKeyMapping);

				if (ACPConstants.ActivityType.OUTSIDE.equals(activity.getActivityType())) {
					outsideList.add(outside);
				}
				
				sellPointList.add(sellPointInTime);
				
				if(null != shiledDate.getShieldBeginDate() && null != shiledDate.getShieldEndDate()){
				    shiledDateList.add(shiledDate);
				}
				if(null != shiledTime.getShieldBeginTime() && null != shiledTime.getShieldEndTime()){
				    shiledTimeList.add(shiledTime);
				}
			}
			if (ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(key.getKeyType())) {
                comboList.add(combo);
            }
			// 待保存组合内容
			comboContentList.add(comboContent);
			lastKeyId = keyId;
		}

		IHandler batchSaveHandler = SpringConfigHelper.getSave4LargeAmountHandler();
		// 1.批量保存活动主信息
		pc.setDaoClassName(ImportKeysFromTemplateMapper.class.getName());
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSaveActivity");

		Map<String, ModelBase> entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < activityList.size(); i++) {
			entitiesMap.put(String.valueOf(i), activityList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);

		// 2.批量保存设键信息
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSaveKey");
		entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < keyList.size(); i++) {
			entitiesMap.put(String.valueOf(i), keyList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);

		// 3.批量保存活动设键对照
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSaveActivityKeyMapping");
		entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < activityKeyMappingList.size(); i++) {
			entitiesMap.put(String.valueOf(i), activityKeyMappingList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);

		// 4.批量保存推广范围
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSavePopularize");
		entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < popularizeList.size(); i++) {
			entitiesMap.put(String.valueOf(i), popularizeList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);

		// 5.批量保存外送
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSaveOutside");
		entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < outsideList.size(); i++) {
			entitiesMap.put(String.valueOf(i), outsideList.get(i));
		}
		if (entitiesMap.size() > 0) {
			pc.setEntities(entitiesMap);
			batchSaveHandler.execute(pc);
		}

		// 6.批量保存价格
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSavePrice");
		entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < priceList.size(); i++) {
			entitiesMap.put(String.valueOf(i), priceList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);

		// 7.批量保存组合内容
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSaveComboContent");
		entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < comboContentList.size(); i++) {
			entitiesMap.put(String.valueOf(i), comboContentList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);

		// 8.批量保存套餐
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSaveCombo");
		entitiesMap = new HashMap<String, ModelBase>();
		// 根据comboList处理套餐，便于数据保存 start
		List<ComboInfo> newComboList = new ArrayList<ComboInfo>();
		String tmpKeyId = "";
		int tmpInt = 1;
		ComboInfo tmpCombo = new ComboInfo();
		for(int i = 0; i < comboList.size(); i++){
		    ComboInfo combo = comboList.get(i);
		    if("".equals(tmpKeyId)){
		        tmpKeyId = combo.getKeyId();
                tmpCombo = combo;
                tmpInt= combo.getTmpInt(); // 临时存放组合内容的序号
            }else{
                if(combo.getKeyId().equals(tmpKeyId) && tmpInt != combo.getTmpInt()){
                    // 如果当前套餐的设键与前一个设键一致，则说明是同一个设键下的套餐，所以组合套餐名称
                    String comboName = tmpCombo.getComboName() + "、" + combo.getComboName();
                    tmpCombo.setComboName(comboName);
                }else{
                    newComboList.add(tmpCombo);
                    tmpCombo = combo;
                    tmpKeyId = combo.getKeyId();
                }
            }
		    if(i == comboList.size() - 1){
		        newComboList.add(tmpCombo);   
		    }
		}
		// 根据comboList处理套餐，便于数据保存 end
		
		for (int i = 0; i < newComboList.size(); i++) {
			entitiesMap.put(String.valueOf(i), newComboList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);

		// 9.保存售卖时间
		pc.clearDaoMethodID();
		pc.setDaoMethodID("batchSaveSelltime");
		entitiesMap = new HashMap<String, ModelBase>();
		for (int i = 0; i < sellPointList.size(); i++) {
			entitiesMap.put(String.valueOf(i), sellPointList.get(i));
		}
		pc.setEntities(entitiesMap);
		batchSaveHandler.execute(pc);
		
		// 10.批量保存屏蔽原价产品日期(有条件的保存,shiledDateList存对象前已判断对象有效性)
		if(shiledDateList.size() > 0){
		    pc.clearDaoMethodID();
		    pc.setDaoMethodID("batchSaveProductShiledDate");
		    entitiesMap.clear();
		    for (int i=0; i< shiledDateList.size() ; i++){
		        entitiesMap.put(String.valueOf(i), shiledDateList.get(i));
		    }
		    pc.setEntities(entitiesMap);
		    batchSaveHandler.execute(pc);
		}
		
		// 11.批量保存屏蔽原价产品时间(有条件的保存，shiledTimeList存对象前已判断对象有效性)
       if(shiledTimeList.size() > 0){
           pc.clearDaoMethodID();
           pc.setDaoMethodID("batchSaveProductShiledTime");
           entitiesMap.clear();
           for (int i=0; i< shiledTimeList.size() ; i++){
               entitiesMap.put(String.valueOf(i), shiledTimeList.get(i));
           }
           pc.setEntities(entitiesMap);
           batchSaveHandler.execute(pc);
        }
	}

	/**
	 * 
	 * @param popularizeList
	 * @param popularize
	 * @param isActivityPopularize
	 * @return
	 */
	private boolean checkPopularizeExists(List<PopularizeInfo> popularizeList, PopularizeInfo popularize, boolean isActivityPopularize) {

	    if(popularizeList == null || popularizeList.size() == 0){
	        return false;
	    }else{
	        for (PopularizeInfo p : popularizeList) {
	            if (isActivityPopularize) {
	                if (p.getActivityId().equals(popularize.getActivityId()) && p.getKeyId() == null) {
	                    if (StringUtils.isNotEmpty(popularize.getOpsMarketCode()) && StringUtils.isEmpty(popularize.getCityCode())) {
	                        if (p.getOpsMarketCode() != null && p.getOpsMarketCode().equals(popularize.getOpsMarketCode())) {
	                            return true;
	                        }
	                    } else if (StringUtils.isNotEmpty(popularize.getCityCode())) {
	                        if (p.getCityCode() != null && p.getCityCode().equals(popularize.getCityCode())) {
	                            return true;
	                        }
	                    } else if (StringUtils.isNotEmpty(popularize.getStoreCode())) {
	                        if (p.getStoreCode() != null && p.getStoreCode().equals(popularize.getStoreCode())) {
	                            return true;
	                        }
	                    } else {
	                        return true;
	                    }
	                }
	            } else {
	                if (p.getActivityId().equals(popularize.getActivityId()) && p.getKeyId() != null && p.getKeyId().equals(popularize.getKeyId())) {
	                    if (StringUtils.isNotEmpty(popularize.getOpsMarketCode()) && StringUtils.isEmpty(popularize.getCityCode())) {
	                        if (p.getOpsMarketCode() != null && p.getOpsMarketCode().equals(popularize.getOpsMarketCode())) {
	                            return true;
	                        }
	                    } else if (StringUtils.isNotEmpty(popularize.getCityCode())) {
	                        if (p.getCityCode() != null && p.getCityCode().equals(popularize.getCityCode())) {
	                            return true;
	                        }
	                    } else if (StringUtils.isNotEmpty(popularize.getStoreCode())) {
	                        if (p.getStoreCode() != null && p.getStoreCode().equals(popularize.getStoreCode())) {
	                            return true;
	                        }
	                    } else {
	                        return true;
	                    }
	                }
	            }
	        }
	    }
		
		return false;
	}

	/**
	 * 
	 * @param activityId
	 * @param keyId
	 * @param beginDate
	 * @param endDate
	 * @param opsMarketCode
	 * @param cityCode
	 * @param storeCode
	 * @return
	 */
	private PopularizeInfo createPopularizeModel(String activityId, String keyId, Date beginDate, Date endDate, String opsMarketCode, String cityCode,
			String storeCode) {
		final PopularizeInfo p = new PopularizeInfo();
		p.setGuid(UUID.randomUUID().toString());
		p.setActivityId(activityId);
		p.setKeyId(keyId); // 设键的推广范围
		p.setBeginDate(beginDate);
		p.setEndDate(endDate);
		p.setPopularizeFlag(String.valueOf(ACPConstants.ZERO));
		p.setGroupId(ACPConstants.ONE);
		p.setSeqId(ACPConstants.ONE);
		p.setOpsMarketCode(opsMarketCode);
		p.setCityCode(cityCode);
		if (StringUtils.isNotEmpty(cityCode)) {
			p.setOpsMarketCode("1");
		}
		p.setStoreCode(storeCode);
		p.setStatus(ACPConstants.KeyStatus.CREATE);

		return p;
	}

	/**
	 * 
	 * @param activityId
	 * @param fkGuid
	 * @param priceTypeId
	 * @param price
	 * @return
	 */
	private PriceInfo createPriceModel(String activityId, String fkGuid, String priceTypeId, String price) {

		final PriceInfo p = new PriceInfo();
		p.setGuid(UUID.randomUUID().toString());
		p.setActivityId(activityId);
		p.setFkGuid(fkGuid);
		p.setSeqId(ACPConstants.ONE);
		p.setPriceTypeId(priceTypeId);
		p.setPrice(price);
		p.setManualInputFlag(ACPConstants.YES);
		return p;
	}

	/**
	 * 
	 * @param pc
	 * @return
	 * @throws Exception
	 */
	private List<PriceTypeInfo> getPriceTypeList(PageContext pc) throws Exception {

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("brandCode", BrandHelper.getBrandCode());
		final List<PriceTypeInfo> basicPriceTypeList = priceTypeInfoMapper.queryPriceTypeListSimple(params);
		Collections.sort(basicPriceTypeList);
		return basicPriceTypeList;
	}
	

    /**
     * <p>根据品牌Code获取品牌标识</p>
     *
     * @param brandCode
     * @return
     * @author tang_yuanyuan
     */
    private String getBrandCodeName(String brandCode) {

        if (StringUtil.isEmpty(brandCode)) {
            return "未知";
        } else if (ACPConstants.brandCode.KFC.equals(brandCode)) {
            return "KFC";
        } else if (ACPConstants.brandCode.PHDI.equals(brandCode)) {
            return "PHDI";
        } else if (ACPConstants.brandCode.ED.equals(brandCode)) {
            return "ED";
        } else if (ACPConstants.brandCode.PHHS.equals(brandCode)) {
            return "PHHS";
        } else {
            return "未知";
        }
    }


    /**
     * <p>判断字符串是否包含中文字符</p>
     *
     * @param str
     * @return
     * @author tang_yuanyuan
     * @date 2015-09-10
     */
    private boolean includeChinese(String str) {

        final int max = 0xff00;
        boolean isIncluCH = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c & max) != 0) {
                isIncluCH = true;
                break;
            }
        }

        return isIncluCH;
    }
    

    /**
     * <p>判断字符串是否是数字</p>
     *
     * @param str
     * @return
     * @author tang_yuanyuan
     * @date 2015-09-10
     */
    private boolean isNumberic(String str) {
        boolean numFlag = false;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            numFlag = false;
        } else {
            numFlag = true;
        }
        return numFlag;
    }


    private int str2Num(String str,int defaultVal) {

        int num = defaultVal;
        if (isNumberic(str)) {
            try {
                num = Integer.parseInt(str);
            } catch (NumberFormatException nfe) {
                num = defaultVal;
            }
        }
        return num;
    }


    /**
     * <p>根据字符串计算其字符长度</p>
     *
     * @param str
     * @return
     */
    private int characterLength(String value) {
        int valueLength = 0;
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (includeChinese(temp)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }
}
