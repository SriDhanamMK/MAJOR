/**
 * Realistic source code comparison example pairs for C, C++, and Python.
 * These are genuine valid programs illustrating various structural transformations.
 */

export const EXAMPLE_PAIRS = {
  C: [
    {
      id: 'c-identical',
      name: 'Identical Programs',
      description: 'Two identical C functions calculating factorial. Expected: 100% similarity.',
      sourceA: `int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}`,
      sourceB: `int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}`
    },
    {
      id: 'c-renamed',
      name: 'Renamed Variables & Parameters',
      description: 'Consistent local identifier canonicalization (x -> param_0, total -> var_0). Expected: 100% similarity.',
      sourceA: `int compute_sum(int count, int multiplier) {
    int total = 0;
    for (int i = 0; i < count; i++) {
        total += i * multiplier;
    }
    return total;
}`,
      sourceB: `int compute_sum(int n_elements, int factor) {
    int accumulator = 0;
    for (int idx = 0; idx < n_elements; idx++) {
        accumulator += idx * factor;
    }
    return accumulator;
}`
    },
    {
      id: 'c-formatting',
      name: 'Formatting & Comments',
      description: 'Different indentation, extra whitespace, and arbitrary comments. Expected: 100% similarity.',
      sourceA: `int max(int a, int b) {
    if (a > b) return a;
    return b;
}`,
      sourceB: `// Function to find maximum of two values
int   max  (  int   a ,   int   b ) 
{
    /* Check if first number is strictly greater */
    if ( a > b ) {
        return a;
    }

    // Otherwise return second
    return b;
}`
    },
    {
      id: 'c-safe-reorder',
      name: 'Safe Independent Statement Reordering',
      description: 'Disjoint local variable initializations with no dataflow hazards. Expected: High similarity.',
      sourceA: `void init_system() {
    int max_retries = 3;
    int timeout_sec = 30;
    int buffer_size = 1024;
}`,
      sourceB: `void init_system() {
    int buffer_size = 1024;
    int timeout_sec = 30;
    int max_retries = 3;
}`
    },
    {
      id: 'c-unsafe-reorder',
      name: 'Unsafe Dependent Statement Reordering',
      description: 'RAW data dependency hazard (reading a variable before assignment). Reordering disallowed.',
      sourceA: `int calculate_offset(int base) {
    int offset = base * 2;
    int result = offset + 10;
    return result;
}`,
      sourceB: `int calculate_offset(int base) {
    int result = offset + 10;
    int offset = base * 2;
    return result;
}`
    },
    {
      id: 'c-changed-literals',
      name: 'Changed Numeric Literals',
      description: 'Identical control flow with modified constant threshold numbers.',
      sourceA: `int check_status(int code) {
    if (code >= 200 && code < 300) {
        return 1;
    }
    return 0;
}`,
      sourceB: `int check_status(int code) {
    if (code >= 400 && code < 500) {
        return 1;
    }
    return 0;
}`
    },
    {
      id: 'c-structural-diff',
      name: 'Structurally Different Algorithms',
      description: 'Bubble sort versus binary search. Expected: Low similarity.',
      sourceA: `void bubble_sort(int arr[], int n) {
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}`,
      sourceB: `int binary_search(int arr[], int low, int high, int key) {
    while (low <= high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] == key) return mid;
        if (arr[mid] < key) low = mid + 1;
        else high = mid - 1;
    }
    return -1;
}`
    }
  ],
  CPP: [
    {
      id: 'cpp-identical',
      name: 'Identical C++ Code',
      description: 'Standard C++ template-style vector processing.',
      sourceA: `#include <iostream>
#include <vector>

int main() {
    std::vector<int> numbers = {1, 2, 3, 4, 5};
    int sum = 0;
    for (int n : numbers) {
        sum += n;
    }
    std::cout << sum << std::endl;
    return 0;
}`,
      sourceB: `#include <iostream>
#include <vector>

int main() {
    std::vector<int> numbers = {1, 2, 3, 4, 5};
    int sum = 0;
    for (int n : numbers) {
        sum += n;
    }
    std::cout << sum << std::endl;
    return 0;
}`
    },
    {
      id: 'cpp-renamed',
      name: 'Renamed C++ Class & Methods',
      description: 'Class encapsulation with renamed member variables and methods.',
      sourceA: `class Rectangle {
private:
    double width;
    double height;
public:
    Rectangle(double w, double h) : width(w), height(h) {}
    double getArea() {
        return width * height;
    }
};`,
      sourceB: `class Rectangle {
private:
    double dimA;
    double dimB;
public:
    Rectangle(double a, double b) : dimA(a), dimB(b) {}
    double getArea() {
        return dimA * dimB;
    }
};`
    }
  ],
  PYTHON: [
    {
      id: 'py-identical',
      name: 'Identical Python Functions',
      description: 'Recursive Fibonacci function in Python. Expected: 100% similarity.',
      sourceA: `def fibonacci(n):
    if n <= 0:
        return 0
    elif n == 1:
        return 1
    return fibonacci(n - 1) + fibonacci(n - 2)`,
      sourceB: `def fibonacci(n):
    if n <= 0:
        return 0
    elif n == 1:
        return 1
    return fibonacci(n - 1) + fibonacci(n - 2)`
    },
    {
      id: 'py-renamed',
      name: 'Renamed Python Parameters & Locals',
      description: 'List filtering with renamed variable names. Expected: 100% similarity.',
      sourceA: `def filter_evens(input_list):
    result = []
    for item in input_list:
        if item % 2 == 0:
            result.append(item)
    return result`,
      sourceB: `def filter_evens(numbers):
    evens = []
    for val in numbers:
        if val % 2 == 0:
            evens.append(val)
    return evens`
    },
    {
      id: 'py-reorder',
      name: 'Safe Independent Statements in Python',
      description: 'Disjoint assignments in Python module/function body.',
      sourceA: `def setup_config():
    host = "127.0.0.1"
    port = 8080
    debug = True
    return host, port, debug`,
      sourceB: `def setup_config():
    debug = True
    port = 8080
    host = "127.0.0.1"
    return host, port, debug`
    }
  ]
};
