//
// Created by leif on 8/20/2023.
//


#include <iostream>
#include <vector>

#pragma once

using namespace std;

class byteUtil {
public:

    vector<uint8_t> getBytes(const bool &val);
    vector<uint8_t> getBytes(const uint8_t &val);
    vector<uint8_t> getBytes(const uint16_t &val);
    vector<uint8_t> getBytes(const int16_t &val);
    vector<uint8_t> getBytes(const uint32_t val);
    vector<uint8_t> getBytes(const int32_t val);
    vector<uint8_t> getBytes(const uint64_t val);
    vector<uint8_t> getBytes(const int64_t val);
    vector<uint8_t> getBytes(const float &val);
    vector<uint8_t> getBytes(const string &val);

    bool getBool(const vector<uint8_t> &bytes);
    char getChar(const vector<uint8_t> &bytes);
    uint8_t getUInt8(const vector<uint8_t> &bytes);
    uint16_t getUInt16(const vector<uint8_t> &bytes);
    int16_t getInt16(const vector<uint8_t> &bytes);
    uint32_t getUInt32(const vector<uint8_t> &bytes);
    int32_t getInt32(const vector<uint8_t> &bytes);
    int64_t getInt64(const vector<uint8_t> &bytes);
    float getFloat(const vector<uint8_t> &bytes);
    string getString(const vector<uint8_t> &bytes);

    vector<uint8_t> mergeDynamicBytes(const vector<vector<uint8_t>> &bytes);
    vector<vector<uint8_t>> splitDynamicBytes(const vector<uint8_t> &bytes) const;
    vector<vector<uint8_t>> splitBytes(const vector<uint8_t> &bytes, const vector<size_t> &sizeEachSplit);
    vector<uint8_t> mergeBytes(const vector<vector<uint8_t>> &bytes);

};

