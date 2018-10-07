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
           "+ \"abc睡def觉 \" + \"\\\"值: \" + \"非法的\\\"\";" #"\"值"
test_str = unicode(test_str)
print test_str
chinese_words = re.compile(u"\"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")
# m = chinese_words.search(test_str,0)
test_str = test_str.replace("\\\"", "(TEMP_#$%)")
it = re.finditer(chinese_words, test_str)
index = 1
for match in it:
    print (match.group().replace("(TEMP_#$%)","\\\""))
    test_str = test_str.replace(match.group(), "COMMONENUM.CLASSNAME.param" + str(index))
    # test_str = re.subn(match.group(), "COMMONENUM.CLASSNAME.param" + str(index), test_str)
    index += 1

test_str = test_str.replace("(TEMP_#$%)", "\\\"")

print test_str