//
// Created by leif on 8/27/2023.
//

#ifndef TEST_TESTCLASSES_H
#define TEST_TESTCLASSES_H

#include <vector>
#include <string>
#include "byteUtil.h"
#include "pack.h"

using namespace std;

class testclass1 : pack {
public:

    explicit testclass1(byteUtil *util) : pack (util){}
    explicit testclass1(byteUtil *util, int x) : pack (util){}

    int a=4;
    int b=123123;
    string test = "asdasdasdasd";

    void toObject(const vector<uint8_t> &bytes) override  {
        vector<vector<uint8_t>> res = util->splitDynamicBytes(bytes);
        a = util->getInt32(res[0]);
        b= util->getInt32(res[1]);
        test = util->getString(res[2]);
    };

    vector<uint8_t> toBytes() override {
        vector<uint8_t> merged =
                util->mergeDynamicBytes({
                                               util->getBytes(a),
                                               util->getBytes(b),
                                               util->getBytes(test)
                                       });
        return merged;
    };

};


class testclass2 : pack {
public:

    explicit testclass2(byteUtil *util ):
        pack (util),t1(util), t2(util)
    {}

    int a=4;
    testclass1 t1;
    int b=123123;
    string test = "asdasdasdasd";
    testclass1 t2;

    void toObject(const vector<uint8_t> &bytes) override  {
        vector<vector<uint8_t>> res = util->splitDynamicBytes(bytes);
        a = util->getInt32(res[0]);
        t1.toObject(res[1]);
        b= util->getInt32(res[2]);
        test = util->getString(res[3]);
        t2.toObject(res[4]);
    };

    vector<uint8_t> toBytes() override  {
        vector<uint8_t> merged =
                util->mergeDynamicBytes(
                        {
                           util->getBytes(a),
                           t1.toBytes(),
                           util->getBytes(b),
                           util->getBytes(test),
                           t2.toBytes()
                        });
        return merged;
    };
};

#endif //TEST_TESTCLASSES_H
