#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
from config.config import *
from abstractinterpretor import *


class Js4i18nInterpretor(AbstractInterpretor):

    def _convertline(self, line, file_name):
        retvalue = line
        retvalue = unicode(retvalue)

        it_comment = re.finditer(self.comment_words, retvalue)
        comment_temp_str = ""
        for match in it_comment:
            comment_temp_str = match.group()
            retvalue = retvalue.replace(match.group(), "[$TEMP_COMMENT_STR$]")

        it = re.finditer(self.qtlt_with_squote_words, retvalue)  # for cashDailyPosItemConfigTableDiv.push('<th width="120">项目</th>');
        enum_lines = []
        index = 1
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.gtlt_words, retvalue_sub)
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");
                replace_str_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, prop_name)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                match_str = match_sub.group()
                match_str = match_str[1:-1]
                enum_lines.append("//{}\n".format(match_str))
                enum_lines.append("{}:\"{}\",\n".format(prop_name, match_str.replace("\"", "\\\"")))
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        # print retvalue
        it = re.finditer(self.slash_squote_words,
                         retvalue)  # for cashDailyPosItemConfigTableDiv.push('\'项目\'');
        for match in it:
            # print (match.group())
            prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
            # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
            # print match.group()
            if prop_name in self.prop_name:
                prop_name = prop_name + "_" + str(self.globe_count)
                self.globe_count += 1
            self.prop_name[prop_name] = "1"
            # CommonI18n.rb.getString("COMMON.SEARCH");
            replace_str = "\\\'' + {}.{} + '\\\'".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, prop_name)
            # print match.group()
            retvalue = retvalue.replace(match.group(), replace_str)

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            # print(match_str)
            match_str = match_str[2:-2]
            enum_lines.append("//{}\n".format(match_str))
            enum_lines.append("{}:\"{}\",\n".format(prop_name, match_str.replace("\"", "\\\"")))
            index += 1

        it = re.finditer(self.qtlt_with_quote_words,
                         retvalue)  # + "<a href='#' class='blue'  onclick=\"updateSpecialCashier('"+this.guid+"')\" >修改</a>&nbsp;&nbsp;"
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.gtlt_words, retvalue_sub)
            for match_sub in it_sub:
                # print (match.group())
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"
                # CommonI18n.rb.getString("COMMON.SEARCH");
                replace_str_sub = ">\" + {}.{} + \"</".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, prop_name)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                match_str = match_sub.group()
                match_str = match_str[1:-2]
                enum_lines.append("//{}\n".format(match_str))
                enum_lines.append("{}:\"{}\",\n".format(prop_name, match_str.replace("\"", "\\\"")))
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        retvalue = retvalue.replace("\\\"", "[$TEMP_QUOTE_STR$]")
        retvalue = retvalue.replace("\\\'", "[$TEMP_SQUOTE_STR$]")
        const_lines = []

        it = re.finditer(self.chinese_words, retvalue)  # for abc = "吃饭吃饭"
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
            replace_str = "{}.{}".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, prop_name)
            retvalue = retvalue.replace(match.group(), replace_str)

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            match_str = match_str[1:-1]
            enum_lines.append("//{}\n".format(match_str))
            enum_lines.append("{}:\"{}\",\n".format(prop_name, match_str))
            index += 1

        it = re.finditer(self.squote_words, retvalue)  # for 'xxxxxx'
        for match in it:
            # print (match.group())
            prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
            # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
            # print match.group()
            if prop_name in self.prop_name:
                prop_name = prop_name + "_" + str(self.globe_count)
                self.globe_count += 1
            self.prop_name[prop_name] = "1"
            # CommonI18n.rb.getString("COMMON.SEARCH");
            replace_str = "{}.{}".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, prop_name)
            retvalue = retvalue.replace(match.group(), replace_str)

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            match_str = match_str[1:-1]
            enum_lines.append("//{}\n".format(match_str))
            enum_lines.append("{}:\"{}\",\n".format(prop_name, match_str.replace("\"", "\\\"")))
            index += 1

        retvalue = retvalue.replace("[$TEMP_QUOTE_STR$]", "\\\"")
        retvalue = retvalue.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
        retvalue = retvalue.replace("[$TEMP_COMMENT_STR$]", comment_temp_str)

        return retvalue, enum_lines, const_lines
