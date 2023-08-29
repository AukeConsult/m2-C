//
// Created by leif on 8/27/2023.
//

#ifndef TEST_PACK_H
#define TEST_PACK_H

#include "byteUtil.h"

class pack {

protected:
    byteUtil * util;

public:

    explicit pack(byteUtil * util) {
        this->util=util;
    }

    virtual void toObject(const vector<uint8_t> &bytes) = 0;
    virtual vector<uint8_t> toBytes() = 0;
};


#endif //TEST_PACK_H
