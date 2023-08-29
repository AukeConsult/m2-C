//
// Created by leif on 8/20/2023.
//

#include <chrono>
#include "system.h"

int64_t system::currentTime() {
    return std::chrono::duration_cast<std::chrono::milliseconds>
            (std::chrono::system_clock::now().time_since_epoch()).count();
}
