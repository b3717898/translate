#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import sys
reload(sys)
sys.setdefaultencoding('utf8')
from config.config import *
from abstractinterpretor import *


class Jsp4i18nInterpretor(AbstractInterpretor):

    def _convertline(self, line, file_name):
        retvalue = line
        retvalue = unicode(retvalue)

        it_comment = re.finditer(self.comment_words, retvalue)
        comment_temp_str = ""
        for match in it_comment:
            comment_temp_str = match.group()
            retvalue = retvalue.replace(match.group(), "[$TEMP_COMMENT_STR$]")


        it_comment_comment = re.finditer(self.comment_comment_words, retvalue)
        comment_comment_temp_str = ""
        for match in it_comment_comment:
            comment_comment_temp_str = match.group()
            retvalue = retvalue.replace(match.group(), "[$TEMP_COMMENT_COMMENT_STR$]")

        const_lines = []
        # it_case = re.finditer(self.chinese_words_with_CASE, retvalue)
        # for match in it_case:
        #     prop_name = "{}_L{}".format(file_name, str(self.line))
        #     replace_str = "case {}.{}.{}".format(JAVA_COMMON_4_I18N_CONST_PACKAGE_NAME,
        #                                     JAVA_COMMON_4_I18N_CONST_OUTPUT_FILENAME.replace(".java", ""),
        #                                     prop_name)
        #     retvalue = retvalue.replace(match.group(), replace_str)
        #     match_str = match.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
        #     const_lines.append("    /** {} */\n".format(match_str))
        #     const_lines.append("    String {} = {};\n".format(prop_name, match_str.replace("case ", "")))

        it = re.finditer(self.gt_dollar_words, retvalue)  # 1 检查>和${之间的>xxx< for <h4>订单号:<abc>${result.stor
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
                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "1")
                replace_str_sub = "><fmt:message key=\"%s\" bundle=\"${lang}\"/><" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1

            it_sub = re.finditer(self.chinese_without_gt_words, retvalue_sub)  #   2 检查>和${之间的"xxx"
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

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "2")
                replace_str_sub = "\"<fmt:message key='%s' bundle='${lang}'/>\"" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)


        it = re.finditer(self.gt_dollar_words, retvalue)  # 3 检查>和${之间的 本尊内容 for <h4>订单号:${result.stor
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
            match_str = match_str[1:-2]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "3")
            replace_str = "><fmt:message key=\"%s\" bundle=\"${lang}\"/>${" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.dollar_dollar_words,
                         retvalue)  # 4.0 检查}和${之间的"xxx" for storeOrderInfoBean.orderCode} &nbsp;&nbsp;订货日期： ${/dfdfdfd>
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.chinese_words, retvalue_sub)
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

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.0")
                replace_str_sub = "\"<fmt:message key='%s' bundle='${lang}'/>\"" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.dollar_dollar_words, retvalue)  # 4.1 检查}和${之间的本尊 for storeOrderInfoBean.orderCode} &nbsp;&nbsp;订货日期： ${/dfdfdfd>
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
            match_str = match_str[1:-2]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "4.1")
            replace_str = "}<fmt:message key=\"%s\" bundle=\"${lang}\"/>${" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.dollar_lt_words,
                         retvalue)  # 5 检查}和<之间的>xxx< for fo.status == 1}">已发布</c:if>
        for match in it:
            # print (match.group())
            retvalue_sub = match.group()
            it_sub = re.finditer(self.gtlt_words, retvalue_sub)
            for match_sub in it_sub:
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "5")
                replace_str_sub = "><fmt:message key=\"%s\" bundle=\"${lang}\"/><" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.chinese_words, retvalue_sub)  #  6 检查}和<之间的"xxx"
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

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "6")
                replace_str_sub = "\"<fmt:message key='%s' bundle='${lang}'/>\"" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        it = re.finditer(self.dollar_lt_words,
                         retvalue)  # 7 检查}和<之间的本尊 for displayName }  已确认调入接收完成！ </span>
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "7")
            replace_str = "}<fmt:message key=\"%s\" bundle=\"${lang}\"/><" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.gtlt_words, retvalue) # 8 检查>和<之间的本尊 for >xxxxxx<
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "8")
            replace_str = "><fmt:message key=\"%s\" bundle=\"${lang}\"/><" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.gt_gt_words, retvalue)  # 8.1 检查>和>之间的本尊 for >xxxxxx>
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "8")
            replace_str = "><fmt:message key=\"%s\" bundle=\"${lang}\"/>>" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.gt_gt_words, retvalue)  # 8.11 检查>和>之间的本尊 for >xxxxxx>  第二次（防止吃>掉）
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "8.1")
            replace_str = "><fmt:message key=\"%s\" bundle=\"${lang}\"/>>" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.tab_lt_words, retvalue)  # 8.2      吃饭<
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "8.2")
            replace_str = "    <fmt:message key=\"%s\" bundle=\"${lang}\"/><" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.gt_nl_words, retvalue)  # 8.3      >吃饭\n
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "8.3")
            replace_str = "><fmt:message key=\"%s\" bundle=\"${lang}\"/>\r" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.squote_words, retvalue)  # 9.0 检查'和'之间的'xxx"xx"xxx' for 'xx"xx"xx'
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.chinese_words, retvalue_sub)
            for match_sub in it_sub:
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines,key = self.get_common_prop_lines(prop_name, match_str, "9.0")
                replace_str_sub = "\"<fmt:message key='%s' bundle='${lang}'/>\"" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.slash_quote_words, retvalue_sub)  # 9.05 检查'和'之间的'xxx\"从发\"dfdf'
            for match_sub in it_sub:
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[2:-2]
                prop_lines,key = self.get_common_prop_lines(prop_name, match_str, "9.05")
                replace_str_sub = "\\\"<fmt:message key='%s' bundle='${lang}'/>\\\"" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.squote_dollar_words, retvalue_sub)   # 9.1 检查'和'之间的'xxx${xxx'
            for match_sub in it_sub:
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-2]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "9.1")
                replace_str_sub = "'<fmt:message key=\"%s\" bundle=\"${lang}\"/>${" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.dollar_squote_words, retvalue_sub)  # 9.2 检查'和'之间的'xxxx}xx'
            for match_sub in it_sub:
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "9.2")
                replace_str_sub = "}<fmt:message key=\"%s\" bundle=\"${lang}\"/>'" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)


        retvalue = retvalue.replace("\\\'", "[$TEMP_SQUOTE_STR$]")
        it = re.finditer(self.squote_words, retvalue)  # 10 检查'和'之间的本尊  for 'xxxxxx'
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "10")
            replace_str = "'<fmt:message key=\"%s\" bundle=\"${lang}\"/>'" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.chinese_words, retvalue)  #
        for match in it:
            retvalue_sub = match.group()
            it_sub = re.finditer(self.quote_dollar_words, retvalue_sub)  # 11.0 检查"和"之间的"xxx${xxxx"
            for match_sub in it_sub:
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-2]
                prop_lines,key = self.get_common_prop_lines(prop_name, match_str, "11.0")
                replace_str_sub = "\"<fmt:message key='%s' bundle='${lang}'/>${" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            it_sub = re.finditer(self.dollar_quote_words, retvalue_sub)  # 11.1 检查"和"之间的"xxx}xxxx"
            for match_sub in it_sub:
                prop_name = "{}_L{}_{}".format(file_name, str(self.line), str(index))
                # file_name + "_" + JAVA_COMMON_ENUM_PARAM_PREFIX + "_" + str(self.index)
                # print match.group()
                if prop_name in self.prop_name:
                    prop_name = prop_name + "_" + str(self.globe_count)
                    self.globe_count += 1
                self.prop_name[prop_name] = "1"

                match_str = match_sub.group().replace("[$TEMP_QUOTE_STR$]", "\\\"")
                match_str = match_str.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
                match_str = match_str[1:-1]
                prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "11.1")
                replace_str_sub = "}<fmt:message key='%s' bundle='${lang}'/>\"" % key
                retvalue_sub = retvalue_sub.replace(match_sub.group(), replace_str_sub)
                enum_lines.extend(prop_lines)
                index += 1
            retvalue = retvalue.replace(match.group(), retvalue_sub)

        retvalue = retvalue.replace("\\\"", "[$TEMP_QUOTE_STR$]")
        it = re.finditer(self.chinese_words, retvalue)  # 12 检查"和"之间的本尊 for "xxxxxx"
        for match in it:
            if "微软雅黑" in match.group():
                continue
            if "<!--" in match.group():
                continue
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
            prop_lines,key = self.get_common_prop_lines(prop_name, match_str, "12")
            replace_str = "\"<fmt:message key='%s' bundle='${lang}'/>\"" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.tab_dollar_words, retvalue)  # 13      吃饭${
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
            match_str = match_str[1:-2]
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "13")
            replace_str = "    <fmt:message key=\"%s\" bundle=\"${lang}\"/>${" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        it = re.finditer(self.dollar_nl_words, retvalue)  # 13.1      }吃饭\n
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
            prop_lines, key = self.get_common_prop_lines(prop_name, match_str, "13.1")
            replace_str = "}<fmt:message key=\"%s\" bundle=\"${lang}\"/>\r" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1


        it = re.finditer(self.tab_nl_words, retvalue)  # 14       吃饭\n
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
            prop_lines,key = self.get_common_prop_lines(prop_name, match_str, "14")
            replace_str = "    <fmt:message key=\"%s\" bundle=\"${lang}\"/>\r" % key
            retvalue = retvalue.replace(match.group(), replace_str)
            enum_lines.extend(prop_lines)
            index += 1

        retvalue = retvalue.replace("[$TEMP_QUOTE_STR$]", "\\\"")
        retvalue = retvalue.replace("[$TEMP_SQUOTE_STR$]", "\\\'")
        retvalue = retvalue.replace("[$TEMP_COMMENT_STR$]", comment_temp_str)
        retvalue = retvalue.replace("[$TEMP_COMMENT_COMMENT_STR$]", comment_comment_temp_str)

        return retvalue, enum_lines, const_lines

    def get_common_prop_lines(self, prop_name, match_str, rule):
        retvalue=[]
        key, other_key, matched = self.findkey(prop_name, match_str.encode('unicode_escape'), "P")
        if not matched: # key not matched
            if other_key is None:
                comment_str = "#{} [#]rule:%s \n" % rule
            else:
                comment_str = "#{} [#]rule:%s #matched in JS of %s\n" % (rule, other_key)
            retvalue.append(comment_str.format(match_str))
            retvalue.append("{} = {}\n".format(key, match_str.encode('unicode_escape')))
        return retvalue, key