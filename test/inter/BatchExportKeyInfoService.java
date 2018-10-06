package com.yum.boh.acp.service.activitymanage;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.common.util.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.yum.boh.acp.model.activitymanage.ActivityInfo;
import com.yum.boh.acp.model.activitymanage.ComboContentInfo;
import com.yum.boh.acp.model.activitymanage.ComboContentInfo4Page;
import com.yum.boh.acp.model.activitymanage.ComboInfo;
import com.yum.boh.acp.model.activitymanage.ComboKeyInfo;
import com.yum.boh.acp.model.activitymanage.CondimentClassInfo;
import com.yum.boh.acp.model.activitymanage.FavorableConfigInfo;
import com.yum.boh.acp.model.activitymanage.KeyContentInfo;
import com.yum.boh.acp.model.activitymanage.KeyInfo;
import com.yum.boh.acp.model.activitymanage.OutsideOrderInfo;
import com.yum.boh.acp.model.activitymanage.Popularize;
import com.yum.boh.acp.model.activitymanage.PriceInfo;
import com.yum.boh.acp.model.activitymanage.ProductDosingInfo;
import com.yum.boh.acp.model.activitymanage.ProductInfo;
import com.yum.boh.acp.model.activitymanage.ProductKeyInfo;
import com.yum.boh.acp.model.activitymanage.ProductPropertyContentInfo;
import com.yum.boh.acp.model.activitymanage.ProductPropertyInfo4Page;
import com.yum.boh.acp.model.activitymanage.ProductShiledDate;
import com.yum.boh.acp.model.activitymanage.ProductShiledTime;
import com.yum.boh.acp.model.activitymanage.SellPointInTimeInfo;
import com.yum.boh.acp.model.activitymanage.SpecialSellDateInfo;
import com.yum.boh.acp.model.systemmanage.ProductHierarchyUsingInfo;
import com.yum.boh.acp.model.systemmanage.SystemParameterInfo;
import com.yum.boh.acp.service.systemmanage.QueryProductHierarchyUsingInfoService;
import com.yum.boh.acp.service.systemmanage.QuerySystemParameterInfoService;
import com.yum.boh.acp.util.constant.ACPConfigConstants;
import com.yum.boh.acp.util.constant.ACPConstants;
import com.yum.boh.acp.util.helper.BrandHelper;
import com.yum.boh.acp.util.helper.ProductNamesConvertHelper;
import com.yum.boh.core.helper.PageContext;
import com.yum.boh.core.helper.PageResult;
import com.yum.boh.core.helper.SpringConfigHelper;
import com.yum.boh.core.helper.SuccessData;
import com.yum.boh.core.model.Code;
import com.yum.boh.core.packet.DataListResult;
import com.yum.boh.core.service.IService;
import com.yum.boh.core.service.ServiceBase;
import com.yum.boh.core.util.LogService;
import com.yum.boh.core.util.ResourceUtil;
import com.yum.boh.core.util.StringUtil;
import com.yum.boh.core.util.SystemParam;

/**
 * <p>
 * 导出设键
 * </p>
 * 
 * @page PIM_R010102
 * @module 活动管理
 * @author zhou_lantao
 * @date 2014-11-27
 * @version 1.0 修改履历 <br/>
 *          日期： 作者： 内容： <br/>
 */
@Service("com.yum.boh.acp.service.activitymanage.BatchExportKeyInfoService")
public class BatchExportKeyInfoService extends ServiceBase {

    /**
     * 工作薄
     */
    private HSSFWorkbook                  wb;

    /**
     * 工作表1
     */
    private HSSFSheet                     sheet1;

    /**
     * 行数
     */
    private int                           rowNum;

    /**
     * 总列数
     */
    private int                           totalColumnNum;

    /**
     * 单元格格式
     */
    // private HSSFDataFormat format;

    @Autowired
    private InitActivityInfoService       initActivityInfoService;

    @Autowired
    private QueryKeyInfoService           queryKeyInfoService;

    @Autowired
    InitProductKeyInfoService             initProductKeyInfoService;

    @Autowired
    InitKeyInfoService                    initKeyInfoService;

    @Autowired
    QueryComboKeyInfoService              queryComboKeyInfoService;

    @Autowired
    QueryFavorableConfigInfoService       queryFavorableConfigInfoService;

    @Autowired
    QueryProductHierarchyUsingInfoService productHierarchyUsingsevice;

    @Autowired
    QueryOutsideOrderInfoService          queryOutsideOrderInfoService;

    // 日志对象
    private final LogService              logger     = LogService.getLogger(this.getClass());

    // key值样式
    private HSSFCellStyle                 keyCellStyle;

    // value值样式
    private HSSFCellStyle                 valueCellStyle;

    // 序号样式
    private HSSFCellStyle                 numCellStyle;

    // 品牌
    private String                        brandCode;

    // 是否为调整设键
    private boolean                       isAdjust   = false;

    // 是否为外送活动
    private boolean                       isOutSide  = false;

    /** yyyy-MM-dd */
    private final SimpleDateFormat        dateFormat = new SimpleDateFormat(ACPConfigConstants.DATA_FORMAT);


    /**
     * 在service执行之前运行的代码
     * 
     * @param pc
     *            pageContext对象
     */
    @Override
    public boolean doBeforeService(PageContext pc) throws Exception {

        wb = new HSSFWorkbook();
        sheet1 = wb.createSheet("活动设键详情");

        // 设置工作表保护(此处为设置密码，效果和保护工作表相同)
        sheet1.protectSheet("123");
        // format = wb.createDataFormat();

        keyCellStyle = getKeyCellStyle();
        valueCellStyle = getValueCellStyle();
        numCellStyle = getNumCellStyle();

        // 获取品牌信息
        brandCode = BrandHelper.getBrandCode();

        // 调整设键
        final String adjust = pc.getBizdDataValueByKey("adjust");
        if (StringUtil.isNotEmpty(adjust) && "adjust".equals(adjust)) {
            isAdjust = true;
        }

        // 设置是否为外送活动
        final String activityType = pc.getBizdDataValueByKey("activityType");
        if (StringUtil.isNotEmpty(activityType) && ACPConstants.ActivityType.OUTSIDE.equals(activityType)) {
            isOutSide = true;
        }

        // 品牌
        pc.setBizDataValueByKey(ACPConstants.BRAND_CODE, BrandHelper.getBrandCode());
        final PageResult resultProductHierarchyUsing = productHierarchyUsingsevice.doServiceWithoutHandler(pc);

        // 返回层级启用信息
        pc.setBizDataValueByKey("productHierarchyUsingInfo", resultProductHierarchyUsing.getFirstObjectFromList());

        return true;
    }


    /**
     * 批量导出设键Excel
     * 
     * @param pc
     *            消息上下文
     * @return 操作结果
     * @throws Exception
     *             异常信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public PageResult doServiceWithoutHandler(PageContext pc) throws Exception {

        // 查询活动信息
        PageResult keyResult = initActivityInfoService.doServiceWithoutHandler(pc);

        // 获取查询结果
        final Map <String, Object> resultMap = (Map <String, Object>) keyResult.getData();

        // 获取活动信息
        final ActivityInfo activityInfoData = (ActivityInfo) resultMap.get("activityInfo");

        // 获取活动下设键信息
        pc.setBizDataValueByKey(ACPConstants.FunctionType.FUNCTION_TYPE, ACPConstants.FunctionType.VIEW_KEY);

        // 获取设键信息
        keyResult = queryKeyInfoService.doServiceWithoutHandler(pc);

        if (null != keyResult) {
            final List <KeyInfo> keyInfos = keyResult.getData(List.class);
            Collections.sort(keyInfos);

            // 判断是否存在设键信息
            if (null != keyInfos && !keyInfos.isEmpty()) {
                // 封装Excel工作簿内容
                createWorkTitle(activityInfoData, keyInfos.size());

                int count = 1;
                for (KeyInfo keyInfo : keyInfos) {
                    final String keyClassify = keyInfo.getKeyClassify();
                    final String keyType = keyInfo.getKeyType();

                    // 设置设键编号
                    setAlignmentStyle("设键 " + count, false, true, true);

                    // 设置设键分类和类型
                    pc.setBizDataValueByKey(ACPConstants.KeyInfo.KEY_CLASSIFY, keyClassify);
                    pc.setBizDataValueByKey(ACPConstants.KeyInfo.KEY_TYPE, keyType);
                    pc.setBizDataValueByKey(ACPConstants.KeyInfo.KEY_ID, keyInfo.getGuid());

                    // 根据设键类型获取设键信息
                    if (ACPConstants.KeyClassify.PRODUCT.equals(keyClassify) && ACPConstants.KeyType.PRODUCT.equals(keyType)) {
                        // 产品-产品
                        pc.setBizDataValueByKey("productType", keyInfo.getProductTypeId());

                        // 生成产品-产品的工作表
                        createProductWork(pc, keyClassify, keyType, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.PRODUCT.equals(keyClassify) && ACPConstants.KeyType.CONDIMENT.equals(keyType)) {
                        // 产品-配料
                        createCondimentWork(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.COMBO.equals(keyClassify)) {
                        // 套餐
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.MEALDEAL.equals(keyClassify)) {
                        // MEALDEAL
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.TRADEUP.equals(keyClassify)) {
                        // 查询TRADEUP优惠内容
                        pc.setBizDataValueByKey("comboType", "0");

                        // TRADEUP
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.DISCOUNT_COUPON.equals(keyClassify)) {
                        // 折价凭券
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.DISCOUNT_NOT_COUPON.equals(keyClassify)) {
                        if (ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(keyType) || ACPConstants.KeyType.MANY_PRODUCT_MANY_KEY.equals(keyType)
                                || ACPConstants.KeyType.ONE_PRODUCT_ONE_KEY.equals(keyType)) {
                            // 折价不凭券-产品折价
                            createComboPkg(pc, activityInfoData.getActivityType());
                        } else if (ACPConstants.KeyType.PRODUCT_PROMOT.equals(keyType)) {
                            // 折价不凭券-产品折价
                            createComboPkg(pc, activityInfoData.getActivityType());
                        }
                    } else if (ACPConstants.KeyClassify.FREE_EXCHANGE.equals(keyClassify)) {
                        // 免费兑换
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.TICKET_SELL.equals(keyClassify)) {
                        // 餐券售卖
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.TICKET_RECLAIM.equals(keyClassify)) {
                        // 餐券回收
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.DISCOUNT.equals(keyClassify)) {
                        // 折扣
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.CONSUME_CARD.equals(keyClassify)) {
                        // 消费卡
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.FAVORABLE_CONFIG.equals(keyClassify)) {

                        pc.setBizDataValueByKey("guid", keyInfo.getGuid());

                        // 优惠配置
                        createFavorableConfig(pc, keyType);
                    } else if (ACPConstants.KeyClassify.YUM_RECOVERY.equals(keyClassify)) {
                        // 百胜卡回收
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.DONATION_CLASSIFY.equals(keyClassify)) {
                        //捐款
                        createComboPkg(pc, activityInfoData.getActivityType());
                    } else if (ACPConstants.KeyClassify.YUM_CARDBILL_CLASSIFY.equals(keyClassify)) {
                        //百胜卡挂账
                        createComboPkg(pc, activityInfoData.getActivityType());
                    }

                    // 设置间隔行
                    setAlignmentStyle("", false, false, true);

                    count++;
                }
            }

            // 导出为Excel
            final String fileName = export2Excel(activityInfoData.getActivityName());
            final SuccessData successData = new SuccessData();

            // 返回结果
            successData.putExtra("filename", fileName);
            return new PageResult(successData);
        }

        return null;
    }


    /**
     * 优惠配置
     * 
     * @param pc
     *            消息上下文
     * @param keyType
     *            设键类型
     * @param keyClassify
     *            设键分类
     */
    @SuppressWarnings("unchecked")
    private void createFavorableConfig(PageContext pc, final String keyType) {

        PageResult keyResult = null;
        try {
            keyResult = queryFavorableConfigInfoService.doServiceWithoutHandler(pc);

            final Map <String, Object> resultMap = (Map <String, Object>) keyResult.getData();
            FavorableConfigInfo favorableConfigInfo = (FavorableConfigInfo) resultMap.get("favorableConfigInfo");

            // 初始化信息
            initFavorableConfigInfo(favorableConfigInfo);

            // 设置设键类型
            setWorkTitleStyle("设键类型", SystemParam.getCodeNameByMapKeyAndCode("ACP_KEY_TYPE", favorableConfigInfo.getKeyInfo().getKeyType()), true);

            // 产品组信息
            if (!ACPConstants.KeyType.FAV_REDUCE_MONEY.equals(keyType) && !ACPConstants.KeyType.FAV_DISCOUNT.equals(keyType)
                    && !ACPConstants.KeyType.FAV_SEND_FREE.equals(keyType)) {
                if (ACPConstants.KeyType.FAV_BUY_A_SEND_B_DISCOUNT.equals(keyType) || ACPConstants.KeyType.FAV_BUY_A_SEND_B_REDUCE.equals(keyType)
                        || ACPConstants.KeyType.FAV_BUY_A_SEND_B_PERCENT.equals(keyType) || ACPConstants.KeyType.FAV_BUY_A_DISCOUNT.equals(keyType)
                        || ACPConstants.KeyType.FAV_BUY_A_SEND_FREE.equals(keyType)
                        || ACPConstants.KeyType.FAV_BUY_A_SEND_B_HALF_PRICE.equals(keyType)) {
                    // 设置A产品组信息
                    setProductAGroupInfo(favorableConfigInfo);
                }

                if (ACPConstants.KeyType.FAV_BUY_A_SEND_B_DISCOUNT.equals(keyType) || ACPConstants.KeyType.FAV_BUY_A_SEND_B_REDUCE.equals(keyType)
                        || ACPConstants.KeyType.FAV_BUY_A_SEND_B_PERCENT.equals(keyType)
                        || ACPConstants.KeyType.FAV_BUY_X_MONEY_SEND_X_MONEY.equals(keyType)
                        || ACPConstants.KeyType.FAV_BUY_X_MONEY_DISCOUNT_X_MONEY.equals(keyType)
                        || ACPConstants.KeyType.FAV_BUY_X_MONEY_PERCENT_X_MONEY.equals(keyType)) {
                    // 设置B产品组信息
                    setProductBGroupInfo(favorableConfigInfo);
                }
            }

            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 网上中文描述
            setWorkTitleStyle("网上中文描述", favorableConfigInfo.getOnLineCnDescription(), true);

            // 网上英文描述
            setWorkTitleStyle("网上英文描述", favorableConfigInfo.getOnLineEnDescription(), true);

            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 优惠中文抬头
            setWorkTitleStyle("优惠中文抬头", favorableConfigInfo.getFavorableCnTitle(), true);

            // 优惠英文抬头
            setWorkTitleStyle("优惠英文抬头", favorableConfigInfo.getFavorableEnTitle(), true);

            // 封装基本信息
            createFavorableBaseInfo(keyType, favorableConfigInfo);

            // 封装使用日子
            createSuitDays(favorableConfigInfo);

            // 封装属性信息
            createFavProperty(keyType, favorableConfigInfo);

            // 推广范围
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            setWorkTitleStyle("推广范围", favorableConfigInfo.getPopularize().getPopularizeAreaName(), true);

            // 优惠配置信息
            createFavPropertyConfig(favorableConfigInfo);

            // 时间段
            // 设置间隔行
            setAlignmentStyle("", false, false, true);
            setWorkTitleStyle("使用时间", favorableConfigInfo.getUseBeginDate().concat(" 到   ").concat(favorableConfigInfo.getUseEndDate()), true);
            setWorkTitleStyle(
                    "有效期",
                    formatFavDate(favorableConfigInfo.getExpirationBeginDate()).concat(" 到   ").concat(
                            formatFavDate(favorableConfigInfo.getExpirationEndDate())), true);

            // 用餐时间段
            setAlignmentStyle("", false, false, true);
            setWorkTitleStyle("用餐时段", favorableConfigInfo.getDayPartName(), true);

            // 描述信息
            createRemark(favorableConfigInfo.getRemark());
        } catch (Exception e) {
            logger.error("导出优惠配置信息出错", e.getMessage(), e, null);
        }
    }


    /**
     * 封装优惠配置信息
     * 
     * @param favorableConfigInfo
     *            优惠配置信息
     */
    private void createFavPropertyConfig(final FavorableConfigInfo favorableConfigInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置节假日是否参加
        createKeyContentInfos(keyContentInfos, "节假日是否参加", favorableConfigInfo.getIsHolidayJoinDescription());

        // 设置销售渠道
        createKeyContentInfos(keyContentInfos, "销售渠道", favorableConfigInfo.getSellChannelDescription());

        // 设置是否预约单
        createKeyContentInfos(keyContentInfos, "是否预约单", favorableConfigInfo.getIsPreOrderDescription());

        // 设置餐厅类别
        createKeyContentInfos(keyContentInfos, "餐厅类别", favorableConfigInfo.getStoreTypeDescription());

        // 是否外带或外送
        createKeyContentInfos(keyContentInfos, "外带或外送", favorableConfigInfo.getTakeoutTypeDescription());

        // 是否提前预约时间
        createKeyContentInfos(keyContentInfos, "提前预约时间", favorableConfigInfo.getPreOrderTime());

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 设置优惠配置属性信息
     * 
     * @param keyType
     *            设键类型
     * @param favorableConfigInfo
     *            优惠配置信息
     */
    private void createFavProperty(final String keyType, final FavorableConfigInfo favorableConfigInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        if (!ACPConstants.KeyType.FAV_BUY_A_SEND_B_HALF_PRICE.equals(keyType)) {
            // 设置多买多送
            createKeyContentInfos(keyContentInfos, "多买多送", converDes(favorableConfigInfo.getIsBuyMoreGetMore()));
        }

        // 设置是否凭券
        createKeyContentInfos(keyContentInfos, "是否新店优惠", converDes(favorableConfigInfo.getNewStoreFavorable()));

        // 设置是否弹出
        if (((ACPConstants.brandCode.KFC.equals(brandCode) || ACPConstants.brandCode.ED.equals(brandCode)) && ("00034".equals(keyType)
                || "00035".equals(keyType) || "00038".equals(keyType) || "00039".equals(keyType)))
                || (ACPConstants.brandCode.PHHS.equals(brandCode) && ("00031".equals(keyType) || "00032".equals(keyType) || "00033".equals(keyType)
                        || "00035".equals(keyType) || "00036".equals(keyType) || "00038".equals(keyType) || "00039".equals(keyType)
                        || "00040".equals(keyType) || "00041".equals(keyType)))) {
            createKeyContentInfos(keyContentInfos, "是否弹出", converDes(favorableConfigInfo.getIsPop()));
        }
        if (!ACPConstants.KeyType.FAV_BUY_A_SEND_B_HALF_PRICE.equals(keyType)) {

            // 设置优惠代码
            createKeyContentInfos(keyContentInfos, "是否自动执行", converDes(favorableConfigInfo.getIsAutoExecute()));

        }

        if (ACPConstants.brandCode.PHHS.equals(brandCode)) {
            // 是否适用RBD餐厅
            createKeyContentInfos(keyContentInfos, "是否适用RBD餐厅", converDes(favorableConfigInfo.getIsSuitRbd()));
        }

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 封装使用日子
     * 
     * @param favorableConfigInfo
     */
    private void createSuitDays(final FavorableConfigInfo favorableConfigInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        String suitDays = "DayOfWeek";
        String suitDay = "";
        String[] DayOfSuit = null;
        if ("1".equals(favorableConfigInfo.getSuitDays())) {
            suitDays = "DayOfMonth";
            suitDay = favorableConfigInfo.getDayOfMonth();
            DayOfSuit = new String[] {"1号", "2号", "3号", "4号", "5号", "6号", "7号", "8号", "9号", "10号", "11号", "12号", "13号", "14号", "15号", "16号", "17号",
                    "18号", "19号", "20号", "21号", "22号", "23号", "24号", "25号", "26号", "27号", "28号", "29号", "30号", "31号"};
        } else {
            suitDay = favorableConfigInfo.getDayOfWeek();
            DayOfSuit = new String[] {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        }
        setWorkTitleStyle("适用日子", suitDays);

        // 封装页面上显示的信息
        if ("0".equals(favorableConfigInfo.getSuitDays())) {
            // 以星期为单位
            for (int i = 0; i < DayOfSuit.length; i++) {
                if (suitDay.indexOf(String.valueOf(i + 1)) == -1) {
                    DayOfSuit[i] = "";
                }
            }
        } else if ("1".equals(favorableConfigInfo.getSuitDays())) {
            // 以月为单位
            for (int i = 0; i < DayOfSuit.length; i++) {
                String tmp = "";
                if (String.valueOf(i + 1).length() == 1) {
                    tmp = "0".concat(String.valueOf(i + 1));
                } else {
                    tmp = String.valueOf(i + 1);
                }

                if (suitDay.indexOf(tmp) == 0) {
                    suitDay = suitDay.substring(ACPConstants.TWO, suitDay.length());
                } else {
                    DayOfSuit[i] = "";
                }
            }
        }
        createTableTitleForFav(DayOfSuit);

    }


    /**
     * 封装优惠配置基础信息
     * 
     * @param keyType
     *            设键类型
     * @param favorableConfigInfo
     *            优惠配置信息
     */
    private void createFavorableBaseInfo(final String keyType, final FavorableConfigInfo favorableConfigInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置是否凭券
        createKeyContentInfos(keyContentInfos, "是否凭券", converDes(favorableConfigInfo.getIsUseTicket()));

        // 设置优惠代码
        createKeyContentInfos(keyContentInfos, "优惠代码", favorableConfigInfo.getFavorableCode());

        // 设置优惠数值
        if (ACPConstants.brandCode.KFC.equals(BrandHelper.getBrandCode()) || ACPConstants.brandCode.ED.equals(BrandHelper.getBrandCode())) {
            createKeyContentInfos(keyContentInfos, "优惠数值", String.valueOf(favorableConfigInfo.getFavorableValueName()));
        } else {
            createKeyContentInfos(keyContentInfos, "优惠数值", String.valueOf(favorableConfigInfo.getFavorableValue()));
        }

        if (!ACPConstants.KeyType.FAV_BUY_A_SEND_B_HALF_PRICE.equals(keyType)) {
            // 设置整单金额
            createKeyContentInfos(keyContentInfos, "整单金额", String.valueOf(favorableConfigInfo.getEntireAmount()));
        }

        // 设置优先级
        createKeyContentInfos(keyContentInfos, "优先级", favorableConfigInfo.getPriority());

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 转换字段描述信息
     * 
     * @param converPam
     *            转换的字段
     * @return 转换后的字段描述
     */
    private String converDes(final String converPam) {

        return converDes(converPam, "N");
    }


    /**
     * 转换字段描述信息
     * 
     * @param converPam
     *            转换的字段
     * @return 转换后的字段描述
     */
    private String converDes(final String converPam, final String compam) {

        String value = "是";
        if (compam.equals(converPam)) {
            value = "否";
        }

        return value;
    }


    /**
     * 设置A产品组信息
     * 
     * @param favorableConfigInfo
     *            优惠信息
     */
    private void setProductAGroupInfo(final FavorableConfigInfo favorableConfigInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置A产品组
        createKeyContentInfos(keyContentInfos, "A产品组", favorableConfigInfo.getProductAGroupName());

        // 设置A产品
        if (CollectionUtils.isEmpty(favorableConfigInfo.getFavorableConfigProductList())) {
            createKeyContentInfos(keyContentInfos, "A产品", "");
        } else {
            createKeyContentInfos(keyContentInfos, "A产品", favorableConfigInfo.getFavorableConfigProductList().get(0).getProductName());
        }

        // 设置A产品类别
        createKeyContentInfos(keyContentInfos, "A产品类别", favorableConfigInfo.getProductACategoryName());

        // 设置A产品份数
        createKeyContentInfos(keyContentInfos, "A产品份数", String.valueOf(favorableConfigInfo.getProductAAmount()));

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 设置B产品组信息
     * 
     * @param favorableConfigInfo
     *            优惠信息
     */
    private void setProductBGroupInfo(final FavorableConfigInfo favorableConfigInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置B产品组
        createKeyContentInfos(keyContentInfos, "B产品组", favorableConfigInfo.getProductBGroupName());

        // 设置B产品
        if (CollectionUtils.isEmpty(favorableConfigInfo.getFavorableConfigProductList())) {
            createKeyContentInfos(keyContentInfos, "B产品", "");
        } else {
            createKeyContentInfos(keyContentInfos, "B产品", favorableConfigInfo.getFavorableConfigProductList().get(1).getProductName());
        }

        // 设置B产品份数
        createKeyContentInfos(keyContentInfos, "B产品份数", String.valueOf(favorableConfigInfo.getProductBAmount()));

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 封装产品详情
     * 
     * @param pc
     *            消息上下文
     * @param keyClassify
     * @param keyType
     * @param activityType
     */
    @SuppressWarnings("unchecked")
    private void createProductWork(PageContext pc, final String keyClassify, final String keyType, final String activityType) {

        PageResult keyResult = null;
        try {
            keyResult = initProductKeyInfoService.doServiceWithoutHandler(pc);

            // 获取产品信息
            final Map <String, Object> tempResultMap = (Map <String, Object>) keyResult.getData();
            final ProductKeyInfo productKey = (ProductKeyInfo) tempResultMap.get("productKeyInfo");

            // 获取设键信息
            final KeyInfo keyInfo = productKey.getKeyInfo();

            // 封装基础数据
            createBaseKeyInfo(keyInfo);

            // 封装产品类型
            setAlignmentStyle("", false, false, true);
            final String productTypeName = (String) tempResultMap.get("productTypeViewString");
            setWorkTitleStyle("产品类型", productTypeName);

            // 方式与类别
            List <ProductPropertyInfo4Page> propertyInfos = productKey.getProductPropertyList();
            createProperty(propertyInfos);

            // 规格
            final String preOrderViewStr = (String) tempResultMap.get("preOrderViewStr");
            createProductType(keyInfo, activityType, preOrderViewStr);

            // 推广时间和范围
            createPopularize(keyInfo);

            // 售卖时间
            createSellTime(keyInfo);

            // 售卖时间点
            createSellDate(keyInfo);

            // 特殊售卖日期
            createSpecialSellDate(keyInfo);

            // 产品层级
            final Map <String, Object> resultMap = (Map <String, Object>) pc.getBizDataObjectByKey("productHierarchyUsingInfo");
            final ProductHierarchyUsingInfo usingInfo = (ProductHierarchyUsingInfo) resultMap.get("productHierarchyUsingInfo");
            createProduct(usingInfo, productKey, activityType);

            // 设置是否关联RBD
            createIsRBD(keyInfo);

            // 外送产品
            createOutSider(pc);

            // 备注
            createRemark(keyInfo.getRemark());
        } catch (Exception e) {
            logger.error("导出产品信息出错", e.getMessage(), e, null);
        }
    }


    /**
     * 封装外送信息
     * 
     * @param pc
     *            消息上下文
     * @param activityType
     *            活动类型
     */
    @SuppressWarnings("unchecked")
    private void createOutSider(PageContext pc) {

        PageResult keyResult = null;

        try {
            if (isOutSide) {
                keyResult = queryOutsideOrderInfoService.doServiceWithoutHandler(pc);

                // 获取产品信息
                final Map <String, Object> tempResultMap = (Map <String, Object>) keyResult.getData();
                final OutsideOrderInfo outsideOrderInfo = (OutsideOrderInfo) tempResultMap.get("outsideOrderInfo");

                // 设置销售渠道
                createOutSideChannel(outsideOrderInfo);

                // 信息输入
                createOutSideInfo(outsideOrderInfo);
            }
        } catch (Exception e) {
            logger.error("封装外送信息失败", e.getMessage(), e, null);
        }
    }


    /**
     * 封装外送信息
     * 
     * @param outsideOrderInfo
     *            外送信息
     */
    private void createOutSideInfo(final OutsideOrderInfo outsideOrderInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        createKeyContentInfos(keyContentInfos, "中文描述", outsideOrderInfo.getCnDescription());

        createKeyContentInfos(keyContentInfos, "英文描述", outsideOrderInfo.getEnDescription());

        if (!ACPConstants.brandCode.PHHS.equals(brandCode)) {
            createKeyContentInfos(keyContentInfos, "PromotionArea 中文", outsideOrderInfo.getPromotionarea());

            createKeyContentInfos(keyContentInfos, "PromotionArea 英文", outsideOrderInfo.getPromotionareaEn());
        }
        createKeyContentInfos(keyContentInfos, "是否可售", outsideOrderInfo.getiSalesflagName());

        if (!ACPConstants.brandCode.PHHS.equals(brandCode)) {
            createKeyContentInfos(keyContentInfos, "量词", outsideOrderInfo.getQuantifierName());
        }
        createKeyContentInfos(keyContentInfos, "断货是否显示", outsideOrderInfo.getIsOutOfStockName());

        createKeyContentInfos(keyContentInfos, "CSC Promotion", outsideOrderInfo.getIsCscpromotionName());

        if (!ACPConstants.brandCode.PHHS.equals(brandCode)) {
            createKeyContentInfos(keyContentInfos, "优惠金额", outsideOrderInfo.getPromotionAmount());
        }
        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 设置销售渠道
     * 
     * @param outsideOrderInfo
     *            外送信息
     */
    private void createOutSideChannel(final OutsideOrderInfo outsideOrderInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        String sellChannel = "";
        // if (ACPConstants.YES.equals(outsideOrderInfo.getIsSuitOs())) {
        // sellChannel = sellChannel.concat("OS,");
        // }
        //
        // if (ACPConstants.YES.equals(outsideOrderInfo.getIsSuitIos())) {
        // sellChannel = sellChannel.concat("IOS,");
        // }
        //
        // if (ACPConstants.YES.equals(outsideOrderInfo.getIsSuitMos())) {
        // sellChannel = sellChannel.concat("MOS");
        // }
        final String distributionName = outsideOrderInfo.getDistributionChannelName();
        if (StringUtil.isNotEmpty(distributionName)) {
            sellChannel = distributionName;
        }

        setWorkTitleStyle("销售渠道", sellChannel, true);

    }


    /**
     * 封装是否适用RBD餐厅
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createIsRBD(final KeyInfo keyInfo) {

        if (ACPConstants.brandCode.PHHS.equals(brandCode)) {
            setWorkTitleStyle("是否适用RBD餐厅", converDes(keyInfo.getIsSuitrbdStore()));
        }
    }


    /**
     * 导出配料
     * 
     * @param pc
     *            消息上下文
     * @param activityType
     *            活动类型
     */
    @SuppressWarnings("unchecked")
    private void createCondimentWork(PageContext pc, final String activityType) {

        PageResult keyResult = null;

        try {
            keyResult = initKeyInfoService.doServiceWithoutHandler(pc);

            final Map <String, Object> resultMap = (Map <String, Object>) keyResult.getData();
            final List <CondimentClassInfo> condimentClassList = (List <CondimentClassInfo>) resultMap.get("CondimentClassList");
            final KeyInfo keyInfo = (KeyInfo) resultMap.get("keyInfo");

            // 封装基础数据
            createBaseKeyInfo(keyInfo);

            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 方式与类别
            String condimentClassName = "";
            // 初始化condiment中文
            for (CondimentClassInfo condimentClass : condimentClassList) {
                if (keyInfo.getCondimentClassId().equals(condimentClass.getGuid())) {
                    condimentClassName = condimentClass.getCondimentClass();
                }
            }
            setWorkTitleStyle("Condiment类别", condimentClassName);

            // 设置是否preOrder
            final String preOrderViewStr = (String) resultMap.get("preOrderViewStr");
            createIsPreOrder(activityType, keyInfo.getIsPreOrder(), preOrderViewStr);

            // 推广时间和范围 BF7827相关添加
            createPopularize(keyInfo);

            // 售卖时间 BF7827相关添加
            createSellTime(keyInfo);

            // 售卖时间点 BF7827相关添加
            createSellDate(keyInfo);

            // 特殊售卖日期 BF7827相关添加
            createSpecialSellDate(keyInfo);

            // 产品层级
            final ProductInfo productKey = (ProductInfo) resultMap.get("CondimentProdInfo");
            final Map <String, Object> resultMap1 = (Map <String, Object>) pc.getBizDataObjectByKey("productHierarchyUsingInfo");
            final ProductHierarchyUsingInfo usingInfo = (ProductHierarchyUsingInfo) resultMap1.get("productHierarchyUsingInfo");
            createProductHierarchy(usingInfo, productKey);

            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 设置成本与价格
            List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

            // 设置是否多种规格 isAllowVariousNorms
            createKeyContentInfos(keyContentInfos, "价格", keyInfo.getPrice());

            // 设置Non-Food
            createKeyContentInfos(keyContentInfos, "成本", keyInfo.getCost());

            setWorkTitleStyle(keyContentInfos);

            // 若是PHHS品牌，则设置是否RBD餐厅
            createIsRBD(keyInfo);
            // 备注
            createRemark(keyInfo.getRemark());

        } catch (Exception e) {
            logger.error("导出配料信息出错", e.getMessage(), e, null);
        }
    }


    /**
     * 导出套餐
     * 
     * @param pc
     *            消息上下文
     * @param activityType
     *            活动类型
     */
    private void createComboPkg(PageContext pc, final String activityType) {

        PageResult keyResult = null;

        try {
            keyResult = queryComboKeyInfoService.doServiceWithoutHandler(pc);

            // 获取组合内容信息
            final Map <String, Object> resultMap = ProductNamesConvertHelper.convertProductNames(keyResult);
            final ComboKeyInfo resultComboKey = (ComboKeyInfo) resultMap.get("comboKeyInfo");

            // 获取设键信息
            final KeyInfo keyInfo = resultComboKey.getKeyInfo();

            final String keyType = keyInfo.getKeyType();
            final String keyClassify = keyInfo.getKeyClassify();

            // 封装基础数据
            createBaseKeyInfo(keyInfo);

            // 设置是否preOrder
            final String preOrderViewStr = (String) resultMap.get("preOrderViewStr");
            createIsPreOrder(activityType, keyInfo.getIsPreOrder(), preOrderViewStr);

            // 折扣相关的设键，设置isTraceMenumix BF8528 start
            if ("00010".equals(keyClassify)) {
                createIsTraceMenumix(keyInfo.getIsTraceMenumix());
            }
            // BF8528 end

            // 推广范围与时间
            createPopularize(keyInfo);

            // 售卖时间、售卖时间点、特殊售卖日期
            if (!ACPConstants.KeyClassify.TICKET_SELL.equals(keyClassify) && !ACPConstants.KeyClassify.DONATION_CLASSIFY.equals(keyClassify)) {
                // 售卖时间
                createSellTime(keyInfo);

                // 售卖时间点
                createSellDate(keyInfo);

                // 特殊售卖日期
                createSpecialSellDate(keyInfo);
            }

            if (ACPConstants.KeyClassify.TRADEUP.equals(keyClassify)) {
                // 优惠内容
                createTradeUpWork(keyInfo, pc);

                // 优惠内容
                createFreeContent(resultComboKey);
            } else if (ACPConstants.KeyClassify.COMBO.equals(keyClassify)) {
                // 组合内容
                createComboContentInfo(keyInfo, resultComboKey.getComboContent4PageList());

                // 饮料是否可换
                createStandardDrink(pc, keyInfo);

                // 套餐详情
                createComboPkgContent(keyType, resultComboKey);

                // 套餐价格
                createComboPkgPrice(keyInfo, resultComboKey);
            } else if (ACPConstants.KeyClassify.MEALDEAL.equals(keyClassify)) {
                // 封装组合内容信息
                getComboContentInfo4MealDealPage(resultComboKey);

                // 组合内容
                createComboContentInfoForMealDeal(keyInfo, resultComboKey.getComboContent4PageList());

                // 设置MealDeal优先级
                createMealDealPriority(keyInfo);

                // 设置价格
                setPrice(keyInfo.getHasNonFood(), keyClassify, resultComboKey, keyInfo);
            } else if (ACPConstants.KeyClassify.DISCOUNT_COUPON.equals(keyClassify)
                    || ACPConstants.KeyClassify.DISCOUNT_NOT_COUPON.equals(keyClassify)) {

                // 如果是屏蔽原价产品的折扣不凭券
                if (ACPConstants.KeyClassify.DISCOUNT_NOT_COUPON.equals(keyClassify) && ACPConstants.KeyType.PRODUCT_PROMOT.equals(keyType)) {
                    // 封装屏蔽原价产品信息
                    createProductPromot(keyInfo);
                } else {
                    // 组合内容
                    createComboContentInfo(keyInfo, resultComboKey.getComboContent4PageList());

                    // 组合内容详情
                    createCombintionContent(keyType, resultComboKey);
                }

                if (ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(keyType)) {
                    // 设置组合内容价格
                    createPrice(resultComboKey.getComboList(), keyInfo, true);
                } else {
                    // 设置价格 (多个产品多个键位、屏蔽原价的产品促销)
                    setPrice(keyInfo.getHasNonFood(), keyClassify, resultComboKey, keyInfo);
                }
            } else if (ACPConstants.KeyClassify.FREE_EXCHANGE.equals(keyClassify)) {
                // PROMO FREE 组合内容
                createFreeChangeComboContent(keyInfo, resultComboKey);
            } else if (ACPConstants.KeyClassify.TICKET_SELL.equals(keyClassify) || ACPConstants.KeyClassify.DONATION_CLASSIFY.equals(keyClassify)) {
                // 设置间隔行
                setAlignmentStyle("", false, false, true);

                // 价格
                setWorkTitleStyle("价格", keyInfo.getPrice().concat("元"));

                // 生效日
                createComboAdjustPrice(keyInfo);
            } else if (ACPConstants.KeyClassify.TICKET_RECLAIM.equals(keyClassify)) {
                // 价格
                createTicketReclaim(keyInfo, resultComboKey);
            } else if (ACPConstants.KeyClassify.DISCOUNT.equals(keyClassify)) {
                // 设置表单内容
                createDiscount(keyInfo, resultComboKey);
            } else if (ACPConstants.KeyClassify.CONSUME_CARD.equals(keyClassify)) {
                // 设置表单内容
                createConsumeCard(keyInfo, resultComboKey);
            } else if (ACPConstants.KeyClassify.YUM_CARDBILL_CLASSIFY.equals(keyClassify)) {
                createConsumeCard(keyInfo, resultComboKey);

            }

            // 封装售卖类型
            createSellType(keyInfo);

            // 设置外送信息
            createCmoboOutSideInfo(pc, keyInfo);

            // 设置是否百胜卡
            createIsYumCard(keyInfo);

            // 设置是否允许打折
            createIsAllowDisCount(keyInfo);

            // 封装备注
            createRemark(keyInfo.getRemark());
        } catch (Exception e) {
            logger.error("导出套餐信息出错", e.getMessage(), e, null);
        }
    }


    /**
     * 封装组合内容页面显示信息
     * 
     * @param resultComboKey
     *            设键信息
     */
    private void getComboContentInfo4MealDealPage(final ComboKeyInfo resultComboKey) {

        final List <ComboContentInfo4Page> comboContentList = resultComboKey.getComboContent4PageList();
        Map <Integer, Integer> comboSeqIdCount = new HashMap <Integer, Integer>();
        if (!CollectionUtils.isEmpty(comboContentList)) {

            for (int i = 0; i < comboContentList.size(); i++) {

                if (comboSeqIdCount.containsKey(comboContentList.get(i).getComboSeqId())) {
                    comboSeqIdCount.put(comboContentList.get(i).getComboSeqId(), comboSeqIdCount.get(comboContentList.get(i).getComboSeqId()) + 1);
                } else {
                    comboSeqIdCount.put(comboContentList.get(i).getComboSeqId(), 1);
                }
            }

            for (int i = 0; i < comboContentList.size(); i++) {
                comboContentList.get(i).setComboSeqRowSpan(comboSeqIdCount.get(comboContentList.get(i).getComboSeqId()));

                if (i == 0) {
                    comboContentList.get(i).setShowFlag(ACPConstants.YES);
                } else {
                    ComboContentInfo4Page preComboContent = comboContentList.get(i - 1);
                    if (preComboContent.getComboSeqId() != comboContentList.get(i).getComboSeqId()) {
                        comboContentList.get(i).setShowFlag(ACPConstants.YES);
                    }
                }
            }
        }
    }


    /**
     * 设置产品屏蔽信息
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createProductPromot(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置成本与价格
        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置是否是否屏蔽原价产品
        createKeyContentInfos(keyContentInfos, "是否屏蔽原价产品", converDes(keyInfo.getIsShieldOldProduct()));

        if ("Y".equals(keyInfo.getIsShieldOldProduct())) {
            // 设置原价产品名称
            createKeyContentInfos(keyContentInfos, "原价产品名称", keyInfo.getShieldOldProductName());
        }

        setWorkTitleStyle(keyContentInfos);

        if ("Y".equals(keyInfo.getIsShieldOldProduct())) {
            // 设置原价产品屏蔽信息
            createShieldSell(keyInfo);

            // 设置屏蔽原价产品日期
            createShieldSellDate(keyInfo);

            // 设置屏蔽原价产品时间
            createShieldSellTime(keyInfo);
            // 生效日
            if (isAdjust) {
                final Date comboContentEffectiveBeginDate = keyInfo.getAdjustComboContentEffectiveBeginDate();
                if (null != comboContentEffectiveBeginDate) {
                    setWorkTitleStyle("生效日", dateFormat.format(comboContentEffectiveBeginDate), true);
                } else {
                    setWorkTitleStyle("生效日", "", true);
                }

            }
        }
    }


    /**
     * 设置外送信息
     * 
     * @param pc
     *            消息上下文
     * @param keyInfo
     *            设键信息
     */
    private void createCmoboOutSideInfo(PageContext pc, final KeyInfo keyInfo) {

        final String keyClassify = keyInfo.getKeyClassify();
        final String keyType = keyInfo.getKeyType();

        if (ACPConstants.KeyClassify.COMBO.equals(keyClassify) || ACPConstants.KeyClassify.TRADEUP.equals(keyClassify)
                || ACPConstants.KeyClassify.MEALDEAL.equals(keyClassify) || ACPConstants.KeyClassify.DISCOUNT_COUPON.equals(keyClassify)
                || ACPConstants.KeyClassify.DISCOUNT_NOT_COUPON.equals(keyClassify)
                || (ACPConstants.KeyClassify.FREE_EXCHANGE.equals(keyClassify) && ACPConstants.KeyType.ZERO_PRODUCT.equals(keyType))
                || ACPConstants.KeyClassify.TICKET_SELL.equals(keyClassify) || ACPConstants.KeyClassify.TICKET_RECLAIM.equals(keyClassify)
                || ACPConstants.KeyClassify.DISCOUNT.equals(keyClassify) || ACPConstants.KeyClassify.CONSUME_CARD.equals(keyClassify)
                || ACPConstants.KeyClassify.YUM_RECOVERY.equals(keyClassify) || ACPConstants.KeyClassify.DONATION_CLASSIFY.equals(keyClassify)) {
            if (isOutSide) {
                // 披萨品牌显示是否RBD餐厅
                if (ACPConstants.brandCode.PHHS.equals(brandCode)) {
                    // 设置是否关联RBD
                    createIsRBD(keyInfo);
                }

                if (!ACPConstants.KeyClassify.TRADEUP.equals(keyClassify) && !ACPConstants.KeyClassify.TICKET_SELL.equals(keyClassify)
                        && !ACPConstants.KeyClassify.TICKET_RECLAIM.equals(keyClassify) && !ACPConstants.KeyClassify.DISCOUNT.equals(keyClassify)
                        && !ACPConstants.KeyClassify.CONSUME_CARD.equals(keyClassify) && !ACPConstants.KeyClassify.YUM_RECOVERY.equals(keyClassify)
                        && !ACPConstants.KeyClassify.DONATION_CLASSIFY.equals(keyClassify)) {
                    // 外送产品
                    createOutSider(pc);
                }
            }
        }
    }


    /**
     * 设置是否百胜卡
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createIsYumCard(final KeyInfo keyInfo) {

        final String keyClassify = keyInfo.getKeyClassify();

        // 百胜卡出售赠送品项
        if (ACPConstants.KeyClassify.DISCOUNT_COUPON.equals(keyClassify) || (ACPConstants.KeyClassify.DISCOUNT_NOT_COUPON.equals(keyClassify))) {

            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            setWorkTitleStyle("百胜卡出售赠送品项", converDes(keyInfo.getYumCard()));
        }
    }


    /**
     * 封装是否允许被打折
     * 
     * @param keyInfo
     */
    private void createIsAllowDisCount(final KeyInfo keyInfo) {

        final String keyType = keyInfo.getKeyType();
        final String keyClassify = keyInfo.getKeyClassify();

        // 打折信息
        if (ACPConstants.KeyClassify.DISCOUNT_COUPON.equals(keyClassify) || ACPConstants.KeyClassify.FREE_EXCHANGE.equals(keyClassify)
                || ACPConstants.KeyClassify.DISCOUNT_NOT_COUPON.equals(keyClassify) || ACPConstants.KeyClassify.COMBO.equals(keyClassify)
                || ACPConstants.KeyClassify.MEALDEAL.equals(keyClassify) || ACPConstants.KeyClassify.TRADEUP.equals(keyClassify)) {

            if (ACPConstants.KeyClassify.DISCOUNT_NOT_COUPON.equals(keyClassify)) {
                if (ACPConstants.KeyType.PRODUCT_PROMOT.equals(keyType)) {
                    return;
                }
            }

            if (ACPConstants.brandCode.PHDI.equals(brandCode)) {
                // 设置间隔行
                setAlignmentStyle("", false, false, true);

                // 封装打折信息
                String isAllowDiscount = "是";
                if ("0".equals(keyInfo.getIsAllowDiscount())) {
                    isAllowDiscount = "否";
                }
                setWorkTitleStyle("是否允许被打折", isAllowDiscount);
            }
        }
    }


    /**
     * 封装消费卡
     * 
     * @param keyInfo
     *            设键信息
     * @param resultComboKey
     *            组合内容信息
     */
    private void createConsumeCard(final KeyInfo keyInfo, final ComboKeyInfo resultComboKey) {

        if (ACPConstants.KeyType.FIXED_MONEY.equals(keyInfo.getKeyType()) || ACPConstants.KeyType.ALIPAY.equals(keyInfo.getKeyType())) {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 价格
            setWorkTitleStyle("价格", keyInfo.getPrice().concat("元"));
        }

        // 封装是否RBD餐厅
        getIsSuitrbdStore(keyInfo);

        if (ACPConstants.KeyType.FIXED_PRODUCT.equals(keyInfo.getKeyType()) || ACPConstants.KeyType.YUM_CARDBILL_TYPE.equals(keyInfo.getKeyType())) {
            // 封装组合内容
            createComboContentInfo(keyInfo, resultComboKey.getComboContent4PageList());

            // 设置价格
            setPrice(keyInfo.getHasNonFood(), keyInfo.getKeyClassify(), resultComboKey, keyInfo);
        }
    }


    /**
     * 封装设置是否RBD餐厅
     * 
     * @param keyInfo
     *            设键信息
     */
    private void getIsSuitrbdStore(final KeyInfo keyInfo) {

        if (ACPConstants.ActivityType.OUTSIDE.equals(keyInfo.getActivityType()) && ACPConstants.brandCode.PHHS.equals(brandCode)) {
            String isSuitrbdStore = "是";
            if ("N".equals(keyInfo.getIsSuitrbdStore())) {
                isSuitrbdStore = "否";
            }

            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 价格
            setWorkTitleStyle("是否适用RBD餐厅", isSuitrbdStore);
        }
    }


    /**
     * 封装折扣
     * 
     * @param keyInfo
     *            设键信息
     * @param resultComboKey
     *            组合内容信息
     */
    private void createDiscount(final KeyInfo keyInfo, final ComboKeyInfo resultComboKey) {

        if (ACPConstants.KeyType.PRODUCT_DISCOUNT.equals(keyInfo.getKeyType())) {
            // 组合内容
            createComboContentInfo(keyInfo, resultComboKey.getComboContent4PageList());

            if (ACPConstants.brandCode.KFC.equals(brandCode)) {
                // 封装组合内容
                createComboName("优惠内容", "折扣名称", resultComboKey);

                // 设置组合内容价格
                createPrice(resultComboKey.getComboList(), keyInfo, true);
            } else if (ACPConstants.brandCode.ED.equals(brandCode)) {
                // 封装组合内容
                createComboContentName("", "折扣名称", resultComboKey, false);
            }

            if (ACPConstants.brandCode.ED.equals(brandCode) || ACPConstants.brandCode.PHDI.equals(brandCode)
                    || ACPConstants.brandCode.PHHS.equals(brandCode)) {
                // 设置组合内容价格
                setPrice(keyInfo.getHasNonFood(), keyInfo.getKeyClassify(), resultComboKey, keyInfo);
            }
        } else if (ACPConstants.KeyType.PERCENT_DISCOUNT.equals(keyInfo.getKeyType())) {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 价格
            setWorkTitleStyle("折扣百分比", keyInfo.getDiscountPercent().concat("%"));
        } else if (ACPConstants.KeyType.CASH_DISCOUNT.equals(keyInfo.getKeyType())) {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 价格
            setWorkTitleStyle("折扣金额", keyInfo.getPrice().concat("元"));
        }
    }


    /**
     * 封装餐券回收
     * 
     * @param keyInfo
     *            设键信息
     * @param resultComboKey
     *            组合内容信息
     */
    private void createTicketReclaim(final KeyInfo keyInfo, final ComboKeyInfo resultComboKey) {

        if (!ACPConstants.KeyType.CASH_TICKET_RECOVER.equals(keyInfo.getKeyType())) {
            // 组合内容
            createComboContentInfo(keyInfo, resultComboKey.getComboContent4PageList());

            // 设置价格
            setPrice(keyInfo.getHasNonFood(), keyInfo.getKeyClassify(), resultComboKey, keyInfo);
        } else {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 价格
            setWorkTitleStyle("价格", keyInfo.getPrice().concat("元"));

            // 生效日
            createComboAdjustPrice(keyInfo);
        }

        // 封装是否RBD餐厅
        getIsSuitrbdStore(keyInfo);
    }


    /**
     * 
     * @param ketType
     * @param resultComboKey
     */
    private void createFreeChangeComboContent(final KeyInfo keyInfo, final ComboKeyInfo resultComboKey) {

        if (ACPConstants.KeyType.PROMO_FREE.equals(keyInfo.getKeyType())) {
            // 组合内容
            createComboContentInfo(keyInfo, resultComboKey.getComboContent4PageList());

            // 免费兑换
            createFreeChangeCombo(resultComboKey);
        } else if (ACPConstants.KeyType.ZERO_PRODUCT.equals(keyInfo.getKeyType())) {
            // 组合内容
            createComboContentInfo(keyInfo, resultComboKey.getComboContent4PageList());

            // 免费兑换中组合内容名称
            createComboContentNameForZeroProduct(resultComboKey);
        } else if (ACPConstants.KeyType.FREE_RECUP.equals(keyInfo.getKeyType())) {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 设置组合内容
            setAlignmentStyle("添加免费续杯", false, true, true);

            // 设置标题行
            String[] tableTitle = new String[] {"序号", "前置条件", "续杯饮料 ", "续杯次数"};
            createTableTitle(tableTitle);

            List <ComboContentInfo4Page> comboList = resultComboKey.getComboContent4PageList();

            if (!CollectionUtils.isEmpty(comboList)) {
                final int len = comboList.size();

                for (int i = 0; i < len; i++) {
                    // 重起一行
                    HSSFRow row = sheet1.createRow(rowNum++);

                    ComboContentInfo4Page comboContentInfo = comboList.get(i);

                    // 设置序号
                    setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                    // 配料Condiment类别 condimentClassName
                    setTitleValue(row, ++totalColumnNum,
                            comboContentInfo.getPreposeProductNames().replaceAll("<br/>", System.getProperty("line.separator")), valueCellStyle);

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum,
                            comboContentInfo.getProductNames().replaceAll("<br/>", System.getProperty("line.separator")), valueCellStyle);

                    // add by zhang_yanyan start 2015-3-31
                    // 设置续杯次数
                    setTitleValue(row, ++totalColumnNum,
                            comboContentInfo.getFreecupTimes().replaceAll("<br/>", System.getProperty("line.separator")), valueCellStyle);
                    // add by zhang_yanyan end 2015-3-31

                    // 重置
                    totalColumnNum = 0;
                }
            }
        }
    }


    /**
     * 封装展示组合内容中心名称及新简称的表单
     * 
     * @param titleName
     *            表单标题
     * @param tableTitleName
     *            表单表格内容
     * @param resultComboKey
     *            组合内容
     */
    private void createComboName(final String titleName, final String tableTitleName, final ComboKeyInfo resultComboKey) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle(titleName, false, true, true);// 免费兑换名称

        // 设置标题行
        String[] tableTitle = new String[] {"序号", tableTitleName, "新名称", "新简称"}; // "套餐名称"
        if (isAdjust && ACPConstants.brandCode.KFC.equals(brandCode)) {
            tableTitle = new String[] {"序号", tableTitleName, "新名称", "新简称", "生效日"};
        }
        createTableTitle(tableTitle);

        List <ComboInfo> comboList = resultComboKey.getComboList();

        if (!CollectionUtils.isEmpty(comboList)) {
            final int len = comboList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ComboInfo comboContentInfo = comboList.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboNewName(), valueCellStyle);

                // 设置份数
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboShortName(), valueCellStyle);
                // 设置生效日
                if (isAdjust && ACPConstants.brandCode.KFC.equals(brandCode)) {
                    if (null != comboContentInfo.getAdjustNameEffectiveBeginDate()) {
                        setTitleValue(row, ++totalColumnNum, dateFormat.format(comboContentInfo.getAdjustNameEffectiveBeginDate()), valueCellStyle);
                    } else {
                        setTitleValue(row, ++totalColumnNum, "", valueCellStyle);
                    }

                }

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 封装套餐中组合内容的内容
     * 
     * @param titleName
     *            表单标题
     * @param tableTitleName
     *            表单表格内容
     * @param resultComboKey
     *            组合内容
     * @param isShowTitle
     *            是否显示表头
     *
     */
    private void createComboContentName(final String titleName, final String tableTitleName, final ComboKeyInfo resultComboKey,
            final boolean isShowTitle) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        if (isShowTitle) {
            // 设置组合内容
            setAlignmentStyle(titleName, false, true, true);// 免费兑换中组合内容名称
        }

        // 设置标题行
        String[] tableTitle = new String[] {"序号", tableTitleName, "新名称", "新简称"};// 套餐中组合内容名称
        createTableTitle(tableTitle);

        List <ComboContentInfo> comboContentList = resultComboKey.getComboContentList();

        if (!CollectionUtils.isEmpty(comboContentList)) {
            final int len = comboContentList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ComboContentInfo comboContentInfo = comboContentList.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewName(), valueCellStyle);

                // 设置份数
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewShortName(), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 封装免费兑换组内容信息
     * 
     * @param resultComboKey
     *            组合内容信息
     */
    private void createFreeChangeCombo(final ComboKeyInfo resultComboKey) {

        if (ACPConstants.brandCode.KFC.equals(brandCode)) {
            // 封装组合内容
            createComboName("免费兑换名称", "套餐名称", resultComboKey);
        } else if (ACPConstants.brandCode.ED.equals(brandCode)) {
            // 封装组合内容
            createComboContentName("免费兑换中组合内容名称", "套餐中组合内容名称", resultComboKey, true);
        }
    }


    /**
     * 封装套餐中组合内容的内容
     * 
     * @param resultComboKey
     *            组合内容
     *
     */
    private void createComboContentNameForZeroProduct(final ComboKeyInfo resultComboKey) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle("免费兑换中组合内容名称", false, true, true);// 免费兑换中组合内容名称

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "组合内容名称", "新中文名称", "新简称"};// 套餐中组合内容名称

        // PHDI品牌和PHHS品牌不需要新简称
        if (ACPConstants.brandCode.PHDI.equals(brandCode) || ACPConstants.brandCode.PHHS.equals(brandCode)) {
            tableTitle[ACPConstants.THREE] = "";
        }
        createTableTitle(tableTitle);

        List <ComboContentInfo> comboContentList = resultComboKey.getComboContentList();

        if (!CollectionUtils.isEmpty(comboContentList)) {
            final int len = comboContentList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ComboContentInfo comboContentInfo = comboContentList.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewName(), valueCellStyle);

                if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                    // 设置份数
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewShortName(), valueCellStyle);
                }

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 封装TradeUp优惠方式
     * 
     * @param keyInfo
     *            设键信息
     * @param pc
     *            消息上下文
     */
    private void createTradeUpWork(final KeyInfo keyInfo, PageContext pc) throws Exception {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        String favorablePreconditionType = "产品";

        if ("1".equals(keyInfo.getFavorablePreconditionType())) {
            favorablePreconditionType = "价格";
        }
        setWorkTitleStyle("优惠前提条件类型", favorablePreconditionType, true);

        // 产品
        if ("0".equals(keyInfo.getFavorablePreconditionType())) {
            // 设置标题行
            String[] tableTitle = new String[] {"序号", "项目", "数量"};
            createTableTitle(tableTitle);

            // 重起一行
            HSSFRow row = sheet1.createRow(rowNum++);

            // 设置序号
            setTitleValue(row, totalColumnNum, "序号1", numCellStyle);

            pc.setBizDataValueByKey("comboType", "1");
            pc.setBizDataValueByKey("activityId", keyInfo.getActivityId());
            pc.setBizDataValueByKey("keyId", keyInfo.getGuid());
            final IService service = SpringConfigHelper.getServiceBeanByName(QueryComboKeyInfoService.class.getName());
            final PageResult result = service.execute(pc);
            final DataListResult dataListResult2 = result.getData(DataListResult.class);
            final ComboKeyInfo comboKey = (ComboKeyInfo) dataListResult2.getExtra("comboKeyInfo");
            final List <ComboContentInfo4Page> contents = comboKey.getComboContent4PageList();
            if (!CollectionUtils.isEmpty(contents)) {
                // 设置序号
                setTitleValue(row, ++totalColumnNum, contents.get(0).getProductNames().replaceAll("<br/>", System.getProperty("line.separator")),
                        valueCellStyle);

                // 设置序号
                setTitleValue(row, ++totalColumnNum, String.valueOf(contents.get(0).getAmount()), valueCellStyle);
            }
        } else if ("1".equals(keyInfo.getFavorablePreconditionType())) {
            // 设置标题行
            String[] tableTitle = new String[] {"优惠方式", "金额"};
            createTableTitle(tableTitle);

            // 重起一行
            HSSFRow row = sheet1.createRow(rowNum++);

            // 设置序号
            setTitleValue(row, totalColumnNum, "满", valueCellStyle);

            // 设置序号
            setTitleValue(row, ++totalColumnNum, keyInfo.getFavorableEnoughPrice() + "元", valueCellStyle);
        }

        // 重置
        totalColumnNum = 0;

        // 优惠方式
        createFavorableMethod(keyInfo);
    }


    /**
     * 封装优惠内容
     * 
     * @param resultComboKey
     */
    private void createFreeContent(final ComboKeyInfo resultComboKey) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle("优惠内容", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "项目", "系数", "份数"};
        createTableTitle(tableTitle);

        List <ComboContentInfo4Page> comboContent4PageList = resultComboKey.getComboContent4PageList();

        if (!CollectionUtils.isEmpty(comboContent4PageList)) {
            final int len = comboContent4PageList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ComboContentInfo4Page comboContentInfo = comboContent4PageList.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getProductNames().replaceAll("<br/>", System.getProperty("line.separator")),
                        valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, String.valueOf(comboContentInfo.getFactor()), valueCellStyle);

                // 设置份数
                setTitleValue(row, ++totalColumnNum, String.valueOf(comboContentInfo.getAmount()), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle("确认优惠内容键位名称", false, true, true);

        // 设置标题行
        String[] tableTitle1 = new String[] {"序号", "优惠内容名称", "优惠内容新名称"};
        createTableTitle(tableTitle1);

        List <ComboContentInfo> comboContentList = resultComboKey.getComboContentList();

        if (!CollectionUtils.isEmpty(comboContentList)) {
            final int len = comboContentList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ComboContentInfo comboContentInfo = comboContentList.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewName(), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }

        // 设置生效日
        createComboAdjustContent(resultComboKey.getKeyInfo());
    }


    /**
     * 优惠方式
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createFavorableMethod(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle("优惠方式", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"优惠方式", "优惠价格", "获得方式", "优惠次数"};
        createTableTitle(tableTitle);

        // 重起一行
        HSSFRow row = sheet1.createRow(rowNum++);

        if ("0".equals(keyInfo.getFavorableMethod())) {
            // 设置序号
            setTitleValue(row, totalColumnNum, "加", valueCellStyle);

            // 设置序号
            setTitleValue(row, ++totalColumnNum, keyInfo.getFavorablePrice() + "元", valueCellStyle);
        } else if ("1".equals(keyInfo.getFavorableMethod())) {
            // 设置序号
            setTitleValue(row, totalColumnNum, "满", valueCellStyle);

            // 设置序号
            setTitleValue(row, ++totalColumnNum, keyInfo.getFavorablePrice() + "元", valueCellStyle);
        } else if ("2".equals(keyInfo.getFavorableMethod())) {
            // 设置序号
            setTitleValue(row, totalColumnNum, "打", valueCellStyle);

            // 设置序号
            setTitleValue(row, ++totalColumnNum, keyInfo.getFavorablePrice() + "折", valueCellStyle);
        }

        // 设置序号
        setTitleValue(row, ++totalColumnNum, "得", valueCellStyle);

        if ("0".equals(keyInfo.getFavorableType())) {
            // 设置序号
            setTitleValue(row, ++totalColumnNum, "最少" + keyInfo.getFavorableTimes() + "次  最多" + keyInfo.getMaxFavorableTimes() + "次", valueCellStyle);
        } else if ("1".equals(keyInfo.getFavorableType())) {
            // 设置序号
            setTitleValue(row, ++totalColumnNum, "不限次数", valueCellStyle);
        }

        // 重置
        totalColumnNum = 0;
    }


    /**
     * 设置MealDeal优先级
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createMealDealPriority(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置成本与价格
        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置是否多种规格 isAllowVariousNorms
        createKeyContentInfos(keyContentInfos, "MEALDEAL优先级", keyInfo.getMealdealPriority());

        // 设置Non-Food
        createKeyContentInfos(keyContentInfos, "是否允许打散重组", converDes(keyInfo.getIsAllowScatteredRestructuring()));

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 封装套餐类型
     * 
     * @param keyInfo
     */
    private void createSellType(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle("Sell Type", false, true, true);

        // 设置成本与价格
        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置是否多种规格 isAllowVariousNorms
        createKeyContentInfos(keyContentInfos, "售卖方式总类", keyInfo.getSellTypeName());

        // 设置Non-Food
        createKeyContentInfos(keyContentInfos, "套餐类别", keyInfo.getComboTypeName());

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 封装组合内容价格
     * 
     * @param keyType
     *            设键类型
     * @param isNonFood
     *            是否为非食品
     * @param resultComboKey
     *            组合内容列表
     */
    private void createComboPkgPrice(final KeyInfo keyInfo, final ComboKeyInfo resultComboKey) {

        final String keyType = keyInfo.getKeyType();
        final String keyClassify = keyInfo.getKeyClassify();
        final String isNonFood = keyInfo.getHasNonFood();

        if (ACPConstants.KeyType.COMBOHEAD_WITH_NO_CONSTRAINT.equals(keyType)) {

            // 设置价格
            setPrice(isNonFood, keyClassify, resultComboKey, keyInfo);
        }

        if (ACPConstants.KeyType.SINGLE_BUTTON.equals(keyType) || ACPConstants.KeyType.NO_COMBOHEAD.equals(keyType)) {
            // 获取组合内容
            createPrice(resultComboKey.getComboList(), keyInfo, true);
        }

        if (ACPConstants.KeyType.COMBOHEAD_WITH_CONSTRAINT.equals(keyType)) {
            // 封装组合内容价格
            createComboContentPrice(resultComboKey.getComboContentList(), keyInfo);
        }
    }


    /**
     * 设置产品价格
     * 
     * @param isNonFood
     * @param resultComboKey
     */
    private void setPrice(final String isNonFood, final String keyClassify, final ComboKeyInfo resultComboKey, final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置MealDeal价格类型
        if (ACPConstants.KeyClassify.MEALDEAL.equals(keyClassify)) {
            setWorkTitleStyle("价格方式", resultComboKey.getPriceInfoList().get(0).getPriceMethodName());
        }

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "名称", "价格", ""};

        if (ACPConstants.IsNonFood.YES.equals(isNonFood)) {
            tableTitle[ACPConstants.THREE] = "非食品价格";
        }

        createTableTitle(tableTitle);

        // 价格
        List <PriceInfo> priceInfoList = resultComboKey.getPriceInfoList();

        if (!CollectionUtils.isEmpty(priceInfoList)) {
            final int len = priceInfoList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                PriceInfo priceInfo = priceInfoList.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, priceInfo.getPriceTypeName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, priceInfo.getPrice(), valueCellStyle);

                if (ACPConstants.IsNonFood.YES.equals(isNonFood)) {
                    setTitleValue(row, ++totalColumnNum, priceInfo.getNonFoodPrice(), valueCellStyle);
                }

                // 重置
                totalColumnNum = 0;
            }
        }

        // 设置生效日
        createComboAdjustPrice(keyInfo);
    }


    /**
     * 维护套餐内容
     * 
     * @param keyType
     *            设键类型
     * @param resultComboKey
     *            组合内容
     */
    private void createComboPkgContent(final String keyType, final ComboKeyInfo resultComboKey) {

        if (ACPConstants.KeyType.SINGLE_BUTTON.equals(keyType) || ACPConstants.KeyType.NO_COMBOHEAD.equals(keyType)) {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 设置组合内容
            setAlignmentStyle("套餐名称 ", false, true, true);

            // 设置标题行
            String[] tableTitle = new String[] {"序号", "套餐名称", "新名称", "", "备注", ""};
            if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                tableTitle[ACPConstants.THREE] = "新简称";
            }

            if (isAdjust) {
                tableTitle[ACPConstants.FIVE] = "生效日";
            }

            createTableTitle(tableTitle);

            // 获取组合内容
            List <ComboInfo> comboList = resultComboKey.getComboList();

            if (!CollectionUtils.isEmpty(comboList)) {
                final int len = comboList.size();

                for (int i = 0; i < len; i++) {
                    // 重起一行
                    HSSFRow row = sheet1.createRow(rowNum++);

                    ComboInfo comboContentInfo = comboList.get(i);

                    // 设置序号
                    setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                    // 配料Condiment类别 condimentClassName
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboName(), valueCellStyle);

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboNewName(), valueCellStyle);

                    if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboShortName(), valueCellStyle);
                    }

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getRemark(), valueCellStyle);

                    if (isAdjust) {
                        if (resultComboKey.getKeyInfo().getAdjustComboContentEffectiveBeginDate() != null) {
                            // 设置生效日
                            setTitleValue(row, ++totalColumnNum,
                                    formatFavDate(resultComboKey.getKeyInfo().getAdjustComboContentEffectiveBeginDate()), valueCellStyle);
                        } else {
                            // 设置生效日
                            setTitleValue(row, ++totalColumnNum, "", valueCellStyle);
                        }

                    }

                    // 重置
                    totalColumnNum = 0;
                }
            }
        }

        if (ACPConstants.KeyType.COMBOHEAD_WITH_CONSTRAINT.equals(keyType) || ACPConstants.KeyType.COMBOHEAD_WITH_NO_CONSTRAINT.equals(keyType)
                || ACPConstants.KeyType.NO_COMBOHEAD.equals(keyType)) {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 设置组合内容
            setAlignmentStyle("套餐中组合内容名称", false, true, true);

            // 设置标题行
            String[] tableTitle = new String[] {"序号", "套餐中组合内容名称", "新名称", "", ""};

            if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                tableTitle[ACPConstants.THREE] = "新简称";
            }

            if (ACPConstants.KeyType.COMBOHEAD_WITH_NO_CONSTRAINT.equals(keyType) || ACPConstants.KeyType.NO_COMBOHEAD.equals(keyType)) {
                tableTitle[ACPConstants.FOUR] = "备注";
            }

            createTableTitle(tableTitle);

            // 套餐中组合内容
            List <ComboContentInfo> comboContentList = resultComboKey.getComboContentList();

            if (!CollectionUtils.isEmpty(comboContentList)) {
                final int len = comboContentList.size();

                for (int i = 0; i < len; i++) {
                    // 重起一行
                    HSSFRow row = sheet1.createRow(rowNum++);

                    ComboContentInfo comboContentInfo = comboContentList.get(i);

                    // 设置序号
                    setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                    // 配料Condiment类别 condimentClassName
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentName(), valueCellStyle);

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewName(), valueCellStyle);

                    if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewShortName(), valueCellStyle);
                    }

                    if (ACPConstants.KeyType.COMBOHEAD_WITH_NO_CONSTRAINT.equals(keyType) || ACPConstants.KeyType.NO_COMBOHEAD.equals(keyType)) {
                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, comboContentInfo.getRemark(), valueCellStyle);
                    }

                    // 重置
                    totalColumnNum = 0;
                }
            }
        }
    }


    /**
     * 维护套餐内容
     * 
     * @param keyType
     *            设键类型
     * @param resultComboKey
     *            组合内容
     */
    private void createCombintionContent(final String keyType, final ComboKeyInfo resultComboKey) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        if (ACPConstants.KeyType.MANY_PRODUCT_ONE_KEY.equals(keyType)) {
            // 设置组合内容
            setAlignmentStyle("组合内容名称 ", false, true, true);

            // 设置标题行
            String[] tableTitle = new String[] {"序号", "组合内容名称", "新名称", "", "备注"};
            if (isAdjust && ACPConstants.brandCode.KFC.equals(brandCode)) {
                tableTitle = new String[] {"序号", "组合内容名称", "新名称", "", "备注", "生效日"};
            }

            if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                tableTitle[ACPConstants.THREE] = "新简称";
            }

            createTableTitle(tableTitle);

            // 获取组合内容
            List <ComboInfo> comboList = resultComboKey.getComboList();

            if (!CollectionUtils.isEmpty(comboList)) {
                final int len = comboList.size();

                for (int i = 0; i < len; i++) {
                    // 重起一行
                    HSSFRow row = sheet1.createRow(rowNum++);

                    ComboInfo comboContentInfo = comboList.get(i);

                    // 设置序号
                    setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                    // 配料Condiment类别 condimentClassName
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboName(), valueCellStyle);

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboNewName(), valueCellStyle);

                    if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboShortName(), valueCellStyle);
                    }

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getRemark(), valueCellStyle);
                    // 设置生效日
                    if (isAdjust && ACPConstants.brandCode.KFC.equals(brandCode)) {
                        if (null != comboContentInfo.getAdjustNameEffectiveBeginDate()) {
                            setTitleValue(row, ++totalColumnNum, dateFormat.format(comboContentInfo.getAdjustNameEffectiveBeginDate()),
                                    valueCellStyle);
                        } else {
                            setTitleValue(row, ++totalColumnNum, "", valueCellStyle);
                        }
                    }

                    // 重置
                    totalColumnNum = 0;
                }
            }
        } else if (ACPConstants.KeyType.MANY_PRODUCT_MANY_KEY.equals(keyType) || ACPConstants.KeyType.ONE_PRODUCT_ONE_KEY.equals(keyType)) {
            // 设置组合内容
            setAlignmentStyle("组合内容名称 ", false, true, true);

            // 设置标题行
            String[] tableTitle = new String[] {"序号", "组合内容名称", "新名称", "", "备注"};

            if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                tableTitle[ACPConstants.THREE] = "新简称";
            }

            createTableTitle(tableTitle);

            // 获取组合内容
            List <ComboContentInfo> comboContentList = resultComboKey.getComboContentList();

            if (!CollectionUtils.isEmpty(comboContentList)) {
                final int len = comboContentList.size();

                for (int i = 0; i < len; i++) {
                    // 重起一行
                    HSSFRow row = sheet1.createRow(rowNum++);

                    ComboContentInfo comboContentInfo = comboContentList.get(i);

                    // 设置序号
                    setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                    // 配料Condiment类别 condimentClassName
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentName(), valueCellStyle);

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewName(), valueCellStyle);

                    if (!ACPConstants.brandCode.PHDI.equals(brandCode) && !ACPConstants.brandCode.PHHS.equals(brandCode)) {
                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, comboContentInfo.getComboContentNewShortName(), valueCellStyle);
                    }

                    // 设置数量
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getRemark(), valueCellStyle);

                    // 重置
                    totalColumnNum = 0;
                }
            }
        }
    }


    /**
     * 组合内容
     * 
     * @param keyType
     *            设键类型
     * @param comboContent4PageList
     *            组合内容
     */
    private void createComboContentInfo(final KeyInfo keyInfo, final List <ComboContentInfo4Page> comboContent4PageList) {

        final String keyType = keyInfo.getKeyType();
        final String keyClassify = keyInfo.getKeyClassify();

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle("组合内容", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "项目", "", "系数", "份数", ""};

        if (ACPConstants.KeyClassify.MEALDEAL.equals(keyClassify)) {
            tableTitle[ACPConstants.TWO] = "优先级";
        }

        if (!ACPConstants.KeyType.COMBOHEAD_WITH_CONSTRAINT.equals(keyType)) {
            if (!ACPConstants.KeyClassify.FREE_EXCHANGE.equals(keyClassify) && !ACPConstants.KeyClassify.CONSUME_CARD.equals(keyClassify)
                    && !ACPConstants.KeyClassify.TICKET_RECLAIM.equals(keyClassify) && !ACPConstants.KeyClassify.DISCOUNT.equals(keyClassify)
                    && !ACPConstants.KeyClassify.YUM_CARDBILL_CLASSIFY.equals(keyClassify)) {
                tableTitle[ACPConstants.FIVE] = "优惠";
            }
        }

        createTableTitle(tableTitle);

        if (!CollectionUtils.isEmpty(comboContent4PageList)) {
            final int len = comboContent4PageList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ComboContentInfo4Page comboContentInfo = comboContent4PageList.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getProductNames().replaceAll("<br/>", System.getProperty("line.separator")),
                        valueCellStyle);

                // 封装优先级
                if (ACPConstants.KeyClassify.MEALDEAL.equals(keyClassify)) {
                    // 设置优先级
                    setTitleValue(row, ++totalColumnNum, comboContentInfo.getPriorityName(), valueCellStyle);
                }

                // 设置数量
                setTitleValue(row, ++totalColumnNum, String.valueOf(comboContentInfo.getFactor()), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, String.valueOf(comboContentInfo.getAmount()), valueCellStyle);

                if (!ACPConstants.KeyType.COMBOHEAD_WITH_CONSTRAINT.equals(keyType)) {
                    if (!ACPConstants.KeyClassify.FREE_EXCHANGE.equals(keyClassify) && !ACPConstants.KeyClassify.CONSUME_CARD.equals(keyClassify)
                            && !ACPConstants.KeyClassify.TICKET_RECLAIM.equals(keyClassify) && !ACPConstants.KeyClassify.DISCOUNT.equals(keyClassify)) {
                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, comboContentInfo.getFavorableNames(), valueCellStyle);
                    }
                }

                // 重置
                totalColumnNum = 0;
            }
        }

        // 生效日
        createComboAdjustContent(keyInfo);
    }


    /**
     * 组合内容
     * 
     * @param keyType
     *            设键类型
     * @param comboContent4PageList
     *            组合内容
     */
    private void createComboContentInfoForMealDeal(final KeyInfo keyInfo, final List <ComboContentInfo4Page> comboContent4PageList) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置组合内容
        setAlignmentStyle("组合内容", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "项目", "优先级", "系数", "份数", "优惠"};
        createTableTitle(tableTitle);

        if (!CollectionUtils.isEmpty(comboContent4PageList)) {
            final int len = comboContent4PageList.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ComboContentInfo4Page comboContentInfo = comboContent4PageList.get(i);

                // 获取组合内容占几行
                final int rowSpan = comboContentInfo.getComboSeqRowSpan();

                if (ACPConstants.YES.equals(comboContentInfo.getShowFlag())) {
                    // 设置序号
                    setTitleValue(row, totalColumnNum, "序号" + comboContentInfo.getComboSeqId(), numCellStyle, rowSpan);
                }

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getProductNames().replaceAll("<br/>", System.getProperty("line.separator")),
                        valueCellStyle);

                // 设置优先级
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getPriorityName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, String.valueOf(comboContentInfo.getFactor()), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, String.valueOf(comboContentInfo.getAmount()), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, comboContentInfo.getFavorableNames(), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }

        // 生效日
        createComboAdjustContent(keyInfo);
    }


    /**
     * 设置组合内容生效日
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createComboAdjustContent(final KeyInfo keyInfo) {

        // final String keyType = keyInfo.getKeyType();

        // 设置生效日
        if (isAdjust && keyInfo.getAdjustComboContentEffectiveBeginDate() != null) {
            // if (!ACPConstants.KeyType.SINGLE_BUTTON.equals(keyType) &&
            // !ACPConstants.KeyType.NO_COMBOHEAD.equals(keyType)) {
            setWorkTitleStyle("生效日", formatFavDate(keyInfo.getAdjustComboContentEffectiveBeginDate()));
            // }
        }
    }


    /**
     * 封装调整产品的生效日
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createComboAdjustPrice(final KeyInfo keyInfo) {

        // 设置生效日(调整)
        if (isAdjust && keyInfo.getAdjustPriceEffectiveBeginDate() != null) {
            // 不校验什么设键类型,有值就显示.业务已控制
            // if ("0".equals(keyInfo.getIsAllowVariousNorms())) {
            setWorkTitleStyle("生效日", formatFavDate(keyInfo.getAdjustPriceEffectiveBeginDate()));
            // }
        }
    }


    /**
     * 是否饮料
     * 
     * @param pc
     *            消息上下文
     * @param keyInfo
     *            设键信息
     * @throws Exception
     *             异常信息
     */
    @SuppressWarnings("unchecked")
    private void createStandardDrink(PageContext pc, final KeyInfo keyInfo) throws Exception {

        // 单个设键套餐时需增加饮料
        if (ACPConstants.KeyType.SINGLE_BUTTON.equals(keyInfo.getKeyType())) {
            final QuerySystemParameterInfoService seviceParameter = (QuerySystemParameterInfoService) SpringConfigHelper
                    .getServiceBeanByName(QuerySystemParameterInfoService.class.getName());
            final PageResult resultParameter = seviceParameter.doServiceWithoutHandler(pc);
            final List <SystemParameterInfo> systemParameterInfo = resultParameter.getData(List.class);

            final String parameterValue = systemParameterInfo.get(ACPConstants.FIVE).getParameterValue();

            if ("Y".equals(parameterValue)) {
                // 设置间隔行
                setAlignmentStyle("", false, false, true);

                // 设置成本与价格
                List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

                String isExchange = "否";
                if ("Y".equals(keyInfo.getDrinkIsExchange())) {
                    isExchange = "是";
                }

                // 设置是否多种规格 isAllowVariousNorms
                createKeyContentInfos(keyContentInfos, "饮料是否可换", isExchange);

                if ("Y".equals(keyInfo.getDrinkIsExchange())) {
                    // 设置Non-Food
                    createKeyContentInfos(keyContentInfos, "标配饮料", keyInfo.getStandardConfigurationDrinkName());
                }

                setWorkTitleStyle(keyContentInfos);
            }
        }
    }


    /**
     * 封装是否PreOrder
     * 
     * @param activityType
     *            活动类型
     * @param isPreOrder
     *            是否preOrder
     * @param preOrderViewStr
     *            preOrder字段
     */
    private void createIsPreOrder(final String activityType, final String isPreOrder, final String preOrderViewStr) {

        // 是否适用Pre-Order
        if (ACPConstants.ActivityType.DINNER.equals(activityType) && ACPConstants.brandCode.KFC.equals(brandCode)) {
            // 设置间隔行
            setAlignmentStyle("", false, false, true);

            // 设置是否Pre-Order
            setWorkTitleStyle("是否适用Pre-Order", converDes(isPreOrder, "0"));

            // 是否显示preOrder
            if ("1".equals(isPreOrder)) {
                // 设置pre-order类别
                setWorkTitleStyle("Pre-Order类别", preOrderViewStr, true);
            }
        }
    }


    /**
     * 产品
     * 
     * @param usingInfo
     *            产品层级是否使用
     * @param productKey
     *            产品信息
     */
    private void createProduct(final ProductHierarchyUsingInfo usingInfo, final ProductKeyInfo productKey, final String activityType) {

        // 获取产品信息
        List <ProductInfo> productInfos = productKey.getProductList();

        final KeyInfo keyInfo = productKey.getKeyInfo();

        if (!CollectionUtils.isEmpty(productInfos)) {

            // 设置产品规格名称
            if ("1".equals(productKey.getKeyInfo().getIsAllowVariousNorms())) {
                createProductHierarchyName(productKey);
            }

            for (ProductInfo productInfo : productInfos) {

                // 设置产品层级
                createProductHierarchy(usingInfo, productInfo);

                // 设置产品配料
                createProductDosing(productInfo);

                // 设置产品价格
                createPrice(productInfo);

                // 封装生效日
                createProductAdjustPrice(productInfo, keyInfo);

                // 设置成本
                createProductCost(productInfo);

                // 封装关联堂食
                createProductRelated(productInfo, activityType);
            }
        }
    }


    /**
     * 封装产品的价格调整生效日
     * 
     * @param productInfo
     *            产品信息
     * @param keyInfo
     *            设键信息
     */
    private void createProductAdjustPrice(final ProductInfo productInfo, final KeyInfo keyInfo) {

        if (isAdjust && productInfo.getAdjustPriceEffectiveBeginDate() != null) {
            // if ("0".equals(keyInfo.getIsAllowVariousNorms())) {
            setWorkTitleStyle("生效日", formatFavDate(productInfo.getAdjustPriceEffectiveBeginDate()));
            // }
        }
    }


    /**
     * 
     * 封装成本
     * 
     * @param productInfo
     *            产品信息
     */
    private void createProductCost(final ProductInfo productInfo) {

        if (!ACPConstants.brandCode.ED.equals(brandCode)) {
            setWorkTitleStyle("成本", productInfo.getCost());
        }

    }


    /**
     * 封装关联堂食
     * 
     * @param productInfo
     *            产品信息
     * @param activityType
     *            活动类型
     */
    private void createProductRelated(final ProductInfo productInfo, final String activityType) {

        if (ACPConstants.ActivityType.OUTSIDE.equals(activityType)) {
            setWorkTitleStyle("关联堂食产品", productInfo.getRelatedProductName());
        }
    }


    /**
     * 设置产品规格名称
     * 
     * @param usingInfo
     *            产品层级是否启用
     * @param productInfo
     *            产品信息
     */
    private void createProductHierarchyName(final ProductKeyInfo productKey) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置层级名称
        setAlignmentStyle("产品规格名称", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "规格", "子规格", "新中文名称", "简称"};
        createTableTitle(tableTitle);

        List <ProductInfo> productInfos = productKey.getProductList();

        if (!CollectionUtils.isEmpty(productInfos)) {
            final int len = productInfos.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ProductInfo productInfo = productInfos.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, productInfo.getNormsName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, productInfo.getSubNormsName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, productInfo.getProductName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, productInfo.getShortName(), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 设置产品层级
     * 
     * @param usingInfo
     *            产品层级是否启用
     * @param productInfo
     *            产品信息
     */
    private void createProductHierarchy(final ProductHierarchyUsingInfo usingInfo, final ProductInfo productInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置层级名称
        setAlignmentStyle(productInfo.getProductName() + " 层级", false, true, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置层级一
        if (ACPConstants.YES.equals(usingInfo.getIsHierarchyOneUsing())) {
            createKeyContentInfos(keyContentInfos, "产品层级一（门）", productInfo.getHierarchyOneName());
        }

        // 设置层级二
        if (ACPConstants.YES.equals(usingInfo.getIsHierarchyTwoUsing())) {
            createKeyContentInfos(keyContentInfos, "产品层级二（纲）", productInfo.getHierarchyTwoName());
        }

        // 设置层级三
        if (ACPConstants.YES.equals(usingInfo.getIsHierarchyThreeUsing())) {
            createKeyContentInfos(keyContentInfos, "产品层级三（目）", productInfo.getHierarchyThreeName());
        }

        // 设置层级四
        if (ACPConstants.YES.equals(usingInfo.getIsHierarchyFourUsing())) {
            createKeyContentInfos(keyContentInfos, "产品层级四（属）", productInfo.getHierarchyFourName());
        }

        // 设置层级五
        if (ACPConstants.YES.equals(usingInfo.getIsHierarchyFiveUsing())) {
            createKeyContentInfos(keyContentInfos, "产品层级五（其他）", productInfo.getHierarchyFiveName());
        }

        // 设置层级
        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 产品配料
     * 
     * @param productInfo
     *            产品
     */
    private void createProductDosing(final ProductInfo productInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置配料名称
        setAlignmentStyle(productInfo.getProductName() + " 配料", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "配料Condiment类别", "数量", "配料详细信息"};
        createTableTitle(tableTitle);

        List <ProductDosingInfo> productDosingInfos = productInfo.getProductDosingList();

        if (!CollectionUtils.isEmpty(productDosingInfos)) {
            final int len = productDosingInfos.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ProductDosingInfo dosingInfo = productDosingInfos.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 配料Condiment类别 condimentClassName
                setTitleValue(row, ++totalColumnNum, dosingInfo.getCondimentClassName(), valueCellStyle);

                // 设置数量
                setTitleValue(row, ++totalColumnNum, dosingInfo.getDosingAmount(), valueCellStyle);

                // 设置配料详细信息
                List <KeyInfo> keyInfos = dosingInfo.getProductDosingDetailInfoList();
                setTitleValue(row, ++totalColumnNum, getValueString(keyInfos), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 转换页面上显示的内容
     * 
     * @param keyInfos
     *            设键信息
     * @return 页面上显示的内容
     */
    private String getValueString(List <KeyInfo> keyInfos) {

        if (!CollectionUtils.isEmpty(keyInfos)) {
            final String strSeparator = System.getProperty("line.separator");
            StringBuffer infStr = new StringBuffer();

            for (KeyInfo keyInfo : keyInfos) {
                infStr.append(keyInfo.getCnName());
                infStr.append(strSeparator);
            }

            String keyStr = infStr.toString();
            keyStr = keyStr.substring(0, keyStr.lastIndexOf(strSeparator));
            return keyStr;
        }

        return null;
    }


    /**
     * 设置价格
     * 
     * @param productInfo
     *            产品信息
     */
    private void createPrice(final ProductInfo productInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置售卖时间点
        setAlignmentStyle(productInfo.getProductName() + " 价格", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "名称", "价格"};
        createTableTitle(tableTitle);

        List <PriceInfo> keyPriceInfos = productInfo.getKeyPriceList();

        if (!CollectionUtils.isEmpty(keyPriceInfos)) {
            final int len = keyPriceInfos.size();

            for (int i = 0; i < len; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);
                PriceInfo priceInfo = keyPriceInfos.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 设置开始日期
                setTitleValue(row, ++totalColumnNum, priceInfo.getPriceTypeName(), valueCellStyle);

                // 设置结束日期
                setTitleValue(row, ++totalColumnNum, priceInfo.getPrice(), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 设置价格
     * 
     * @param productInfo
     *            产品信息
     */
    private void createPrice(List <ComboInfo> comboList, final KeyInfo keyInfo, final boolean isShowTitle) {

        if (!CollectionUtils.isEmpty(comboList)) {
            for (ComboInfo comboInfo : comboList) {
                // 设置间隔行
                setAlignmentStyle("", false, false, true);

                // 是否显示价格标题
                if (isShowTitle) {
                    // 设置售卖时间点
                    setAlignmentStyle(comboInfo.getComboNewName() + " 价格", false, true, true);
                }

                // 设置标题行
                String[] tableTitle = new String[] {"序号", "名称", "价格", ""};

                if (ACPConstants.IsNonFood.YES.equals(comboInfo.getHasNonFood())) {
                    tableTitle[ACPConstants.THREE] = "非食品价格";
                }
                createTableTitle(tableTitle);

                // 价格
                List <PriceInfo> priceInfoList = comboInfo.getPriceList();

                if (!CollectionUtils.isEmpty(priceInfoList)) {
                    final int len = priceInfoList.size();

                    for (int i = 0; i < len; i++) {
                        // 重起一行
                        HSSFRow row = sheet1.createRow(rowNum++);

                        PriceInfo priceInfo = priceInfoList.get(i);

                        // 设置序号
                        setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                        // 配料Condiment类别 condimentClassName
                        setTitleValue(row, ++totalColumnNum, priceInfo.getPriceTypeName(), valueCellStyle);

                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, priceInfo.getPrice(), valueCellStyle);

                        if (ACPConstants.IsNonFood.YES.equals(comboInfo.getHasNonFood())) {
                            setTitleValue(row, ++totalColumnNum, priceInfo.getNonFoodPrice(), valueCellStyle);
                        }

                        // 重置
                        totalColumnNum = 0;
                    }
                }

                // 生效日
                createComboAdjustPrice(keyInfo);
            }
        }
    }


    /**
     * 设置价格
     * 
     * @param productInfo
     *            产品信息
     */
    private void createComboContentPrice(List <ComboContentInfo> comboContentList, final KeyInfo keyInfo) {

        if (!CollectionUtils.isEmpty(comboContentList)) {
            for (ComboContentInfo comboInfo : comboContentList) {
                // 设置间隔行
                setAlignmentStyle("", false, false, true);

                // 设置售卖时间点
                setAlignmentStyle(comboInfo.getComboContentNewName() + " 价格", false, true, true);

                // 设置标题行
                String[] tableTitle = new String[] {"序号", "名称", "价格"};
                createTableTitle(tableTitle);

                // 价格
                List <PriceInfo> priceInfoList = comboInfo.getPriceList();

                if (!CollectionUtils.isEmpty(priceInfoList)) {
                    final int len = priceInfoList.size();

                    for (int i = 0; i < len; i++) {
                        // 重起一行
                        HSSFRow row = sheet1.createRow(rowNum++);

                        PriceInfo priceInfo = priceInfoList.get(i);

                        // 设置序号
                        setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                        // 配料Condiment类别 condimentClassName
                        setTitleValue(row, ++totalColumnNum, priceInfo.getPriceTypeName(), valueCellStyle);

                        // 设置数量
                        setTitleValue(row, ++totalColumnNum, priceInfo.getPrice(), valueCellStyle);

                        // 重置
                        totalColumnNum = 0;
                    }
                }

                // 生效日
                createComboAdjustPrice(keyInfo);
            }
        }
    }


    /**
     * 备注
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createRemark(final String remark) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置备注
        setWorkTitleStyle("备注", remark, true);
    }


    /**
     * 设置特殊售卖时间点
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createSpecialSellDate(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置售卖时间点
        setAlignmentStyle("特殊售卖日期", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "开始时间", "结束时间"};
        createTableTitle(tableTitle);

        // 设置表格内容
        List <SpecialSellDateInfo> timeInfos = keyInfo.getSpecialSellDateInfoList();

        if (!CollectionUtils.isEmpty(timeInfos)) {
            final int timeLen = timeInfos.size();
            for (int i = 0; i < timeLen; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                SpecialSellDateInfo timeInfo = timeInfos.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 设置开始日期
                setTitleValue(row, ++totalColumnNum, formatDate(timeInfo.getSpecialBeginDate()), valueCellStyle);

                // 设置结束日期
                setTitleValue(row, ++totalColumnNum, formatDate(timeInfo.getSpecialEndDate()), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 设置特殊售卖时间点
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createShieldSellDate(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置售卖时间点
        setAlignmentStyle("屏蔽原价产品日期", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "原价产品屏蔽开始日期", "原价产品屏蔽结束日期"};
        createTableTitle(tableTitle);

        // 设置表格内容
        List <ProductShiledDate> timeInfos = keyInfo.getProductShiledDateList();

        if (!CollectionUtils.isEmpty(timeInfos)) {
            final int timeLen = timeInfos.size();
            for (int i = 0; i < timeLen; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ProductShiledDate timeInfo = timeInfos.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 设置开始日期
                setTitleValue(row, ++totalColumnNum, formatDate(timeInfo.getShieldBeginDate()), valueCellStyle);

                // 设置结束日期
                setTitleValue(row, ++totalColumnNum, formatDate(timeInfo.getShieldEndDate()), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 设置售卖时间点
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createSellDate(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置售卖时间点
        setAlignmentStyle("售卖时间点", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "开始时间", "结束时间", "地区"};
        createTableTitle(tableTitle);

        // 设置表格内容
        List <SellPointInTimeInfo> timeInfos = keyInfo.getSellPointInTimeInfoList();

        if (!CollectionUtils.isEmpty(timeInfos)) {
            final int timeLen = timeInfos.size();
            for (int i = 0; i < timeLen; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                SellPointInTimeInfo timeInfo = timeInfos.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 设置开始日期
                setTitleValue(row, ++totalColumnNum, formatSellDate(timeInfo.getBeginTime()), valueCellStyle);

                // 设置结束日期
                setTitleValue(row, ++totalColumnNum, timeInfo.getEndTimeString(), valueCellStyle);

                // 设置地区
                setTitleValue(row, ++totalColumnNum, timeInfo.getArea(), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    /**
     * 设置售卖时间点
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createShieldSellTime(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置售卖时间点
        setAlignmentStyle("屏蔽原价产品时间", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "原价产品屏蔽开始时间", "原价产品屏蔽结束时间"};
        createTableTitle(tableTitle);

        // 设置表格内容
        List <ProductShiledTime> timeInfos = keyInfo.getProductShiledTimeList();

        if (!CollectionUtils.isEmpty(timeInfos)) {
            final int timeLen = timeInfos.size();
            for (int i = 0; i < timeLen; i++) {
                // 重起一行
                HSSFRow row = sheet1.createRow(rowNum++);

                ProductShiledTime timeInfo = timeInfos.get(i);

                // 设置序号
                setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

                // 设置开始日期
                setTitleValue(row, ++totalColumnNum, timeInfo.getShieldBeginTime(), valueCellStyle);

                // 设置结束日期
                setTitleValue(row, ++totalColumnNum, timeInfo.getShieldEndTime(), valueCellStyle);

                // 重置
                totalColumnNum = 0;
            }
        }
    }


    private void createIsTraceMenumix(final String isTraceMenumix) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);
        setWorkTitleStyle("是否需要在DWBI的Menumix追踪", converDes(isTraceMenumix, "0"));
    }


    /**
     * 设置售卖时间
     * 
     * @param keyInfo
     */
    private void createSellTime(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置售卖时间
        final String weekSellTime = keyInfo.getWeeklySellTime() + keyInfo.getNationalHolidayIsSell();
        setWorkTitleStyle("售卖时间", converSellTime(weekSellTime), true);
    }


    /**
     * 设置售卖时间
     * 
     * @param keyInfo
     */
    private void createShieldSell(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置售卖时间
        final String weekSellTime = keyInfo.getShieldWeeklySellTime() + keyInfo.getShieldNationalHolidayIsSell();
        setWorkTitleStyle("屏蔽售卖时间", converSellTime(weekSellTime), true);
    }


    /**
     * 转换售卖时间点
     * 
     * @param weekSellTime
     *            售卖时间点
     * @return 售卖时间点
     */
    private String converSellTime(final String weekSellTime) {

        // 周一到周日，再加节假日
        final String[] dayOfWeek = new String[] {"周一", "周二", "周三", "周四", "周五", "周六", "周日", "国定假日"};
        char[] weekSell = weekSellTime.toCharArray();

        String sellTimeStr = "";
        StringBuffer sellTime = null;
        char isSell = '1';

        if (weekSellTime.indexOf('1') != -1) {
            sellTime = new StringBuffer();

            for (int i = 0; i < weekSell.length; i++) {
                if (isSell == weekSell[i]) {
                    sellTime.append(dayOfWeek[i]);
                    sellTime.append(',');
                }
            }

            sellTimeStr = sellTime.toString();
            sellTimeStr = sellTimeStr.substring(0, sellTimeStr.lastIndexOf(','));
        }

        return sellTimeStr;
    }


    /**
     * 设置推广时间和范围
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createPopularize(final KeyInfo keyInfo) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        // 设置推广时间和范围
        setAlignmentStyle("推广时间和范围", false, true, true);

        // 设置标题行
        String[] tableTitle = new String[] {"序号", "开始日期", "结束日期", "推广范围", "除", "备注"};
        createTableTitle(tableTitle);

        // 设置表格内容
        createPopularizeTableContent(keyInfo.getPopularizeList());
    }


    /**
     * 创建推广时间和范围表格内容
     * 
     * @param popularizeList
     */
    private void createPopularizeTableContent(List <Popularize> popularizeList) {

        final int popSize = popularizeList.size();
        for (int i = 0; i < popSize; i++) {
            // 重起一行
            HSSFRow row = sheet1.createRow(rowNum++);

            Popularize popularize = popularizeList.get(i);

            // 设置序号
            setTitleValue(row, totalColumnNum, "序号" + (i + 1), numCellStyle);

            // 设置开始日期
            setTitleValue(row, ++totalColumnNum, formatDate(popularize.getBeginDate()), valueCellStyle);

            // 设置结束日期
            setTitleValue(row, ++totalColumnNum, formatDate(popularize.getEndDate()), valueCellStyle);

            // 设置推广范围
            setTitleValue(row, ++totalColumnNum, popularize.getPopularizeAreaName(), valueCellStyle);

            // 设置例外范围
            setTitleValue(row, ++totalColumnNum, popularize.getExceptAreaName(), valueCellStyle);

            // 设置描述信息
            setTitleValue(row, ++totalColumnNum, popularize.getOtherDescription(), valueCellStyle);

            // 重置
            totalColumnNum = 0;
        }
    }


    /**
     * 设置单元格内容及样式
     * 
     * @param row
     *            行号
     * @param totalColumnNum
     *            列号
     * @param titleContent
     *            内容
     * @param cellStyle
     *            样式
     */
    private void setTitleValue(final HSSFRow row, final int totalColumnNum, final String titleContent, final HSSFCellStyle cellStyle,
            boolean isAlignment) {

        HSSFCell cell = row.createCell(totalColumnNum);
        cell.setCellValue(titleContent);
        cell.setCellStyle(cellStyle);

        int len = ACPConstants.TEN;
        try {
            if (isAlignment) {
                len = len * ACPConstants.FIVE;
            }
            double remainder = getValueLength(titleContent, len);
            short height = Short.parseShort(new DecimalFormat("0").format(remainder * ACPConstants.TWO_HUNDRED_FIFTY_SIX));
            if (height > row.getHeight()) {
                row.setHeight(height);
            }
        } catch (Exception e) {
            logger.error("计算行高出错", e.getMessage(), e, titleContent);
        }
    }


    /**
     * 设置单元格内容及样式
     * 
     * @param row
     *            行号
     * @param totalColumnNum
     *            列号
     * @param titleContent
     *            内容
     * @param cellStyle
     *            样式
     */
    private void setTitleValue(final HSSFRow row, final int totalColumnNum, final String titleContent, final HSSFCellStyle cellStyle) {

        // 默认按照最小的字符来计算行高
        setTitleValue(row, totalColumnNum, titleContent, cellStyle, false);
    }


    /**
     * 设置单元格内容及样式
     * 
     * @param row
     *            行号
     * @param totalColumnNum
     *            列号
     * @param titleContent
     *            内容
     * @param cellStyle
     *            样式
     */
    private void setTitleValue(final HSSFRow row, final int totalColumnNum, final String titleContent, final HSSFCellStyle cellStyle,
            final int rowSpan) {

        CellRangeAddress region1 = new CellRangeAddress(rowNum - 1, rowNum + rowSpan - ACPConstants.TWO, 0, 0);

        setRegionStyle(sheet1, region1, cellStyle);

        // 默认按照最小的字符来计算行高
        setTitleValue(row, totalColumnNum, titleContent, cellStyle, false);
    }


    /**
     * 创建表格标题行
     * 
     * @param tableTitle
     *            标题行
     */
    private void createTableTitle(String[] tableTitle) {

        // 重起一行
        HSSFRow row = sheet1.createRow(rowNum++);

        for (String title : tableTitle) {
            if (StringUtil.isNotEmpty(title)) {
                setTitleValue(row, totalColumnNum, title, keyCellStyle);
                totalColumnNum = totalColumnNum + 1;
            }
        }

        // 重置列号
        totalColumnNum = 0;
    }


    /**
     * 创建表格标题行(优惠配置)
     * 
     * @param tableTitle
     *            标题行
     */
    private void createTableTitleForFav(String[] tableTitle) {

        // 重起一行
        HSSFRow row = sheet1.createRow(rowNum++);

        int count = 0;
        for (int i = 0; i < tableTitle.length; i++) {

            // 每行显示6
            if (count == ACPConstants.SIX) {
                row = sheet1.createRow(rowNum++);
                totalColumnNum = 0;
                count = 0;
            }

            if (StringUtil.isNotEmpty(tableTitle[i])) {
                setTitleValue(row, totalColumnNum, tableTitle[i], valueCellStyle);
                totalColumnNum = totalColumnNum + 1;

                count++;
            }
        }

        // 重置列号
        totalColumnNum = 0;
    }


    /**
     * 设置规格
     * 
     * @param keyInfo
     *            设键信息
     * @param activityType
     *            活动类型
     * @param preOrderViewStr
     *            Pre-Order类别
     */
    private void createProductType(final KeyInfo keyInfo, final String activityType, final String preOrderViewStr) {

        // 设置间隔行
        setAlignmentStyle("", false, false, true);

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        final String strSeparator = System.getProperty("line.separator");

        // 设置是否多种规格 isAllowVariousNorms
        createKeyContentInfos(keyContentInfos, "是否多种规格", converDes(keyInfo.getIsAllowVariousNorms(), "0"));

        // 设置Non-Food
        createKeyContentInfos(keyContentInfos, "Non-Food", converDes(keyInfo.getIsNonFood(), "0"));

        if (ACPConstants.brandCode.ED.equals(brandCode)) {
            // 设置是否参加主餐随心配 isXpressWithMealdeal
            createKeyContentInfos(keyContentInfos, "是否参加" + strSeparator + "主餐随心配", converDes(keyInfo.getIsXpressWithMealdeal(), "0"));
        }

        setWorkTitleStyle(keyContentInfos);

        // 设置是否适用Pre-Order
        createIsPreOrder(activityType, keyInfo.getIsPreOrder(), preOrderViewStr);
    }


    /**
     * 封装方式与类别
     * 
     * @param propertyInfos
     *            产品属性信息
     */
    private void createProperty(List <ProductPropertyInfo4Page> propertyInfos) {

        setAlignmentStyle("", false, false, true);

        // 判断如果产品属性值为空，则不将该属性显示在页面
        final List <ProductPropertyInfo4Page> checkProperties = new ArrayList <ProductPropertyInfo4Page>();
        String propertyChooseValue = "";
        for (ProductPropertyInfo4Page property : propertyInfos) {
            final List <ProductPropertyContentInfo> contentList = property.getPropertyValueList();
            propertyChooseValue = property.getPropertyValue();
            if (null != contentList && !contentList.isEmpty()) {
                for (ProductPropertyContentInfo productPropertyContentInfo : contentList) {
                    if (null == propertyChooseValue) {
                        property.setPropertyChooseValue("");
                    } else if (propertyChooseValue.equals(productPropertyContentInfo.getGuid())) {
                        property.setPropertyChooseValue(productPropertyContentInfo.getPropertyValue());
                    }
                }
                checkProperties.add(property);
            }
        }

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        if (null != checkProperties && !checkProperties.isEmpty()) {
            final int rowCount = checkProperties.size();

            KeyContentInfo keyContentInfo = null;
            // 处理塞满的行数
            for (int i = 0; i < rowCount; i++) {
                keyContentInfo = new KeyContentInfo();
                keyContentInfo.setKey(checkProperties.get(i).getPropertyName());
                final String defaultValue = checkProperties.get(i).getPropertyChooseValue();// BF8350 修改下拉框默认值，不再调用initSelectValues方法
                keyContentInfo.setValue(defaultValue);

                keyContentInfos.add(keyContentInfo);
            }
        }

        setWorkTitleStyle(keyContentInfos);
    }


    /**
     * 设置每个产品属性的下拉框列表值
     * 
     * @param propertyInfo
     *            产品属性
     * @return defaultValue
     */
    private String initSelectValues(ProductPropertyInfo4Page propertyInfo) {

        final List <ProductPropertyContentInfo> contentList = propertyInfo.getPropertyValueList();
        String defaultValue = "";
        if (null != contentList && !contentList.isEmpty()) {
            final List <Code> codeList = new ArrayList <Code>();
            for (int i = 0; i < contentList.size(); i++) {
                final Code code = new Code();
                code.setCode(contentList.get(i).getGuid());
                code.setCodeName(contentList.get(i).getPropertyValue());
                if (i == 0) {
                    code.setDefaultCheck(true);
                    defaultValue = contentList.get(i).getPropertyValue();
                }
                codeList.add(code);
            }
        }

        return defaultValue;
    }


    /**
     * 封装基本属性,名称(调整时间)
     * 
     * @param keyInfo
     *            设键信息
     */
    private void createBaseKeyInfo(final KeyInfo keyInfo) {

        List <KeyContentInfo> keyContentInfos = new ArrayList <KeyContentInfo>();

        // 设置中文名称/BOH名称
        String cnName = "中文名称";
        if (!ACPConstants.brandCode.PHHS.equals(brandCode)) {
            cnName = "BOH名称";
        }

        createKeyContentInfos(keyContentInfos, cnName, keyInfo.getCnName());

        // 设置英文名称
        if (!ACPConstants.brandCode.PHHS.equals(brandCode)) {
            createKeyContentInfos(keyContentInfos, "英文名称", keyInfo.getEnName());
        }

        // 设键简称
        if (ACPConstants.brandCode.PHHS.equals(brandCode)) {
            createKeyContentInfos(keyContentInfos, "SUS水单名称", keyInfo.getSusName());
        } else if (ACPConstants.brandCode.KFC.equals(brandCode)) {
            createKeyContentInfos(keyContentInfos, "简称", keyInfo.getShortName());
        }

        // 设置OS/IOS中文/英文名称
        if (ACPConstants.brandCode.PHHS.equals(brandCode)) {
            createKeyContentInfos(keyContentInfos, "OS/IOS中文名称", keyInfo.getShortName());
            createKeyContentInfos(keyContentInfos, "OS/IOS英文名称", keyInfo.getEnName());
        }

        // 封装基本信息
        setWorkTitleStyle(keyContentInfos);

        // 设置生效日(调整)
        if (isAdjust && keyInfo.getAdjustNameEffectiveBeginDate() != null) {

            // 不校验什么品牌，设键类型啥的，有值就显示。业务已做控制
            // if ("0".equals(keyInfo.getIsAllowVariousNorms())) {
            setWorkTitleStyle("生效日", formatFavDate(keyInfo.getAdjustNameEffectiveBeginDate()));
            // }
        }
    }


    /**
     * 封装键值对信息
     * 
     * @param keyContentInfos
     *            设键基础信息对象
     * @param key
     *            表格头
     * @param value
     *            表格值
     */
    private void createKeyContentInfos(List <KeyContentInfo> keyContentInfos, final String key, final String value) {

        KeyContentInfo keyContentInfo = new KeyContentInfo();
        keyContentInfo.setKey(key);
        keyContentInfo.setValue(value);

        keyContentInfos.add(keyContentInfo);
    }


    /**
     * 导出为Excel
     * 
     * @return 文件名
     * @throws Exception
     *             异常
     */
    private String export2Excel(final String activityName) throws Exception {

        String exportPath = ServletActionContext.getServletContext().getRealPath("/export");
        File exportDir = new File(exportPath);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        String fileName = activityName + "_" + getCurrentDateSeria() + ".xls";

        FileOutputStream fos = new FileOutputStream(exportPath + "/" + fileName);
        wb.write(fos);
        fos.flush();
        fos.close();

        return fileName;
    }


    /**
     * 根据当前事件生成序列号
     * 
     * @return 序列号
     */
    private String getCurrentDateSeria() {

        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }


    /**
     * 设置工作簿活动内容头样式
     * 
     * @param activityInfo
     *            活动信息
     * @param keyLen
     *            活动中设键个数
     */
    private void createWorkTitle(ActivityInfo activityInfo, int keyLen) {

        // 重新计算行数
        rowNum = 0;

        // 重新计算列数
        totalColumnNum = 0;

        setAlignmentStyle("活动主信息", true, false, false);

        // 活动名称
        setWorkTitleStyle("活动名称", activityInfo.getActivityName(), true);

        // 活动类型
        String activityType = "堂食";
        if (ACPConstants.ActivityType.OUTSIDE.equals(activityInfo.getActivityType())) {
            activityType = "外送";
        }

        setWorkTitleStyle("活动类型", activityType, true);

        // 申请人
        setWorkTitleStyle("申请人", activityInfo.getApplicantName(), true);

        // 开始时间
        setWorkTitleStyle("开始日期", formatDate(activityInfo.getPopularize().getBeginDate()), true);

        // 介绍时间
        setWorkTitleStyle("结束日期", formatDate(activityInfo.getPopularize().getEndDate()), true);

        // 推广范围
        setWorkTitleStyle("推广范围", activityInfo.getPopularize().getPopularizeAreaName(), true);

        // 例外范围
        setWorkTitleStyle("例外范围", activityInfo.getPopularize().getExceptAreaName(), true);

        // 备注
        setWorkTitleStyle("备注", activityInfo.getPopularize().getOtherDescription(), true);

        // 活动申请日期
        setWorkTitleStyle("活动申请日期", formatPassTime(activityInfo.getApprovedPassTime()), true);

        // 设置设键个数
        setAlignmentStyle("设键信息-共" + keyLen + "个设键", true, false, false);
    }


    /**
     * 设置工作簿活动内容头样式
     * 
     * @param content
     *            行内容
     * @param isSetStyle
     *            是否需要设置样式
     */
    private void setWorkTitleStyle(String key, String value) {

        HSSFRow row = sheet1.createRow(rowNum++);

        setTitleValue(row, totalColumnNum, key, keyCellStyle);
        setTitleValue(row, totalColumnNum + 1, value, valueCellStyle);
    }


    /**
     * 设置工作簿活动内容头样式
     * 
     * @param content
     *            行内容
     * @param isSetStyle
     *            是否需要设置样式
     */
    private void setWorkTitleStyle(String key, String value, boolean isAlignment) {

        HSSFRow row = sheet1.createRow(rowNum++);

        setTitleValue(row, totalColumnNum, key, keyCellStyle);
        setTitleValue(row, totalColumnNum + 1, value, valueCellStyle, isAlignment);

        // 是否合并
        if (isAlignment) {
            CellRangeAddress region1 = new CellRangeAddress(rowNum - 1, rowNum - 1, 1, ACPConstants.FIVE);
            setRegionStyle(sheet1, region1, valueCellStyle);
        }

        // 自动调节行高度
        autoSetRowHeight(row, value, isAlignment);
    }


    /**
     * 设置自动行高
     * 
     * @param row
     *            当前行
     * @param value
     *            判断长度的值
     * @param isAlignment
     *            是否合并行
     */
    private void autoSetRowHeight(HSSFRow row, String value, boolean isAlignment) {

        try {
            int maxLen = ACPConstants.TEN;

            if (isAlignment) {
                maxLen = maxLen * ACPConstants.FIVE;
            }

            // 大于1行时才需要设置，否则无需设置
            double remainder = getValueLength(value, maxLen);
            if (remainder > 1) {
                try {
                    row.setHeight(Short.parseShort(new DecimalFormat("0").format(remainder * ACPConstants.TWO_HUNDRED_FIFTY_SIX)));
                } catch (Exception e) {
                    logger.error("类型转换错误", e.getMessage(), e, value);
                }
            }
        } catch (Exception e) {
            logger.error("计算行高出错");
        }
    }


    /**
     * 设置自动行高
     * 
     * @param row
     *            当前行
     * @param value
     *            判断长度的值
     * @param isAlignment
     *            是否合并行
     */
    private void autoSetRowHeight(HSSFRow row, List <KeyContentInfo> keyContentInfos) {

        try {
            double maxRemainder = 0.0;
            if (!CollectionUtils.isEmpty(keyContentInfos)) {
                for (KeyContentInfo contentInfo : keyContentInfos) {
                    double keyRemainder = getValueLength(contentInfo.getKey());
                    double valueRemainder = getValueLength(contentInfo.getValue());

                    double remainder = keyRemainder > valueRemainder ? keyRemainder : valueRemainder;
                    maxRemainder = maxRemainder > remainder ? maxRemainder : remainder;
                }
            }

            if (maxRemainder > 1) {
                row.setHeight(Short.parseShort(new DecimalFormat("0").format(maxRemainder * ACPConstants.TWO_HUNDRED_FIFTY_SIX)));
            }
        } catch (Exception e) {
            logger.error("类型转换错误", e.getMessage(), e, null);
        }
    }


    /**
     * 默认按照10个字符来换行
     * 
     * @param strValue
     *            字符串
     * @return 字符串拆分行数
     */
    private double getValueLength(final String strValue) {

        return getValueLength(strValue, ACPConstants.TEN);
    }


    /**
     * 计算一个字段串需拆分成几行(判断存在换行的情况)
     * 
     * @param strValue
     *            字符串
     */
    private double getValueLength(final String strValue, int maxLen) {

        int count = 0;
        int index = 0;

        if (StringUtil.isNotEmpty(strValue)) {

            final String separator = System.getProperty("line.separator");
            // 如果该字符串中存在换行符
            if (strValue.indexOf(separator) != -1) {
                while (true) {
                    index = strValue.indexOf(separator, index + 1);

                    if (index > 0) {
                        count = count + 1;
                    } else {
                        break;
                    }
                }

                // 因为是计算换行符，所以需要加1。加0.5则排版更美观
                return count + ACPConstants.TWO_POINT_FIVE;
            } else {
                // 加0.5是为了保证不管怎样都取最大，如2.1行则为3行
                return ((strValue.length() + ACPConstants.ZERO_POINT_FIVE) / maxLen) + ACPConstants.ZERO_POINT_FIVE;
            }
        }

        // 默认返回为1行
        return 1;
    }


    /**
     * 批量封装基础信息
     * 
     * @param keyContentInfos
     *            基础信息列表
     */
    private void setWorkTitleStyle(List <KeyContentInfo> keyContentInfos) {

        HSSFRow row = sheet1.createRow(rowNum++);
        final int columnNum = keyContentInfos.size();

        for (int i = 0; i < columnNum; i++) {

            // 超过3组值需换行
            if ((i + 1) % ACPConstants.THREE == 1 && i != 0) {
                row = sheet1.createRow(rowNum++);
                totalColumnNum = 0;
            }

            final KeyContentInfo keyContentInfo = keyContentInfos.get(i);
            setTitleValue(row, totalColumnNum, keyContentInfo.getKey(), keyCellStyle);
            setTitleValue(row, ++totalColumnNum, keyContentInfo.getValue(), valueCellStyle);
            totalColumnNum = totalColumnNum + 1;
        }

        // 调整行高
        autoSetRowHeight(row, keyContentInfos);
        totalColumnNum = 0;
    }


    /**
     * 设置单元格合并内容及样式
     * 
     * @param content
     *            合并行内容
     * @param isAlign
     *            是否居中
     * @param isSetStyle
     *            是否设置样式
     * @param isFont
     *            是否设置字体样式
     */
    private void setAlignmentStyle(String content, boolean isAlign, boolean isSetStyle, boolean isFont) {

        HSSFRow row = sheet1.createRow(rowNum++);
        HSSFCell cell = row.createCell(totalColumnNum);
        cell.setCellValue(content);

        HSSFCellStyle cellStyle = wb.createCellStyle();

        // 设置单元格格式
        for (int i = 0; i < ACPConstants.SIX; i++) {
            sheet1.setColumnWidth(i, ACPConstants.THIRTY * ACPConstants.TWO_HUNDRED_FIFTY_SIX);
        }

        CellRangeAddress region1 = new CellRangeAddress(rowNum - 1, rowNum - 1, 0, ACPConstants.FIVE);

        setRegionStyle(sheet1, region1, cellStyle);

        if (isFont) {
            // 设置单元格格式
            HSSFFont font = wb.createFont();
            font.setFontHeightInPoints((short) ACPConstants.ELEVEN);// 字号
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
            font.setColor(HSSFColor.BLACK.index);

            cellStyle.setFont(font);
        }

        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        if (isSetStyle) {
            cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        } else {
            cellStyle.setFillForegroundColor(HSSFColor.WHITE.index);
        }

        // 此处无需设置文本格式，否则出现###的情况
        // cellStyle.setDataFormat(format.getFormat("@"));
        if (isAlign) {
            cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }
        cell.setCellStyle(cellStyle);

        setBorder(cellStyle);

        // 自动调节行高度
        if (StringUtil.isNotEmpty(content)) {
            row.setHeight((short) ACPConstants.THREE_HUNDRED_THIRTY_TWO);
        }
        // autoSetRowHeight(row, content, true);
    }


    /**
     * 设置合并单元格格式
     * 
     * @param sheet
     * @param region
     * @param cs
     */
    public void setRegionStyle(HSSFSheet sheet, CellRangeAddress region, HSSFCellStyle cs) {

        sheet1.addMergedRegion(region);

        for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
            HSSFRow row = HSSFCellUtil.getRow(i, sheet);
            for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                HSSFCell cell = HSSFCellUtil.getCell(row, (short) j);
                cell.setCellStyle(cs);
            }
        }
    }


    /**
     * <p>
     * 设置EXCEL边框
     * </p>
     *
     * @param cellStyle
     */
    public void setBorder(HSSFCellStyle cellStyle) {

        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
        // 设置自动换行
        cellStyle.setWrapText(true);
    }


    /**
     * 
     * <p>
     * 根据格式返回当前时间。
     * </p>
     *
     * @return 字符串 yyyy-MM-dd (周X)
     */
    public String formatDate(final Date date) {

        final String[] dayOfWeek = new String[] {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        try {
            if (date == null) {
                return null;
            } else {
                final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                String nowDate = sf.format(date);
                int weekIndex = dayForWeek(nowDate) - 1;
                String week = dayOfWeek[weekIndex];

                return nowDate + "(" + week + ")";
            }
        } catch (Exception e) {
            logger.error("转换时间错误", e.getMessage(), e, null);
            return null;
        }
    }


    /**
     * 
     * <p>
     * 根据格式返回当前时间。
     * </p>
     *
     * @return 字符串 HH:mm
     */
    public String formatSellDate(final Date date) {

        try {
            final SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
            String nowDate = sf.format(date);
            return nowDate;
        } catch (Exception e) {
            logger.error("转换时间错误", e.getMessage(), e, null);
            return null;
        }
    }


    /**
     * 判断当前日期是星期几<br>
     * <br>
     * 
     * @param pTime
     *            修要判断的时间<br>
     * @return dayForWeek 判断结果<br>
     * @Exception 发生异常<br>
     */
    public int dayForWeek(String pTime) throws Exception {

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(format.parse(pTime));
        int dayForWeek = 0;
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            dayForWeek = ACPConstants.SEVEN;
        } else {
            dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
        }
        return dayForWeek;
    }


    /**
     * 获取key值样式
     * 
     * @return key值样式
     */
    private HSSFCellStyle getKeyCellStyle() {

        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) ACPConstants.ELEVEN);// 字号
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
        font.setColor(HSSFColor.BLACK.index);

        HSSFCellStyle cellStyle = wb.createCellStyle();
        // 垂直居中
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        // cellStyle.setDataFormat(format.getFormat("@"));
        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        setBorder(cellStyle);

        return cellStyle;
    }


    /**
     * 获取value值样式
     * 
     * @return value值样式
     */
    private HSSFCellStyle getValueCellStyle() {

        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) ACPConstants.ELEVEN);// 字号
        font.setColor(HSSFColor.BLACK.index);

        HSSFCellStyle cellStyle = wb.createCellStyle();
        // 垂直居中
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        // cellStyle.setDataFormat(format.getFormat("@"));
        cellStyle.setFont(font);
        cellStyle.setWrapText(true);
        setBorder(cellStyle);

        return cellStyle;
    }


    /**
     * 设置序号值样式
     * 
     * @return 序号值样式
     */
    private HSSFCellStyle getNumCellStyle() {

        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) ACPConstants.ELEVEN);// 字号
        font.setColor(HSSFColor.BLACK.index);

        HSSFCellStyle cellStyle = wb.createCellStyle();
        // 垂直居中
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(HSSFColor.LIGHT_TURQUOISE.index);
        // cellStyle.setDataFormat(format.getFormat("@"));
        cellStyle.setFont(font);
        setBorder(cellStyle);

        return cellStyle;
    }


    /**
     * 初始化配置信息
     * 
     * @param favorableConfigInfo
     *            优惠配置信息
     */
    private void initFavorableConfigInfo(FavorableConfigInfo favorableConfigInfo) {

        if (null == favorableConfigInfo) {
            favorableConfigInfo = new FavorableConfigInfo();
        }

        // 转换成06:00的格式
        if (!StringUtils.isEmpty(favorableConfigInfo.getUseBeginDate())) {
            favorableConfigInfo.setUseBeginDate(favorableConfigInfo.getUseBeginDate().substring(0, ACPConstants.TWO) + ":"
                    + favorableConfigInfo.getUseBeginDate().substring(ACPConstants.TWO, ACPConstants.FOUR));
        }
        if (!StringUtils.isEmpty(favorableConfigInfo.getUseEndDate())) {
            favorableConfigInfo.setUseEndDate(favorableConfigInfo.getUseEndDate().substring(0, ACPConstants.TWO) + ":"
                    + favorableConfigInfo.getUseEndDate().substring(ACPConstants.TWO, ACPConstants.FOUR));
        }
        if (!CollectionUtils.isEmpty(favorableConfigInfo.getFavorableConfigProductList())) {
            favorableConfigInfo.getFavorableConfigProductList().get(0)
                    .setProductName(favorableConfigInfo.getFavorableConfigProductList().get(0).getProductName());
            if (favorableConfigInfo.getFavorableConfigProductList().size() > 1) {
                favorableConfigInfo.getFavorableConfigProductList().get(1)
                        .setProductName(favorableConfigInfo.getFavorableConfigProductList().get(1).getProductName());
            }
        }

        // 初始化页面radio
        initRadio(favorableConfigInfo);
    }


    /**
     * 初始化页面上所有的radio
     * 
     * @param favorableConfigInfo
     *            优惠配置信息
     */
    private void initRadio(FavorableConfigInfo favorableConfigInfo) {

        // 初始化节假日是否参加，默认为是
        if (StringUtils.isEmpty(favorableConfigInfo.getIsBuyMoreGetMore())) {
            favorableConfigInfo.setIsBuyMoreGetMore(ACPConstants.YES);
        }

        // 初始化新店开业优惠，默认为否
        if (StringUtils.isEmpty(favorableConfigInfo.getNewStoreFavorable())) {
            favorableConfigInfo.setNewStoreFavorable(ACPConstants.NO);
        }

        // 初始化是否弹出，默认为否
        if (StringUtils.isEmpty(favorableConfigInfo.getIsPop())) {
            favorableConfigInfo.setIsPop(ACPConstants.NO);
        }

        // 初始化共享优惠，默认为是
        if (StringUtils.isEmpty(favorableConfigInfo.getIsShareFavorable())) {
            favorableConfigInfo.setIsShareFavorable(ACPConstants.YES);
        }

        // 初始化是否自动执行，默认为否
        if (StringUtils.isEmpty(favorableConfigInfo.getIsAutoExecute())) {
            favorableConfigInfo.setIsAutoExecute(ACPConstants.NO);
        }

        // 初始化是否适用RBD餐厅，默认为是
        if (StringUtils.isEmpty(favorableConfigInfo.getIsSuitRbd())) {
            favorableConfigInfo.setIsSuitRbd(ACPConstants.YES);
        }

        if (StringUtils.isEmpty(favorableConfigInfo.getIsHolidayJoin())) {
            favorableConfigInfo.setIsHolidayJoinDescription(ResourceUtil.getMessageByKey(ACPConstants.PIM_ALL));
        }

        if (StringUtils.isEmpty(favorableConfigInfo.getStoreType())) {
            favorableConfigInfo.setStoreTypeDescription(ResourceUtil.getMessageByKey(ACPConstants.PIM_ALL));
        }

        if (StringUtils.isEmpty(favorableConfigInfo.getSellChannel())) {
            favorableConfigInfo.setSellChannelDescription(ResourceUtil.getMessageByKey(ACPConstants.PIM_ALL));
        }

        if (StringUtils.isEmpty(favorableConfigInfo.getTakeoutType())) {
            favorableConfigInfo.setTakeoutTypeDescription(ResourceUtil.getMessageByKey(ACPConstants.PIM_ALL));
        }

        if (StringUtils.isEmpty(favorableConfigInfo.getIsPreOrder())) {
            favorableConfigInfo.setIsPreOrderDescription(ResourceUtil.getMessageByKey(ACPConstants.PIM_ALL));
        }
    }


    /**
     * 格式化时间
     * 
     * @param partner
     *            日期格式
     * @param date
     *            日期
     * @return 格式化后时间
     */
    private String formatFavDate(Date date) {

        if (null == date) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }


    /**
     * 格式化时间
     * 
     * @param partner
     *            日期格式
     * @param date
     *            日期
     * @return 格式化后时间
     */
    private String formatPassTime(Date date) {

        if (null == date) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }


    @Override
    public boolean isUseHandler() {

        return false;
    }

}
