#!/usr/bin/env python
# -*- coding: utf-8 -*-

JAVA_BIN = '/System/Library/Frameworks/JavaVM.framework/Versions/Current'
UNICODE_2_UTF8_CMD = 'native2ascii -reverse -encoding utf-8 {} {}'
UTF8_2_UNICODE_CMD = 'native2ascii {} {}'
JAVA_COMMON_ENUM_CLASSNAME = 'com.rydeen.boh.core.i18n.Trans4Java'
JAVA_COMMON_ENUM_PARAM_PREFIX = 'L'
JAVA_COMMON_4_I18N_ENUM_CLASSNAME = 'com.rydeen.i18n.M4J'
JAVA_COMMON_4_I18N_ENUM_OUTPUT_FILENAME = 'message4i18nInJava_en_US.properties'
JAVA_COMMON_4_I18N_CONST_PACKAGE_NAME = 'com.rydeen.boh.fund.i18n'
JAVA_COMMON_4_I18N_CONST_OUTPUT_FILENAME = 'C4J.java'
JAVA_COMMON_4_I18N_JSP_ENUM_OUTPUT_FILENAME = 'message4i18nInJsp_en_US.properties'
JAVA_COMMON_4_I18N_COMMON_ENUM_OUTPUT_FILENAME = 'message4i18nCommon_en_US.properties'
JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_FILENAME = 'message4i18nInJs_en_US.js'
JAVA_COMMON_4_I18N_JS_ENUM_OUTPUT_PREFIX = 'M4JS'
JAVA_COMMON_4_I18N_JSP_FMT_OUTPUT_FILENAME = 'message4EL.jsp'
