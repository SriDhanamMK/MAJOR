"""Base programs (originals and independently written alternatives) for the
labelled evaluation dataset.

Template markers:
  @INIT@   block of 10 mutually independent statements (reordering target)
  @CHK@    prints a checksum of the INIT block ("chk ..." line)
  @A1(x)   x + 1   | variant: 1 + x
  @A2(x)   x * 2   | variant: x + x
  @AM(a,b) a % b == 0 | variant: a - a / b * b == 0   (non-negative ints)
"""

# split: "dev" bases are used to choose the threshold, "test" bases only to report.

C_PRELUDE = '#include <stdio.h>\n\nstatic int sq(int v) { return v * v; }\n\n'
CPP_PRELUDE = ('#include <iostream>\n#include <vector>\nusing namespace std;\n\n'
               'static int sq(int v) { return v * v; }\n\n')
PY_PRELUDE = 'def sq(v):\n    return v * v\n\n\n'

# --------------------------------------------------------------------- C ----
C = {}

C['sum_squares'] = dict(split='dev', size='small', renames={
    'sum_squares': 'sum_squares', 'arr': 'items', 'n': 'count', 'total': 'acc', 'i': 'idx',
    'data': 'numbers', 'result': 'answer', 'shifted': 'moved', 'k': 'pos'}, fn_renames={'sum_squares': 'accumulate_sq'},
    src='''int sum_squares(int *arr, int n) {
    int total = 0;
    // accumulate squares
    for (int i = 0; i < n; i++) {
        total += arr[i] * arr[i];
    }
    return total;
}

int main() {
    @INIT@
    int data[6] = {4, 8, 15, 16, 23, 42};
    int result = sum_squares(data, 6);
    int shifted = 0;
    for (int k = 0; k < 5; k++) {
        shifted += @A2(data[@A1(k)]);
    }
    printf("r1 %d\\n", result);
    printf("r2 %d\\n", shifted);
    @CHK@
    return 0;
}
''',
    alt='''int sum_squares(int *arr, int n) {
    int s = 0;
    int i = 0;
    while (i < n) {
        s = s + arr[i] * arr[i];
        i = i + 1;
    }
    return s;
}

int main() {
    @INIT@
    int data[6] = {4, 8, 15, 16, 23, 42};
    int result = sum_squares(data, 6);
    int shifted = 0;
    int k = 0;
    while (k < 5) {
        shifted = shifted + data[k + 1] + data[k + 1];
        k = k + 1;
    }
    printf("r1 %d\\n", result);
    printf("r2 %d\\n", shifted);
    @CHK@
    return 0;
}
''')

C['fib'] = dict(split='dev', size='small', renames={
    'fib_iter': 'fib_iter', 'n': 'steps', 'a': 'prev', 'b': 'curr', 'i': 'step', 'next': 'nxt',
    'f10': 'ten', 'f20': 'twenty', 'total': 'running', 'last': 'final_v'}, fn_renames={'fib_iter': 'fibonacci_value'},
    src='''long fib_iter(int n) {
    long a = 0;
    long b = 1;
    for (int i = 0; i < n; i++) {
        long next = a + b;
        a = b;
        b = next;
    }
    return a;
}

int main() {
    @INIT@
    long f10 = fib_iter(10);
    long f20 = fib_iter(20);
    long total = 0;
    // sum of doubled values
    for (int n = 0; n < 8; n++) {
        total += @A2(fib_iter(n));
    }
    printf("r1 %ld\\n", f10);
    printf("r2 %ld\\n", f20);
    printf("r3 %ld\\n", total);
    @CHK@
    return 0;
}
''',
    alt='''long fib_iter(int n) {
    if (n < 2) {
        return n;
    }
    return fib_iter(n - 1) + fib_iter(n - 2);
}

int main() {
    @INIT@
    long f10 = fib_iter(10);
    long f20 = fib_iter(20);
    long total = 0;
    int n = 0;
    while (n < 8) {
        total = total + fib_iter(n) + fib_iter(n);
        n = n + 1;
    }
    printf("r1 %ld\\n", f10);
    printf("r2 %ld\\n", f20);
    printf("r3 %ld\\n", total);
    @CHK@
    return 0;
}
''')

C['primes'] = dict(split='dev', size='medium', renames={
    'is_prime': 'is_prime', 'n': 'value', 'd': 'div', 'count_primes': 'count_primes', 'limit': 'upper',
    'count': 'found', 'p': 'probe', 'c1': 'first_cnt', 'c2': 'second_cnt'},
    fn_renames={'is_prime': 'check_prime', 'count_primes': 'tally_primes'},
    src='''int is_prime(int n) {
    if (n < 2) {
        return 0;
    }
    for (int d = 2; d * d <= n; d++) {
        if (@AM(n, d)) {
            return 0;
        }
    }
    return 1;
}

int count_primes(int limit) {
    int count = 0;
    for (int p = 2; p <= limit; p++) {
        if (p > 1 && is_prime(p)) {
            count += 1;
        }
    }
    return count;
}

int main() {
    @INIT@
    int c1 = count_primes(30);
    int c2 = count_primes(100);
    printf("r1 %d\\n", c1);
    printf("r2 %d\\n", c2);
    printf("r3 %d\\n", is_prime(97));
    @CHK@
    return 0;
}
''',
    alt='''int is_prime(int n) {
    int i = 2;
    int ok = 1;
    if (n < 2) {
        return 0;
    }
    while (i * i <= n) {
        if (n % i == 0) {
            ok = 0;
        }
        i = i + 1;
    }
    return ok;
}

int count_primes(int limit) {
    int count = 0;
    int p = 2;
    while (p <= limit) {
        count = count + is_prime(p);
        p = p + 1;
    }
    return count;
}

int main() {
    @INIT@
    int c1 = count_primes(30);
    int c2 = count_primes(100);
    printf("r1 %d\\n", c1);
    printf("r2 %d\\n", c2);
    printf("r3 %d\\n", is_prime(97));
    @CHK@
    return 0;
}
''')

C['bubble'] = dict(split='dev', size='medium', renames={
    'bubble_sort': 'bubble_sort', 'arr': 'vals', 'n': 'size', 'i': 'pass_no', 'j': 'cursor', 'tv': 'hold',
    'data': 'sample', 'swapped': 'moved', 'out': 'ordered'}, fn_renames={'bubble_sort': 'sort_values'},
    src='''void bubble_sort(int *arr, int n) {
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[@A1(j)]) {
                int tv = arr[j];
                arr[j] = arr[@A1(j)];
                arr[@A1(j)] = tv;
            }
        }
    }
}

int main() {
    @INIT@
    int data[8] = {64, 34, 25, 12, 22, 11, 90, 5};
    bubble_sort(data, 8);
    int out = 0;
    // weighted checksum of the sorted array
    for (int i = 0; i < 8; i++) {
        out += data[i] * (i + 1);
    }
    printf("r1 %d\\n", out);
    printf("r2 %d %d\\n", data[0], data[7]);
    @CHK@
    return 0;
}
''',
    alt='''void bubble_sort(int *arr, int n) {
    for (int i = 0; i < n; i++) {
        int min_idx = i;
        for (int j = i + 1; j < n; j++) {
            if (arr[j] < arr[min_idx]) {
                min_idx = j;
            }
        }
        int tv = arr[i];
        arr[i] = arr[min_idx];
        arr[min_idx] = tv;
    }
}

int main() {
    @INIT@
    int data[8] = {64, 34, 25, 12, 22, 11, 90, 5};
    bubble_sort(data, 8);
    int out = 0;
    for (int i = 0; i < 8; i++) {
        out += data[i] * (i + 1);
    }
    printf("r1 %d\\n", out);
    printf("r2 %d %d\\n", data[0], data[7]);
    @CHK@
    return 0;
}
''')

C['bsearch'] = dict(split='test', size='medium', renames={
    'binary_search': 'binary_search', 'arr': 'sorted_vals', 'n': 'length', 'target': 'wanted', 'lo': 'left',
    'hi': 'right', 'mid': 'middle', 'data': 'table', 'idx': 'found_at', 't': 'attempt', 'hits': 'matches'},
    fn_renames={'binary_search': 'find_index'},
    src='''int binary_search(int *arr, int n, int target) {
    int lo = 0;
    int hi = n - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (arr[mid] == target) {
            return mid;
        }
        if (arr[mid] < target) {
            lo = mid + 1;
        } else {
            hi = mid - 1;
        }
    }
    return -1;
}

int main() {
    @INIT@
    int data[8] = {2, 5, 8, 12, 16, 23, 38, 56};
    int hits = 0;
    // probe several targets
    for (int t = 0; t < 60; t++) {
        int idx = binary_search(data, 8, t);
        if (idx >= 0 && idx < 8) {
            hits += idx + 1;
        }
    }
    printf("r1 %d\\n", hits);
    printf("r2 %d\\n", binary_search(data, 8, 23));
    @CHK@
    return 0;
}
''',
    alt='''int search_range(int *arr, int lo, int hi, int target) {
    if (lo > hi) {
        return -1;
    }
    int mid = (lo + hi) / 2;
    if (arr[mid] == target) {
        return mid;
    }
    if (arr[mid] > target) {
        return search_range(arr, lo, mid - 1, target);
    }
    return search_range(arr, mid + 1, hi, target);
}

int main() {
    @INIT@
    int data[8] = {2, 5, 8, 12, 16, 23, 38, 56};
    int hits = 0;
    for (int t = 0; t < 60; t++) {
        int idx = search_range(data, 0, 7, t);
        if (idx >= 0) {
            hits += idx + 1;
        }
    }
    printf("r1 %d\\n", hits);
    printf("r2 %d\\n", search_range(data, 0, 7, 23));
    @CHK@
    return 0;
}
''')

C['gcd'] = dict(split='test', size='small', renames={
    'gcd': 'gcd', 'a': 'first', 'b': 'second', 'rem': 'leftover', 'lcm': 'lcm', 'x': 'p', 'y': 'q',
    'total': 'sum_all', 'u': 'row', 'v': 'col'}, fn_renames={'gcd': 'greatest_divisor', 'lcm': 'least_multiple'},
    src='''int gcd(int a, int b) {
    while (b != 0) {
        int rem = a % b;
        a = b;
        b = rem;
    }
    return a;
}

int lcm(int a, int b) {
    return a / gcd(a, b) * b;
}

int main() {
    @INIT@
    int total = 0;
    // accumulate gcd and lcm over a small grid
    for (int u = 1; u < 12; u++) {
        for (int v = 1; v < 12; v++) {
            total += @A2(gcd(u, v));
        }
    }
    int x = lcm(12, 18);
    int y = gcd(48, 180);
    if (x > 0 && y > 0) {
        total += x + y;
    }
    printf("r1 %d\\n", total);
    @CHK@
    return 0;
}
''',
    alt='''int gcd(int a, int b) {
    if (b == 0) {
        return a;
    }
    return gcd(b, a % b);
}

int lcm(int a, int b) {
    return a * b / gcd(a, b);
}

int main() {
    @INIT@
    int total = 0;
    int u = 1;
    while (u < 12) {
        int v = 1;
        while (v < 12) {
            total = total + gcd(u, v) + gcd(u, v);
            v = v + 1;
        }
        u = u + 1;
    }
    int x = lcm(12, 18);
    int y = gcd(48, 180);
    if (x > 0 && y > 0) {
        total = total + x + y;
    }
    printf("r1 %d\\n", total);
    @CHK@
    return 0;
}
''')

C['matmul'] = dict(split='test', size='large', renames={
    'multiply': 'multiply', 'a': 'left_m', 'b': 'right_m', 'c': 'prod', 'i': 'row', 'j': 'col', 'k': 'inner',
    'x': 'mat_a', 'y': 'mat_b', 'z': 'mat_c', 'trace': 'diag_sum', 'edge': 'border'},
    fn_renames={'multiply': 'mat_mul'},
    src='''void multiply(int a[3][3], int b[3][3], int c[3][3]) {
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            c[i][j] = 0;
            for (int k = 0; k < 3; k++) {
                c[i][j] += a[i][k] * b[k][j];
            }
        }
    }
}

int main() {
    @INIT@
    int x[3][3] = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
    int y[3][3] = {{9, 8, 7}, {6, 5, 4}, {3, 2, 1}};
    int z[3][3];
    multiply(x, y, z);
    int trace = 0;
    int edge = 0;
    // diagonal and border sums
    for (int i = 0; i < 3; i++) {
        trace += z[i][i];
        for (int j = 0; j < 3; j++) {
            if (i == 0 && j == 0) {
                edge += z[i][j];
            }
        }
    }
    printf("r1 %d\\n", trace);
    printf("r2 %d\\n", edge);
    printf("r3 %d\\n", z[1][2]);
    @CHK@
    return 0;
}
''',
    alt='''void multiply(int a[3][3], int b[3][3], int c[3][3]) {
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            int sum = 0;
            for (int k = 0; k < 3; k++) {
                sum = sum + a[i][k] * b[k][j];
            }
            c[i][j] = sum;
        }
    }
}

int main() {
    @INIT@
    int x[3][3] = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
    int y[3][3] = {{9, 8, 7}, {6, 5, 4}, {3, 2, 1}};
    int z[3][3];
    multiply(x, y, z);
    int trace = z[0][0] + z[1][1] + z[2][2];
    int edge = z[0][0];
    printf("r1 %d\\n", trace);
    printf("r2 %d\\n", edge);
    printf("r3 %d\\n", z[1][2]);
    @CHK@
    return 0;
}
''')

C['palin'] = dict(split='test', size='medium', renames={
    'is_palindrome': 'is_palindrome', 's': 'text', 'len': 'size', 'i': 'head', 'j': 'tail', 'words': 'samples',
    'lens': 'sizes', 'w': 'which', 'count': 'matches'}, fn_renames={'is_palindrome': 'reads_same'},
    src='''int is_palindrome(const char *s, int len) {
    int i = 0;
    int j = len - 1;
    while (i < j) {
        if (s[i] != s[j]) {
            return 0;
        }
        i++;
        j--;
    }
    return 1;
}

int main() {
    @INIT@
    const char *words[5] = {"level", "hello", "noon", "abca", "racecar"};
    int lens[5] = {5, 5, 4, 4, 7};
    int count = 0;
    // count palindromic words
    for (int w = 0; w < 5; w++) {
        if (w >= 0 && is_palindrome(words[w], lens[w])) {
            count += 1;
        }
    }
    printf("r1 %d\\n", count);
    @CHK@
    return 0;
}
''',
    alt='''int is_palindrome(const char *s, int len) {
    for (int i = 0; i < len / 2; i++) {
        if (s[i] != s[len - 1 - i]) {
            return 0;
        }
    }
    return 1;
}

int main() {
    @INIT@
    const char *words[5] = {"level", "hello", "noon", "abca", "racecar"};
    int lens[5] = {5, 5, 4, 4, 7};
    int count = 0;
    for (int w = 0; w < 5; w++) {
        count = count + is_palindrome(words[w], lens[w]);
    }
    printf("r1 %d\\n", count);
    @CHK@
    return 0;
}
''')

# ------------------------------------------------------------------ C++ ----
CPP = {}

CPP['sum_vec'] = dict(split='dev', size='small', renames={
    'sum_squares': 'sum_squares', 'v': 'items', 'total': 'acc', 'i': 'idx', 'data': 'numbers', 'result': 'answer'},
    fn_renames={'sum_squares': 'accumulate_sq'},
    src='''int sum_squares(const vector<int> &v) {
    int total = 0;
    // accumulate squares
    for (int i = 0; i < (int)v.size(); i++) {
        total += v[i] * v[i];
    }
    return total;
}

int main() {
    @INIT@
    vector<int> data = {4, 8, 15, 16, 23, 42};
    int result = sum_squares(data);
    cout << "r1 " << result << endl;
    @CHK@
    return 0;
}
''',
    alt='''int sum_squares(const vector<int> &v) {
    int s = 0;
    for (int x : v) {
        s = s + x * x;
    }
    return s;
}

int main() {
    @INIT@
    vector<int> data = {4, 8, 15, 16, 23, 42};
    int result = sum_squares(data);
    cout << "r1 " << result << endl;
    @CHK@
    return 0;
}
''')

CPP['primes_vec'] = dict(split='dev', size='medium', renames={
    'is_prime': 'is_prime', 'n': 'value', 'd': 'div', 'collect': 'collect', 'limit': 'upper', 'out': 'found',
    'p': 'probe', 'weight': 'score'}, fn_renames={'is_prime': 'check_prime', 'collect': 'gather_primes'},
    src='''bool is_prime(int n) {
    if (n < 2) {
        return false;
    }
    for (int d = 2; d * d <= n; d++) {
        if (@AM(n, d)) {
            return false;
        }
    }
    return true;
}

vector<int> collect(int limit) {
    vector<int> out;
    for (int p = 2; p <= limit; p++) {
        if (p > 1 && is_prime(p)) {
            out.push_back(p);
        }
    }
    return out;
}

int main() {
    @INIT@
    vector<int> primes = collect(60);
    int weight = 0;
    for (int i = 0; i < (int)primes.size(); i++) {
        weight += primes[i] * (i + 1);
    }
    cout << "r1 " << primes.size() << endl;
    cout << "r2 " << weight << endl;
    @CHK@
    return 0;
}
''',
    alt='''bool is_prime(int n) {
    if (n < 2) {
        return false;
    }
    int d = 2;
    while (d * d <= n) {
        if (n % d == 0) {
            return false;
        }
        d = d + 1;
    }
    return true;
}

vector<int> collect(int limit) {
    vector<int> out;
    for (int p = 2; p <= limit; p++) {
        if (is_prime(p)) {
            out.push_back(p);
        }
    }
    return out;
}

int main() {
    @INIT@
    vector<int> primes = collect(60);
    int weight = 0;
    int i = 0;
    while (i < (int)primes.size()) {
        weight = weight + primes[i] * (i + 1);
        i = i + 1;
    }
    cout << "r1 " << primes.size() << endl;
    cout << "r2 " << weight << endl;
    @CHK@
    return 0;
}
''')

CPP['bubble_vec'] = dict(split='test', size='medium', renames={
    'bubble_sort': 'bubble_sort', 'v': 'vals', 'n': 'size', 'i': 'pass_no', 'j': 'cursor', 'tv': 'hold',
    'data': 'sample', 'out': 'ordered'}, fn_renames={'bubble_sort': 'sort_values'},
    src='''void bubble_sort(vector<int> &v) {
    int n = (int)v.size();
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (v[j] > v[@A1(j)]) {
                int tv = v[j];
                v[j] = v[@A1(j)];
                v[@A1(j)] = tv;
            }
        }
    }
}

int main() {
    @INIT@
    vector<int> data = {64, 34, 25, 12, 22, 11, 90, 5};
    bubble_sort(data);
    int out = 0;
    // weighted checksum
    for (int i = 0; i < (int)data.size(); i++) {
        out += data[i] * (i + 1);
    }
    cout << "r1 " << out << endl;
    cout << "r2 " << data[0] << " " << data[7] << endl;
    @CHK@
    return 0;
}
''',
    alt='''void bubble_sort(vector<int> &v) {
    int n = (int)v.size();
    for (int i = 1; i < n; i++) {
        int key = v[i];
        int j = i - 1;
        while (j >= 0 && v[j] > key) {
            v[j + 1] = v[j];
            j = j - 1;
        }
        v[j + 1] = key;
    }
}

int main() {
    @INIT@
    vector<int> data = {64, 34, 25, 12, 22, 11, 90, 5};
    bubble_sort(data);
    int out = 0;
    for (int i = 0; i < (int)data.size(); i++) {
        out += data[i] * (i + 1);
    }
    cout << "r1 " << out << endl;
    cout << "r2 " << data[0] << " " << data[7] << endl;
    @CHK@
    return 0;
}
''')

CPP['gcd_vec'] = dict(split='test', size='small', renames={
    'gcd': 'gcd', 'a': 'first', 'b': 'second', 'rem': 'leftover', 'pairs': 'inputs', 'total': 'sum_all',
    'i': 'pos'}, fn_renames={'gcd': 'greatest_divisor'},
    src='''int gcd(int a, int b) {
    while (b != 0) {
        int rem = a % b;
        a = b;
        b = rem;
    }
    return a;
}

int main() {
    @INIT@
    vector<int> pairs = {48, 180, 12, 18, 17, 5, 100, 75, 81, 27};
    int total = 0;
    // gcd of consecutive entries
    for (int i = 0; i + 1 < (int)pairs.size(); i++) {
        total += @A2(gcd(pairs[i], pairs[@A1(i)]));
    }
    cout << "r1 " << total << endl;
    @CHK@
    return 0;
}
''',
    alt='''int gcd(int a, int b) {
    if (b == 0) {
        return a;
    }
    return gcd(b, a % b);
}

int main() {
    @INIT@
    vector<int> pairs = {48, 180, 12, 18, 17, 5, 100, 75, 81, 27};
    int total = 0;
    int i = 0;
    while (i + 1 < (int)pairs.size()) {
        total = total + gcd(pairs[i], pairs[i + 1]) + gcd(pairs[i], pairs[i + 1]);
        i = i + 1;
    }
    cout << "r1 " << total << endl;
    @CHK@
    return 0;
}
''')

# --------------------------------------------------------------- Python ----
PY = {}

PY['sum_squares'] = dict(split='dev', size='small', renames={
    'values': 'items', 'total': 'acc', 'i': 'idx', 'data': 'numbers', 'result': 'answer', 'shifted': 'moved',
    'k': 'pos'}, fn_renames={'sum_squares': 'accumulate_sq'},
    src='''def sum_squares(values):
    total = 0
    # accumulate squares
    for i in range(len(values)):
        total += values[i] * values[i]
    return total


def main():
    @INIT@
    data = [4, 8, 15, 16, 23, 42]
    result = sum_squares(data)
    shifted = 0
    for k in range(5):
        shifted += @A2(data[@A1(k)])
    print("r1", result)
    print("r2", shifted)
    @CHK@


main()
''',
    alt='''def sum_squares(values):
    s = 0
    i = 0
    while i < len(values):
        s = s + values[i] * values[i]
        i = i + 1
    return s


def main():
    @INIT@
    data = [4, 8, 15, 16, 23, 42]
    result = sum_squares(data)
    shifted = 0
    k = 0
    while k < 5:
        shifted = shifted + data[k + 1] + data[k + 1]
        k = k + 1
    print("r1", result)
    print("r2", shifted)
    @CHK@


main()
''')

PY['fib'] = dict(split='dev', size='small', renames={
    'n': 'steps', 'a': 'prev', 'b': 'curr', 'i': 'step', 'nxt': 'follow', 'f10': 'ten', 'f20': 'twenty',
    'total': 'running'}, fn_renames={'fib_iter': 'fibonacci_value'},
    src='''def fib_iter(n):
    a = 0
    b = 1
    for i in range(n):
        nxt = a + b
        a = b
        b = nxt
    return a


def main():
    @INIT@
    f10 = fib_iter(10)
    f20 = fib_iter(20)
    total = 0
    # sum of doubled values
    for n in range(8):
        total += @A2(fib_iter(n))
    print("r1", f10)
    print("r2", f20)
    print("r3", total)
    @CHK@


main()
''',
    alt='''def fib_iter(n):
    if n < 2:
        return n
    return fib_iter(n - 1) + fib_iter(n - 2)


def main():
    @INIT@
    f10 = fib_iter(10)
    f20 = fib_iter(20)
    total = 0
    n = 0
    while n < 8:
        total = total + fib_iter(n) + fib_iter(n)
        n = n + 1
    print("r1", f10)
    print("r2", f20)
    print("r3", total)
    @CHK@


main()
''')

PY['primes'] = dict(split='dev', size='medium', renames={
    'n': 'value', 'd': 'div', 'limit': 'upper', 'count': 'found', 'p': 'probe', 'c1': 'first_cnt',
    'c2': 'second_cnt'}, fn_renames={'is_prime': 'check_prime', 'count_primes': 'tally_primes'},
    src='''def is_prime(n):
    if n < 2:
        return False
    for d in range(2, n):
        if d * d > n:
            break
        if @AM(n, d):
            return False
    return True


def count_primes(limit):
    count = 0
    for p in range(2, limit + 1):
        if p > 1 and is_prime(p):
            count += 1
    return count


def main():
    @INIT@
    c1 = count_primes(30)
    c2 = count_primes(100)
    print("r1", c1)
    print("r2", c2)
    print("r3", is_prime(97))
    @CHK@


main()
''',
    alt='''def is_prime(n):
    if n < 2:
        return False
    i = 2
    while i * i <= n:
        if n % i == 0:
            return False
        i = i + 1
    return True


def count_primes(limit):
    return sum(1 for p in range(2, limit + 1) if is_prime(p))


def main():
    @INIT@
    c1 = count_primes(30)
    c2 = count_primes(100)
    print("r1", c1)
    print("r2", c2)
    print("r3", is_prime(97))
    @CHK@


main()
''')

PY['bubble'] = dict(split='dev', size='medium', renames={
    'arr': 'vals', 'n': 'size', 'i': 'pass_no', 'j': 'cursor', 'tv': 'hold', 'data': 'sample', 'out': 'ordered'},
    fn_renames={'bubble_sort': 'sort_values'},
    src='''def bubble_sort(arr):
    n = len(arr)
    for i in range(n - 1):
        for j in range(n - i - 1):
            if arr[j] > arr[@A1(j)]:
                tv = arr[j]
                arr[j] = arr[@A1(j)]
                arr[@A1(j)] = tv


def main():
    @INIT@
    data = [64, 34, 25, 12, 22, 11, 90, 5]
    bubble_sort(data)
    out = 0
    # weighted checksum of the sorted list
    for i in range(8):
        out += data[i] * (i + 1)
    print("r1", out)
    print("r2", data[0], data[7])
    @CHK@


main()
''',
    alt='''def bubble_sort(arr):
    n = len(arr)
    for i in range(n):
        min_idx = i
        for j in range(i + 1, n):
            if arr[j] < arr[min_idx]:
                min_idx = j
        tv = arr[i]
        arr[i] = arr[min_idx]
        arr[min_idx] = tv


def main():
    @INIT@
    data = [64, 34, 25, 12, 22, 11, 90, 5]
    bubble_sort(data)
    out = 0
    for i in range(8):
        out += data[i] * (i + 1)
    print("r1", out)
    print("r2", data[0], data[7])
    @CHK@


main()
''')

PY['bsearch'] = dict(split='test', size='medium', renames={
    'arr': 'sorted_vals', 'n': 'length', 'target': 'wanted', 'lo': 'left', 'hi': 'right', 'mid': 'middle',
    'data': 'table', 'idx': 'found_at', 't': 'attempt', 'hits': 'matches'},
    fn_renames={'binary_search': 'find_index'},
    src='''def binary_search(arr, n, target):
    lo = 0
    hi = n - 1
    while lo <= hi:
        mid = lo + (hi - lo) // 2
        if arr[mid] == target:
            return mid
        if arr[mid] < target:
            lo = mid + 1
        else:
            hi = mid - 1
    return -1


def main():
    @INIT@
    data = [2, 5, 8, 12, 16, 23, 38, 56]
    hits = 0
    # probe several targets
    for t in range(60):
        idx = binary_search(data, 8, t)
        if idx >= 0 and idx < 8:
            hits += idx + 1
    print("r1", hits)
    print("r2", binary_search(data, 8, 23))
    @CHK@


main()
''',
    alt='''def search_range(arr, lo, hi, target):
    if lo > hi:
        return -1
    mid = (lo + hi) // 2
    if arr[mid] == target:
        return mid
    if arr[mid] > target:
        return search_range(arr, lo, mid - 1, target)
    return search_range(arr, mid + 1, hi, target)


def main():
    @INIT@
    data = [2, 5, 8, 12, 16, 23, 38, 56]
    hits = 0
    for t in range(60):
        idx = search_range(data, 0, 7, t)
        if idx >= 0:
            hits += idx + 1
    print("r1", hits)
    print("r2", search_range(data, 0, 7, 23))
    @CHK@


main()
''')

PY['gcd'] = dict(split='test', size='small', renames={
    'a': 'first', 'b': 'second', 'rem': 'leftover', 'x': 'p', 'y': 'q', 'total': 'sum_all', 'u': 'row',
    'v': 'col'}, fn_renames={'gcd': 'greatest_divisor', 'lcm': 'least_multiple'},
    src='''def gcd(a, b):
    while b != 0:
        rem = a % b
        a = b
        b = rem
    return a


def lcm(a, b):
    return a // gcd(a, b) * b


def main():
    @INIT@
    total = 0
    # accumulate gcd over a small grid
    for u in range(1, 12):
        for v in range(1, 12):
            total += @A2(gcd(u, v))
    x = lcm(12, 18)
    y = gcd(48, 180)
    if x > 0 and y > 0:
        total += x + y
    print("r1", total)
    @CHK@


main()
''',
    alt='''def gcd(a, b):
    if b == 0:
        return a
    return gcd(b, a % b)


def lcm(a, b):
    return a * b // gcd(a, b)


def main():
    @INIT@
    total = 0
    u = 1
    while u < 12:
        v = 1
        while v < 12:
            total = total + gcd(u, v) + gcd(u, v)
            v = v + 1
        u = u + 1
    x = lcm(12, 18)
    y = gcd(48, 180)
    if x > 0 and y > 0:
        total = total + x + y
    print("r1", total)
    @CHK@


main()
''')

PY['matmul'] = dict(split='test', size='large', renames={
    'a': 'left_m', 'b': 'right_m', 'c': 'prod', 'i': 'row', 'j': 'col', 'k': 'inner', 'x': 'mat_a', 'y': 'mat_b',
    'z': 'mat_c', 'trace': 'diag_sum', 'edge': 'border', 'size': 'dim'}, fn_renames={'multiply': 'mat_mul'},
    src='''def multiply(a, b, size):
    c = [[0] * size for _ in range(size)]
    for i in range(size):
        for j in range(size):
            for k in range(size):
                c[i][j] += a[i][k] * b[k][j]
    return c


def main():
    @INIT@
    x = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
    y = [[9, 8, 7], [6, 5, 4], [3, 2, 1]]
    z = multiply(x, y, 3)
    trace = 0
    edge = 0
    # diagonal and corner sums
    for i in range(3):
        trace += z[i][i]
        for j in range(3):
            if i == 0 and j == 0:
                edge += z[i][j]
    print("r1", trace)
    print("r2", edge)
    print("r3", z[1][2])
    @CHK@


main()
''',
    alt='''def multiply(a, b, size):
    c = []
    for i in range(size):
        row = []
        for j in range(size):
            s = 0
            for k in range(size):
                s = s + a[i][k] * b[k][j]
            row.append(s)
        c.append(row)
    return c


def main():
    @INIT@
    x = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
    y = [[9, 8, 7], [6, 5, 4], [3, 2, 1]]
    z = multiply(x, y, 3)
    trace = z[0][0] + z[1][1] + z[2][2]
    edge = z[0][0]
    print("r1", trace)
    print("r2", edge)
    print("r3", z[1][2])
    @CHK@


main()
''')

PY['palin'] = dict(split='test', size='medium', renames={
    's': 'text', 'n': 'size', 'i': 'head', 'j': 'tail', 'words': 'samples', 'w': 'which', 'count': 'matches'},
    fn_renames={'is_palindrome': 'reads_same'},
    src='''def is_palindrome(s, n):
    i = 0
    j = n - 1
    while i < j:
        if s[i] != s[j]:
            return False
        i += 1
        j -= 1
    return True


def main():
    @INIT@
    words = ["level", "hello", "noon", "abca", "racecar"]
    count = 0
    # count palindromic words
    for w in range(5):
        if w >= 0 and is_palindrome(words[w], len(words[w])):
            count += 1
    print("r1", count)
    @CHK@


main()
''',
    alt='''def is_palindrome(s, n):
    for i in range(n // 2):
        if s[i] != s[n - 1 - i]:
            return False
    return True


def main():
    @INIT@
    words = ["level", "hello", "noon", "abca", "racecar"]
    count = 0
    for w in range(5):
        count = count + int(is_palindrome(words[w], len(words[w])))
    print("r1", count)
    @CHK@


main()
''')
