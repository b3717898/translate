#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
from config.config import *
from abstractinterpretor import *


class Java4i18nInterpretor(AbstractInterpretor):

    def _convertline(self, line, file_name):
        retvalue = line
        retvalue = unicode(retvalue)

        it_comment = re.finditer(self.comment_words, retvalue)
        comment_temp_str = ""
        for match in it_comment:
            comment_temp_str = match.group()
            retvalue = retvalue.replace(match.group(), "[$TEMP_COMMENT_STR$]")
        retvalue = retvalue.replace("\\\"", "[$TEMP_QUOTE_STR$]")
        const_lines = []
        it_case = re.finditer(self.chinese_words_with_CASE, retvalue)
        for match in it_case:
            prop_name = "{}_L{}".format(file_name, str(self.line))
            replace_str = "case {}.{}.{}".format(JAVA_COMMON_4_I18N_CONST_PACKAGE_NAME,
                                            JAVA_COMMON_4_I18N_CONST_OUTPUT_FILENAME.replace(".java", ""),
                                            prop_name)
            retvalue = retvalue.replace(match.group(), replace_str)
            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            const_lines.append("    /** {} */\n".format(match_str))
            const_lines.append("    String {} = {};\n".format(prop_name, match_str.replace("case ", "")))

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
            # CommonI18n.rb.getString("COMMON.SEARCH");
            replace_str = "{}.rb.getString(\"{}\")".format(JAVA_COMMON_4_I18N_ENUM_CLASSNAME, prop_name)
            retvalue = retvalue.replace(match.group(), replace_str)

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str[1:-1]
            enum_lines.append("#{}\n".format(match_str))
            enum_lines.append("{} = {}\n".format(prop_name, match_str.encode('unicode_escape')))
            index += 1
        retvalue = retvalue.replace("[$TEMP_QUOTE_STR$]", "\\\"")
        retvalue = retvalue.replace("[$TEMP_COMMENT_STR$]", comment_temp_str)

        return retvalue, enum_lines, const_lines
