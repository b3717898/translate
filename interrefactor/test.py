#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
#  String abc = "吃饭abc" + "睡觉def";
import re
test_str = "String abc = \"abc吃abc饭abc\" + \"睡觉def\" + \"睡觉\" + \"abc睡觉\" + \"abc睡 觉 \" + \"123\" + \"abc睡def觉 \";"
test_str = unicode(test_str)
chinese_words = re.compile(u"\"[^\"]*[\u4e00-\u9fa5]+[^\"]*\"")
# m = chinese_words.search(test_str,0)
it = re.finditer(chinese_words, test_str)
index = 1
for match in it:
    print (match.group())
    test_str = re.sub(match.group(), "COMMONENUM.CLASSNAME.param" + str(index), test_str)
    index += 1

print test_str