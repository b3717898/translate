#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
#  String abc = "吃饭abc" + "睡觉def";
import re
test_str = "Stri\\\"ng abc = \"abc品牌存在错误活动类型[ac\" + \"睡觉def\" " \
           "+ \"睡觉\" + \"abc睡觉\" + \"abc睡 觉 \" + \"123\" " \
           "+ \"abc睡def觉 \" + \"\\\"值: \" + \"非法的\\\"\";" + "\"workAreaCode\"))){//下拉框的全dfd部选项是拼上去的，由于联动标签好像不支持\"全部\""


test_str = "<span class=\"span_left-rgm\" ><h4>订单号：${result.storeOrderInfoBean.orderCode} &nbsp;&nbsp;订货日期：${result.storeOrderInfoBean.orderDate} &nbsp;&nbsp;收货餐厅:${result.storeOrderInfoBean.storeName}</h4></span>"
test_str = unicode(test_str)
print test_str
slash_squote_words = re.compile(u">[^<}]*[\u4e00-\u9fa5]+[^$]*\\$\\{")  # abc = \'吃饭吃饭\'
it_slash = re.finditer(slash_squote_words, test_str)
for match in it_slash:
    print match.group()
#
#
# comment_words = re.compile(u"//.*[\u4e00-\u9fa5]+.*")
# it_comment = re.finditer(comment_words, test_str)
# comment_temp_str = ""
# for match in it_comment:
#     comment_temp_str = match.group()
#     test_str = test_str.replace(match.group(), "[$TEMP_COMMENT_STR$]")
# # m = chinese_words.search(test_str,0)
# chinese_words = re.compile(u"\"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")
# test_str = test_str.replace("\\\"", "(TEMP_#$%)")
# it = re.finditer(chinese_words, test_str)
# index = 1
# for match in it:
#     print (match.group().replace("(TEMP_#$%)","\\\""))
#     test_str = test_str.replace(match.group(), "COMMONENUM.CLASSNAME.param" + str(index))
#     # test_str = re.subn(match.group(), "COMMONENUM.CLASSNAME.param" + str(index), test_str)
#     index += 1
#
# test_str = test_str.replace("(TEMP_#$%)", "\\\"")
# test_str = test_str.replace("[$TEMP_COMMENT_STR$]", comment_temp_str)
#
# print test_str