#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
from config.config import *
from abstractinterpretor import *

class JavaInterpretor(AbstractInterpretor):
    def _convertline(self, line):
        retvalue = line
        retvalue = unicode(retvalue)
        it = re.finditer(self.chinese_words, line)
        index = 1
        has_chinese = False
        for match in it:
            # print (match.group())
            retvalue = re.sub(match.group(), JAVA_COMMON_ENUM_CLASSNAME + "." +
                              JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(index), retvalue)
            index += 1
            has_chinese = True
        return has_chinese, retvalue
