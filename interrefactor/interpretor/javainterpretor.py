#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
from config.config import *
from abstractinterpretor import *

class JavaInterpretor(AbstractInterpretor):
    def _convertline(self, line, file_name):
        retvalue = line
        retvalue = unicode(retvalue)
        it = re.finditer(self.chinese_words, line)
        index = 1
        enum_lines = []
        for match in it:
            # print (match.group())
            prop_name = file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(index)
            retvalue = re.sub(match.group(), JAVA_COMMON_ENUM_CLASSNAME + "." + prop_name, retvalue)
            enum_lines.append("    /** {} */\n".format(match.group()))
            enum_lines.append("    String {} = {};\n".format(prop_name, match.group()))
            # enum_lines.append("    String " + prop_name + " = " + match.group() + ";\n")
            index += 1

        return retvalue, retvalue
