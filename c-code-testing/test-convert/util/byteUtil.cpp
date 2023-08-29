//
// Created by leif on 8/28/2023.
//

#include <iostream>
#include <vector>

#include "byteUtil.h"

vector<uint8_t> byteUtil::getBytes(const bool &val){
    vector<uint8_t> result(1);
    uint8_t v = val ? 1 : 0;
    memcpy(result.data(), &v, 1);
    return result;
}

bool byteUtil::getBool(const vector<uint8_t> &bytes){
    uint8_t result=0;
    memcpy(&result, bytes.data(), 1);
    return result == 1;
}
vector<uint8_t> byteUtil::getBytes(const uint8_t &val){
    vector<uint8_t> result(1);
    memcpy(result.data(), &val, 1);
    return result;
}
char byteUtil::getChar(const vector<uint8_t> &bytes){
    char result = 0;
    memcpy(&result, bytes.data(), 1);
    return result;
}
uint8_t byteUtil::getUInt8(const vector<uint8_t> &bytes) {return (uint8_t)getChar(bytes);}

vector<uint8_t> byteUtil::getBytes(const uint16_t &val){
    vector<uint8_t> result(2);
    memcpy(result.data(), &val, 2);
    return result;
}
vector<uint8_t> byteUtil::getBytes(const int16_t &val){return getBytes((uint16_t)val);}

uint16_t byteUtil::getUInt16(const vector<uint8_t> &bytes) {
    uint16_t result;
    memcpy(&result, bytes.data(), bytes.size());
    return result;
}
int16_t byteUtil::getInt16(const vector<uint8_t> &bytes) {return (int16_t)byteUtil::getUInt16(bytes);}

vector<uint8_t> byteUtil::getBytes(const uint32_t val){
    vector<uint8_t> result(4);
    memcpy(result.data(), &val, 4);
    return result;
}
vector<uint8_t> byteUtil::getBytes(const int32_t val){return getBytes((uint32_t)val);}

uint32_t byteUtil::getUInt32(const vector<uint8_t> &bytes) {
    uint32_t result;
    memcpy(&result, bytes.data(), 4);
    return result;
}
int32_t byteUtil::getInt32(const vector<uint8_t> &bytes) {return (int32_t)getUInt32(bytes);}

vector<uint8_t> byteUtil::getBytes(const uint64_t val){
    vector<uint8_t> result(8);
    memcpy(result.data(), &val, 8);
    return result;
}
vector<uint8_t> byteUtil::getBytes(const int64_t val){return getBytes((uint64_t)val);}

uint64_t getUInt64(const vector<uint8_t> &bytes) {
    uint64_t result;
    memcpy(&result, bytes.data(), bytes.size());
    return result;
}
int64_t byteUtil::getInt64(const vector<uint8_t> &bytes) {return (int64_t)getUInt64(bytes);}

vector<uint8_t> byteUtil::getBytes(const float &val){
    vector<uint8_t> result(4);
    memcpy(result.data(), &val, 4);
    return result;
}
float byteUtil::getFloat(const vector<uint8_t> &bytes){
    float result=0;
    memcpy(&result, bytes.data(), 4);
    return result;
}

vector<uint8_t> byteUtil::getBytes(const string &val){
    vector<uint8_t> result(val.length());
    copy(val.c_str(), val.c_str()+val.length(), result.begin());
    return result;
}
string byteUtil::getString(const vector<uint8_t> &bytes) {
    string result(bytes.begin(),bytes.end());
    return result;
}

vector<uint8_t> byteUtil::mergeDynamicBytes(const vector<vector<uint8_t>> &bytes){
    vector<uint8_t> result=vector<uint8_t>(0);

    for (vector<uint8_t> array : bytes) {

        if (array.empty()) {
            array = vector<uint8_t>(0);
        }

        size_t size = array.size();
        vector<uint8_t> array_val(array.size()+4);
        memcpy(array_val.data(), &size, 4);
        memcpy(array_val.data()+4, array.data(), size);

        vector<uint8_t> result_new(result.size()+array_val.size());
        std::copy(result.begin(), result.end(), result_new.begin());
        std::copy(array_val.begin(), array_val.end(), result_new.begin()+(int32_t )result.size());
        result = result_new;

    }
    return result;

}

[[nodiscard]] vector<vector<uint8_t>> byteUtil::splitDynamicBytes(const vector<uint8_t> &bytes) const {
    vector<vector<uint8_t>> result;
    size_t index=0;
    while(index<bytes.size()) {
        uint32_t len;
        memcpy(&len,bytes.data()+index,4);
        vector<uint8_t> val(len);
        memcpy(val.data(),bytes.data()+index+4,len);
        index+=len+4;
        result.push_back(val);
    }
    return result;
}

vector<uint8_t> byteUtil::mergeBytes(const vector<vector<uint8_t>> &bytes){
    vector<uint8_t> result;
    for (auto array : bytes) {
        if (array.empty()) {
            array = vector<uint8_t>(0);
        }
        result.insert(result.end(),array.begin(),array.end());
    }
    return result;
}

vector<vector<uint8_t>> byteUtil::splitBytes(const vector<uint8_t> &bytes, const vector<size_t> &sizeEachSplit) {
    vector<vector<uint8_t>> result;
    size_t size = bytes.size();
    size_t index = 0;
    for (uint32_t len : sizeEachSplit) {

        if (len >= size - index)
            len = size - index;

        vector<uint8_t> sub(len);
        memcpy(sub.data(),bytes.data()+index,len);
        result.push_back(sub);
        index += len;
        if (index >= size)
            break;
    }
    return result;
}