# basix - Basic Data Structures and Functions

This tiny library contains some structures and functions
which I frequently use by myself - and offer therefore for 
others as well.

## Getting Started

Use these maven coordinates to incorporate the library in your
work:

    groupId: io.github.ralfspoeth
    artefactId: basix
    version: 1.0.1

You'll need Java version 21 or later to utilize this library.

When working with JPMS modules, add this to your
`module-info.java` file:

    module your.module {
        requires io.github.ralfspoeth.basix;
    }

# Purity Stack and Queue Implementations

Though the Java Collections library contains a rich set
of classes which provide much more functionality then 
these two `Stack` and `Queue` implementations I prefer
them frequently when I want to utilize their push/pop or 
add/remove semantics, respectively, in pure manner.

## Stack

The LIFO (last in, first out) stack structure, defined as an abstract data type,
provides, the two operations `push` and `pop` and the two 
state enquiries `isEmpty` and `top` only.

Hence

    var s = new Stack<T>();
    assert s.isEmpty();
    assert s.top()==null;

and 

    s.push(t);
    assert t = s.pop();

as well as

    s.push(t);
    s.push(u);
    assert u==s.pop() && t==s.pop() && s.isEmpty();

## Queue

The FIFO (first in, first out) queue is similar but provides
only `add` (or `addLast`) and `remove` (or `removeFirst`) 
operations and -- as the stack -- `isEmpty` and additionally
`head` and `tail` operations. The `head` of a queue is the 
least recent element added to the queue and `tail` is the most recent 
element.

Hence,

    var q = new Queue<T>();
    assert q.isEmpty() && q.head()==null && q.tail()==null;

and for the operations

    q.add(t);
    q.add(u);
    assert t == q.remove() &&  u == q.remove() && q.isEmpty();

Both queues and stacks do not implement either of the 
interfaces of the Java Collections Framework. They are useful 
exclusively when Java collections simply provide way to much
for the task at hand.

# Functions

