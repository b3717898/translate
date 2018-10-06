var pageParams = {
	"op.module" : "acp",// 该页面所属模块名
	"op.pageID" : "PIM_R010102",// 页面标识
	"op.pageTitle" : PIM_R010102_TITLE,// 页面标题
	"op.pageStyle" : "Query",// 页面类型，Query：一览；Save：新增；Edit：编辑；Detail：查看
	"op.openStyle" : "Redirect",// 跳转类型，Redirect：跳转；Pop：弹出层
	"entryName" : "keyInfo",
	"op.defaultOrder" : "createTime",// 默认分页条件，一览页面需要
	"op.defaultOrderStyle" : "desc",// 默认分页类型，一览页面需要，DESC：倒序，ASC：正序
	//"op.defaultPageSize" : "50" // 默认的Grid显示条数，一览页面需要
};

function initValidator() {

}

function getGridColunms() {

	var brandCode = $("#brandCode").val();
	var adjust = $("#adjust").val();// 是否活动调整

	var gridColumns = [];

	gridColumns.push({
		display : PIM_KEYCLASSIFY,
		name : 'keyClassify',
		width : '10%',
		align : 'left',
		isSort : true,
		isShowTitle : true,
		render : function(record) {
			return record.code2Name.keyClassify;
		}
	});
	
	if(PIM_ED != brandCode)
	{
		gridColumns.push({
			display : PIM_KEYTYPE,
			name : 'keyType',
			width : '10%',
			align : 'left',
			isSort : true,
			render : function(record) {
				return record.code2Name.keyType;
			},
			isShowTitle : true
		});
	}
	else
	{
		gridColumns.push({
			display : PIM_KEYTYPE,
			name : 'keyType',
			width : '22%',
			align : 'left',
			isSort : true,
			render : function(record) {
				return record.code2Name.keyType;
			},
			isShowTitle : true
		});
	}
		
	gridColumns.push({
		display : function() {
			if (PIM_PHHS != brandCode) {
				return PIM_KEYCNNAME;
			} else {
				return PIM_BOHCNNAME;
			}
			;
		},
		name : 'cnName',
		width : '13%',
		align : 'left',
		isSort : true,
		isShowTitle : true
	});

	//ED品牌屏蔽设键简称
	if(PIM_ED != brandCode)
	{
		gridColumns.push({
			display : function() {
				if (PIM_PHHS != brandCode) {
					return PIM_KEYFORSHORT;
				} else {
					return PIM_BOHOSCNNAME;
				}
				;
			},
			name : 'shortName',
			width : '12%',
			align : 'left',
			isSort : true,
			isShowTitle : true,
			render : function(record) {
				if (PIM_KFC == brandCode || PIM_PHHS == brandCode || PIM_TBG == brandCode) {
					return record.shortName;
				} else {
					return "";
				}
			}
		});
	}
	

	gridColumns.push({
		display : PIM_KEYSTATUS,
		name : 'keyStatus',
		width : '10%',
		isSort : true,
		render : function(record) {
			return record.code2Name.keyStatus;
		},
		isShowTitle : true
	});

	gridColumns.push({
		display : PIM_KEYSOURCE,
		name : 'keySource',
		width : '10%',
		isSort : true,
		render : function(record) {
			return record.code2Name.keySource;
		}
	});

	if ("adjust" == adjust) {
		gridColumns.push({
			display : '是否已调整',
			name : 'isAdjust',
			width : '12%',
			align : 'center',
			render : isAdjustRender,
			isSort : false
		});
	}

	gridColumns.push({
		display : PIM_OPERATE,
		width : '24%',
		align : 'center',
		render : operationColumnRender,
		isSort : false
	});

	gridColumns.push({
		display : PIM_STEP,
		name : 'underWayStep',
		width : '9%',
		align : 'center',
		isSort : true,
		render : operationColumnRenderforStep
	});

	return gridColumns;
}

// ready函数
$(function() {
	var mode = $("#pageStyle").val();
	if ("view" == mode) {
		document.getElementById("buttonSave").style.display = "none";
		document.getElementById("buttonView").style.display = "";
	} else {
		document.getElementById("buttonSave").style.display = "";
		document.getElementById("buttonView").style.display = "none";
	}
	
	// 禁用Enter键表单自动提交
	$("#searchText").keydown(function(event) {
		if (event.keyCode == 13) {
			return false;
		}
	});

	// initValidator();//绑定Form的验证器，默认必须调用
	_submitUrl = getUrl('Query');
	// 构建用户表格
	$("#keyInfoList").myLigerGrid({
		columns : getGridColunms(),
		url : _submitUrl,
		width : "100%",
		sortName : pageParams["op.defaultOrder"],
		sortOrder : pageParams["op.defaultOrderStyle"],
		pageSize : pageParams["op.defaultPageSize"]

	});
	query('searchForm', 'keyInfoList');
});

function operationColumnRender(record, rowindex, value, column) {
	var mode = $("#pageStyle").val();
	var activityId = $("#activityId").val();
	var activityType = $("#activityType").val();
	var activityStatus = $("#activityStatus").val();
	var adjust = $("#adjust").val();// 是否活动调整
	var keyId = record.guid;
	var keyClassify = record.keyClassify;// 设键分类
	var keyType = record.keyType;// 设键类型
	var productType = record.productTypeId;// 产品类型
	var keyStatus = record.keyStatus; //
	var underWayStep = record.underWayStep; // 设键完成步骤 （用于判断设键复制：只有设键完成了才可以复制）

	var innerHtml = "<a href='#' onclick=viewKey('" + activityId + "','"
			+ activityType + "','" + keyId + "','" + keyClassify + "','"
			+ keyType + "','" + productType + "'); " + "class='blue'>"
			+ PIM_DETAIL + "</a>";
	if ("adjust" == adjust) {
		if ("view" == mode) {
			innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;" + "<font class='gray'>"
					+ PIM_EDIT + "</font>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;<font class='gray'>"
					+ PIM_COPYKEY + "</font>" + "&nbsp;&nbsp;&nbsp;&nbsp;"
					+ "<font class='gray'>" + PIM_DELETE + "</font>";
		} else {
			// 可调整的设键 (以及进行中的活动再次调整时锁定的设键也能调整BF6911)
			var lastResumeType = $("#lastResumeType").val();
			if ("N" != record.adjustFlag || ("N" == record.adjustFlag && lastResumeType == "00013")) {
				innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick=editKey('"
						+ keyId
						+ "','"
						+ keyClassify
						+ "','"
						+ keyType
						+ "','"
						+ productType
						+ "',''); class='blue'>"
						+ PIM_ADJUST
						+ "</a>";
				if ($("#canAdd").val() == "true" || activityStatus == "00001" || activityStatus == "00011") {
					innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' class='red' onclick=deleteKey('"
							+ keyId
							+ "','"
							+ record.keySource
							+ "','"
							+ keyType
							+ "','" + keyClassify
							+ "','" + record.cnName
							+ "')>" + PIM_DELETE + "</a>";
				}
			} else {
				innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<font class='gray'>"
						+ PIM_ADJUST + "</font>";
			}
		}
	} else {
		if ("view" == mode) {
			innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;" + "<font class='gray'>"
					+ PIM_EDIT + "</font>"
					+ "&nbsp;&nbsp;&nbsp;&nbsp;<font class='gray'>"
					+ PIM_COPYKEY + "</font>" + "&nbsp;&nbsp;&nbsp;&nbsp;"
					+ "<font class='gray'>" + PIM_DELETE + "</font>";
		} else {
			if ("00001" == keyStatus || "00002" == keyStatus
					|| "00003" == keyStatus || "00004" == keyStatus
					|| "00005" == keyStatus || "00012" == keyStatus) {
				innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' onclick=editKey('"
						+ keyId
						+ "','"
						+ keyClassify
						+ "','"
						+ keyType
						+ "','"
						+ productType
						+ "',''); class='blue'>"
						+ PIM_EDIT
						+ "</a>";

				if ("00004" == underWayStep) {
					innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' class='blue' onclick=copyKey('"
							+ keyId + "')>" + PIM_COPYKEY + "</a>";
				} else {
					innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<font class='gray'>"
							+ PIM_COPYKEY + "</font>";
				}

				innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' class='red' onclick=deleteKey('"
						+ keyId
						+ "','"
						+ record.keySource
						+ "','"
						+ keyType
						+ "','" + keyClassify 
						+ "','" + record.cnName
						+ "')>" + PIM_DELETE + "</a>";
			} else {
				innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<font class='gray'>"
						+ PIM_EDIT + "</font>";
				if ("00004" == underWayStep) {
					innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' class='blue' onclick=copyKey('"
							+ keyId + "')>" + PIM_COPYKEY + "</a>";
				} else {
					innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<font class='gray'>"
							+ PIM_COPYKEY + "</font>";
				}
				innerHtml += "&nbsp;&nbsp;&nbsp;&nbsp;<font class='gray'>"
						+ PIM_DELETE + "</font>";
			}
		}
	}
	return innerHtml;
}

function isAdjustRender(record, rowindex, value, column) {

	if (record.isAdjust == PIM_CONSTANTS_Y) {
		return "<font color='red'>已调整</font>";
	} else {
		return "未调整";
	}
}

function operationColumnRenderforStep(record, rowindex, value, column) {
	var mode = $("#pageStyle").val();
	var underWayStep = record.underWayStep;
	var keyId = record.guid;
	var keyClassify = record.keyClassify;// 设键分类
	var keyType = record.keyType;// 设键类型
	var productType = record.productTypeId;// 产品类型
	// var is
	var innerHtml = "";
	// 需要先获得设键步骤的属性值，如果已经完成则显示填写完成，如果没有完成则限制连接跳到第几步，步骤由后台返回。服务未完善。待完善后，修改
	if ("view" != mode && "00004" != underWayStep) {
		innerHtml = "<a href='#' onclick=editKey('" + keyId + "','"
				+ keyClassify + "','" + keyType + "','" + productType + "','"
				+ record.underWayStep + "'); class='blue'>" + value + "</a>";
	} else {
		innerHtml = "<font class='gray'>" + record.code2Name.underWayStep
				+ "</font>";
	}
	return innerHtml;
}

function queryKeysInfo(type) {
	var guid = $("#activityId").val();
	var activityType = $("#activityType").val();// 活动类型
	var activityName = $("#activityName").val();
	var activityBeginDate = $("#activityBeginDate").val();
	var activityEndDate = $("#activityEndDate").val();
	var url = _urlPrefix + 'acp/PIM_R010105Action_Init.action?functionType='
			+ type + '&activityId=' + guid + '&activityType=' + activityType
			+ '&activityNamePage=' + encodeURIComponent(activityName) + '&activityBeginDate='
			+ activityBeginDate + '&activityEndDate=' + activityEndDate;
	redirectWithUrlStack(url, true);

}

// 编辑设键
function editKey(keyId, keyClassify, keyType, productType, underWayStep) {

	var activityType = $("#activityType").val();// 活动类型
	var activityId = $("#activityId").val();// 活动id
	var activityName = $("#activityName").val();
	var parentPageStyle = $("#pageStyle").val();
	var brandCode = $("#brandCode").val();
	var adjust = $("#adjust").val();
	var hrefValue = '?activityId=' + activityId + '&activityType='
			+ activityType + '&keyClassify=' + keyClassify + '&keyType='
			+ keyType + '&keyId=' + keyId + '&guid=' + keyId + '&activityName='
			+ encodeURIComponent(activityName) + '&sys.pageStyle=editor&parentPageStyle='
			+ parentPageStyle + '&adjust=' + adjust + '&brandCode=' + brandCode;

	if (("00001" == keyClassify) && ("00001") == keyType) {// 产品-产品
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R010603Action_Init.action'
					+ hrefValue + "&productType=" + productType;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R010602Action_Init.action'
					+ hrefValue + "&productType=" + productType;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010601Action_Init.action'
					+ hrefValue + "&productType=" + productType;
		}
	} else if (("00001" == keyClassify) && ("00002") == keyType) {// 产品-配料
		hrefValue = _urlPrefix + 'acp/PIM_R010701Action_Init.action'
				+ hrefValue;
	} else if ("00002" == keyClassify) {// 套餐
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R010803Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R010802Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00003" == keyClassify) {// MEALDEAL
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R010903Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R010902Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00004" == keyClassify) {// TRADEUP
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011003Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011002Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00005" == keyClassify) {// 折价凭券
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011103Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011102Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if (("00006" == keyClassify)
			&& ("00026" == keyType || "00011" == keyType || "00037" == keyType || "00012" == keyType)) {// 折价不凭券-产品折价
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011103Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011102Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if (("00006" == keyClassify) && ("00027" == keyType)) {// 折价不凭券-产品促销
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011803Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011802Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00007" == keyClassify) {// 免费兑换
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011203Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011202Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00008" == keyClassify) {// 餐券售卖
		hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
				+ hrefValue;
	} else if ("00009" == keyClassify) {// 餐券回收
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011403Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011402Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00010" == keyClassify) {// 折扣
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011503Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011502Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00011" == keyClassify) {// 折扣
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011603Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011602Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	} else if ("00012" == keyClassify) {// 优惠配置
		hrefValue = _urlPrefix + 'acp/PIM_R011701Action_Init.action'
				+ hrefValue;
		window.location.href = hrefValue;
	} else if ("00013" == keyClassify){//百胜卡回收
		hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
		+ hrefValue;
	} else if("00014" == keyClassify){//捐款
		hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
		+ hrefValue;
	} else if("00015" == keyClassify){//百胜卡挂账-百胜卡挂账
		if ("00003" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011103Action_Init.action'
					+ hrefValue;
		} else if ("00002" == underWayStep) {
			hrefValue = _urlPrefix + 'acp/PIM_R011102Action_Init.action'
					+ hrefValue;
		} else {
			hrefValue = _urlPrefix + 'acp/PIM_R010801Action_Init.action'
					+ hrefValue;
		}
	}
	redirectWithUrlStack(hrefValue, true);
}

function selectPop() {
	var activityId = $("#activityId").val();
	var activityType = $("#activityType").val();
	var activityName = $("#activityName").val();
	var adjust = $("#adjust").val();
	openPageByPop("acp/PIM_R010104Action_Init.action", activityId
			+ "&activityType=" + activityType + "&activityName=" + encodeURIComponent(activityName)
			+ "&adjust=" + adjust + getUrlStackForPopPage(), 450, 400, "edit",
			"", PIM_R010104_TITLE);
}

// 删除设键
function deleteKey(keyId, keySource, keyType, keyClassify,cnName) {

	$.ligerDialog.confirm(CONFIRM_MESSAGE, function(yes) {
		if (yes) {
			openWaittingDialog("正在删除，请稍后...");
			$.post("acp/PIM_R010102Action_Delete.action", {
				'keyId' : keyId,
				'activityId' : $("#activityId").val(),
				'keySource' : keySource,
				'keyType' : keyType,
				'keyClassify' : keyClassify,
				'cnName' : cnName //删除时带入设键名称，用于提示时显示
			}, function(data) {
				if (data.success) {
					closeWaittingDialog();
					showSuccessDialog(data.msg);
					$("#keyInfoList").ligerGetGridManager().loadData();
				} else {
					closeWaittingDialog();
					showFailureDialog(data.msg);
				}

			});
		}
	});
}

// 复制设键
function copyKey(keyId) {
	openWaittingDialog("正在复制设键，请稍后...");
	$.post("acp/PIM_R010102Action_Copy.action", {
		'guidList' : "'" + keyId + "'",
		'originalActivityList' : "'" + $("#activityId").val() + "'",
		'activityId' : $("#activityId").val(),
	}, function(data) {
		if (data.success) {
			showSuccessDialog(data.msg);
			$("#keyInfoList").ligerGetGridManager().loadData();
		} else {
			showFailureDialog(data.msg);
		} 

	});
}

// 返回活动申请页面，此处需要将活动id作为参数传递给PIM_R010101页面
function returnActivityPage() {
	var activityId = $("#activityId").val();
	var extraUrlParams = "sys.guid=" + activityId+"&needSaveFlag=N";
	returnPrevPage(extraUrlParams);
//	redirectWithUrlStack(_urlPrefix
//			+ "acp/PIM_R010101Action_Init.action?sys.guid=" + activityId, true);
}

// 批量导出设键信息
function query4exportkey() {
	var activityId = $("#activityId").val();
	var activityType = $("#activityType").val();
	var adjust = $("#adjust").val();

//	var hrefValue = 'acp/PIM_R01010209Action_Init.action?activityId='
//			+ activityId + '&activityType=' + activityType
//			+ '&sys.pageStyle=view'
//			+ '&isExport=false&isBatchExport=true&adjust=' + adjust;
//
//	redirectWithUrlStack(_urlPrefix + hrefValue, true);
	
	openWaittingDialog("导出中……");
	var params = {
		"activityId" : activityId,
		"activityType" : activityType,
		"adjust" : adjust
	};

	ajaxRequestByUserOperate(_urlPrefix
			+ 'acp/PIM_R01010209Action_Export.action', params,
			'returnFunction(rValue)');
}

function returnFunction(rValue) {
	closeWaittingDialog();
	if (rValue.success) {
		window.location = $("#path").val() + "/export/" + rValue.extra.filename;
	} else {
		if (rValue.timeout) {
			timeOutOperate();
			return;
		} else {
			showMessageBox(rValue);
		}
	}
}

function query4exportByBrand(id, girdid, context) {
	var validator = $("#" + id).validate().form();
	var brandCode = $("#brandCode").val();

	if (validator || "undefined" == typeof (validator)) {
		// 获取查询条件
		var params = getObjectParam(id, "input,select,textarea");
		var gridManager = $("#" + girdid).ligerGetGridManager();
		var options = gridManager.options;
		var excelHeader = "[{\"display\":\"设键分类\",\"name\":\"keyClassify\",\"align\":\"left\"}, {\"display\":\"设键类型\", \"name\":\"keyType\", \"align\":\"left\"}, {\"display\":\"中文名称\",  \"name\":\"cnName\", \"align\":\"left\"}, {\"display\":\"设键简称\",  \"name\":\"shortName\", \"align\":\"left\"}, {\"display\":\"设键状态\",  \"name\":\"keyStatus\", \"align\":\"left\"}, {\"display\":\"设键来源\",  \"name\":\"keySource\", \"align\":\"left\"}, {\"display\":\"步骤\",  \"name\":\"underWayStep\", \"align\":\"center\"}]";

		if (PIM_PHHS == brandCode) {
			excelHeader = "[{\"display\":\"设键分类\",\"name\":\"keyClassify\",\"align\":\"left\"}, {\"display\":\"设键类型\", \"name\":\"keyType\", \"align\":\"left\"}, {\"display\":\"BOH名称\",  \"name\":\"cnName\", \"align\":\"left\"}, {\"display\":\"OS/IOS中文名称\",  \"name\":\"shortName\", \"align\":\"left\"}, {\"display\":\"设键状态\",  \"name\":\"keyStatus\", \"align\":\"left\"}, {\"display\":\"设键来源\",  \"name\":\"keySource\", \"align\":\"left\"}, {\"display\":\"步骤\",  \"name\":\"underWayStep\", \"align\":\"center\"}]";
		}

		params["op.export"] = excelHeader;

		var url = options.url;
		params[options.sortnameParmName] = options.sortName;
		params[options.sortorderParmName] = options.sortOrder;

		var paramStr = "";
		var start = false;
		for ( var attr in params) {
			if (start) {
				paramStr += "&";
			}

			paramStr += attr;
			paramStr += "=";
			paramStr += encodeURIComponent(params[attr]);
			start = true;
		}

		openWaittingDialog("导出中……");
		$.post(url, paramStr, function(data) {

			closeWaittingDialog();
			if (data.success) {
				download(null, context, null, data.extra.guid);
			} else {
				if (data.timeout) {
					timeOutOperate();
					return;
				} else {
					showMessageBox(data);
				}
			}
		}, 'json');
	}
}


function searchKeyInfo() {
	query('searchForm', 'keyInfoList');
}