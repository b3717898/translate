#!/usr/bin/env python
# -*- coding: utf-8 -*-

from copy import deepcopy
from trans_sourcemap import *
from trans_state import *
import re

try:
    import psyco
    psyco.full()
except:
    pass


class Translator(object):
    def __init__(self, to_encoding):
        self.to_encoding = to_encoding
        self.map = MAPS[to_encoding]
        self.result = UEMPTY

    def fetch(self, char):
        self.first_state.append(char, self.map)
        sub_states = []
        for st in self.states:
            new = st.find(char,self.map)
            if new:
                sub_states.append(new)
        if sub_states:
            self.states.extend(sub_states)



        # for st in self.states:
        #     if st.state == PENDING:
        #         new_state = st.find(char, self.map)
        #     if new_state:
        #         self.states.append(new_state)
        # self.states = [st for st in self.states if st.state != FAIL]
        self.states = [st for st in self.states if st.state != FAIL]
        all_ok = True
        for st in self.states:
            if st.state != END:
                all_ok = False
                break
        if all_ok:
            self._clean()
        return self.get_result()

    def _clean(self):
        if len(self.states):
            self.states.sort(key=lambda x: len(x))
            self.result += self.states[0].final
        else:
            self.result += self.first_state.final

        self.states = [TransState()]
        self.first_state = TransState()

    def start(self):
        self.states = [TransState()]
        self.first_state = TransState()
        self.result = UEMPTY

    def end(self):
        self.states = [st for st in self.states
                if st.state == END]
        self._clean()

    def convert(self, string):
        self.start()
        for char in string:
            self.fetch(char)
        self.end()
        return self.get_result()

    def get_result(self):
        return self.result

