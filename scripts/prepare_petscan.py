#!/usr/bin/python
# -*- coding: utf-8 -*-
"""
Reads tests json from src\test\resources\test_data\*.js
Takes 'firstRevision' items, sorts them by revid,
prints them to file in the csv format prepared as
json strings array, used later in "wiki_tools" mocked data
as mocked response of catscan service (petscan).
"""

import argparse
import json
import os
from operator import itemgetter, attrgetter


def main():
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('src',
                        help='path to test json')
    parser.add_argument('out',
                        help='path to print results')
    args = parser.parse_args()
    with open(args.src, "r") as read_file:
        data = json.load(read_file, strict=False)
    items = data['wiki']['firstRevision']
    buf = list(items)
    buf.sort(reverse=True, key=itemgetter('revid'))
    num = 1
    with open(args.out, 'w') as out_file:        
        for item in buf:
            title = item['title'].replace(' ', '_')
            try:
                title_cp866 = title.encode('cp866')
            except UnicodeEncodeError:
                title_cp866 = '?'
            title_utf8 = title.encode('utf8')
            print('{}\t{}\t{}\t\t45204\t20160603134155'.format(
                num, title_cp866, item['revid']))
            out_file.write('"{}\t{}\t{}\t\t45204\t20160603134155",\n'.format(
                num, title_utf8, item['revid']))
            num = num + 1


if __name__ == "__main__":
    main()

