#!/usr/bin/env python
# -*- coding: utf-8 -*-

JAVA_BIN = '/System/Library/Frameworks/JavaVM.framework/Versions/Current'
UNICODE_2_UTF8_CMD = 'native2ascii -reverse -encoding utf-8 {} {}'
UTF8_2_UNICODE_CMD = 'native2ascii {} {}'
JAVA_COMMON_ENUM_CLASSNAME = 'com.rydeen.boh.core.i18n.Trans4Java'
JAVA_COMMON_ENUM_PARAM_PREFIX = 'PROP'