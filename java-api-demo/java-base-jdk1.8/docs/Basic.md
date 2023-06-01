# Base

checkException 和 uncheckException 的区别

* 检查异常（Checked Exception）：这类异常在编译时强制要求进行异常处理，否则会导致编译错误。通常情况下，检查异常是程序在运行时可能会遇到的外部条件导致的异常，例如文件 I/O 操作中的文件不存在、网络连接失败等。例如，IOException、SQLException 等都是检查异常。
* 非检查异常（Unchecked Exception）：这类异常在编译时不强制要求进行异常处理，但是在运行时如果没有进行异常处理，程序会终止。通常情况下，非检查异常是程序在运行时由于编程错误导致的异常，例如数组越界、空指针等。例如，NullPointerException、ArrayIndexOutOfBoundsException 等都是非检查异常。
* 判断标准：**RuntimeException and its subclasses** are unchecked exceptions. All other exceptions are checked exceptions.
