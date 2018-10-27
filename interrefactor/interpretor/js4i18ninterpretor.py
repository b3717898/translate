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

        it = re.finditer(self.qtlt_with_squote_words, retvalue)  # 1 for cashDailyPosItemConfigTableDiv.push('<th width="120">项目</th>');
        enum_lines = []
        index = 1
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.gt_squote_words, retvalue_sub)
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "1.1", True)
                replace_str_sub = ">' + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "1", True)
                replace_str_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        # print retvalue
        it = re.finditer(self.slash_squote_words,
                         retvalue)  # 2 for cashDailyPosItemConfigTableDiv.push('\'项目\'');
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

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            # print(match_str)
            match_str = match_str[2:-2]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2", True)
            replace_str = "\\\'' + {}.{} + '\\\'".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.qtlt_with_quote_words,
                         retvalue)
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.quote_lt_words, retvalue_sub)  # 2.05 " 工作站,排人优先级顺序有误<xxx 中的 " 工作站,排人优先级顺序有误<
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.05", True)
                replace_str_sub = " {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gt_quote_words, retvalue_sub) #2.1 "     <xxxxxx>吃饭“xxxx</ddd>" 中的>吃饭“
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.1", True)
                replace_str_sub = ">\" + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.chinese_words, retvalue_sub)  # 2.2 "     <xxxxxx>" + "吃饭" + "</ddd>"	中的"吃饭“
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2.2", True)
                replace_str_sub = " {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gtlt_words, retvalue_sub) # 3 + "<a href='#' class='blue'  onclick=\"updateSpecialCashier('"+this.guid+"')\" >修改</a>&nbsp;&nbsp;"
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3", True)
                replace_str_sub = ">\" + {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        retvalue = retvalue.replace("\\\"", "[$TEMP_QUOTE_STR$]")
        retvalue = retvalue.replace("\\\'", "[$TEMP_SQUOTE_STR$]")
        const_lines = []

        it = re.finditer(self.chinese_words, retvalue)  # 3.9 for abc = "xxxx>吃饭<xxxx"
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.9", True)
                replace_str_sub = ">\" + {}.{} + \"<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.squote_words, retvalue_sub)     #3.91 for abc = "xxxx'吃饭'xxxx"
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3.91", True)
                replace_str_sub = "'\" + {}.{} + \"'".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.chinese_words, retvalue)  # 4 for abc = "吃饭吃饭"
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


            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            match_str = match_str[1:-1]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4", False)
            replace_str = "{}.{}".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.squote_words, retvalue)  # 'xxxxx'
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.squote_lt_words, retvalue_sub)  # 4.8 for 'xx吃饭<x' 中的 ‘xx吃饭<
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.8", True)
                replace_str_sub = " {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gt_squote_words, retvalue_sub)    # 4.81 for 'x>吃饭' 中的 >吃饭'
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.81", True)
                replace_str_sub = ">' + {}.{} ".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.gtlt_words, retvalue_sub) # 4.9 for 'xx>吃饭<x'
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

                match_str = match_sub.group()
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.9", True)
                replace_str_sub = ">' + {}.{} + '<".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.squote_words, retvalue)  # 5 for 'xxxxxx'
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

            match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
            match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
            match_str = match_str[1:-1]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "5", True)
            replace_str = "{}.{}".format(JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX, key)
            retvalue = retvalue.replace(match.group(), replace_str)

            enum_lines.extend(prop_lines)
            index += 1

        retvalue = retvalue.replace("[$TEMP_QUOTE_STR$]", "\\\"")
        retvalue = retvalue.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
        retvalue = retvalue.replace("[$TEMP_COMMENT_STR$]", comment_temp_str)

        return retvalue, enum_lines, const_lines

    def get_common_prop_lines(self, prop_name, match_str, rule, replace_quote_flag):
        retvalue=[]
        key, other_key, matched = self.findkey(prop_name, match_str, "J")
        if not matched: # key not matched
            if other_key is None:
                comment_str = "//{} [#]rule:%s \n" % rule
            else:
                comment_str = "//{} [#]rule:%s #matched in properties of %s\n" % (rule, other_key)
            retvalue.append(comment_str.format(match_str))
            if replace_quote_flag:
                retvalue.append("{}:\"{}\",\n".format(key, match_str.replace("\"", "\\\"")))
            else:
                retvalue.append("{}:\"{}\",\n".format(key, match_str))
        return retvalue, key