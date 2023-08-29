
#include "lib/system.h"
#include "tests/test_all.cpp"

using namespace std;

int main() {

    auto ms = system::currentTime();

    test_all();

    return 0;
}
