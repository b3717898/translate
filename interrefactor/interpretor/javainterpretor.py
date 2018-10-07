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
        retvalue = retvalue.replace("\\\"", "(TEMP_#$%)")
        it = re.finditer(self.chinese_words, retvalue)
        enum_lines = []
        index = 1
        for match in it:
            # print (match.group())
            prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
            #file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
            # print match.group()
            if prop_name in self.prop_name:
                prop_name = prop_name + "_" + str(self.globe_count)
                self.globe_count += 1
            self.prop_name[prop_name] = "1"
            retvalue = retvalue.replace(match.group(), JAVA_COMMON_ENUM_CLASSNAME + "." + prop_name)

            # retvalue = re.sub(match.group(), JAVA_COMMON_ENUM_CLASSNAME + "." + prop_name, retvalue)
            enum_lines.append("    /** {} */\n".format(match.group().replace("(TEMP_#$%)", "\\\"")))
            enum_lines.append("    String {} = {};\n".format(prop_name, match.group().replace("(TEMP_#$%)", "\\\"")))
            # enum_lines.append("    String " + prop_name + " = " + match.group() + ";\n")
            index += 1
        retvalue = retvalue.replace("(TEMP_#$%)", "\\\"")
        return retvalue, enum_lines
