# java dump thread stack 解析

文件名：SimpleTest.java 命令 jstack -e -l pid

解析参考文档：
[thread dump of stackOverflow](https://stackoverflow.com/questions/7599608/how-to-analyze-a-java-thread-dump)
[thread dump of dzone](https://dzone.com/articles/how-to-read-a-thread-dump)

* "VM Periodic Task Thread"
This thread is responsible for timer events (i.e. interrupts)
that are used to schedule execution of periodic operations

* "GC task thread#x (ParallelGC)"
These threads support the different types of garbage collection activities that occur in the JVM
-XX:ParallelGCThreads=n default value is hardware thread Number

* "VM Thread" 执行一些需要达到安全点（safe-point) 的操作，
例如 “stop-the-world"垃圾回收、线程堆栈转储、线程暂停和偏向锁撤销

* "main" main 函数所在的线程

* "Reference Handler" 引用处理线程作用是清除Reference对象.
  * 补充：
  Reference对象的子类有WeakReference、SoftReference、PhantomReference，分别对应弱引用、软引用和虚引用。
  当一个对象被回收时，Reference对象中的referent字段就是指向被引用的对象，当被引用的对象被回收时，Reference对象就会被加入到ReferenceQueue对垒中
  ReferenceHandler线程会不断的从ReferenceQueue中取出Reference对象，然后调用Reference对象的clear()方法，
  clear()方法会将Reference对象中的referent字段置为null，这样就可以避免内存泄漏了。
  
* "Finalizer" Finalizer线程的作用是调用对象的finalize()方法，finalize()方法是Object类中的方法，所有的类都继承了Object类.
  * 补充：
  Only after the GC has finished, JVM understands that apart from the Finalizers nothing refers to our instances,
  so it can mark all Finalizers pointing to those instances to be ready for processing.
  So the GC internals add all Finalizer objects to a special queue atjava.lang.ref.Finalizer.ReferenceQueue.
  The Finalizer thread is responsible for processing the queue and calling the finalize() method on the objects.
  calls the finalize() method and removes the reference from Finalizer class,
  so the next time the GC runs the Finalizer and the referenced object can now be GCd
  
* "Signal Dispatcher" When the OS raises a signal to the JVM, the signal dispatcher thread will pass the signal to the appropriate handler
用于分发处理操作系统发送给JVM的信号

* “Attach Listener” 负责处理动态加载的JAR包

* “C2 CompilerThread0” C2编译器线程

* ”C1 CompilerThread0” C1编译器线程

* "Service Thread" 用于处理操作系统的请求，比如对操作系统线程的调度
