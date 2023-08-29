//
// Created by leif on 8/27/2023.
//

#include <iostream>
#include "../util/byteUtil.h"
#include "../util/testclasses.h"

using namespace std;

inline void test_mergedynamic() {

    byteUtil butil;
    string leif = "erweerwe";
    auto merged = butil.mergeDynamicBytes(
            {
                    butil.getBytes(true),
                    butil.getBytes(10000),
                    butil.getBytes(123.33f),
                    butil.getBytes((int64_t)912312312319),
                    butil.getBytes(leif),
                    butil.getBytes(string("this is a test"))
            });

    auto split = butil.splitDynamicBytes(merged);

    cout << "val 1 " << butil.getBool(split[0]) << endl;
    cout << "val 2 " << butil.getInt32(split[1]) << endl;
    cout << "val 3 " << butil.getFloat(split[2]) << endl;
    cout << "val 4 " << butil.getInt64(split[3]) << endl;
    cout << "val 5 " << butil.getString(split[4]) << endl;
    cout << "val 6 " << butil.getString(split[5]) << endl;

}


inline void test_merge() {
    byteUtil butil;

    string leif = "asdasdasdasd";
    auto merged = butil.mergeBytes(
            {
                    butil.getBytes(true),
                    butil.getBytes(10000),
                    butil.getBytes(123.33f),
                    butil.getBytes((int64_t)912312312319),
                    butil.getBytes(leif),
                    butil.getBytes(string("this is a test"))
            });

    auto split = butil.splitBytes(merged,{1,4,4,8,12,24});

    cout << "val 1 " << butil.getBool(split[0]) << endl;
    cout << "val 2 " << butil.getInt32(split[1]) << endl;
    cout << "val 3 " << butil.getFloat(split[2]) << endl;
    cout << "val 4 " << butil.getInt64(split[3]) << endl;
    cout << "val 5 " << butil.getString(split[4]) << endl;
    cout << "val 6 " << butil.getString(split[5]) << endl;

}

inline void test_class1() {

    byteUtil butil;

    testclass1 t1(&butil);
    testclass1 t2(&butil);

    t1.a= 10000;
    t1.b= 20000;
    t1.test="test1";
    t2.toObject(t1.toBytes());
    if(t1.toBytes()==t2.toBytes()) {
        cout << "t1 and t2 equal"  << endl;
    }

}

inline void test_class2() {

    byteUtil butil;
    testclass2 tx1(&butil);
    testclass2 tx2(&butil);

    tx1.a= 10000;
    tx1.b= 20000;
    tx1.test="test1";

    tx1.t1.a=12332;
    tx1.t1.b=123;
    tx1.t1.test="xx12dasd asdasdas  asdasdasda";

    tx1.t2.a=912;
    tx1.t2.b=9123;
    tx1.t2.test="999xx1 sadasdasd";

    tx2.toObject(tx1.toBytes());

    if(tx2.toBytes()==tx1.toBytes()) {
        cout << "tx1 and tx2 equal"  << endl;
    }

}


inline void test_all() {

    test_mergedynamic();
    test_merge();
    test_class1();
    test_class2();

}
